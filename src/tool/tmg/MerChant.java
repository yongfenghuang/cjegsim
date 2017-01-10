package tool.tmg;

import java.io.PrintStream;
import java.text.DecimalFormat;
import java.util.Iterator;

import edu.stanford.math.primitivelib.autogen.pair.IntIntPair;

/**
 * @author yfhuang created at 2014-7-10
 *
 */
public class MerChant {
	private int nu;
	private int gamma;
	private int connect_village;
	private int connect_resident;
	private double prob_mobility;
	private int mid;
	private long time;
	public MerChant(int _nu,int _gamma,double _prob_mobility, int _mid,long _time){
		this.nu=_nu;
		this.gamma=_gamma;
		this.prob_mobility=_prob_mobility;
		this.mid=_mid;
		this.time=_time;
	}
	
	public void generate() {
		connect_village=RandomUtility.nextUniformInt(0,nu-1);
		connect_resident=RandomUtility.nextUniformInt(0,gamma-1);
		String eventtypes="UP";
		TravelingMerchantGraph.insertRecord_m(time,connect_village,connect_resident,mid,eventtypes);
	}
	
	public void generateNext(long t) {
		time=t;
		if (RandomUtility.nextBernoulli(prob_mobility)==1){
			String eventtypes="DOWN";
			TravelingMerchantGraph.insertRecord_m(time,connect_village,connect_resident,mid,eventtypes);
			int new_village=connect_village;
			while (new_village==connect_village)
			   new_village=RandomUtility.nextUniformInt(0,nu-1);
			connect_village=new_village;
			connect_resident=RandomUtility.nextUniformInt(0,gamma-1);
			eventtypes="UP";
			TravelingMerchantGraph.insertRecord_m(time,connect_village,connect_resident,mid,eventtypes);
		}
	}
	
	@Override
	public String toString() {
		String retstr="this is merchant:"+mid+"\n";
		retstr=retstr+"Merchant(" + connect_village + "," + connect_resident + ")\n";
		retstr=retstr+"\n";
		return retstr;
	}
	
	public void set_village(int _village){
		connect_village=_village;
	}
	
	public void set_resident(int _resident){
		connect_resident=_resident;
	}
}
