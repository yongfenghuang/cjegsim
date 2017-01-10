package tool;
/**
 **@author yfhuang
 **created at 2014-8-19
 */
public class CrJourneyClusterNoEdge {
	public int stime;
	public int etime;
	public int delay;
	public int ctype;
    public int hop;
    
	public CrJourneyClusterNoEdge(int _stime, int _etime, int _delay, int _ctype) {
		this.stime = _stime;
		this.etime = _etime;
		this.delay = _delay;
		this.ctype = _ctype;
	}
	
	public CrJourneyClusterNoEdge(int _stime, int _etime, int _delay, int _ctype,int _hop) {
		this.stime = _stime;
		this.etime = _etime;
		this.delay = _delay;
		this.ctype = _ctype;
		this.hop= _hop;
	}
	
}
