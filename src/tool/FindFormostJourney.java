package tool;

import input.ConnectionEvent;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map.Entry;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Scanner;
import java.util.regex.Pattern;
import app.AppConfig;
import app.CjegException;
import java.io.File;
import java.io.FileNotFoundException;

/**
 * 
 * @author yfhuang created at 2014-4-5 this algorithm is an implementation of
 *         computer formost journey in paper
 *         "COMPUTING SHORTEST, FASTEST,AND FOREMOST JOURNEYS IN DYNAMIC NETWORKS"
 * 
 */
public class FindFormostJourney {

	// parameter set
	long starttime = 1920;
	int source = 32;
	int destination = 46;
	int nodes_number = 62;
	
	
	long tead[] = new long[nodes_number + 1];
	int father[] = new int[nodes_number + 1];
	private AppConfig appconfig = AppConfig.getInstance();
	private HashMap<Integer, AdjacentListMap> adjacentlists_map = new HashMap<Integer, AdjacentListMap>();

	/**
	 * @param args
	 * @throws FileNotFoundException
	 */
	public static void main(String[] args) throws FileNotFoundException {
		// TODO Auto-generated method stub
		FindFormostJourney findformostj = new FindFormostJourney();
		findformostj.constructAdjacentList();
		// findformostj.printAdjacentList();
		findformostj.findFormostJ();
		findformostj.printFormostJ();
	}

	public void constructAdjacentList() throws FileNotFoundException {
		File eventsfile = AppUtil.getFile(appconfig.getStorename(),
				appconfig.getCrawdadfile());

		// read events file and contruct adjacentlist

		Scanner scanner = new Scanner(eventsfile);
		// skip empty and comment lines
		Pattern skippattern = Pattern.compile("(#.*)|(^\\s*$)");
		while (scanner.hasNextLine()) {
			String eventline = scanner.nextLine();
			if (skippattern.matcher(eventline).matches()) {
				// skip empty and comment lines
				Log.writeln("read Events skip one line", 1);
				continue;
			}
			Scanner linescan = new Scanner(eventline);
			long etime = 0;
			int fromaddr;
			int toaddr;
			String conntype;
			boolean isup;
			ConnectionEvent conevent = null;
			etime = linescan.nextLong();
			fromaddr = linescan.nextInt();
			toaddr = linescan.nextInt();
			conntype = linescan.next();
			if (conntype.equals("UP"))
				isup = true;
			else if (conntype.equals("DOWN"))
				isup = false;
			else
				throw new CjegException(
						"read crawdadfile error:there is no such conntype");
			conevent = new ConnectionEvent(fromaddr, toaddr, isup, etime);
			updateAdjacentList(conevent);
		}
	}

	public void updateAdjacentList(ConnectionEvent conevent) {
		long etime = conevent.getTime();
		int fromaddr = conevent.getFromaddr();
		int toaddr = conevent.getToaddr();
		boolean isup = conevent.isUp();
		dealWithAdjacentList(fromaddr, toaddr, etime, isup);
		dealWithAdjacentList(toaddr, fromaddr, etime, isup);
	}

	public void dealWithAdjacentList(int fromaddr, int toaddr, long etime,
			boolean isup) {
		if (adjacentlists_map.containsKey(fromaddr)) {
			AdjacentListMap adjacentlistmap = adjacentlists_map.get(fromaddr);
			if (adjacentlistmap.containsKey(toaddr)) {
				EdgeScheduleList edgeschedule_list = adjacentlistmap
						.get(toaddr);
				EdgeSchedule lastschedule = edgeschedule_list.getLast();
				if (!isup) {
					if (lastschedule.stoptime != Long.MAX_VALUE)
						throw new CjegException(
								"The stop time of lastschedule should be Long.Maxvalue");
					lastschedule.stoptime = etime;
				} else {
					if (lastschedule.stoptime == Long.MAX_VALUE) {
						System.out.println("starttime:" + etime);
						System.out.println("fromaddr:" + fromaddr);
						System.out.println("toaddr:" + toaddr);
						throw new CjegException(
								"The stop time of lastschedule shouldn't be Long.Maxvalue");
					}
					EdgeSchedule edgeschedule = new EdgeSchedule(etime,
							Long.MAX_VALUE);
					edgeschedule_list.add(edgeschedule);
				}
			} else {
				if (!isup) {
					throw new CjegException(
							"adjacentlistmap does not contain toaddr exception");
				}
				EdgeScheduleList edgeschedule_list = new EdgeScheduleList();
				EdgeSchedule edgeschedule = new EdgeSchedule(etime,
						Long.MAX_VALUE);
				edgeschedule_list.add(edgeschedule);
				adjacentlistmap.put(toaddr, edgeschedule_list);
			}
		} else {
			if (!isup) {
				throw new CjegException(
						"adjacentlists_map does not contain fromaddr exception");
			}
			AdjacentListMap adjacentlistmap = new AdjacentListMap();
			EdgeScheduleList edgeschedule_list = new EdgeScheduleList();
			EdgeSchedule edgeschedule = new EdgeSchedule(etime, Long.MAX_VALUE);
			edgeschedule_list.add(edgeschedule);
			adjacentlistmap.put(toaddr, edgeschedule_list);
			adjacentlists_map.put(fromaddr, adjacentlistmap);
		}
	}

	public void printAdjacentList() {
		Iterator<Entry<Integer, AdjacentListMap>> fromite = adjacentlists_map
				.entrySet().iterator();
		while (fromite.hasNext()) {
			Entry<Integer, AdjacentListMap> fromentry = fromite.next();
			System.out.println("---------fromnode id:" + fromentry.getKey()
					+ "------------");
			AdjacentListMap adjacentlistmap = fromentry.getValue();
			Iterator<Entry<Integer, EdgeScheduleList>> toite = adjacentlistmap
					.entrySet().iterator();
			while (toite.hasNext()) {
				Entry<Integer, EdgeScheduleList> toentry = toite.next();
				System.out.println("******tonode id:" + toentry.getKey()
						+ "********");
				EdgeScheduleList schedulelist = toentry.getValue();
				for (int k = 0; k < schedulelist.size(); k++) {
					EdgeSchedule edgeschedule = schedulelist.get(k);
					System.out.println("startime:" + edgeschedule.starttime
							+ " stoptime:" + edgeschedule.stoptime);
				}
				System.out.println();

			}
			System.out.println();

		}
	}

	public void findFormostJ() {
		// initialize tead and father array
		for (int i = 0; i < nodes_number + 1; i++) {
			tead[i] = Long.MAX_VALUE;
			father[i] = Integer.MAX_VALUE;
		}

		tead[source] = starttime;

		Comparator<Integer> qicmp;
		qicmp = new Comparator<Integer>() {
			public int compare(Integer e1, Integer e2) {
				if (tead[e1.intValue()] < tead[e2.intValue()])
					return -1;
				if (tead[e1.intValue()] == tead[e2.intValue()])
					return 0;
				return 1;
			}
		};

		Queue<Integer> qi = new PriorityQueue<Integer>(20, qicmp);
		qi.add(source);
		HashSet<Integer> selectedset = new HashSet<Integer>();
		while (!qi.isEmpty()) {
			int u = qi.poll();
			selectedset.add(u);
			Log.writeln("u is:" + u + " tead is:" + tead[u], 100);
			if (u == destination)
				break;
			AdjacentListMap adjacentlistmap = adjacentlists_map.get(u);
			Iterator<Entry<Integer, EdgeScheduleList>> neighborsite = adjacentlistmap
					.entrySet().iterator();
			while (neighborsite.hasNext()) {
				Entry<Integer, EdgeScheduleList> entry = neighborsite.next();
				int v = entry.getKey();
				//Log.write("v is:" + v + " ", 100);
				EdgeScheduleList edgeschedulelist = entry.getValue();
				long arrivetime = getEarliestArrive(u, v, tead[u],
						edgeschedulelist);
				Log.writeln("arrive time:" + arrivetime, 100);
				if (arrivetime < tead[v]) {
					tead[v] = arrivetime;
					father[v] = u;
					if (!qi.contains(v) && !selectedset.contains(v)) {
						Log.writeln(
								"added v to Q:" + v + " tead is:" + tead[v],
								100);
						qi.add(v);
					}
				}
			}
			Queue<Integer> temp = new PriorityQueue<Integer>(20, qicmp);
			// adjust qi according comaprarator because some tead changed.
			while (!qi.isEmpty()) {
				int node=qi.poll();
				temp.add(node);
			}
			qi=temp;
		}
		Log.writeln("traverse Q ended!", 100);
	}

	/**
	 * 
	 * @param _u
	 * @param _v
	 * @param fromtime
	 * @return earliest arrive time from u to v starting from fromtime,return
	 *         Long.Maxvalue if not exist.
	 * 
	 */
	public long getEarliestArrive(int _u, int _v, long fromtime,
			EdgeScheduleList edgeschedulelist) {
		for (int k = 0; k < edgeschedulelist.size(); k++) {
			EdgeSchedule edgeschedule = edgeschedulelist.get(k);
			if (edgeschedule.stoptime - edgeschedule.starttime < appconfig
					.getTau()
					|| edgeschedule.stoptime - fromtime < appconfig.getTau())
				continue;
			// stoptime>=starttime+tau && stoptime>=fromtime+tau
			if (fromtime < edgeschedule.starttime)
				return edgeschedule.starttime + appconfig.getTau();
			else
				return fromtime + appconfig.getTau();
		}
		return Long.MAX_VALUE;
	}

	public void printFormostJ() {
		if (father[destination] == Integer.MAX_VALUE)
			System.out.println("there is no formost journey from:" + starttime
					+ " between source and destination.");
		else {
			String formostjourney = "";
			int cursor = destination;
			int precursor = father[cursor];
			while (cursor != source) {
				formostjourney = precursor + ","
						+ (tead[cursor] - appconfig.getTau()) + "  "
						+ formostjourney;
				cursor = precursor;
				precursor = father[cursor];
			}
			System.out.println(formostjourney);
		}
	}

	class AdjacentListMap extends HashMap<Integer, EdgeScheduleList> {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
	}

	class EdgeScheduleList extends LinkedList<EdgeSchedule> {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
	}

	class EdgeSchedule {
		long starttime;
		long stoptime;

		EdgeSchedule(long _start, long _stop) {
			starttime = _start;
			stoptime = _stop;
		}
	}
}
