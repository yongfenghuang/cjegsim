package tool.reality;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.TreeMap;
import java.util.Vector;
import tool.CrJourneyClusterNoEdge;
import tool.Log;
import tool.NodePair;
import tool.tmg.RandomUtility;

/**
 ** @author yfhuang created at 2014-8-10 hop distribution data
 */

public class Reality_HopDistribution {

	String database = "reality";
	static int level = 1000;
	Connection conn;

	public Reality_HopDistribution() {

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

		HashSet<Integer> activenodes = new HashSet<Integer>();
		// read record from mysql
		String sqlstr1 = "SELECT node_id from cjeg_activenodes";
		//Log.writeln(sqlstr1, level);
		ResultSet rs = null;
		PreparedStatement stmt1;
		try {
			stmt1 = conn.prepareStatement(sqlstr1);
			rs = stmt1.executeQuery();
			while (rs.next()) {
				int nodeid = rs.getInt("node_id");
				activenodes.add(nodeid);
			}
			if (rs != null)
				rs.close();
			if (stmt1 != null)
				stmt1.close();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {

		}

		Object[] activenodes_array = new Object[activenodes.size()];
		activenodes_array = activenodes.toArray();
		int selectedcount = 0;
		HashSet<NodePair> selectedpair = new HashSet<NodePair>();
		while (selectedcount < _paircount) {
			int sourceindex = RandomUtility.nextUniformInt(0,
					activenodes_array.length - 1);
			int desindex = RandomUtility.nextUniformInt(0,
					activenodes_array.length - 1);
			if (sourceindex == desindex)
				continue;
			Integer source = (Integer) activenodes_array[sourceindex];
			Integer destination = (Integer) activenodes_array[desindex];
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
		String sqlstr1 = "SELECT stime, etime, ctype,hopcount,delay FROM cjeg_crjourneyegmap_1 WHERE source ="
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
				int hop = rs.getInt("hopcount");
				CrJourneyClusterNoEdge crjc = new CrJourneyClusterNoEdge(stime,
						etime, delay, ctype,hop);
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
	
	public void insertToMysql(Vector<Integer> rhop_set){
		PreparedStatement stmt2=null;
		String sqlstr2 = "INSERT INTO `cjeg_rhop` (`rhop`) VALUES (?)";  
		try {
			stmt2 = conn.prepareStatement(sqlstr2);
			int vecsize = rhop_set.size();
			Log.writeln("size of vector:"+rhop_set.size(), level);
			int hasinserted=0;
			while(hasinserted<vecsize){
				Integer element = (Integer)rhop_set.get(hasinserted);
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
		//reality
		int paircount = 720;
		int maxtime = 1209493;
		int randomcount = 100;
		
		Vector<Integer> rhop_set=new Vector<Integer>();
		getConnection();
		HashSet<NodePair> selectedpairs = getRandomSelectedPairs(paircount);
		Object[] pairsarr = selectedpairs.toArray();
		Log.writeln("total " + pairsarr.length + " pairs.", level);
		for (int i = 0; i < pairsarr.length; i++) {
			rhop_set.clear();
			NodePair onepair = (NodePair) pairsarr[i];
			Log.write("pair is:(" + onepair.source + "," + onepair.destination
					+ ")\n", level);
			TreeMap<Integer, CrJourneyClusterNoEdge> crjcmap = getCjeg_BetweenPairs(
					onepair.source, onepair.destination);
			
			int counted = 0;
			while (counted < randomcount) {
				int t = RandomUtility.nextUniformInt(0, maxtime);
				Log.writeln("t:" + t, level);
				int rhop=Integer.MAX_VALUE;
				Integer stime1 = crjcmap.floorKey(t);
				Integer stime2 = crjcmap.ceilingKey(t);
				if (stime1 == null && stime2 == null) {
					// do nothing
				} else if ((stime1 == null) && (stime2 != null)) {
					rhop = crjcmap.get(stime2).hop;
				} else if ((stime1 != null) && (stime2 == null)) {
					CrJourneyClusterNoEdge crjc1 = crjcmap.get(stime1);
					Integer etime1 = crjc1.etime;
					if (etime1 >= t)
						rhop = crjc1.hop;
				} else {
					CrJourneyClusterNoEdge crjc1 = crjcmap.get(stime1);
					int etime1 = crjc1.etime;
					CrJourneyClusterNoEdge crjc2 = crjcmap.get(stime2);
					if (etime1 >= t) {
						rhop = crjc1.hop;
					} else {
						rhop = crjc2.hop;
					}
				}
				
				if (rhop!=Integer.MAX_VALUE) rhop_set.add(rhop);
				counted++;
			}
			insertToMysql(rhop_set);
		}
		close();
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		// TODO Auto-generated method stub
		Reality_HopDistribution hopdis = new Reality_HopDistribution();
		hopdis.exec();
	}
}
