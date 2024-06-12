package DAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;



import Beans.Folder;

public class FolderDAO {
	private Connection connection;
	
	public FolderDAO(Connection conn) {
		connection = conn;
	}
	
	public List<Folder> getRootFolders(String username) throws SQLException{
		List<Folder> folders = new ArrayList<Folder>();
		Folder addfolder = null;
		//Initially i need to find all the route folders......
		String querry = "Select * From folder Where Author=? And FolderID Not In (Select SubFolder from foldersfolder)";
		PreparedStatement pstatement = null;
		ResultSet result = null;
		try {
			pstatement = connection.prepareStatement(querry);
			pstatement.setString(1, username);
			result  = pstatement.executeQuery();
			while(result.next()) {
				addfolder = new Folder(result.getInt("FolderID"),result.getString("Name"), result.getString("CreationDate"), result.getString("Author"),true );
				addSubfolder(username,addfolder);
				folders.add(addfolder);
			}
		
		}catch(SQLException e) {
			throw new SQLException(e);
		}
		finally {
			if(pstatement!=null) {
				try {
					pstatement.close();
				}catch(Exception e1) {
					throw new SQLException(e1);
				}
			}
			if(result!=null) {
				try {
					result.close();
				}catch(Exception e2) {
					throw new SQLException(e2);
				}
			}
		}
		return folders;
	}
	private void addSubfolder(String username, Folder root) throws SQLException {
		String querry = "Select * From folder Where Author = ? And FolderID In(Select SubFolder from foldersfolder Where Folder=?) ";
		PreparedStatement statement = null;
		ResultSet result = null;
		try {
			statement = connection.prepareStatement(querry);
			statement.setString(1, root.getAuthor());
			statement.setInt(2,root.getID());
			result = statement.executeQuery();
			while(result.next()) {
				Folder addfolder = new Folder(result.getInt("FolderID"),result.getString("Name"), result.getString("CreationDate"), result.getString("Author"),false);
				addSubfolder(username,addfolder);
				root.addSubFolder(addfolder);	
			}
		}catch(SQLException e) {
			throw new SQLException(e);
		}
		finally {
			if(statement!=null) {
				try {
					statement.close();
				}catch(Exception e1) {
					throw new SQLException(e1);
				}
			}
			if(result!=null) {
				try {
					result.close();
				}catch(Exception e2) {
					throw new SQLException(e2);
				}
			}
		}
		
	}
    
    public List<Folder> getAllFolders(String username) throws SQLException{
    	String querry = "Select * From folder Where Author = ?";
    	PreparedStatement statement = null;
    	ResultSet result = null;
    	List<Folder> folders = new ArrayList<Folder>();
    	try {
    		statement = connection.prepareStatement(querry);
    		statement.setString(1,username);
    		result = statement.executeQuery();
    		while(result.next()) {
    			Folder folder = new Folder(result.getInt("FolderID"),result.getString("Name"), result.getString("CreationDate"), result.getString("Author"),false);
    			folders.add(folder);		
    		}
    	}catch(SQLException e) {
			throw new SQLException(e);
		}
		finally {
			if(statement!=null) {
				try {
					statement.close();
				}catch(Exception e1) {
					throw new SQLException(e1);
				}
			}
			if(result!=null) {
				try {
					result.close();
				}catch(Exception e2) {
					throw new SQLException(e2);
				}
			}
		}
		return folders;
    	
    }
	
	public Folder getFolder(int FolderID, String Username) throws SQLException {
		String querry = "Select * From folder Where FolderID=? And Author = ?";
		PreparedStatement pstatement = null;
		ResultSet  result = null;
		Folder folder = null;
		try {
			pstatement  = connection.prepareStatement(querry);
			pstatement.setInt(1,FolderID);
			pstatement.setString(2,Username);
			result = pstatement.executeQuery();
			if(result.next()) {
				folder = new Folder(result.getInt("FolderID"),result.getString("Name"), result.getString("CreationDate"), result.getString("Author"),false);
			}
		}catch(SQLException e) {
			throw new SQLException(e);
		}
		finally {
			if(pstatement!=null) {
				try {
					pstatement.close();
				}catch(Exception e1) {
					throw new SQLException(e1);
				}
			}
			if(result!=null) {
				try {
					result.close();
				}catch(Exception e2) {
					throw new SQLException(e2);
				}
			}
		}
		return folder;
		
	}
	public List<Folder> getSubFolders(String username, int FolderID) throws SQLException{

		String querry = "Select * From folder Where Author = ? And FolderID in(Select SubFolder From foldersfolder Where Folder=?) ";
		PreparedStatement pstatement = null;
		ResultSet result = null;
		List<Folder> subfolders = new ArrayList<Folder>();
		try {
			pstatement = connection.prepareStatement(querry);
			pstatement.setString(1, username);
			pstatement.setInt(2, FolderID);
			result = pstatement.executeQuery();
			while(result.next())
			{
				Folder subfolder = new Folder(result.getInt("FolderID"),result.getString("Name"), result.getString("CreationDate"), result.getString("Author"),false );
				subfolders.add(subfolder);
			}
		}catch(SQLException e) {
				throw new SQLException(e);
			}
			finally {
				if(pstatement!=null) {
					try {
						pstatement.close();
					}catch(Exception e1) {
						throw new SQLException(e1);
					}
				}
				if(result!=null) {
					try {
						result.close();
					}catch(Exception e2) {
						throw new SQLException(e2);
					}
				}
			}
		return subfolders;
	}
	
	public void addFolder(String user, String name, String Date, int rootFolder) throws SQLException {
		String querry = "Insert into folder(FolderID,Name,CreationDate,Author) Values(?,?,?,?)";
		String querry1 ="Insert into foldersfolder(Folder,SubFolder) Values(?,?)";
		String querry2 = "Select MAX(FolderID) from folder";
		int currentID = 0;
		PreparedStatement statement2 = null;
		PreparedStatement statement = null;
		PreparedStatement statement1 = null;
		int result = 0;
		ResultSet Result = null;
		try {
			statement2 = connection.prepareStatement(querry2);
			Result = statement2.executeQuery();
			if(Result.next()) {
				currentID = Result.getInt(1);
			}
				connection.setAutoCommit(false);
				statement = connection.prepareStatement(querry);
				statement.setInt(1,currentID+1);
				statement.setString(2,name);
				statement.setString(3,Date);
				statement.setString(4,user);
				result = statement.executeUpdate();
				if(rootFolder!=-1) {
					statement1 = connection.prepareStatement(querry1);
					statement1.setInt(1, rootFolder);
					statement1.setInt(2, currentID+1);
					result=statement1.executeUpdate();
				}
				connection.commit();
			}catch(SQLException e) {
				connection.rollback();
				throw new SQLException(e);
			}
			finally {
				if(statement!=null) {
					try {
						statement.close();
					}catch(Exception e1) {
						throw new SQLException(e1);
					}
				}
				if(statement1!=null) {
					try {
						statement1.close();
					}catch(Exception e3) {
						throw new SQLException(e3);
					}
				}
				if(statement2!=null) {
					try {
						statement2.close();
					}catch(Exception e4) {
						throw new SQLException(e4);
					}
				}
				if(Result!=null) {
					try {
						Result.close();
					}catch(Exception e2) {
						throw new SQLException(e2);
					}
				}
				connection.setAutoCommit(true);
			}
		}
	
	public void deleteFolder(int folderID,String username) throws SQLException {
		String querry = "Delete From folder Where FolderID = ? And Author = ?";
		PreparedStatement statement = null;
		int result = 0;
		try {
			statement = connection.prepareStatement(querry);
			statement.setInt(1, folderID);
			statement.setString(2, username);
			result = statement.executeUpdate();
		}catch(SQLException e) {
			throw new SQLException(e);
		}
		finally {
			if(statement!=null) {
				try {
					statement.close();
				}catch(SQLException e1) {
					throw new SQLException(e1);
				}
			}
		}
		
		
		
		
	}

	public boolean checkFolderName(int rootFolder, String name,String user) throws SQLException{
		String querry="Select Name from folder Where Name=? And FolderID In(Select SubFolder From foldersfolder Where Folder=?)";
		String querry1="Select Name from folder Where Name=? And Author=? And FolderID Not In(Select SubFolder From foldersfolder)";
		PreparedStatement statement1 = null;
		ResultSet result1 = null;
		boolean isPresent = false;
		PreparedStatement statement = null;
		ResultSet result = null;
		if(rootFolder==-1) {
			try {
				statement1 = connection.prepareStatement(querry1);
				statement1.setString(1,name);
				statement1.setString(2, user);
				result1 = statement1.executeQuery();
				if(result1.next()) {
					isPresent = true;
				}
			}catch(SQLException e) {
				throw new SQLException(e);
			}
			finally {
				if(statement1!=null) {
					try {
						statement1.close();
					}catch(Exception e1) {
						throw new SQLException(e1);
					}
				}
				if(result1!=null) {
					try {
						result1.close();
					}catch(Exception e2) {
						throw new SQLException(e2);
					}
				}
			}
		}
		else {
			try {
			statement = connection.prepareStatement(querry);
			statement.setString(1,name);
			statement.setInt(2, rootFolder);
			result = statement.executeQuery();
			if(result.next()) {
				isPresent = true;
			}
			}catch(SQLException e) {
				throw new SQLException(e);
				}
			finally {
				if(statement!=null) {
					try {
						statement.close();
						}catch(Exception e1) {
							throw new SQLException(e1);
							}
					}
				if(result!=null) {
					try {
						result.close();
						}catch(Exception e2) {
							throw new SQLException(e2);
							}
				}
			}
		}
		return isPresent;
		
	}
}


