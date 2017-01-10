package tool.rollernet;

import java.io.File;
import java.io.FileNotFoundException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.Scanner;
import tool.Log;

/**
 * @author yfhuang created at 2014-3-28
 * 
 */
public class GenerateDeviceLog {

	// config storename which must be the same with storename in cjeg.txt
	String storename;
	Scanner scanner;
	File eventsfile;
	Connection conn;
	// the first time in dataset
	long timeoffset;

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		GenerateDeviceLog gdl = new GenerateDeviceLog();
		gdl.init();
		gdl.getConnection();
		gdl.insertGdlData();
		gdl.closeConnection();
		Log.writeln("insert to sql finished", 1000);
	}

	private void init() {
		storename = "rollernet";
		eventsfile = new File("rollernet/contacts.dat");
		timeoffset = 1156083900;
		try {
			this.scanner = new Scanner(eventsfile);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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

	private void closeConnection() {
		try {
			if (conn != null)
				conn.close();
			if (scanner != null)
				scanner.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void insertGdlData() {

		PreparedStatement insertstmt = null;
		String insertsql = insertSql();
		int eventsdealed=0;
		try {
			insertstmt = conn.prepareStatement(insertsql);
			while (scanner.hasNextLine()) {
				String eventline = scanner.nextLine();
				Scanner linescan = new Scanner(eventline);
				int user1_id;
				int user2_id;
				long starttime;
				long endtime;
				user1_id = linescan.nextInt();
				user2_id = linescan.nextInt();
				starttime = linescan.nextLong();
				endtime = linescan.nextLong();
				starttime = starttime - timeoffset;
				endtime = endtime - timeoffset;
				insertstmt.setInt(1, user1_id);
				insertstmt.setInt(2, user2_id);
				insertstmt.setLong(3, starttime);
				insertstmt.setLong(4, endtime);
				insertstmt.executeUpdate();
				eventsdealed++;
				Log.writeln("dealed "+eventsdealed+" records", 1000);
			}
			insertstmt.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private String insertSql() {
		String insertsql = "insert into cjeg_device_log"
				+ "(user1_id,user2_id,starttime,endtime) values(?,?,?,?)";
		return insertsql;
	}
}
