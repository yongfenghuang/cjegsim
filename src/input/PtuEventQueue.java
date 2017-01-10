package input;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import app.CjegException;


import core.Edge;

/**
 ** @author yfhuang created at 2014-2-26
 */
public class PtuEventQueue implements EventQueue {
	
	private TreeMap<Long, HashMap<Edge, PtuEvent>> queuebuffer;
	private PtuEventListener ptueventlistener;

	public void setEventlistener(PtuEventListener ptueventlistener) {
		this.ptueventlistener = ptueventlistener;
	}
	
	@Override
	public CjEventListener getEventListener() {
		// Log.write("get reception eventlistener");
		return ptueventlistener;
	}

	public PtuEventQueue() {
		this.queuebuffer = new TreeMap<Long, HashMap<Edge, PtuEvent>>();
		// put max value time event
		queuebuffer.put(Long.MAX_VALUE, new HashMap<Edge, PtuEvent>(0));
	}

	public void queue(PtuEvent _ptuevent) {
		Long time = _ptuevent.getTime();
		HashMap<Edge, PtuEvent> eventsattime = queuebuffer.get(time);
		Edge edge = new Edge(_ptuevent.getFromaddr(), _ptuevent.getToaddr());
		if (eventsattime == null) {
			eventsattime = new HashMap<Edge, PtuEvent>();
			queuebuffer.put(time, eventsattime);
		}

		if (eventsattime.get(edge) == null) {
			queuebuffer.get(time).put(edge, _ptuevent);
		}

	}

	public void removeExistevent(Long _time, Edge _edge) {
		HashMap<Edge, PtuEvent> eventsattime = queuebuffer.get(_time);
		if (eventsattime != null){
				eventsattime.remove(_edge);
				if (eventsattime.isEmpty()) queuebuffer.remove(_time);
		}else {
			throw new CjegException("there's no such PtuEvent in ptueventqueue");
		}
	}

	@Override
	public List<CjEvent> nextEvents() {
		List<CjEvent> eventslist = new ArrayList<CjEvent>();
		HashMap<Edge, PtuEvent> readlist;
		Map.Entry<Long, HashMap<Edge, PtuEvent>> entry = queuebuffer
				.pollFirstEntry();
		readlist = entry.getValue();
		for (PtuEvent ptuevent : readlist.values()) {
			//clear receptionevent time in inverse_reception_map is in node.onReception method
			eventslist.add(ptuevent);
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
