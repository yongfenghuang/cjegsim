package tool.rollernet;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import tool.Log;

/**
 * @author yfhuang created at 2014-7-15 
 */

public class Rollernet_NetworkEfficiency {

	int nodenumber = 62;

	// lastetime lastetime between one node pair it equals to o(s,d) in the end
	long[][] lastetime = new long[nodenumber + 1][nodenumber + 1];
	// efficiency betweennodes
	double[][] efficiency = new double[nodenumber + 1][nodenumber + 1];
	double networkefficiency=0;
	double average_networkefficiency=0;
	double T_LENGTH=10140;
	long maxtime=10140;
	int loglevel = 1000;
	static String database = "rollernet";
	DecimalFormat df = new DecimalFormat("#.########");
	Connection conn;

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String outfile="E:/c/project/p2pstitcher/cjeg/"+database+"/networkefficiency.txt";
		boolean debug=false;
		try {
			FileOutputStream fos = new FileOutputStream(
					outfile);
			BufferedOutputStream bos = new BufferedOutputStream(fos, 1024);
			PrintStream ps = new PrintStream(bos, true);
			if (debug) System.setOut(ps);
			
			Rollernet_NetworkEfficiency calculator = new Rollernet_NetworkEfficiency();
			calculator.getConnection();
			calculator.calculateEfficiency();
			calculator.getNetworkEfficiency();
			calculator.printValue();
			calculator.close();
			ps.close();
			bos.close();
			fos.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public Rollernet_NetworkEfficiency() {
		// initialize
		for (int i = 1; i <= nodenumber; i++)
			for (int j = 1; j <= nodenumber; j++)
				efficiency[i][j] = 0;

		for (int i = 1; i <= nodenumber; i++)
			for (int j = 1; j <= nodenumber; j++)
				lastetime[i][j] = 0;

	}

	public void getConnection() throws ClassNotFoundException, SQLException {
		String username = "root";
		String password = "";
		Class.forName("com.mysql.jdbc.Driver");
		conn = java.sql.DriverManager.getConnection("jdbc:mysql://localhost/"
				+ database + "?useUnicode=true&characterEncoding=UTF8",
				username, password);

	}

	public void calculateEfficiency() {
		// read record from mysql
		String sqlstr1 = "SELECT ctype,stime,etime,delay,source,destination from cjeg_crjourneyegmap_1 limit ?,?";
		int pagerecords = 50000;
		int recordnumber = 0;
		ResultSet rs = null;
		PreparedStatement stmt1;
		try {
			stmt1 = conn.prepareStatement(sqlstr1);
			while (true) {
				stmt1.setInt(1, recordnumber);
				stmt1.setInt(2, pagerecords);
				rs = stmt1.executeQuery();
				if (!rs.next())
					break;
				boolean hasrecords = true;
				while (hasrecords) {
					int ctype = rs.getInt("ctype");
					long stime = rs.getLong("stime");
					long etime = rs.getLong("etime");
					int delay = rs.getInt("delay");
					int source = rs.getInt("source");
					int destination = rs.getInt("destination");
					
					if (etime>maxtime) etime=maxtime;
					
					if (source>nodenumber || destination>nodenumber) {
						recordnumber++;
						hasrecords = rs.next();
						continue;
					}
					if (ctype == 1) {
						long x1 = lastetime[source][destination];
						long x2 = stime;
						long L = x2 - x1 + delay;
						efficiency[source][destination] = efficiency[source][destination]
								+ Integral(L, x2-x1);
						double tmp=(double)(etime-stime)/(double)delay;
						efficiency[source][destination] = efficiency[source][destination]+tmp;
						//if (efficiency[source][destination]>10000) Log.writeln("efficiency:"+efficiency[source][destination], loglevel);
					} else {
						long x1 = lastetime[source][destination];
						long x2 = stime;
						long L = x2 - x1 + delay;
						efficiency[source][destination] = efficiency[source][destination]
								+ Integral(L, x2-x1);
					}
					lastetime[source][destination] = etime;

					// Log.writeln(journey, 1000);
					recordnumber++;
					hasrecords = rs.next();
				}
				//Log.writeln("finished " + recordnumber + " records", loglevel);
				if (rs != null)
					rs.close();
			}
			if (stmt1 != null)
				stmt1.close();
			Log.writeln("recordnumber:" + recordnumber, loglevel);
			Log.writeln("betweenness calculate finished", loglevel);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {

		}
	}

	/**
	 * @param x1
	 *            =cluster.lastetime
	 * @param x2
	 *            =cluster.stime
	 * @param l
	 *            =x2-x1+cluster.delay
	 *            
	 * @return integral(1/(cluster.delay+cluster.stime-cluster.lastetime-x)) on
	 *         interval [cluster.lastetime,cluster.stime] =ln(L-0/L-x)
	 *         求得是(1/（L-x))在[cluster.lastetime,cluster.stime]上的定积分
	 *      
	 */
	public double Integral(long l, long x) {
		double result=Math.log((double) (l - 0) / (double) (l - x));
		if (result<0) Log.writeln("L:"+l,loglevel);
		return result;
	}

	private void getNetworkEfficiency() throws SQLException {
		for (int i = 1; i <= nodenumber; i++)
			for (int j = 1; j <= nodenumber; j++) {
				if (lastetime[i][j]>0)
				networkefficiency = networkefficiency
						+ (double) efficiency[i][j] /T_LENGTH ;
			}
		
		int nodepairs=nodenumber*(nodenumber-1);
		average_networkefficiency = networkefficiency/nodepairs;
	}

	private void printValue() {
		for (int i = 1; i <= nodenumber; i++)
			for (int j = 1; j <= nodenumber; j++) {
				if (lastetime[i][j]>0) Log.writeln("efficiency between pairs of node "+i+" node "+j+" is:"+df.format(efficiency[i][j]/T_LENGTH),loglevel);
		}
		Log.writeln("efficiency of network is:"+df.format(networkefficiency),loglevel);
		Log.writeln("average_efficiency of network is:"+df.format(average_networkefficiency),loglevel);
	}

	public void close() throws SQLException {
		if (conn != null)
			conn.close();
	}

}
