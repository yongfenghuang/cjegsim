package core;

/**
 * @author yfhuang created at 2014-3-5
 * 
 */

public enum JourneyType {

	DIRECT("DIRECT",0), INDIRECT("INDIRECT",1);
	
	private String desp;
	private int value;

	public int getValue() {
		return value;
	}

	private JourneyType(String _desp,int value) {
		this.desp = _desp;
		this.value=value;
	}

	@Override
	public String toString() {
		return String.valueOf(this.desp);
	}
}
