package tool.rollernet;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.TreeMap;
import java.util.Vector;
import tool.CrJourneyClusterNoEdge;
import tool.Log;
import tool.VelocityPair;

/**
 ** @author yfhuang created at 2014-8-26
 */

public class Rollernet_AverageDelay_Evolving {

	String database = "rollernet";
	static int level = 1000;
	Connection conn;
	TreeMap<Integer, CrJourneyClusterNoEdge> crjcmap[][];
	
	@SuppressWarnings("unchecked")
	public Rollernet_AverageDelay_Evolving() {
		crjcmap=new TreeMap[100][100];
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
		String sqlstr1 = "SELECT nodeid from cjeg_nodes";
		//Log.writeln(sqlstr1, level);
		ResultSet rs = null;
		PreparedStatement stmt1;
		try {
			stmt1 = conn.prepareStatement(sqlstr1);
			rs = stmt1.executeQuery();
			while (rs.next()) {
				int nodeid = rs.getInt("nodeid");
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

	public void getCjeg_BetweenPairs(int source, int destination) {
		crjcmap[source][destination] = new TreeMap<Integer, CrJourneyClusterNoEdge>();
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
				if (crjcmap[source][destination].get(stime) != null) {
					crjcmap[source][destination].put(stime+1, crjc);
				} else {
					crjcmap[source][destination].put(stime, crjc);
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
	}
	
	public void insertToMysql(Vector<VelocityPair> velocity_set){
		PreparedStatement stmt2=null;
		String sqlstr2 = "INSERT INTO `cjeg_average_velocity` (`time`,`ave_velocity`) VALUES (?,?)";  
		try {
			stmt2 = conn.prepareStatement(sqlstr2);
			int vecsize = velocity_set.size();
			Log.writeln("size of vector:"+velocity_set.size(), level);
			int hasinserted=0;
			while(hasinserted<vecsize){
				VelocityPair vp = (VelocityPair)velocity_set.get(hasinserted);
				stmt2.setInt(1,vp.time);
				stmt2.setDouble(2,vp.velocity);
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
		int time=0;
		int interval = 1;
		int maxtime = 10140;
		int activenodes_number=62;
		getConnection();
		Vector<Integer> activenodes = getAllActiveNodes();
		for (int i = 0; i < activenodes.size(); i++) {
			for(int j=i+1;j<activenodes.size();j++){
				int source=activenodes.get(i);
				int destination=activenodes.get(j);
				getCjeg_BetweenPairs(source, destination);
				source=activenodes.get(j);
				destination=activenodes.get(i);
				getCjeg_BetweenPairs(source, destination);
			}
		}
		
		Vector<VelocityPair> velocity_set=new Vector<VelocityPair>();
		while (time < maxtime) {
			    double velocity=0;
				for (int i = 0; i < activenodes.size(); i++) {
					for(int j=i+1;j<activenodes.size();j++){
							int source=activenodes.get(i);
							int destination=activenodes.get(j);
							velocity=velocity+getVelocity(source,destination,time);
							source=activenodes.get(j);
							destination=activenodes.get(i);
							velocity=velocity+getVelocity(source,destination,time);
					}
				}
				velocity=velocity/(activenodes_number*(activenodes_number-1));
				Log.writeln("time is:"+time+" velocity is:"+velocity,level);
				VelocityPair vp=new VelocityPair(time,velocity);
				velocity_set.add(vp);
				time=time+interval;
		}
		insertToMysql(velocity_set);
		close();
	}
	
	public double getVelocity(int source,int destination,int t){
		TreeMap<Integer, CrJourneyClusterNoEdge> onecrjcmap=crjcmap[source][destination];
		Integer stime1 = onecrjcmap.floorKey(t);
		Integer stime2 = onecrjcmap.ceilingKey(t);
		double velocity=0;
		int rdelay=Integer.MAX_VALUE;
		if (stime1 == null && stime2 == null) {
			// do nothing
			//Log.writeln("source:"+source+" destination:"+destination+" do nothing",level);
		} else if ((stime1 == null) && (stime2 != null)) {
			rdelay = (stime2 - t) + onecrjcmap.get(stime2).delay;
		} else if ((stime1 != null) && (stime2 == null)) {
			CrJourneyClusterNoEdge crjc1 = onecrjcmap.get(stime1);
			Integer etime1 = crjc1.etime;
			if (etime1 >= t)
				rdelay = crjc1.delay;
		} else {
			CrJourneyClusterNoEdge crjc1 = onecrjcmap.get(stime1);
			int etime1 = crjc1.etime;
			CrJourneyClusterNoEdge crjc2 = onecrjcmap.get(stime2);
			if (etime1 >= t) {
				rdelay = crjc1.delay;
			} else {
				rdelay = (stime2 - t) + crjc2.delay;
			}
		}
		//Log.writeln("rdelay is:"+rdelay,level);
		velocity=1/(double)rdelay;
		//Log.writeln("velocity is:"+velocity,level);
		return velocity;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		// TODO Auto-generated method stub
		Rollernet_AverageDelay_Evolving ave = new Rollernet_AverageDelay_Evolving();
		ave.exec();
	}
}
