package tool.tmg;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import app.CjegException;


/**
 * @author yfhuang created at 2014-7-10 reference article
 *         "Temporal node centrality in complex networks"
 */

public class TravelingMerchantGraph {

	/* G(eta,nu,gamma,p,b,d) */
	private int eta;// merchant number
	private int nu;// village number
	private int gamma;// resident number
	private double p;// initial probability between two residents in one village
	private double b;// birth probability
	private double d;// death probability
	private double[] prob_mobility;

	private HashMap<Integer, RandomGraph> tmg_hashmap = new HashMap<Integer, RandomGraph>();
	private HashMap<Integer, MerChant> mct_hashmap = new HashMap<Integer, MerChant>();

	private static HashMap<String, Integer> user_hashmap = new HashMap<String, Integer>();
	private static TreeMap<Integer, String> reverse_map = new TreeMap<Integer, String>();

	private static int serialresidentid = 0;
	private static int serialmerchantid = 100;

	private long time_steps;
	private static PrintStream myout;

	TravelingMerchantGraph(int _eta, int _nu, int _gamma, double _p, double _b,
			double _d, double[] _prob_mobility, int _time_steps) {
		eta = _eta;
		nu = _nu;
		gamma = _gamma;
		p = _p;
		b = _b;
		d = _d;
		prob_mobility = _prob_mobility;
		time_steps = _time_steps;
	}

	public void evolving() {

		// generate id
		for (int i = 0; i < nu; i++) {
			for (int j = 0; j < gamma; j++) {
				String userstr = i + "_" + j;
				int residentid = generateResidentId();
				user_hashmap.put(userstr, residentid);
				reverse_map.put(residentid, userstr);
			}
		}

		for (int i = 0; i < eta; i++) {
			String userstr = "m_" + i;
			int merchantid = generateMerchantId();
			user_hashmap.put(userstr, merchantid);
			reverse_map.put(merchantid, userstr);
		}

		// at initial time step 0
		long t = 0;
		System.out.println("in all has " + nu + " villages");

		for (int i = 0; i < nu; i++) {
			RandomGraph rg = new RandomGraph(gamma, p, b, d, i, t);
			rg.generate();
			tmg_hashmap.put(i, rg);
			System.out.println(rg.toString());
		}

		System.out.println("in all has " + eta + " merchant");
		for (int i = 0; i < eta; i++) {
			MerChant mct = new MerChant(nu, gamma, prob_mobility[i], i, t);
			mct.generate();
			mct_hashmap.put(i, mct);
			System.out.println(mct.toString());
		}

		for (t = 1; t <=time_steps; t++) {
			System.out.println("t is:"+t);
			for (int i = 0; i < nu; i++) {
				RandomGraph rg = tmg_hashmap.get(i);
				rg.generateNext(t);
				System.out.println(rg.toString());
			}

			for (int i = 0; i < eta; i++) {
				MerChant mct = mct_hashmap.get(i);
				mct.generateNext(t);
				System.out.println(mct.toString());
			}
		}
	}

	public static void insertRecord(long time, int rgid, int i, int j,
			String eventtypes) {
		String user1_id = rgid + "_" + i;
		String user2_id = rgid + "_" + j;
		myout.println(time + " " + getResidentUserid(user1_id) + " "
				+ getResidentUserid(user2_id) + " " + eventtypes);
	}

	public static void insertRecord_m(long time, int rgid, int i, int mid,
			String eventtypes) {
		String user1_id = rgid + "_" + i;
		String user2_id = "m_" + mid;
		myout.println(time + " " + getResidentUserid(user1_id) + " "
				+ getMerchantUserid(user2_id) + " " + eventtypes);
	}

	public static int generateResidentId() {
		serialresidentid++;
		return serialresidentid;
	}

	public static int generateMerchantId() {
		serialmerchantid++;
		return serialmerchantid;
	}

	public static int getResidentUserid(String residentstr) {
		if (user_hashmap.get(residentstr) != null)
			return user_hashmap.get(residentstr).intValue();
		else {
			throw new CjegException("no such residentstr:" + residentstr);
		}
	}

	public static int getMerchantUserid(String merchantstr) {
		if (user_hashmap.get(merchantstr) != null)
			return user_hashmap.get(merchantstr).intValue();
		else {
			throw new CjegException("no such merchantstr:" + merchantstr);
		}
	}

	public void setMyout(PrintStream _myout) {
		myout = _myout;
	}

	public void printUser() {
		System.out.println("userid----------user");
		for (Map.Entry<Integer, String> entry : reverse_map.entrySet()) {
			System.out.print(entry.getKey());
			System.out.print("     ");
			System.out.println(entry.getValue());
		}
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// parameter config
		int _eta = 20;// merchant number
		int _nu = 10;// village number
		int _gamma = 10;// resident number in every village
		double _p = 0.1;// initial probability between two residents in one
						// village
		double _b = 0.1;// birth probability
		double _d = 0.5;// death probability
		double[] _prob_mobility = new double[_eta];// birth probability array
		int _time_steps = 1000;

		// init _prob_mobility
		for (int i = 0; i < _eta; i++) {
			// _prob_mobility[i] = 1;
			// prob_mobility is in interval [0.5 1]
			_prob_mobility[i] = RandomUtility.nextUniform() / 2 + 0.5; 
		}

		TravelingMerchantGraph tmg = new TravelingMerchantGraph(_eta, _nu,
				_gamma, _p, _b, _d, _prob_mobility, _time_steps);

		try {
			PrintStream myout = new PrintStream(new FileOutputStream(new File(
					"tmg/tmg.txt")),true);
			tmg.setMyout(myout);
			tmg.evolving();
			myout.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

		//init _prob_mobility
		for (int i = 0; i < _eta; i++) {
			String userid="m_"+i;
			System.out.println("prob_mobility of merchant " + tmg.getMerchantUserid(userid) + " is:"+ _prob_mobility[i]);
		}
		tmg.printUser();
	}
}
