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
 * @author yfhuang created at 2014-9-2
 * 
 *
 */

public class Tmg_Velocity_Difference {
		int loglevel = 1000;
		static String database = "tmg";
		DecimalFormat df = new DecimalFormat("#.########");
		Connection conn;

		/**
		 * @param args
		 */
		public static void main(String[] args) {
			String outfile="tmg/velocity_diff.txt";
			boolean debug=false;
			try {
				FileOutputStream fos = new FileOutputStream(outfile);
				BufferedOutputStream bos = new BufferedOutputStream(fos, 1024);
				PrintStream ps = new PrintStream(bos, true);
				if (debug) System.setOut(ps);
				Tmg_Velocity_Difference calculator = new Tmg_Velocity_Difference();
				calculator.getConnection();
				Vector<Integer> activenodes=calculator.getAllNodes();
				calculator.calculateDifference(activenodes);
				calculator.close();
				ps.close();
				bos.close();
				fos.close();
			} catch (Exception e) {
				e.printStackTrace();
			}

		}
		
		public Vector<Integer> getAllNodes() {
			Vector<Integer> activenodes = new Vector<Integer>();
			// read record from mysql
			String sqlstr1 = "SELECT nodeid from cjeg_nodes";
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

		public void getConnection() throws ClassNotFoundException, SQLException {
			String username = "root";
			String password = "";
			Class.forName("com.mysql.jdbc.Driver");
			conn = java.sql.DriverManager.getConnection("jdbc:mysql://localhost/"
					+ database + "?useUnicode=true&characterEncoding=UTF8",
					username, password);

		}

		public double calculateDifference(Vector<Integer> activenodes) {
			int diffnumber=0;
			double total_diff=0;
			double average_diff=0;
			for (int i=0;i<activenodes.size();i++)
				for(int j=i+1;j<activenodes.size();j++){
					int source=activenodes.get(i);
					int destination=activenodes.get(j);
					double va=getVelocity(source,destination);
				    double vb=getVelocity(destination,source);
				    //Log.writeln("va:"+va+" vb:"+vb, loglevel);
				    if (va!=0 || vb!=0){
				    	double diff=Math.abs(va-vb)/(Math.abs(va)+Math.abs(vb));
				    	Log.writeln("diff between "+source+" and "+destination+" is:"+diff, loglevel);
				    	total_diff=total_diff+diff;
				    	diffnumber++;
				    }
				}
			Log.writeln("total diff is:"+total_diff, loglevel);
			average_diff=total_diff/(double)diffnumber;
			Log.writeln("diff number is:"+diffnumber, loglevel);
			Log.writeln("average diff is:"+average_diff, loglevel);
			return average_diff;
		}
		
		public double getVelocity(int source,int destination) {
			double velocity=0;
			String sqlstr1 = "SELECT velocity from cjeg_velocity WHERE source ="
					+ source
					+ " AND destination ="
					+ destination;
			ResultSet rs = null;
			PreparedStatement stmt1;
			try {
				stmt1 = conn.prepareStatement(sqlstr1);
				rs = stmt1.executeQuery();
				
				if (rs.next())
					velocity = rs.getDouble("velocity");
				if (rs != null)
					rs.close();
				if (stmt1 != null)
					stmt1.close();
			} catch (Exception e) {
				e.printStackTrace();
			} finally {

			}
			return velocity;
		}

		public void close() throws SQLException {
			if (conn != null)
				conn.close();
		}

	}

