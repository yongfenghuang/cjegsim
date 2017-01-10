package tool.reality;

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
 * @author yfhuang created at 2014-8-26 average velocity
 *
 */
public class Reality_Velocity {

		int nodenumber = 97;
		
		// lastetime lastetime between one node pair it equals to o(s,d) in the end
		long[][] lastetime = new long[nodenumber + 1][nodenumber + 1];
		// efficiency betweennodes
		double[][] velocity = new double[nodenumber + 1][nodenumber + 1];
		double[][] nvelocity = new double[nodenumber + 1][nodenumber + 1];
		
		double T_LENGTH=1296000;
		long maxtime=1296000;
		int loglevel = 1000;
		static String database = "reality";
		DecimalFormat df = new DecimalFormat("#.########");
		Connection conn;

		/**
		 * @param args
		 */
		public static void main(String[] args) {
			String outfile=database+"/velocity.txt";
			boolean debug=false;
			try {
				FileOutputStream fos = new FileOutputStream(
						outfile);
				BufferedOutputStream bos = new BufferedOutputStream(fos, 1024);
				PrintStream ps = new PrintStream(bos, true);
				if (debug) System.setOut(ps);
				
				Reality_Velocity calculator = new Reality_Velocity();
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
						velocity[i][j]= velocity[i][j] /T_LENGTH ;
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
		
		public Reality_Velocity() {
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
			String sqlstr1 = "SELECT ctype,stime,etime,delay,source,destination from cjeg_crjourneyegmap_14400 limit ?,?";
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
						if (ctype == 1) {//连续行程,分两段
							long x1 = lastetime[source][destination];
							long x2 = stime;
							long L = x2 - x1 + delay;
							velocity[source][destination] = velocity[source][destination]
									+ Integral(L,x2-x1);
							double tmp=(double)(etime-stime)/(double)delay;
							velocity[source][destination] = velocity[source][destination]+tmp;
						} else {//间接行程，分一段
							long x1 = lastetime[source][destination];
							long x2 = stime;
							long L = x2 - x1 + delay;
							velocity[source][destination] = velocity[source][destination]
									+ Integral(L,x2-x1);
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
		 * @param interval
		 * 		=x2-x1
		 *            
		 * @param L
		 *            =x2-x1+cluster.delay
		 *            
		 * @return integral(1/(L-x)) on （x is the distance of current time and x1）
		 *         time interval [0 interval] = ∫(1/L-x) on [0 interval] = ln(L-0/L-interval)  
		 *         which is (1/（L-x)) the definite integration over [cluster.lastetime,cluster.stime]
		 *      
		 */
		
		public double Integral(long L, long interval) {
			double result=Math.log((double) (L - 0) / (double) (L - interval));
			if (result<0) Log.writeln("L:"+L,loglevel);
			return result;
		}
		
		public void insertToMysql(){
			PreparedStatement stmt2=null;
			String sqlstr2 = "INSERT INTO `cjeg_velocity_14400` (`source`,`destination`,`velocity`,`nvelocity`) VALUES (?,?,?,?)";  
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
			for (int i = 1; i <= nodenumber; i++)
				for (int j = 1; j <= nodenumber; j++) {
					if (lastetime[i][j]>0) Log.writeln("velocity between pairs of node "+i+" node "+j+" is:"+df.format(velocity[i][j]),loglevel);
			}
		}

		public void close() throws SQLException {
			if (conn != null)
				conn.close();
		}

	}

