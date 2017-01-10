/**
 * 
 */
package core;

/**
 * @author yfhuang created at 2014-3-5
 * this class keeps one source view of current node
 * vector clock class
 */
public class SourceClock {
	private long date; // largest time until current node from other node by indirect
				// journey
	private DateJourney datej; //the indirect latest journey
	private long hop; // mininum hop until current node from other node by direct
				// journey
	private HopJourney hopj;//the direct latest journey
	
	private EdgeCseqList cseqlist; //cseqlist: the sequence of contact which forms hopj 
	//private EdgeCsequence scseq; //   scseq: the contact which leads to the hopj down
	
	public EdgeCseqList getEcseqlist() {
		return cseqlist;
	}

	public void setEcseqlist(EdgeCseqList ecseqlist) {
		this.cseqlist = ecseqlist;
	}

	public DateJourney getDatej() {
		return datej;
	}

	public HopJourney getHopj() {
		return hopj;
	}

	public long getDate() {
		return date;
	}

	public void setDate(long date) {
		this.date = date;
	}

	public long getHop() {
		return hop;
	}

	public void setHop(long hop) {
		this.hop = hop;
	}
/*
	public SourceClock(long _date, DateJourney _datej, long _hop, HopJourney _hopj) {
		this.date = _date;
		this.datej = _datej;
		this.hop = _hop;
		this.hopj = _hopj;
	}
*/
	public SourceClock(long _date, DateJourney _datej, long _hop, HopJourney _hopj,EdgeCseqList _ecseqlist) {
		this.date = _date;
		this.datej = _datej;
		this.hop = _hop;
		this.hopj = _hopj;
		//this.scseq = _ecseq;
		this.cseqlist=_ecseqlist;
	}
	
	public void setDatej(DateJourney _datej) {
		datej = _datej;
	}

	public void setHopj(HopJourney _hopj) {
		hopj = _hopj;
	}
	
	public SourceClock clone(){
		long _date=date;
		DateJourney _datej= datej.clone();
		long _hop=hop;
		HopJourney _hopj= hopj.clone();
		EdgeCsequence _ecseq=null;
		EdgeCseqList _ecseqlist=null;
		//_ecseq= scseq.clone();
		_ecseqlist=cseqlist.clone();
		SourceClock cloneclock=new SourceClock(_date,_datej,_hop,_hopj,_ecseqlist);
		return cloneclock;
	}
	
	public String toString(){
		StringBuffer strb=new StringBuffer();
		strb.append(" date is:"+date+"\n");
		strb.append(" datej is:[ ");
		strb.append(datej.toString());
		strb.append("]\n");
		
		strb.append(" hop is:"+hop+"\n");
		strb.append(" hopj is:[ ");
		strb.append(hopj.toString());
		strb.append("]\n");
		
		strb.append(" EdgeCseqlist is:[");
		strb.append(cseqlist.toString());
		strb.append("]\n");
		
		
		
		return strb.toString();
	}
	
	public boolean equals(Object o) {
		if (o==this){
			return true;
		}
		if (!(o instanceof SourceClock)) return false;
		SourceClock m= (SourceClock)o;
		return (date==m.getDate() && datej.equals(m.getDatej()) && hop==m.getHop() && hopj.equals(m.getHopj())); 
	}
}
