package core;

import input.DvcEvent;
import input.DvcEventQueue;
import input.PtuEvent;
import input.PtuEventQueue;
import input.ReceptionEvent;
import input.ReceptionEventQueue;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import app.CjegApp;
import app.CjegException;
import tool.AppUtil;
import tool.IdMap;
import tool.Log;

/**
 ** @author yfhuang created at 2014-3-4
 */
public class Node {
	private int address;
	private String nodestr;
	// Integer represents address of source node
	private VectorOnNode vectoronnode;
	private PucCseqList ptucseqlist;
	private VectorOnNode originvec;
	/*
	 * three cases should propage to neighbors case 1: new hop start and this
	 * hop arise view change
	 * 
	 * case 2: new date start and this date arise view change
	 * 
	 * case 3: hop down
	 */
	private boolean viewchanged;
	private boolean vectorchanged;
	private boolean ptuchanged;

	private HashMap<Edge, CrJourneyEvolvingGraph> crjourneyeg_map;
	// private CrJourneyEgReport report;
	private HashSet<Integer> neighbors;

	public boolean conup;
	public HashSet<Integer> fromsets;
	public HashSet<Integer> tosets;

	public HashSet<Integer> dvcsets;

	// keeps vectorclock of current time - tau of neighbor nodes
	private HashMap<Integer, VectorOnNode> neighborsvec;

	// app info
	private CjegApp app;

	// looking for receptionevent trigtime by edge
	private HashMap<Edge, HashSet<Long>> inverse_reception_map;

	// looking for continuecrjcevent trigtime by edge
	private HashMap<Edge, HashSet<Long>> inverse_continue_map;
	
	// looking for continuecrjcevent trigtime by edge
	private HashMap<Edge, HashSet<Long>> inverse_ptu_map;

	// two variables to avoid invalid jhop
	private HashMap<Integer, Integer> ccseq; // current contact sequence
	private HashMap<Edge, Integer> max_disappear;

	private ReceptionEventQueue receventqueue;
	private PtuEventQueue ptueventqueue;
	private DvcEventQueue concrjceventqueue;
	private int tau;

	public boolean isViewChanged() {
		return viewchanged;
	}

	public boolean isVectorChanged() {
		return vectorchanged;
	}

	public boolean isConup() {
		return conup;
	}

	/** previoustime is the time of origin_sc **/
	public void onViewChanged(long previoustime, long now) {
		Log.write("generatecreg current node address:"+AppUtil.getNodesFromAddress(this.getAddress()).getNodeStr()+"\n",1000);
		HashSet<Integer> crjeg_sets = new HashSet<Integer>();
		HashSet<Integer> temp_sets = new HashSet<Integer>();

		// find changed sourceclock and add it to crjeg_sets
		for (Map.Entry<Integer, SourceClock> vec_entry : vectoronnode.entrySet()) {
			int vec_saddress = vec_entry.getKey();
			if (vec_saddress == address)
				continue;
			if (originvec.get(vec_saddress) == null)
				// crjeg_sets.add(vec_saddress);
				temp_sets.add(vec_saddress);
			else if (!originvec.get(vec_saddress).equals(vec_entry.getValue()))
				// crjeg_sets.add(vec_saddress);
				temp_sets.add(vec_saddress);
		}

		crjeg_sets.addAll(dvcsets);
		crjeg_sets.addAll(temp_sets);

		// print critical journey
		for (Iterator<Integer> saddress_ite = crjeg_sets.iterator(); saddress_ite
				.hasNext();) {
			int saddress = saddress_ite.next();

			Edge edge = new Edge(saddress, address);

			// Log.writeln(edge.toString(),1);

			// critical journey may occure on edge
			SourceClock origin_sc = originvec.get(saddress);
			SourceClock sc = vectoronnode.get(saddress);

			int compare_result = compareView(previoustime, origin_sc, now, sc);
			switch (compare_result) {
			case -1: {
				Log.writeln("saddress:" + saddress, 2000);
				Log.writeln(address + " " + sc.getHopj().toString(), 2000);
				throw new CjegException("compare view error -1");
			}
			case -2: {
				Log.writeln("address:" + address, 2000);
				Log.writeln("saddress:" + saddress, 2000);
				Log.writeln("sc_dataj:" + sc.getDatej().toString(), 2000);
				Log.writeln("orgin_sc_dataj:" + sc.getDatej().toString(), 2000);
				Log.writeln("sc_hopj:" + sc.getHopj().toString(), 2000);
				Log.writeln("orgin_sc_hopj:" + origin_sc.getHopj().toString(),
						2000);
				throw new CjegException("compare view error -2");
			}

			case 1: { // IIMPROVED
				long stime = getView(now, sc);
				crJourneyCluStart(edge, stime, stime, sc.getDatej());
				if (sc.getHop() != Long.MAX_VALUE)
					// case1 trig
					trigDvcEvent(now, saddress, stime + sc.getHop() * tau,
							stime);

				break;
			}
			case 2: { // DIMPROVED DCHANGED CATCHUP
				if (dvcsets.contains(saddress) && !temp_sets.contains(saddress)) {
					crJourneyCluStart_onDvc(edge, getView(now, sc),
							Long.MAX_VALUE, sc.getHopj());
				} else {
					crJourneyCluStart(edge, getView(now, sc), Long.MAX_VALUE,
							sc.getHopj());
				}
				break;
			}
			case 3: { // DSTOP
				long etime = getView(now, sc);
				crJourneyCluStop(edge, etime);
				if (sc.getHop() != Long.MAX_VALUE)
					// case1 trig
					trigDvcEvent(now, saddress, etime + sc.getHop() * tau,
							etime);
				break;
			}

			case 4: { // DVC injection case 2
				long etime = getView(now, sc);
				CrJourneyEvolvingGraph crjourneyeg = crjourneyeg_map.get(edge);
				// denote last critical journey cluster has ended
				// ended time is etime
				if (crjourneyeg.getLast().getEtime() != etime) {
					throw new CjegException(
							"generateCreg exception:stop time of last critical journey is not equal to etime");
				}
				// case2 trig
				trigDvcEvent(now, saddress, etime + sc.getHop() * tau, etime);
				break;
			}
			default:
				// Log.write("there's nothing to do in generatecreg", 2);
			}
		}
		dvcsets.clear();
	}

	/**
	 * return value -1 error 0 do nothing 1 discrete critical journey cluster
	 * start 2 continous critical journey cluster start 3 continuous critical
	 * journey cluster stop 4 a new hop start,however, it is not the start of
	 * critical journey but it may trig ContinueCrJcEvent
	 */
	public int compareView(long previous, SourceClock origin_sc, long now,
			SourceClock sc) {

		if (origin_sc == null) {
			if (byHop(now, sc))
				return 2; // DIMPROVED
			else
				return 1; // IIMPROVED
		}

		/**
		 * we must know it is impossible that sc.date<origin_sc.date
		 */
		if (sc.getDate() < origin_sc.getDate()) {
			Log.writeln("sc.date:" + sc.getDate() + " origin_sc.date:"
					+ origin_sc.getDate(), 1);
			return -1;
		}

		/**
		 * we must know it is impossible that the view of sc< the view of
		 * origin_sc
		 */

		if (getView(now, sc) < getView(previous, origin_sc)) {
			Log.writeln("now is:" + now + "    previous is:" + previous, 2000);
			Log.writeln("sc.date:" + sc.getDate() + " origin_sc.date:"
					+ origin_sc.getDate(), 2000);
			Log.writeln(
					"sc.hop:" + sc.getHop() + " origin_sc.hop:"
							+ origin_sc.getHop(), 2000);
			Log.writeln("sc.view:" + getView(now, sc) + " origin_sc.view:"
					+ getView(previous, origin_sc), 2000);

			return -2;
		}

		/** discrete critical journey cluster start **/

		if (!byHop(previous, origin_sc) && !byHop(now, sc)
				&& sc.getDate() > origin_sc.getDate())
			return 1; // IIMPROVED

		// date is not get by updateDateByhop
		if (byHop(previous, origin_sc) && !byHop(now, sc)
				&& sc.getDate() > (now - origin_sc.getHop() * tau))
			return 1; // IIMPROVED

		/**
		 * continous critical journey cluster start (may be former critical
		 * journey didn't stop)
		 **/

		if (byHop(previous, origin_sc) && byHop(now, sc)
				&& sc.getHop() < origin_sc.getHop())
			return 2; // DIMPROVED

		if (byHop(previous, origin_sc) && byHop(now, sc)
				&& sc.getHop() == origin_sc.getHop()
				&& !sc.getHopj().equals(origin_sc.getHopj()))
			return 2; // DCHANGED

		if (!byHop(previous, origin_sc) && byHop(now, sc)) {

			return 2; // "CATCHUP"
		}
		/** continous critical journey cluster down **/

		if (byHop(previous, origin_sc) && !byHop(now, sc)
				&& sc.getHop() > origin_sc.getHop())
			return 3; // "DSTOP"

		/**
		 * a new hop start,however, it is not the start of critical journey due
		 * to !byHop(it imply the last cluster of critical eg has stopped. these
		 * condition has some repeated part with "direct critical journey down".
		 * If so it will return 3 first. (ie. direct critical journey down) We
		 * will also check if we need generate continuecrjc event although no
		 * return 4
		 **/

		if (sc.getHop() != Long.MAX_VALUE && sc.getHop() != origin_sc.getHop()
				&& !byHop(now, sc))
			return 4; // "PREDICT CATCHUP DVC event injection 2"

		/**
		 * all else case: return 0
		 * 
		 * for example below case denote no critical journey rise it is equal to
		 * last case
		 * 
		 * if (!byHop(now,origin_sc) && !byHop(now,sc) &&
		 * sc.getDate()=origin_sc.getDate())
		 * 
		 * 
		 */
		return 0;

	}

	public long getView(long now, SourceClock sc) {
		if (sc.getHop() == Long.MAX_VALUE)
			return sc.getDate();
		if (sc.getDate() == Long.MIN_VALUE)
			return now - sc.getHop() * tau;
		long hop_start = now - sc.getHop() * tau;
		if (hop_start >= sc.getDate())
			return hop_start;
		else
			return sc.getDate();
	}

	public boolean byHop(long now, SourceClock sc) {
		if (sc.getHop() == Long.MAX_VALUE)
			return false;
		if (sc.getDate() == Long.MIN_VALUE)
			return true;
		if ((now - sc.getHop() * tau) >= sc.getDate())
			return true;
		else
			return false;
	}

	public VectorOnNode getOriginvec() {
		return originvec;
	}

	public void setOriginvec() {
		this.originvec = vectoronnode.clone();
		conup = false;
		vectorchanged = false;
		if (tosets != null)
			tosets.clear();
	}
	
	public HashMap<Integer, VectorOnNode> getNeighborsvec() {
		return neighborsvec;
	}

	public void setNeighborsvec(HashMap<Integer, VectorOnNode> neighborsvec) {
		this.neighborsvec = neighborsvec;
	}


	public void RestoreViewchanged() {
		viewchanged = false;
	}

	public void restorPucChanged() {
		ptuchanged = false;
		ptucseqlist.clear();
	}

	public boolean needSend() {
		return (conup || vectorchanged);
	}

	public boolean needPtuSend() {
		return ptuchanged;
	}

	public VectorOnNode getVectorclock() {
		return vectoronnode;
	}

	public int getAddress() {
		return address;
	}

	public void setAdress(int _address) {
		this.address = _address;
	}

	private void setNodeStr(int _address) {
		IdMap idmap = IdMap.getInstance();
		nodestr = idmap.getExternalId(_address);
	}

	public String getNodeStr() {
		return nodestr;
	}

	public void onConnectionUp(Node _peernode, long uptime) {
		Log.write("onConnectionUp current node address:"+AppUtil.getNodesFromAddress(this.getAddress()).getNodeStr()+"\n",1000);
		neighbors.add(_peernode.getAddress());
		// send Vector to peer node ie. trig reception event <currentnode
		// peernode>
		conup = true;

		// new current contact sequence =original_seq+1
		int peeraddress = _peernode.getAddress();
		int newseq;
		if (ccseq.get(peeraddress) != null) {
			newseq = ccseq.get(peeraddress) + 1;
		} else {
			newseq = 1;
		}
		ccseq.put(peeraddress, newseq);

		tosets.add(_peernode.getAddress());
	}

	public void onConnectionDown(Node _fromnode, long downtime) {
		Log.write("onconnectiondown current node address:"+AppUtil.getNodesFromAddress(this.getAddress()).getNodeStr()+"\n",1000);
		neighbors.remove(_fromnode.getAddress());
		// clear reception event from thisnode to _fromnode
		clearRecptionEvent(_fromnode);
		clearPtuEvent(_fromnode);
		// neighbors down
		if (neighborsvec.get(_fromnode.getAddress()) != null)
			neighborsvec.remove(_fromnode.getAddress());

		updateVectorOnDown(_fromnode, downtime);
	}

	public void onReception(Node fromnode, long receptiontime,
			VectorOnNode fromvector) {
		Log.write("onReception current node address:"+AppUtil.getNodesFromAddress(this.getAddress()).getNodeStr()+"\n",1000);
		CjegApp.incrementRecept();
		// remove reception time in inverse_reception_map
		Edge edge = new Edge(fromnode.getAddress(), this.getAddress());
		HashSet<Long> timesets = inverse_reception_map.get(edge);
		timesets.remove(receptiontime);
		if (timesets.size() == 0) {
			inverse_reception_map.remove(timesets);
		}
		// filterUselessHopj(fromnode,fromvector);
		neighborsvec.put(fromnode.getAddress(), fromvector);
		updateVectorOnReception(fromnode, receptiontime, fromvector);
	}

	public void onPucReception(Node fromnode, long receptiontime,
			PucCseqList cseqlist) {
		Log.write("onptureception current node address:"+AppUtil.getNodesFromAddress(this.getAddress()).getNodeStr()+"\n",1000);
		Edge edge = new Edge(fromnode.getAddress(), this.getAddress());
		HashSet<Long> timesets = inverse_ptu_map.get(edge);
		timesets.remove(receptiontime);
		if (timesets.size() == 0) {
			inverse_ptu_map.remove(timesets);
		}
		for (int i = 0; i < cseqlist.size(); i++) {
			EdgeCsequence ecseq = cseqlist.get(i);
			setMax_Disappear(ecseq);
		}
	}

	public void filterUselessHopj(Node fromnode, VectorOnNode fromvector) {
		for (Integer nodeaddr : fromvector.keySet()) {
			if (nodeaddr == address)
				continue;
			SourceClock sclock = fromvector.get(nodeaddr);
			if (!isAlive(sclock.getEcseqlist())) {
				sclock.setHop(Long.MAX_VALUE);
				sclock.getHopj().clear();
				sclock.getEcseqlist().clear();
			}
		}
	}

	public void onDvc(DvcEvent _dvcevent) {
		Log.write("onDvc current node address:"+AppUtil.getNodesFromAddress(this.getAddress()).getNodeStr()+"\n",1000);
		int fromaddr = _dvcevent.getFromaddr();
		int toaddr = _dvcevent.getToaddr();
		long stime = _dvcevent.getStime();
		long trigtime = _dvcevent.getTime();

		// remove continue time in inverse_continue_map
		Edge redge = new Edge(fromaddr, this.getAddress());
		HashSet<Long> timesets = inverse_continue_map.get(redge);
		timesets.remove(trigtime);
		if (timesets.size() == 0) {
			inverse_continue_map.remove(timesets);
		}

		Log.writeln("now is:" + _dvcevent.getTime(), 1);
		Log.writeln("process continuous event from " + fromaddr + " to "
				+ toaddr + " start time:" + stime, 1);
		Edge edge = new Edge(fromaddr, toaddr);
		HashMap<Edge, CrJourneyEvolvingGraph> crjourneyeg_map = app
				.getCrjourneyeg_map();
		CrJourneyEvolvingGraph crjourneyeg = crjourneyeg_map.get(edge);
		if (crjourneyeg == null)
			throw new CjegException("No crjourneyeg now in oncontinuecrjc");
		CrJourneyCluster lastcluster = crjourneyeg.getLast();
		if (lastcluster == null || lastcluster.getEtime() == Long.MAX_VALUE)
			throw new CjegException(
					"lastcluster error in crjourneyeg now in oncontinuecrjc");
		if (toaddr != this.getAddress())
			throw new CjegException("toaddr exception in oncontinuecrjc");
		SourceClock sc = vectoronnode.get(fromaddr);

		if (getView(trigtime, sc) == stime && byHop(trigtime, sc)) {
			viewchanged = true;
			dvcsets.add(fromaddr);
		}
	}

	// time complexity is N*N N is the number of nodes in system
	@SuppressWarnings("unchecked")
	public void updateVectorOnDown(Node _fromnode, long downtime) {

		HashSet<Integer> nodesets = (HashSet<Integer>) neighbors.clone();
		nodesets.remove(_fromnode.getAddress());
		tosets.addAll(nodesets);

		if (!tosets.isEmpty()) {
			Log.writeln(
					"Down from " + _fromnode.getNodeStr() + " to "
							+ this.getNodeStr() + " on down node "
							+ this.getNodeStr() + "'s tosets is:"
							+ AppUtil.setsPrint(tosets), 1);
		}

		vectorchanged = true;
		setPtuSource(_fromnode);
		HashSet<Integer> changeset;
		changeset = updateDatesBasedHops(_fromnode, downtime);
		// Log.writeln("changesets:" + changesets.toString(), 1);
		// reselected vectorclock from neighbors vectorclock after losing
		// contact with fromnode if the shortest hop is come from fromnode
		resetHopInSet(_fromnode, changeset, false);
	}

	public void setPtuSource(Node _fromnode) {
		// set source of original jhop down ecsequence
		int fromaddress = _fromnode.getAddress();
		Edge edge = new Edge(fromaddress, this.getAddress());
		EdgeCsequence ecseq = new EdgeCsequence(edge, ccseq.get(fromaddress));
		setMax_Disappear(ecseq);
	}

	// this node learns from _peernode by direct journey should be updated to
	// indirect journey
	// changesets contains those nodes whose clock must be in vectorclock of
	// currentnode
	// and be impacted by down of connection(fromnode,currentnode)
	// time complexity is N*N N is the number of nodes in system
	public HashSet<Integer> updateDatesBasedHops(Node _fromnode, long now) {
		HashSet<Integer> changeset = new HashSet<Integer>();
		for (Integer nodeaddr : vectoronnode.keySet()) {
			if (nodeaddr == address)
				continue;
			SourceClock sclock = vectoronnode.get(nodeaddr);
			// if the last hop of the sclock's hopj is not equal to _fromnode
			// continue
			// for example D--->A->C is the least hop from D to C, the end of
			// A-C may lead to the end of
			// the direct journey D->C only if the last hop of D--->A is A
			// Log.write("update source clock of node:"+AppUtil.getNodesFromAddress(nodeaddr).getNodeStr());
			if (sclock.getHop() == Long.MAX_VALUE
					|| AppUtil.getLastHop(sclock.getHopj()) != _fromnode
							.getAddress())
				continue;

			changeset.add(nodeaddr);
			if (now - sclock.getHop() * tau > sclock.getDate()) {
				sclock.setDate(now - sclock.getHop() * tau);
				DateJourney datej = new DateJourney();
				HopJourney hopj = sclock.getHopj();
				long hop = sclock.getHop();
				for (int i = 0; i < hopj.size(); i++) {
					Edge edge = hopj.get(i).clone();
					long deptime = (now - hop * tau) + i * tau;
					Departure dep = new Departure(edge, deptime);
					datej.add(dep);
				}
				sclock.setDatej(datej);
			}
			// this hop has end.
			sclock.setHop(Long.MAX_VALUE);
			sclock.getHopj().clear();
			sclock.getEcseqlist().clear();
		}
		return changeset;
	}

	// time complexity is N*N N is the number of nodes in system
	public void resetHopInSet(Node _fromnode, HashSet<Integer> changesets,
			boolean onreception) {
		// Log.writeln("updateHopOnDown", 1);
		// those sourceclocks should be changed are in changesets now
		Iterator<Integer> changesetsite;
		for (changesetsite = changesets.iterator(); changesetsite.hasNext();) {
			int changeaddress = changesetsite.next();
			SourceClock sclock = vectoronnode.get(changeaddress);
			// find the min hop from neighbors vectorclock
			boolean findmin = false;
			SourceClock minhop_clock = sclock;
			Integer prehop_address = -1;
			for (Integer neighbor_address : neighborsvec.keySet()) {
				VectorOnNode nvec = neighborsvec.get(neighbor_address);
				SourceClock nsclock = nvec.get(changeaddress);
				if (nsclock == null)
					continue;

				if (nsclock.getHop() < minhop_clock.getHop()
						//&& !nsclock.getHopj().containsAddress(this.getAddress())
						&& isAlive(nsclock.getEcseqlist())) {
					minhop_clock = nsclock;
					findmin = true;
					prehop_address = neighbor_address;
				}
			}

			// minhop_clock.getHopj().containsAddress(this.address) prevent from
			// hopj "1-3-5-7-9-12-9" to propagate
			// such hopj won't form critical journeys it will be replaced
			// 1-3-5-7-9 if these contacts not disappeared.

			if (findmin
					&& !minhop_clock.getHopj().containsAddress(this.address)) {
				sclock.setHop(minhop_clock.getHop() + 1);
				HopJourney hopj = minhop_clock.getHopj().clone();
				Edge edge = new Edge(prehop_address, this.getAddress());
				hopj.addHop(edge);
				sclock.setHopj(hopj);
				EdgeCseqList ecseqlist = minhop_clock.getEcseqlist().clone();
				sclock.setEcseqlist(ecseqlist);
				if (onreception
						&& !sclock.getHopj().containsAddress(
								_fromnode.getAddress()))
					tosets.add(_fromnode.getAddress());
			}
		}
	}

	public void trigReceptionEvent(Node _tonode, long trigtime,VectorOnNode vec) {
		Log.writeln("trig reception event from:" + this.getAddress() + " to:"
				+ _tonode.getAddress() + " trig time:" + trigtime, 1);
		ReceptionEvent recevent = new ReceptionEvent(this.getAddress(),
				_tonode.getAddress(), trigtime, vec);
		Edge edge = new Edge(this.getAddress(), _tonode.getAddress());
		HashSet<Long> timesets = inverse_reception_map.get(edge);
		if (timesets != null) {
			// Log.write("timeset is not null");
			timesets.add(trigtime);
		} else {
			// Log.write("timeset is null");
			timesets = new HashSet<Long>();
			timesets.add(trigtime);
			inverse_reception_map.put(edge, timesets);
		}
		receventqueue.queue(recevent);
	}
	
	public void trigPtuEvent(long sendtime, Node _tonode, long trigtime) {
		Log.writeln(sendtime + " trig ptu event from:" + this.getNodeStr()
				+ " to:" + _tonode.getNodeStr() + " trig time:" + trigtime,
				1001);
		PtuEvent ptuevent = new PtuEvent(this.getAddress(),
				_tonode.getAddress(), trigtime, ptucseqlist.clone());
		Edge edge = new Edge(this.getAddress(), _tonode.getAddress());
		HashSet<Long> timesets = inverse_ptu_map.get(edge);
		if (timesets != null) {
			// Log.write("timeset is not null");
			timesets.add(trigtime);
		} else {
			// Log.write("timeset is null");
			timesets = new HashSet<Long>();
			timesets.add(trigtime);
			inverse_ptu_map.put(edge, timesets);
		}
		ptueventqueue.queue(ptuevent);
	}

	public void trigDvcEvent(long now, int _fromaddress, long trigtime,
			long _stime) {
		Log.writeln("now is:" + now, 1);
		Log.writeln(
				"trig dvc event from:" + _fromaddress + " to:"
						+ this.getAddress() + " trig time:" + trigtime, 1);
		DvcEvent concrjcevent = new DvcEvent(_fromaddress, this.getAddress(),
				trigtime, _stime);
		Edge edge = new Edge(_fromaddress, this.getAddress());
		HashSet<Long> timesets = inverse_continue_map.get(edge);
		if (timesets != null) {
			Log.writeln("add dvc event to inverse_continue_map", 1);
			timesets.add(trigtime);
		} else {
			Log.writeln("add dvc event to inverse_continue_map", 1);
			timesets = new HashSet<Long>();
			timesets.add(trigtime);
			inverse_continue_map.put(edge, timesets);
		}
		concrjceventqueue.queue(concrjcevent);
		Log.writeln("", 1);
	}

	public void sendPucCseqToNeighbors(long sendtime) {
		long trigtime = sendtime + tau;
		HashSet<Integer> nodesets = (HashSet<Integer>) neighbors.clone();
		if (nodesets != null) {
			Iterator<Integer> nodesetsite = nodesets.iterator();
			while (nodesetsite.hasNext()) {
				Node tonode = AppUtil.getNodesFromAddress(nodesetsite.next());
				trigPtuEvent(sendtime, tonode, trigtime);
			}
		}
	}

	@SuppressWarnings("unchecked")
	public void sendVecToNeighbors(long sendtime) {
		Log.writeln("sendvec to neighbors", 1000);
		long trigtime = sendtime + tau;
		HashSet<Integer> nodesets = (HashSet<Integer>) tosets.clone();
		nodesets.retainAll(neighbors);
		Log.writeln("tosets is:" + AppUtil.setsPrint(nodesets), 1);

		// prevent vector send to fromnode if only one fromnode.
		VectorOnNode vectorclock_bak;
		if (tosets != null) {
			Iterator<Integer> tosetsite = tosets.iterator();
			while (tosetsite.hasNext()) {
				vectorclock_bak = vectoronnode.clone();
				Node tonode = AppUtil.getNodesFromAddress(tosetsite.next());
				if (tonode != null && neighbors.contains(tonode.getAddress())) {

					// add new (v w sequence) to Edge contact sequence list if
					// vec[u].hop!=infty when v contact with w
					int peeraddress = tonode.getAddress();
					Edge edge = new Edge(this.getAddress(), peeraddress);
					EdgeCsequence ecseq = new EdgeCsequence(edge,
							ccseq.get(peeraddress));
					for (Map.Entry<Integer, SourceClock> vec_entry : vectorclock_bak
							.entrySet()) {
						int vec_saddress = vec_entry.getKey();
						SourceClock vec_clock = vec_entry.getValue();
						if (vec_clock.getHop() != Long.MAX_VALUE)
							vec_clock.getEcseqlist().add(ecseq);
					}
					// add ended
					trigReceptionEvent(tonode, trigtime,vectorclock_bak);
				} else if (tonode == null) {
					throw new CjegException(
							"no such neighbor on sendVectoNeighbors");
				}
			}
		}
		Log.writeln("", 1);
	}

	/** time complexity is N*N **/
	@SuppressWarnings("unchecked")
	public void updateVectorOnReception(Node fromnode, long receptiontime,
			VectorOnNode fromvector) {

		HashSet<Integer> nodesets = (HashSet<Integer>) neighbors.clone();
		nodesets.remove(fromnode.getAddress());
		tosets.addAll(nodesets);
		if (!tosets.isEmpty()) {
			Log.writeln("on Reception the node " + this.getNodeStr()
					+ "'s tosets is:" + AppUtil.setsPrint(tosets), 1);
		}

		HashSet<Integer> changeset;
		changeset = updateVectorBasedNewInfo(fromnode, receptiontime,
				fromvector);
		// here updateBasedHops is needn't because if u->w->v,
		// when u->w is down first on w updateBasedHops is excuted.
		// date about u will propagate to v from w.
		resetHopInSet(fromnode, changeset, true);
	}

	/** time complexity is N **/
	public HashSet<Integer> updateVectorBasedNewInfo(Node fromnode,
			long receptiontime, VectorOnNode fromvector) {
		HashSet<Integer> changeset = new HashSet<Integer>();
		for (Integer nodeaddr : fromvector.keySet()) {
			if (nodeaddr == address) {
				continue;
			}

			// vectorclock of some node doesn't exist in current node,so put it
			// into the vectorclock of current node
			if (!vectoronnode.containsKey(nodeaddr)) {
				long date = Long.MIN_VALUE;
				DateJourney datej = new DateJourney();
				long hop = Long.MAX_VALUE;
				HopJourney hopj = new HopJourney();
				EdgeCseqList ecseqlist = new EdgeCseqList();
				EdgeCsequence ecseq = new EdgeCsequence();
				SourceClock sclock = new SourceClock(date, datej, hop, hopj,
						ecseqlist);
				vectoronnode.put(nodeaddr, sclock);
				vectorchanged = true;
			}

			SourceClock nsclock = fromvector.get(nodeaddr);
			SourceClock sclock = vectoronnode.get(nodeaddr);

			// !nsclock.getHopj().contains(this.getAddress() prevent from loop
			// C-->A-->B-->A(this exits imply C-->A has lose contact with each
			// other, or else C-->A's hop is less that C-->A-->B -->A)
			boolean hop_isset = false;
			if (nsclock.getHop() < sclock.getHop() - 1
					//&& !nsclock.getHopj().containsAddress(this.getAddress())
					&& isAlive(nsclock.getEcseqlist())) {
				hop_isset = true;
				sclock.setHop(nsclock.getHop() + 1);
				HopJourney hopj = nsclock.getHopj().clone();
				Edge edge = new Edge(fromnode.getAddress(), this.getAddress());
				hopj.addHop(edge);
				sclock.setHopj(hopj);

				EdgeCseqList ecseqlist = nsclock.getEcseqlist().clone();
				sclock.setEcseqlist(ecseqlist);
				vectorchanged = true;
			}

			if (nsclock.getDate() > sclock.getDate()
					&& !nsclock.getDatej().containsAddress(this.getAddress())) {
				sclock.setDate(nsclock.getDate());
				DateJourney datej = nsclock.getDatej().clone();
				Edge edge = new Edge(fromnode.getAddress(), this.getAddress());
				Departure dep = new Departure(edge, receptiontime - tau);
				datej.addDeparture(dep);
				sclock.setDatej(datej);
				vectorchanged = true;
			}

			if (sclock.getHopj().isEmpty() || hop_isset)
				continue;

			// if sourceclock is come from fromnode and now the sourceclock of
			// fromnode has changed we must reset hop according to all neighbors
			// vectorclock

			if (AppUtil.getLastHop(sclock.getHopj()) == fromnode.getAddress()
					&& (nsclock.getHop() != sclock.getHop() - 1 || !nsclock
							.getHopj().equalsExceptLastHop(sclock.getHopj()))) {
				sclock.setHop(Long.MAX_VALUE);
				sclock.getHopj().clear();
				sclock.getEcseqlist().clear();

				// if (ecseq.getE() != null) {
				// setMax_Disappear(ecseq,sclock);
				// }

				changeset.add(nodeaddr);
				vectorchanged = true;
			}
		}
		// System.out.println("--------------------------end---------------------------");
		return changeset;
	}

	public void setMax_Disappear(EdgeCsequence ecseq) {
		int fromaddr = ecseq.getE().getFromaddr();
		int toaddr = ecseq.getE().getToaddr();
		Edge max_edge;
		max_edge = getNoDirectEdge(fromaddr, toaddr);
		if (max_disappear.get(max_edge) == null
				|| max_disappear.get(max_edge) < ecseq.getSequence()) {
			max_disappear.put(max_edge, ecseq.getSequence());
			ptucseqlist.add(ecseq);
			ptuchanged = true;
		}
	}

	public void setMax_Disappear_NoSet(EdgeCsequence ecseq, SourceClock sclock) {
		int fromaddr = ecseq.getE().getFromaddr();
		int toaddr = ecseq.getE().getToaddr();
		Edge max_edge;
		max_edge = getNoDirectEdge(fromaddr, toaddr);
		if (max_disappear.get(max_edge) == null
				|| ecseq.getSequence() > max_disappear.get(max_edge)) {
			max_disappear.put(max_edge, ecseq.getSequence());
		}
	}

	public boolean isAlive(EdgeCseqList ecseqlist) { // as far as current node
														// know, whether
														// contacts in ecseqlist
														// haven't disappeared
		for (int i = 0; i < ecseqlist.size(); i++) {
			EdgeCsequence ecseq = ecseqlist.get(i);
			Edge edge = ecseq.getE();
			Edge newedge;
			int fromaddr = edge.getFromaddr();
			int toaddr = edge.getToaddr();
			newedge = getNoDirectEdge(fromaddr, toaddr);
			max_disappear.get(newedge);
			if (max_disappear.get(newedge) != null
					&& max_disappear.get(newedge) >= ecseq.getSequence()) {
				return false;
			}
		}
		return true;
	}

	public void printMax_disappear(HopJourney hopj) { // as far as current node
		// know, whether
		// contacts in ecseqlist
		// haven't disappeared
		for (int i = 0; i < hopj.size(); i++) {
			Edge edge = hopj.get(i);
			Edge newedge;
			int fromaddr = edge.getFromaddr();
			int toaddr = edge.getToaddr();
			newedge = getNoDirectEdge(fromaddr, toaddr);
			Log.write(newedge.toString(),2000);
			max_disappear.get(newedge);
			if (max_disappear.get(newedge) != null){
				Log.writeln(","+max_disappear.get(newedge),2000);
			}
		}
	}

	public Edge getNoDirectEdge(int fromaddr, int toaddr) {
		Edge newedge;
		if (fromaddr < toaddr)
			newedge = new Edge(fromaddr, toaddr);
		else
			newedge = new Edge(toaddr, fromaddr);
		return newedge;
	}

	public void clearRecptionEvent(Node _fromnode) {
		// Log.write("clear reception");
		Edge edge = new Edge(_fromnode.getAddress(), this.getAddress());
		HashSet<Long> trigtime_sets = inverse_reception_map.get(edge);
		if (trigtime_sets == null) {
			Log.writeln(
					"clear reception trigtime_sets is null edge's fromaddr:"
							+ edge.getFromaddr() + " toaddr:"
							+ edge.getToaddr(), 1);
			return;
		}

		Iterator<Long> trigite = trigtime_sets.iterator();
		Log.writeln("clear reception the size of trigtime_sets:"
				+ trigtime_sets.size(), 1);
		Long trigtime;
		while (trigite.hasNext()) {
			trigtime = trigite.next();
			receventqueue.removeExistevent(trigtime, edge);
			trigite.remove();

		}
		if (trigtime_sets.isEmpty())
			inverse_reception_map.remove(edge);
	}
	
	public void clearPtuEvent(Node _fromnode) {
		// Log.write("clear ptu");
		Edge edge = new Edge(_fromnode.getAddress(), this.getAddress());
		HashSet<Long> trigtime_sets = inverse_ptu_map.get(edge);
		if (trigtime_sets == null) {
			Log.writeln(
					"clear ptu trigtime_sets is null edge's fromaddr:"
							+ edge.getFromaddr() + " toaddr:"
							+ edge.getToaddr(), 1);
			return;
		}

		Iterator<Long> trigite = trigtime_sets.iterator();
		Log.writeln("clear ptu the size of trigtime_sets:"
				+ trigtime_sets.size(), 1);
		Long trigtime;
		while (trigite.hasNext()) {
			trigtime = trigite.next();
			ptueventqueue.removeExistevent(trigtime, edge);
			trigite.remove();
		}
		if (trigtime_sets.isEmpty())
			inverse_ptu_map.remove(edge);
	}

	public void clearContinueCrJcEvent(int _fromaddress) {
		Edge edge = new Edge(_fromaddress, this.getAddress());
		HashSet<Long> trigtime_sets = inverse_continue_map.get(edge);
		if (trigtime_sets == null) {
			// Log.write("edge's fromaddr:"+edge.getFromaddr()+
			// " toaddr:"+edge.getToaddr());
			return;
		}
		Iterator<Long> trigite = trigtime_sets.iterator();
		Long trigtime;
		while (trigite.hasNext()) {
			trigtime = trigite.next();
			concrjceventqueue.removeExistevent(trigtime, edge);
			trigite.remove();

		}
		if (trigtime_sets.isEmpty())
			inverse_continue_map.remove(edge);
	}

//	public void clearConcrjcTimeInverseMap(Edge edge, long concrjctime) {
//		HashSet<Long> time_sets = inverse_continue_map.get(edge);
//		time_sets.remove(concrjctime);
//		if (time_sets.size() == 0) {
//			inverse_continue_map.remove(time_sets);
//		}
//	}
//	
//	public void clearPtuTimeInverseMap(Edge edge, long ptutime) {
//		HashSet<Long> time_sets = inverse_ptu_map.get(edge);
//		time_sets.remove(ptutime);
//		if (time_sets.size() == 0) {
//			inverse_ptu_map.remove(time_sets);
//		}
//	}
//
//	public void clearReceptionTimeInverseMap(Edge edge, long receptiontime) {
//		HashSet<Long> timesets = inverse_reception_map.get(edge);
//		timesets.remove(receptiontime);
//		if (timesets.size() == 0) {
//			inverse_reception_map.remove(timesets);
//		}
//	}

	public String journeyStr(Edge _edge) {
		return " from " + _edge.getFromStr() + " to " + _edge.getToStr();
	}

	public void crJourneyCluStart(Edge _edge, long _stime, long _etime,
			Journey<?> _sj) {
		CrJourneyEvolvingGraph crjourneyeg = crjourneyeg_map.get(_edge);

		if (crjourneyeg == null) {
			crjourneyeg = new CrJourneyEvolvingGraph();
			crjourneyeg_map.put(_edge, crjourneyeg);
		}

		CrJourneyCluster pre_crjc = null;
		if (!crjourneyeg.isEmpty()) {
			pre_crjc = crjourneyeg.getLast();
		}

		// if there is a critical journey cluster which hasn't stop but it is
		// equal to now, do nothing
		if (pre_crjc != null && pre_crjc.getEtime() == Long.MAX_VALUE
				&& pre_crjc.getRepresentative_crjourney().equals(_sj)) {
			return;
		}

		// if there is a critical journey cluster which hasn't stop, stop it
		if (pre_crjc != null && pre_crjc.getEtime() == Long.MAX_VALUE) {
			long previous_etime;
			previous_etime = _stime + _sj.getDelay()
					- pre_crjc.getRepresentative_crjourney().getDelay();
			crJourneyStop(_edge, pre_crjc, previous_etime);
		}
		// clear all continuecrjc event on edge _edge
		this.clearContinueCrJcEvent(_edge.getFromaddr());

		// critical journey cluster start
		CrJourneyCluster crjc = new CrJourneyCluster(_edge, _stime, _etime,
				_sj.clone());
		crjourneyeg.addCrjc(crjc);
		Log.writeln(
				"crjc from " + _edge.getFromStr() + " to " + _edge.getToStr()
						+ " start at time " + _stime, 1);
		if (crjc.getEtime() != Long.MAX_VALUE)
			Log.writeln(
					"crjc from " + _edge.getFromStr() + " to "
							+ _edge.getToStr() + " stop at time " + _etime, 1);
	}

	// just for statistics compared to crJourneyCluStart
	public void crJourneyCluStart_onDvc(Edge _edge, long _stime, long _etime,
			Journey<?> _sj) {
		CrJourneyEvolvingGraph crjourneyeg = crjourneyeg_map.get(_edge);

		if (crjourneyeg == null) {
			crjourneyeg = new CrJourneyEvolvingGraph();
			crjourneyeg_map.put(_edge, crjourneyeg);
		}

		CrJourneyCluster pre_crjc = null;
		if (!crjourneyeg.isEmpty()) {
			pre_crjc = crjourneyeg.getLast();
		}

		// if there is a critical journey cluster which hasn't stop but it is
		// equal to now, do nothing
		if (pre_crjc != null && pre_crjc.getEtime() == Long.MAX_VALUE
				&& pre_crjc.getRepresentative_crjourney().equals(_sj)) {
			return;
		}

		// if there is a critical journey cluster which hasn't stop, stop it
		if (pre_crjc != null && pre_crjc.getEtime() == Long.MAX_VALUE) {
			long previous_etime;
			previous_etime = _stime + _sj.getDelay()
					- pre_crjc.getRepresentative_crjourney().getDelay();
			crJourneyStop(_edge, pre_crjc, previous_etime);
		}
		// clear all continuecrjc event on edge _edge
		this.clearContinueCrJcEvent(_edge.getFromaddr());

		// critical journey cluster start
		CrJourneyCluster crjc = new CrJourneyCluster(_edge, _stime, _etime,
				_sj.clone());
		crjourneyeg.addCrjc(crjc);
		CjegApp.incrementDvcjourney();
		Log.writeln(
				"crjc from " + _edge.getFromStr() + " to " + _edge.getToStr()
						+ " start at time " + _stime, 1);
		if (crjc.getEtime() != Long.MAX_VALUE)
			Log.writeln(
					"crjc from " + _edge.getFromStr() + " to "
							+ _edge.getToStr() + " stop at time " + _etime, 1);
	}

	public void crJourneyCluStop(Edge _edge, long _etime) {

		CrJourneyEvolvingGraph crjourneyeg = crjourneyeg_map.get(_edge);
		CrJourneyCluster pre_crjc = null;
		if (!crjourneyeg.isEmpty()) {
			pre_crjc = crjourneyeg.getLast();
		}
		if (pre_crjc == null)
			throw new CjegException("crJourney Stop Exception");
		// not end end it.
		if (pre_crjc.getEtime() == Long.MAX_VALUE) {
			Log.writeln(
					"crjc from " + _edge.getFromStr() + " to "
							+ _edge.getToStr() + " stop at time " + _etime, 1);
			pre_crjc.setEtime(_etime);
		}
	}

	public void crJourneyStop(Edge _edge, CrJourneyCluster crjc,
			long previous_etime) {
		crjc.setEtime(previous_etime);
		Log.writeln(
				"crjc from " + _edge.getFromStr() + " to " + _edge.getToStr()
						+ " stop" + " at time " + previous_etime, 1);
	}

	public Node(int _address) {
		setAdress(_address);
		setNodeStr(_address);
		tau = CjegApp.getAppconfig().getTau();
		vectoronnode = new VectorOnNode();
		ptucseqlist = new PucCseqList();

		// add vector of this to vector_map
		long date = Long.MIN_VALUE;
		DateJourney datej = new DateJourney();
		int hop = 0;
		HopJourney hopj = new HopJourney();
		EdgeCseqList ecseqlist = new EdgeCseqList();
		// EdgeCsequence ecseq = new EdgeCsequence();
		SourceClock vec = new SourceClock(date, datej, hop, hopj, ecseqlist);
		vectoronnode.put(this.getAddress(), vec);
		setOriginvec();
		neighborsvec = new HashMap<Integer, VectorOnNode>();
		neighbors = new HashSet<Integer>();
		dvcsets = new HashSet<Integer>();
		app = CjegApp.getInstance();
		ptueventqueue = app.getPtueventqueue();
		receventqueue = app.getReceventqueue();
		concrjceventqueue = app.getConcrjceventqueue();
		inverse_reception_map = app.getInverseReceptionMap();
		inverse_continue_map = app.getInversecontinuemap();
		inverse_ptu_map = app.getInversePtuMap();
		conup = false;
		ptuchanged = false;
		tosets = new HashSet<Integer>();
		crjourneyeg_map = CjegApp.getInstance().getCrjourneyeg_map();
		// report = CrJourneyEgReport.getInstance();
		ccseq = new HashMap<Integer, Integer>();
		max_disappear = new HashMap<Edge, Integer>();
	}
}
