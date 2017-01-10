package input;

/**
 ** @author yfhuang created at 2014-2-26
 */
public class ConnectionEvent extends CjEvent {
	private int fromaddr;
	private int toaddr;
	private boolean up;

	public int getFromaddr() {
		return fromaddr;
	}
	
	public int getToaddr() {
		return toaddr;
	}
	
	public boolean isUp() {
		return up;
	}

	public void setUp(boolean _isup) {
		this.up = _isup;
	}

	public ConnectionEvent(int _fromaddr, int _toaddr, boolean _up, long _time) {
		super(_time);
		this.fromaddr = _fromaddr;
		this.toaddr = _toaddr;
		this.up = _up;
	}
	
	public ConnectionEvent(long time){
		super(time);
	}
	
}
