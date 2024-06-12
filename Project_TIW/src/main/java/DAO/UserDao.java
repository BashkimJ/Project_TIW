package DAO;
import java.sql.*;

import Beans.User;

public class UserDao {
	private Connection connection;
	
	public UserDao(Connection conn) {
		this.connection = conn;
	}
	
	public Boolean checkDuplication(String username)  throws SQLException{
		String querry = "Select * From user Where UserName=?";
		PreparedStatement statement = null;
		ResultSet result = null;
		Boolean duplicate;
		try {
			statement = connection.prepareStatement(querry);
			statement.setString(1, username);
			result = statement.executeQuery();
			if(result.next()) {
				duplicate=true;
			}
			else duplicate=false;
		}catch(SQLException e) {
			throw new SQLException(e);
		}
		finally {
			try {
				if(result!=null) {
					result.close();
				}
			}catch(Exception e1) {
				throw new SQLException(e1);
			}
			try {
				if(statement!=null) {
					statement.close();
				}
			}catch(Exception e2) {
				throw new SQLException(e2);
			}
		}
		return duplicate;
		
	}
	public User checkUser(String user,String pass) throws SQLException {
		String querry = "Select * From user Where UserName=? And Password=?";
		ResultSet result = null;
		PreparedStatement pstatement = null;
		User current_user = null;
		try {
			pstatement = connection.prepareStatement(querry);
			pstatement.setString(1, user);
			pstatement.setString(2, pass);
			result = pstatement.executeQuery();
			if(result.next()) {
				String username = result.getString("UserName");
				String password = result.getString("Password");
				String email = result.getString("Email");
				current_user = new User(email,password,username);		
			}
			
		}catch(SQLException e ) {
		        throw new SQLException(e);
		}
		finally{
			try {
				if(result!=null) {
				result.close();}
			}catch(Exception e1) {
				throw new SQLException(e1);
			}
			try {
				if(result!=null) {
				pstatement.close();}
			}catch(Exception e2) {
				throw new SQLException(e2);
			}
		}
		return current_user;
		
	}
    public Boolean addUser(String user,String pass,String email) throws SQLException{
    	String querry = "Insert into user(Email,UserName,Password) Values(?,?,?)";
    	PreparedStatement statement = null;
    	if(checkDuplication(user)) {
    		return false;
    	}
    	
    	try {
    		statement = connection.prepareStatement(querry);
    		statement.setString(1, email);
    		statement.setString(2, user);
    		statement.setString(3,pass);
    		statement.executeUpdate();
    	}catch(SQLException e) {
			throw new SQLException(e);
		}
		finally {
			try {
				if(statement!=null) {
					statement.close();
				}
			}catch(Exception e2) {
				throw new SQLException(e2);
			}
		}
    	return true;
    }
}


