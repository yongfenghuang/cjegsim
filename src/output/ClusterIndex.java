package output;

/**
 * @author yfhuang created at 2014-3-20
 *
 */
public class ClusterIndex{
	private long stime;
	private long etime;
	
	public long getStime() {
		return stime;
	}
	public long getEtime() {
		return etime;
	}
	
	public ClusterIndex(long _stime,long _etime){
		stime=_stime;
		etime=_etime;
	}
}
