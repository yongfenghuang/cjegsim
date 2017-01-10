package core;
import java.util.LinkedList;

/**
 * @author yfhuang created at 2014-10-31
 *
 */

public class EdgeCseqList extends LinkedList<EdgeCsequence>{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public void addEdgeCse(EdgeCsequence edgecse) {
		this.add(edgecse);
	}
	
	@Override
	public EdgeCseqList clone() {
		EdgeCseqList cloneecseqlist = new EdgeCseqList();
		for (int j = 0; j < this.size(); j++) {
			EdgeCsequence thisecseq = this.get(j);
			EdgeCsequence thatecseq = thisecseq.clone();
			cloneecseqlist.add(thatecseq);
		}
		return cloneecseqlist;
	}
	public EdgeCseqList(){
		
	}
	
	public String toString() {
		StringBuffer strb = new StringBuffer();
		for (int index = 0; index < this.size(); index++) {
			strb.append(this.get(index).toString());
			strb.append(" ");
		}
		return strb.toString();
	}
}
