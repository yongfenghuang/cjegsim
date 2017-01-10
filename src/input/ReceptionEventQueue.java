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
public class ReceptionEventQueue implements EventQueue {
	
	private TreeMap<Long, HashMap<Edge, ReceptionEvent>> queuebuffer;
	private ReceptionEventListener receventlistener;

	public void setEventlistener(ReceptionEventListener receventlistener) {
		this.receventlistener = receventlistener;
	}
	
	@Override
	public CjEventListener getEventListener() {
		// Log.write("get reception eventlistener");
		return receventlistener;
	}

	public ReceptionEventQueue() {
		this.queuebuffer = new TreeMap<Long, HashMap<Edge, ReceptionEvent>>();
		// put max value time event
		queuebuffer.put(Long.MAX_VALUE, new HashMap<Edge, ReceptionEvent>(0));
	}

	public void queue(ReceptionEvent _recevent) {
		Long time = _recevent.getTime();
		HashMap<Edge, ReceptionEvent> eventsattime = queuebuffer.get(time);
		Edge edge = new Edge(_recevent.getFromaddr(), _recevent.getToaddr());
		if (eventsattime == null) {
			eventsattime = new HashMap<Edge, ReceptionEvent>();
			queuebuffer.put(time, eventsattime);
		}

		if (eventsattime.get(edge) == null) {
			queuebuffer.get(time).put(edge, _recevent);
		}

	}

	public void removeExistevent(Long _time, Edge _edge) {
		HashMap<Edge, ReceptionEvent> eventsattime = queuebuffer.get(_time);
		if (eventsattime != null){
				eventsattime.remove(_edge);
				if (eventsattime.isEmpty()) queuebuffer.remove(_time);
		}else {
			throw new CjegException("there's no such ReceptionEvent in receptioneventqueue");
		}
	}

	@Override
	public List<CjEvent> nextEvents() {
		List<CjEvent> eventslist = new ArrayList<CjEvent>();
		HashMap<Edge, ReceptionEvent> readlist;
		Map.Entry<Long, HashMap<Edge, ReceptionEvent>> entry = queuebuffer
				.pollFirstEntry();
		readlist = entry.getValue();
		for (ReceptionEvent recevent : readlist.values()) {
			//clear receptionevent time in inverse_reception_map is in node.onReception method
			eventslist.add(recevent);
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
