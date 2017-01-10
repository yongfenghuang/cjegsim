package tool.tmg;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.Vector;

import tool.Log;


/**
 * @author yfhuang created at 2014-8-26
 *
 */
public class Tmg_Velocity {

		//原有节点数
		int nodenumber = 120;
		
		// lastetime lastetime between one node pair it equals to o(s,d) in the end
		long[][] lastetime = new long[nodenumber + 1][nodenumber + 1];
		// efficiency betweennodes
		double[][] velocity = new double[nodenumber + 1][nodenumber + 1];
		double[][] nvelocity = new double[nodenumber + 1][nodenumber + 1];
		double T_LENGTH=1000;
		long maxtime=1000;
		int loglevel = 1000;
		static String database = "tmg";
		DecimalFormat df = new DecimalFormat("#.########");
		Connection conn;

		/**
		 * @param args
		 */
		public static void main(String[] args) {
			String outfile="E://c/project/p2pstitcher/cjeg/"+database+"/velocity.txt";
			boolean debug=true;
			try {
				FileOutputStream fos = new FileOutputStream(
						outfile);
				BufferedOutputStream bos = new BufferedOutputStream(fos, 1024);
				PrintStream ps = new PrintStream(bos, true);
				if (debug) System.setOut(ps);
				
				Tmg_Velocity calculator = new Tmg_Velocity();
				calculator.getConnection();
				calculator.calculateVelocity();
				calculator.getVelocity();
				calculator.getNormalVelocity();
				calculator.insertToMysql();
				calculator.printValue();
				calculator.close();
				ps.close();
				bos.close();
				fos.close();
			} catch (Exception e) {
				e.printStackTrace();
			}

		}

		private void getVelocity() throws SQLException {
			for (int i = 1; i <= nodenumber; i++)
				for (int j = 1; j <= nodenumber; j++) {
					if (lastetime[i][j]>0)
						velocity[i][j]= + velocity[i][j] /T_LENGTH ;
			}
		}
		
		private void getNormalVelocity() throws SQLException {
			double minnvelocity=Double.MAX_VALUE;
			for (int i = 1; i <= nodenumber; i++)
				for (int j = 1; j <= nodenumber; j++) {
						nvelocity[i][j] = velocity[i][j];
			}
			
			for (int i = 1; i <= nodenumber; i++)
				for (int j = 1; j <= nodenumber; j++) {
					if (nvelocity[i][j]<minnvelocity && nvelocity[i][j]!=0)
						minnvelocity = nvelocity[i][j];
			}
			
			for (int i = 1; i <= nodenumber; i++)
				for (int j = 1; j <= nodenumber; j++) {
					if (lastetime[i][j]>0)
						nvelocity[i][j]= nvelocity[i][j]/minnvelocity;
			}
		}
		
		public Tmg_Velocity() {
			// initialize
			for (int i = 1; i <= nodenumber; i++)
				for (int j = 1; j <= nodenumber; j++)
					velocity[i][j] = 0;

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

		public void calculateVelocity() {
			// read record from mysql
			String sqlstr1 = "SELECT ctype,stime,etime,delay,source,destination from cjeg_crjourneyegmap_10 limit ?,?";
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
							velocity[source][destination] = velocity[source][destination]
									+ Integral(L, x2-x1);
							double tmp=(double)(etime-stime)/(double)delay;
							velocity[source][destination] = velocity[source][destination]+tmp;
							//if (efficiency[source][destination]>10000) Log.writeln("efficiency:"+efficiency[source][destination], loglevel);
						} else {
							long x1 = lastetime[source][destination];
							long x2 = stime;
							long L = x2 - x1 + delay;
							velocity[source][destination] = velocity[source][destination]
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
		 *         interval [cluster.lastetime,cluster.stime] =ln(l-0/l-x)
		 *         求得是(1/（l-x))在[cluster.lastetime,cluster.stime]上的定积分
		 *      
		 */
		public double Integral(long l, long x) {
			double result=Math.log((double) (l - 0) / (double) (l - x));
			if (result<0) Log.writeln("L:"+l,loglevel);
			return result;
		}
		
		public void insertToMysql(){
			PreparedStatement stmt2=null;
			String sqlstr2 = "INSERT INTO `cjeg_velocity_10` (`source`,`destination`,`velocity`,`nvelocity`) VALUES (?,?,?,?)";  
			try {
				stmt2 = conn.prepareStatement(sqlstr2);
				for (int i = 1; i <= nodenumber; i++)
					for (int j = 1; j <= nodenumber; j++) {
						if (velocity[i][j]>0){
						stmt2.setInt(1, i);
						stmt2.setInt(2, j);
						stmt2.setDouble(3, velocity[i][j]);
						stmt2.setDouble(4, nvelocity[i][j]);
						stmt2.executeUpdate();
					}
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
		
		private void printValue() {
			for (int i = 81; i <= nodenumber-10; i++)
				for (int j = 81; j <= nodenumber-10; j++) {
					if (lastetime[i][j]>0) Log.writeln("velocity between pairs of node "+i+" node "+j+" is:"+df.format(velocity[i][j]),loglevel);
			}
		}

		public void close() throws SQLException {
			if (conn != null)
				conn.close();
		}

	}

