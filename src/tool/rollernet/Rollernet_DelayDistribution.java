package tool.rollernet;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.TreeMap;
import java.util.Vector;
import tool.Log;
import tool.tmg.RandomUtility;

/**
 ** @author yfhuang created at 2014-8-10
 */
class NodePair {
	Integer source;
	Integer destination;

	NodePair(Integer _source, Integer _destination) {
		source = _source;
		destination = _destination;
	}
}

class CrJourneyClusterNoEdge {
	int stime;
	int etime;
	int delay;
	int ctype;

	CrJourneyClusterNoEdge(int _stime, int _etime, int _delay, int _ctype) {
		this.stime = _stime;
		this.etime = _etime;
		this.delay = _delay;
		this.ctype = _ctype;
	}
}

public class Rollernet_DelayDistribution {

	String database = "rollernet";
	static int level = 1000;
	Connection conn;

	public Rollernet_DelayDistribution() {

	}

	public void getConnection() {
		try {
			String username = "root";
			String password = "";
			Class.forName("com.mysql.jdbc.Driver");
			conn = java.sql.DriverManager.getConnection(
					"jdbc:mysql://localhost/" + database
							+ "?useUnicode=true&characterEncoding=UTF8",
					username, password);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void close() {
		if (conn != null)
			try {
				conn.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	}

	public HashSet<NodePair> getRandomSelectedPairs(int _paircount) {
		int selectedcount = 0;
		int nodenumber=62;
		HashSet<NodePair> selectedpair = new HashSet<NodePair>();
		while (selectedcount < _paircount) {
			int source = RandomUtility.nextUniformInt(1,nodenumber);
			int destination = RandomUtility.nextUniformInt(1,nodenumber);
			if (source == destination)
				continue;
			NodePair nodepair = new NodePair(source, destination);
			if (selectedpair.contains(nodepair))
				continue;
			selectedpair.add(nodepair);
			selectedcount++;
		}
		return selectedpair;
	}

	public TreeMap<Integer, CrJourneyClusterNoEdge> getCjeg_BetweenPairs(
			int source, int destination) {
		TreeMap<Integer, CrJourneyClusterNoEdge> crjcmap = new TreeMap<Integer, CrJourneyClusterNoEdge>();
		// read record from mysql
		String sqlstr1 = "SELECT stime, etime, ctype, delay FROM cjeg_crjourneyegmap_1 WHERE source ="
				+ source
				+ " AND destination ="
				+ destination
				+ " order by stime";
		//Log.writeln(sqlstr1, level);
		ResultSet rs = null;
		PreparedStatement stmt1;
		try {
			stmt1 = conn.prepareStatement(sqlstr1);
			rs = stmt1.executeQuery();
			while (rs.next()) {
				int stime = rs.getInt("stime");
				int etime = rs.getInt("etime");
				int delay = rs.getInt("delay");
				int ctype = rs.getInt("ctype");
				CrJourneyClusterNoEdge crjc = new CrJourneyClusterNoEdge(stime,
						etime, delay, ctype);
				if (crjcmap.get(stime) != null) {
					//Log.writeln("error:duplicate stime "+stime+" etime "+etime, level);
					//throw new CjegException("error:duplicate stime");
					crjcmap.put(stime+1, crjc);
				} else {
					crjcmap.put(stime, crjc);
				}
			}
			if (rs != null)
				rs.close();
			if (stmt1 != null)
				stmt1.close();
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		} finally {

		}
		return crjcmap;

	}
	
	public void insertToMysql(Vector<Integer> rdelay_set){
		PreparedStatement stmt2=null;
		String sqlstr2 = "INSERT INTO `cjeg_rdelay` (`rdelay`) VALUES (?)";  
		try {
			stmt2 = conn.prepareStatement(sqlstr2);
			int vecsize = rdelay_set.size();
			Log.writeln("size of vector:"+rdelay_set.size(), level);
			int hasinserted=0;
			while(hasinserted<vecsize){
				Integer element = (Integer)rdelay_set.get(hasinserted);
				stmt2.setInt(1,element);
				stmt2.executeUpdate();
				hasinserted++;
			}
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			try {
				if (stmt2 != null)
					stmt2.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public void exec() {
		int paircount = 720;
		int maxtime = 10140;
		int randomcount = 100;
		
		Vector<Integer> rdelay_set=new Vector<Integer>();
		getConnection();
		HashSet<NodePair> selectedpairs = getRandomSelectedPairs(paircount);
		Object[] pairsarr = selectedpairs.toArray();
		Log.writeln("total " + pairsarr.length + " pairs.", level);
		for (int i = 0; i < pairsarr.length; i++) {
			NodePair onepair = (NodePair) pairsarr[i];
			Log.write("pair is:(" + onepair.source + "," + onepair.destination
					+ ")\n", level);
			TreeMap<Integer, CrJourneyClusterNoEdge> crjcmap = getCjeg_BetweenPairs(
					onepair.source, onepair.destination);
			int counted = 0;
			while (counted < randomcount) {
				int t = RandomUtility.nextUniformInt(0, maxtime);
				//Log.writeln("t:" + t, level);
				int rdelay = Integer.MAX_VALUE;
				Integer stime1 = crjcmap.floorKey(t);
				Integer stime2 = crjcmap.ceilingKey(t);
				if (stime1 == null && stime2 == null) {
					// do nothing
				} else if ((stime1 == null) && (stime2 != null)) {
					rdelay = (stime2 - t) + crjcmap.get(stime2).delay;
				} else if ((stime1 != null) && (stime2 == null)) {
					CrJourneyClusterNoEdge crjc1 = crjcmap.get(stime1);
					Integer etime1 = crjc1.etime;
					if (etime1 >= t)
						rdelay = crjc1.delay;
				} else {
					CrJourneyClusterNoEdge crjc1 = crjcmap.get(stime1);
					int etime1 = crjc1.etime;
					CrJourneyClusterNoEdge crjc2 = crjcmap.get(stime2);
					if (etime1 >= t) {
						rdelay = crjc1.delay;
					} else {
						rdelay = (stime2 - t) + crjc2.delay;
					}
				}
				//Log.writeln("rdelay:" + rdelay, 1000);
				rdelay_set.add(rdelay);
				counted++;
			}
		}
		insertToMysql(rdelay_set);
		close();
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		// TODO Auto-generated method stub
		Rollernet_DelayDistribution delaydis = new Rollernet_DelayDistribution();
		delaydis.exec();
	}
}
