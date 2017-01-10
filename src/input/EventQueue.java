package input;

import java.util.List;

/**
 ** @author yfhuang created at 2014-2-26
 */

public interface EventQueue{

	/**
	 * Returns the next event in the queue or ExternalEvent with time of
	 * Long.MAX_VALUE if there are no events left.
	 * 
	 * @return The next eventslist
	 */
	public List<CjEvent> nextEvents();

	/**
	 * Returns next event's time or Long.MAX_VALUE if there are no events left
	 * in the queue.
	 * 
	 * @return Next event's time
	 */
	public long nextEventsTime();

	public CjEventListener getEventListener();
}
