package tool;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.Date;

import app.AppConfig;

/**
 * @author yfhuang created at 2014-3-27
 * 
 */
public class AverageEncounterDuration {

	String storename;
	private Connection conn;
	String tracename;
	int tau;

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		AverageEncounterDuration evd = new AverageEncounterDuration();
		evd.init();
		evd.getConnection();
		evd.calculation();
		evd.closeConnection();
	}

	private Connection getConnection() {
		if (conn != null) {
			return conn;
		} else {
			String username = "root";
			String password = "";
			try {
				Class.forName("com.mysql.jdbc.Driver");
				conn = java.sql.DriverManager.getConnection(
						"jdbc:mysql://localhost/" + storename
								+ "?useUnicode=true&characterEncoding=UTF8",
						username, password);
			} catch (Exception e) {
				e.printStackTrace();
				conn = null;
			}
		}
		return conn;
	}

	private void calculation() {
		ResultSet rs=null;
		try {
			String calsql = "SELECT * from cjeg_device_log where starttime>'2005-03-01 00:00:00' and endtime<'2005-03-04 00:00:00'";
			PreparedStatement calstat = conn.prepareStatement(calsql);
			rs = calstat.executeQuery();
			long duration_sum = 0, recordnumber = 0;
			SimpleDateFormat dateformat = new SimpleDateFormat(
					"yyyy-MM-dd HH:mm:ss");

			while (rs.next()) {
				String starttime = rs.getString("starttime");
				Date stime = dateformat.parse(starttime);
				String endtime = rs.getString("endtime");
				Date etime = dateformat.parse(endtime);
				duration_sum = duration_sum
						+ (etime.getTime() - stime.getTime()) / 1000;
				recordnumber++;
				
			}

			long duration_average = duration_sum / recordnumber;
			Log.writeln("average duration is:" + duration_average, 1000);
			rs.close();
			calstat.close();
		} catch (Exception e) {
			e.printStackTrace();
		} finally{
		}
	}

	private void closeConnection() {
		try {
			if (conn != null)
				conn.close();
			conn = null;
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void init() {
		AppConfig config=AppConfig.getInstance();
		storename = config.getStorename();
		tracename = config.getTracename();
		tau = config.getTau();
	}

}
