package input;
/**
 **@author yfhuang
 **created at 2014-2-26
 */
import java.util.ArrayList;
import java.util.List;

public class EventQueueBus {
	
	private List<EventQueue> queues;
	//private long earliest;
	private CjEventListener cjeventlistener;
	private long eventstime=Long.MAX_VALUE;
	
	public EventQueueBus() {
		queues=new ArrayList<EventQueue>();
	}

	/**
	 * Returns all the loaded event queues
	 * 
	 * @return all the loaded event queues
	 */
	public List<EventQueue> getEventQueues() {
		return this.queues;
	}
	
	public void addQueue(EventQueue eventqueue){
		queues.add(eventqueue);
	}
	
	public List<CjEvent> nextEvents(){
		//Log.write("excute EventQueueBus nextevent");
		long earliest=Long.MAX_VALUE;
		EventQueue nextqueue=queues.get(0);
		//Log.write("next event queue:"+nextqueue.toString());
		/* find the queue that has the next event */
		for (EventQueue eq : queues) {
			//Log.write("nextqueue:"+eq.toString()+" "+eq.nextEventsTime());
			if (eq.nextEventsTime() < earliest){
				nextqueue = eq;
				earliest = eq.nextEventsTime();
			}
		}
		
		cjeventlistener=nextqueue.getEventListener();
		eventstime=earliest;
		List<CjEvent> eventslist=nextqueue.nextEvents();
		return eventslist;
	}
	
	public long getNextEventsTime(){
		long earliest=Long.MAX_VALUE;
		for (EventQueue eq : queues) {
			if (eq.nextEventsTime() < earliest){
				earliest = eq.nextEventsTime();
			}
		}
		return earliest;
	}
	
	public long getEventsTime(){
		return eventstime;
	}
	
	public CjEventListener getEventListener(){
		return cjeventlistener;
	}

}
