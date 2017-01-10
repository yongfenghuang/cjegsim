package input;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import tool.Log;
import app.CjegException;
import core.Edge;

/**
 ** @author yfhuang created at 2014-2-26
 */
public class DvcEventQueue implements EventQueue {
	private TreeMap<Long, HashMap<Edge, DvcEvent>> queuebuffer;
	private DvcEventListener continuecrjclistener;

	public void setEventlistener(DvcEventListener _continuecrjclistener) {
		this.continuecrjclistener = _continuecrjclistener;
	}

	@Override
	public CjEventListener getEventListener() {
		// Log.write("get continuecrjc eventlistener");
		return continuecrjclistener;
	}

	public DvcEventQueue() {
		this.queuebuffer = new TreeMap<Long, HashMap<Edge, DvcEvent>>();
		/* test code begin */
		// long trigtime = 10;
		// HashMap<Edge, ContinueCrJcEvent> trigevents = new HashMap<Edge,
		// ContinueCrJcEvent>(
		// 0);
		// Edge edge = new Edge(1, 2);
		// ContinueCrJcEvent testevent = new ContinueCrJcEvent(1, 2, trigtime,
		// 5);
		// trigevents.put(edge, testevent);
		// queuebuffer.put(trigtime, trigevents);
		/* test code end */
		// put max value time event
		queuebuffer
				.put(Long.MAX_VALUE, new HashMap<Edge, DvcEvent>(0));
	}

	public void queue(DvcEvent _concrjcevent) {
		Long time = _concrjcevent.getTime();
		HashMap<Edge, DvcEvent> eventsattime = queuebuffer.get(time);
		Edge edge = new Edge(_concrjcevent.getFromaddr(),
				_concrjcevent.getToaddr());
		if (eventsattime == null) {
			eventsattime = new HashMap<Edge, DvcEvent>();
			//Log.writeln("new eventsattime at time:"+time+" and put it to queuebuffer", 3);
			queuebuffer.put(time, eventsattime);
		}

		if (eventsattime.get(edge) == null) {
			Log.writeln("put concrjcevent at:"+time+" from "+edge.getFromaddr()+" to "+edge.getToaddr()+" into queuebuffer", 1);
			eventsattime.put(edge, _concrjcevent);
		}

	}

	public void removeExistevent(Long _time, Edge _edge) {
		try{
		HashMap<Edge, DvcEvent> eventsattime = queuebuffer.get(_time);
		if (eventsattime != null) {
			eventsattime.remove(_edge);
			if (eventsattime.isEmpty())
				queuebuffer.remove(_time);
		} else {
			Log.writeln("removeExistevent continuecrjc eventtime is:"+_time+" from "+_edge.getFromaddr()+" to "+_edge.getToaddr(), 1);
			throw new CjegException(
					"there's no such ContinueCrJcEvent in continuecrjceventqueue");
		}
		Log.writeln("", 1);}catch(Exception e){
			e.printStackTrace();
			throw new CjegException(
					"there's no such ContinueCrJcEvent in continuecrjceventqueue");
		}
	}

	@Override
	public List<CjEvent> nextEvents() {
		List<CjEvent> eventslist = new ArrayList<CjEvent>();
		HashMap<Edge, DvcEvent> readlist = new HashMap<Edge, DvcEvent>();
		Map.Entry<Long, HashMap<Edge, DvcEvent>> entry = queuebuffer
				.pollFirstEntry();
		readlist = entry.getValue();
		for (DvcEvent concrjcevent : readlist.values()) {
			//clear concrjcevent time in inverse_continue_map is in node.onContinuecrjc method
			//add it to eventlist
			eventslist.add(concrjcevent);
		}
		return eventslist;
	}

	@Override
	public long nextEventsTime() {
		if (queuebuffer.size() != 0)
			return queuebuffer.firstKey();
		else
			return Long.MAX_VALUE;
	}
}
