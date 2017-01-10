package core;

/**
 * @author yfhuang created at 2014-10-31
 *
 */
public class EdgeCsequence {
	private Edge e;
	private int sequence; //from 0 to increase
	public Edge getE() {
		return e;
	}
	public void setE(Edge e) {
		this.e = e;
	}
	public int getSequence() {
		return sequence;
	}
	public void setSequence(int sequence) {
		this.sequence = sequence;
	}
	
	@Override
	public EdgeCsequence clone() {
		Edge thisedge = this.getE();
		Edge thatedge=null;
		if (thisedge!=null) thatedge = thisedge.clone();
		EdgeCsequence cloneecseq = new EdgeCsequence(thatedge,this.getSequence());
		return cloneecseq;
	}

	public EdgeCsequence(Edge _e, int _sequence){
		setE(_e);
		setSequence(_sequence);
	}
	
	public EdgeCsequence(){
	}
	
	public String toString() {
		StringBuffer strb = new StringBuffer();
		if (e!=null){
			strb.append("edge:"+e.toString());
			strb.append("sequence:"+sequence);
		}
		return strb.toString();
	}
}
