package tool.reality;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Map;
import java.util.TreeMap;
import java.util.Vector;

import tool.CrJourneyClusterNoEdge;
import tool.Log;
import tool.NodePair;
import tool.tmg.RandomUtility;

/**
 ** @author yfhuang created at 2014-8-10 inter-cluster time distribution data
 */
public class Reality_Inter_Crjc {
	String database = "reality";
	static int level = 1000;
	Connection conn;

	public Reality_Inter_Crjc() {

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

	public Vector<Integer> getAllActiveNodes() {

		Vector<Integer> activenodes = new Vector<Integer>();
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

		return activenodes;
	}

	public Vector<CrJourneyClusterNoEdge> getCjeg_BetweenPairs(
			int source, int destination) {
		Vector<CrJourneyClusterNoEdge> crjcvec = new Vector<CrJourneyClusterNoEdge>();
		// read record from mysql
		String sqlstr1 = "SELECT stime, etime, ctype, delay FROM cjeg_crjourneyegmap_1 WHERE source ="
				+ source
				+ " AND destination ="
				+ destination
				+ " order by stime,etime";
		
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
				crjcvec.add(crjc);
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
		return crjcvec;

	}
	
	public void insertToMysql(Vector<Integer> interval_set){
		PreparedStatement stmt2=null;
		String sqlstr2 = "INSERT INTO `cjeg_interval` (`intertime`) VALUES (?)";  
		try {
			stmt2 = conn.prepareStatement(sqlstr2);
			int vecsize = interval_set.size();
			Log.writeln("size of vector:"+interval_set.size(), level);
			int hasinserted=0;
			while(hasinserted<vecsize){
				Integer element = (Integer)interval_set.get(hasinserted);
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
		Vector<Integer> interval_set=new Vector<Integer>();
		getConnection();
		Vector<Integer> activenodes = getAllActiveNodes();
		Log.writeln("total " + activenodes.size() + " nodes.", level);
		for (int i = 0; i < activenodes.size(); i++) {
			for(int j=i+1;j<activenodes.size();j++){
				NodePair onepair = new NodePair(activenodes.get(i),activenodes.get(j));
				//NodePair onepair = new NodePair(86,49);
				Log.write("pair is:(" + onepair.source + "," + onepair.destination
						+ ")\n", level);
				generateIntervalForPair(onepair,interval_set);
				NodePair anotherpair = new NodePair(activenodes.get(j),activenodes.get(i));
				//NodePair anotherpair = new NodePair(49,86);
				Log.write("pair is:(" + anotherpair.source + "," + anotherpair.destination
						+ ")\n", level);
				generateIntervalForPair(anotherpair,interval_set);
			}
		}
		insertToMysql(interval_set);
		close();
	}
	
	public void generateIntervalForPair(NodePair onepair,Vector<Integer> interval_set){
		Vector<CrJourneyClusterNoEdge> crjcvec = getCjeg_BetweenPairs(
				onepair.source, onepair.destination);
		int last_etime=0;
		CrJourneyClusterNoEdge crjc;
		for(int k=0;k<crjcvec.size();k++) {
			crjc=crjcvec.get(k);
			int stime=crjc.stime;
			int interval=stime-last_etime;
			interval_set.add(interval);
			last_etime=crjc.etime;
		}
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		// TODO Auto-generated method stub
		Reality_Inter_Crjc intercrjc = new Reality_Inter_Crjc();
		intercrjc.exec();
	}
}
