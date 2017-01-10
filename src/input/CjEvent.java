package input;
/**
 **@author yfhuang
 **created at 2014-2-26
 */
public abstract class CjEvent {
	protected long time;
	public long getTime() {
		return time;
	}
	public void setTime(long _time) {
		this.time = _time;
	}
	public CjEvent(long _etime) {
		this.time=_etime;
	}
}
