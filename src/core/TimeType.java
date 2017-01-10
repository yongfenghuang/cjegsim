package core;

/**
 * @author yfhuang created at 2014-3-5
 *
 */
public enum TimeType {
	CLOSE("CLOSE"), OPEN("OPEN");	
	private String desp;

	private TimeType(String _desp) {
		this.desp = _desp;
	}

	@Override
	public String toString() {
		return String.valueOf(this.desp);
	}
}
