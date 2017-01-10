package input;

/**
 ** @author yfhuang created at 2014-2-26
 */
public class CrJourneyCluFlushEventListener implements CjEventListener {
	public void onCrJourneyFlush(CrJourneyCluFlushEvent crjflushevent) {

	}

	public void processEvent(CjEvent cjevent) {
		CrJourneyCluFlushEvent crjflushevent = (CrJourneyCluFlushEvent) cjevent;
		onCrJourneyFlush(crjflushevent);
	}
}
