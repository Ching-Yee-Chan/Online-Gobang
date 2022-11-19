
import java.sql.*;

import javax.swing.JOptionPane;


public class DataBase {
	private static DataBase instance;
	private Connection connect;
	
	private DataBase() {

		try {
			Class.forName("com.mysql.jdbc.Driver");
			connect = DriverManager.getConnection(
					"jdbc:mysql://localhost:3306/rergister?useUnicode=true&characterEncoding=utf8",
					"root", "123456");
		} catch (SQLException | ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	public static DataBase getInstance(){
		if(instance==null){
			instance = new DataBase();
		}
		return instance;
	}
	public boolean getInfo(String username, String password, Status status){
		try {
			PreparedStatement command = connect.prepareStatement("select * from user where username= ? and password = ?");
			command.setString(1, username);
			command.setString(2, password);
			command.executeQuery();
			ResultSet result = command.executeQuery();
			if(result==null||!result.next()){
				return false;
			}
			switch(status){
			case HOST:
				Control.getInstance().me = new UserInfo(result.getString("nickname"), result.getInt("level"), true);
				break;
			case CILENT:
				Control.getInstance().opponent = new UserInfo(result.getString("nickname"), result.getInt("level"), false);
				break;
			}
			return true;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}
	public boolean addInfo(String username, String password){
		String nickname = JOptionPane.showInputDialog(null, "«Î ‰»ÎÍ«≥∆", "◊¢≤·", JOptionPane.DEFAULT_OPTION);
		int success = 0;
		try {
		Statement command = connect.createStatement();
			success = command.executeUpdate("insert into user(username, password, nickname, level) values('"+username+"','"+password+"','"+nickname+"','"+1+"')");
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return success==1;
	}
}