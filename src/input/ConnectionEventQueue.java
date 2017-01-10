package input;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import tool.AppUtil;


/**
 ** @author yfhuang created at 2014-2-26
 */

public class ConnectionEventQueue implements EventQueue {
	private File eventsfile;
	// private List<ConnectionEvent> queue;
	private TreeMap<Long, List<ConnectionEvent>> queuebuffer;
	private int QUEUE_SIZE = 500;

	private boolean finishread = false;
	private EventsReader reader;
	private ConnectionEventListener coneventlistener;

	public void setEventlistener(ConnectionEventListener _coneventlistener) {
		this.coneventlistener = _coneventlistener;
	}

	public CjEventListener getEventListener() {
		//Log.write("get connection eventlistener");
		return coneventlistener;
	}

	public ConnectionEventQueue(String _storename, String _eventsfile) {
		this.eventsfile = AppUtil.getFile(_storename, _eventsfile);
		this.queuebuffer = new TreeMap<Long, List<ConnectionEvent>>();
		// put max value time event
		queuebuffer.put(Long.MAX_VALUE, new ArrayList<ConnectionEvent>(0));
		init();
	}

	public void init() {
		this.reader = new StandardEventsReader(eventsfile);
		readEvents(QUEUE_SIZE);
	}

	public void readEvents(int linenumber) {
		List<ConnectionEvent> eventslist = new ArrayList<ConnectionEvent>();
		if (!finishread) {
			eventslist = reader.readEvents(linenumber);
			if (linenumber > 0 && eventslist.size() == 0) {
				reader.close();
				finishread = true;
			}
		}

		for (ConnectionEvent conevent : eventslist)
			queue(conevent);
	}

	public void queue(ConnectionEvent _conevent) {
		Long time = _conevent.getTime();
		List<ConnectionEvent> eventsattime = queuebuffer.get(time);
		if (eventsattime == null) {
			eventsattime = new ArrayList<ConnectionEvent>();
			queuebuffer.put(time, eventsattime);
		}
		queuebuffer.get(time).add(_conevent);

	}

	@Override
	public List<CjEvent> nextEvents() {
		
		List<CjEvent> eventslist = new ArrayList<CjEvent>();
		List<ConnectionEvent> readlist = new ArrayList<ConnectionEvent>();
		Map.Entry<Long, List<ConnectionEvent>> entry = queuebuffer
				.pollFirstEntry();
		readlist = entry.getValue();
		if (nextEventsTime() == Long.MAX_VALUE) // ran out of events
			readEvents(QUEUE_SIZE);
		for (ConnectionEvent conevent : readlist) {
			eventslist.add(conevent);
		}
		return eventslist;
	}

	@Override
	public long nextEventsTime() {
		if (queuebuffer.size() != 0)
			return queuebuffer.firstKey();
		else
			return Long.MIN_VALUE;
	}
}
