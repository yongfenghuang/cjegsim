package input;

import core.PucCseqList;;

/**
 ** @author yfhuang created at 2014-2-26
 */
public class PtuEvent extends CjEvent {
	private int fromaddr;
	private int toaddr;
	private PucCseqList cseqlist;
	
	public int getFromaddr() {
		return fromaddr;
	}

	public int getToaddr() {
		return toaddr;
	}

	
	public PucCseqList getPtuCseqList() {
		return cseqlist;
	}

	public PtuEvent(int _fromaddr, int _toaddr, long _receptiontime,PucCseqList cseqlist) {
		super(_receptiontime);
		this.fromaddr = _fromaddr;
		this.toaddr = _toaddr;
		this.cseqlist=cseqlist;
		// TODO Auto-generated constructor stub
	}
}
