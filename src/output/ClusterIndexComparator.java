package output;

import java.util.Comparator;

/**
 * @author yfhuang created at 2014-3-20
 *
 */
public class ClusterIndexComparator implements Comparator<ClusterIndex>{
	@Override
	public int compare(ClusterIndex tp1,ClusterIndex tp2){
	
		//tp1.fromaddr=tp2.fromaddr and tp1.getToaddr=tp1.getToaddr
		if (tp1.getStime()<tp2.getStime()) return -1;
		if (tp1.getStime()>tp2.getStime()) return 1;
		
		//tp1.fromaddr=tp2.fromaddr and tp1.getToaddr=tp1.getToaddr and tp1.stime=tp2.stime
		if (tp1.getEtime()<tp2.getEtime()) return -1;
		if (tp2.getEtime()>tp2.getEtime()) return -1;
		
		return 0;
	}
}
