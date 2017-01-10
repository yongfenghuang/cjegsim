//generate table encounter in database
package tool.seu;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class GenerateCjegEncounter {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		try {
			String username = "root";
			String password = "";
			int pagerecords = 10000;
			int recordnumber = 0;
			String selsql = "SELECT log.*,(endtime-starttime) as duration FROM `cjeg_device_log` as log limit ?,?";
			String insertsql = "insert into cjeg_encounter(`eventtime`,`eventtype`,`duration`,`user1_id`,`user2_id`,`event_id`) values(?,?,?,?,?,?)";
			long eventtime;
			int eventtype, duration, user1_id, user2_id,event_id;
			Class.forName("com.mysql.jdbc.Driver");
			java.sql.Connection conn = java.sql.DriverManager
					.getConnection(
							"jdbc:mysql://localhost/seu?useUnicode=true&characterEncoding=UTF8",
							username, password);
			ResultSet rs = null;
			PreparedStatement stmt1 = conn.prepareStatement(selsql);
			PreparedStatement stmt2 = conn.prepareStatement(insertsql);
			while (true) {
				stmt1.setInt(1, recordnumber);
				stmt1.setInt(2, pagerecords);
				rs = stmt1.executeQuery();
				if (!rs.next()) break;
				boolean hasrecords=true;
				while (hasrecords) {
					recordnumber++;
					user1_id = Integer.parseInt(rs.getString("user1_id").substring(1));
					user2_id = Integer.parseInt(rs.getString("user2_id").substring(1));
					event_id = rs.getInt("oid");
					duration = 0;
					eventtype = 1;
					eventtime = rs.getLong("starttime");
					stmt2.setLong(1, eventtime);
					stmt2.setInt(2, eventtype);
					stmt2.setInt(3, duration);
					stmt2.setInt(4, user1_id);
					stmt2.setInt(5, user2_id);
					stmt2.setInt(6, event_id);
					
					stmt2.executeUpdate();
					eventtime = rs.getLong("endtime");
					duration = rs.getInt("duration");
					eventtype = 2;
					stmt2.setLong(1, eventtime);
					stmt2.setInt(2, eventtype);
					stmt2.setInt(3, duration);
					stmt2.setInt(4, user1_id);
					stmt2.setInt(5, user2_id);
					stmt2.setInt(6, event_id);
					stmt2.executeUpdate();
					hasrecords=rs.next();
				}
			}
			if (rs!=null) rs.close();
			if (stmt1!=null) stmt1.close();
			if (stmt2!=null) stmt2.close();
			if (conn!=null) conn.close();
			System.out.println("generate encounter OK");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
