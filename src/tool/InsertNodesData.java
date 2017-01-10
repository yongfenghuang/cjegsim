package tool;

import java.sql.PreparedStatement;

public class InsertNodesData {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
				try {
					String username = "root";
					String password = "";
					String insertsql = "INSERT INTO cjeg_nodes (nodeid, nodestr) VALUES(?,?)";
					Class.forName("com.mysql.jdbc.Driver");
					java.sql.Connection conn = java.sql.DriverManager
							.getConnection(
									"jdbc:mysql://localhost/rollernet?useUnicode=true&characterEncoding=UTF8",
									username, password);
					PreparedStatement stmt = conn.prepareStatement(insertsql);
					int i=1;
					while (i<=62) {
							stmt.setInt(1,i);
							stmt.setString(2, String.valueOf(i));
							stmt.executeUpdate();
							i=i+1;
					}
					
					if (stmt!=null) stmt.close();
					if (conn!=null) conn.close();
					System.out.println("generate encounter OK");
					
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

	}

}
