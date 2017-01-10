package input;

import core.VectorOnNode;

/**
 ** @author yfhuang created at 2014-2-26
 */
public class ReceptionEvent extends CjEvent {
	private int fromaddr;
	private int toaddr;
	private VectorOnNode fromvec;
	
	public int getFromaddr() {
		return fromaddr;
	}

	public int getToaddr() {
		return toaddr;
	}

	
	public VectorOnNode getFromvec() {
		return fromvec;
	}

	public ReceptionEvent(int _fromaddr, int _toaddr, long _receptiontime,VectorOnNode fromvec) {
		super(_receptiontime);
		this.fromaddr = _fromaddr;
		this.toaddr = _toaddr;
		this.fromvec=fromvec;
		// TODO Auto-generated constructor stub
	}
}
