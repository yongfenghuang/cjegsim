package input;

/**
 ** @author yfhuang created at 2014-2-26
 *  continuecrjcevent = DvcEvent = dumb view changed event
 */
public class DvcEvent extends CjEvent {
	private int fromaddr;
	private int toaddr;
	//inject_time
	private long stime; //Start time of critical journey cluster between fromaddr and toaddr
	
	public long getStime() {
		return stime;
	}

	public int getFromaddr() {
		return fromaddr;
	}

	public int getToaddr() {
		return toaddr;
	}
	
	public DvcEvent(int _fromaddr, int _toaddr, long _trigtime,long _stime) {
		super(_trigtime);
		this.stime=_stime;
		this.fromaddr = _fromaddr;
		this.toaddr = _toaddr;
	}
}
