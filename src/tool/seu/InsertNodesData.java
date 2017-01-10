package tool.seu;

import java.sql.PreparedStatement;
import java.util.HashSet;
import java.util.Random;

public class InsertNodesData {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String storename = "seu";
		int minid = 1;
		int maxid = 17560;
		int randomnumber = 1000;
		int[] random_array = randomArray(minid, maxid, randomnumber);
		for (int i = 0; i < random_array.length; i++) {
			System.out.println(random_array[i]);
		}

		// TODO Auto-generated method stub
		try {

			String username = "root";
			String password = "";
			String insertsql = "INSERT INTO cjeg_nodes (node_id, nodestr) VALUES(?,?)";
			Class.forName("com.mysql.jdbc.Driver");
			java.sql.Connection conn = java.sql.DriverManager.getConnection(
					"jdbc:mysql://localhost/" + storename
							+ "?useUnicode=true&characterEncoding=UTF8",
					username, password);
			PreparedStatement stmt = conn.prepareStatement(insertsql);
			int i = 0;
			while (i < random_array.length) {
				stmt.setInt(1, random_array[i]);
				stmt.setString(2, String.valueOf(random_array[i]));
				stmt.executeUpdate();
				i = i + 1;
			}

			if (stmt != null)
				stmt.close();
			if (conn != null)
				conn.close();
			System.out.println("generate encounter OK");

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	
	public static int[] randomArray(int min, int max, int n) {
		int len = max - min + 1;
		if (max < min || n > len) {
			return null;
		}
		
		int[] source = new int[len];
		for (int i = min; i < min + len; i++) {
			source[i - min] = i;
		}

		int[] result = new int[n];
		Random rd = new Random();
		int index = 0;
		for (int i = 0; i < result.length; i++) {
			
			index = Math.abs(rd.nextInt() % len--);
			
			result[i] = source[index];
			
			source[index] = source[len];
		}
		return result;
	}

}
