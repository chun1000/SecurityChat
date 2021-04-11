import java.sql.*;

public class DBManager {
	public static String DB_URL = "jdbc:mysql://localhost:3306/crypto?serverTimezone=UTC&useSSL=false"; // db 이름
	public static String DB_USER = "root"; // db userid, pwd 설정
	public static String DB_PASSWORD = "1234";

	public static int get_table_from_DB(String id, String password) {

		Connection conn = null;
		Statement stmt = null;
		ResultSet rs = null;
		
		try {
			// 드라이버 로딩
			Class.forName("com.mysql.cj.jdbc.Driver");
			
			conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);// db연결설정
			
			stmt = conn.createStatement(); // Statement를 가져옴
			String query="SELECT user.index from user where user_id='"+id+"' and user_pw='"+password+"'";
			rs = stmt.executeQuery(query.toString()); // SQL문 실행

			while (rs.next()) { // query 출력 및 실행
				String result=rs.getString(1); //첫번째 값이 넘어옴
				System.out.println("result: "+result);
				return Integer.parseInt(result);
			}

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				rs.close();
				stmt.close();
				conn.close();
			} catch (SQLException e) {
				
			}
		}
		return -1;
	}

	public static String get_salt_from_DB(String id) {
		
		Connection conn = null;
		Statement stmt = null;
		ResultSet rs = null;
		
		try {
			// 드라이버 로딩
			Class.forName("com.mysql.cj.jdbc.Driver");
			
			conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);// db연결설정
			
			stmt = conn.createStatement(); // Statement를 가져옴
			String query="SELECT user.user_salt from user where user_id='"+id+"'";
			rs = stmt.executeQuery(query.toString()); // SQL문 실행

			while (rs.next()) { // query 출력 및 실행
				String result=rs.getString(1); //첫번째 값이 넘어옴
				return result;
			}

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				rs.close();
				stmt.close();
				conn.close();
			} catch (SQLException e) {
				
			}
		}
		return null;
	}
	
}
