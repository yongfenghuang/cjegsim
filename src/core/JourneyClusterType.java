package core;

/**
 * @author yfhuang created at 2014-3-5
 * 
 */
public enum JourneyClusterType {
	DISCRETE("DISCRETE",0), CONTINUOUS("CONTINUOUS",1), UNSURE("UNSURE",2);
	private String desp;
	private int value;

	public String getDesp() {
		return desp;
	}

	public int getValue() {
		return value;
	}

	private JourneyClusterType(String _desp,int value) {
		this.desp = _desp;
		this.value= value;
	}
	
	
	@Override
	public String toString() {
		return String.valueOf(this.desp);
	}
}
