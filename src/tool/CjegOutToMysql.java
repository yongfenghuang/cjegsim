package tool;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;
import app.AppConfig;
import output.CjegTrace;
import core.CrJourneyCluster;
import core.Journey;

/**
 * @author yfhuang created at 2014-3-21 find all edges which are formed by
 *         nodeids in table nodes, search cjegtrace of these edges and put it
 *         into mysql
 */

public class CjegOutToMysql {
	// config storename which must be the same with storename in cjeg.txt
	String storename;
	private Connection conn;
	String tracename;
	int tau;
	String username = "root";
	String password = "";
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		CjegOutToMysql cjegout = new CjegOutToMysql();
		cjegout.init();
		cjegout.getConnection();
		cjegout.newTable();
		cjegout.insertCjegToDb();
		cjegout.closeConnection();
		Log.writeln("out to sql finished", 2000);
	}

	private void init() {
		AppConfig config=AppConfig.getInstance();
		storename = config.getStorename();
		tracename = config.getTracename();
		tau = config.getTau();
	}

	private Connection getConnection() {
		if (conn != null) {
			return conn;
		} else {
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

	private void closeConnection() {
		try {
			if (conn != null)
				conn.close();
			conn = null;
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void newTable() {
		try {
			String createsql = createTableSql();
			Statement createstmt = null;
			createstmt = conn.createStatement();
			createstmt.executeUpdate(createsql);
			createstmt.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void insertCjegToDb() {
		AppConfig config=AppConfig.getInstance();
		CrJourneyCluster crjc;
		String insertsql = crjcToSql();
		PreparedStatement insertstmt = null;
		try {
			String tracedir=AppUtil.getTracedir(config.getTracename(),config.getTau());
			CjegTrace cjegtrace = new CjegTrace(config.getStorename(),
					tracedir, -1);
			cjegtrace.reader_open();
			insertstmt = conn.prepareStatement(insertsql);
			while ((crjc = cjegtrace.read()) != null) {
				insertstmt.setInt(1, crjc.getEdge().getFromaddr());
				insertstmt.setInt(2, crjc.getEdge().getToaddr());
				insertstmt.setInt(3, crjc.getCtype().getValue());
				insertstmt.setLong(4, crjc.getStime());
				insertstmt.setLong(5, crjc.getEtime());
				Journey<?> journey = crjc.getRepresentative_crjourney();
				insertstmt.setInt(6, journey.getHopCount());
				insertstmt.setLong(7, journey.getDelay());
				insertstmt.setString(8, journey.toDbString());
				insertstmt.executeUpdate();
				// errormsg = "source:" + edge.getFromaddr() + " destination:"
				// + edge.getToaddr() + " stime:" + crjc.getStime()
				// + " etime:" + crjc.getEtime();
			}

			cjegtrace.reader_close();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (insertstmt != null)
					insertstmt.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private String crjcToSql() {
		String insertsql = "insert into cjeg_crjourneyegmap_"+tau+"(source,destination,ctype,stime,etime,hopcount,delay,journey) values(?,?,?,?,?,?,?,?)";
		return insertsql;
	}

	private String createTableSql() {
		String createsql = "CREATE TABLE IF NOT EXISTS `cjeg_crjourneyegmap_"
				//+ tau + "` ("
				+ tau + "` ("+ "`crjourneyid` int(11) NOT NULL AUTO_INCREMENT,"
				+ "`ctype` tinyint(1) NOT NULL,"
				+ "`stime` bigint(20) NOT NULL,"
				+ "`etime` bigint(20) NOT NULL,"
				+ "`hopcount` int(4) NOT NULL,"
				+ "`delay` bigint(20) NOT NULL,"
				+ "`journey` varchar(1024) CHARACTER SET utf8 NOT NULL,"
				+ "`source` int(11) NOT NULL,"
				+ "`destination` int(11) NOT NULL,"
				+ "PRIMARY KEY (`crjourneyid`)"
				//+ "PRIMARY KEY (`stime`,`etime`,`source`,`destination`)"
				+ ") ENGINE=MyISAM DEFAULT CHARSET=latin1 AUTO_INCREMENT=1";
		return createsql;
	}
}
