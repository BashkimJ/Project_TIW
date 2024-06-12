package DAO;

import java.sql.*;
import java.util.*;

import Beans.Document;

public class DocumentDAO {
	private Connection connection;

	public DocumentDAO(Connection conn) {
		connection = conn;
	}
	
	public List<Document> getDocuments(int FolderID,String Author) throws SQLException{
		String querry = "Select* From document Where Author = ? And Folder=?";
		PreparedStatement pstatement = null;
		ResultSet result = null;
		List<Document> documents = new ArrayList<Document>();
		try {
			pstatement = connection.prepareStatement(querry);
			pstatement.setString(1, Author);
			pstatement.setInt(2, FolderID);
			result = pstatement.executeQuery();
			while(result.next()) {
				Document addDocument = new Document(result.getInt("ID"),result.getString("Author"),result.getString("Name"),result.getString("Type"),result.getString("CreationDate"),result.getString("Summary"),result.getInt("Folder"));
				documents.add(addDocument);
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
		return documents;
	}
    
	public Document getDocumentInfo(int DocID,String Username) throws SQLException {
		String querry = "Select * From document Where ID=? And Author = ?";
		PreparedStatement statement = null;
		ResultSet result = null;
		Document doc = null;
		
		try {
			statement = connection.prepareStatement(querry);
			statement.setInt(1, DocID);
			statement.setString(2, Username);
			result = statement.executeQuery();
			if(result.next()) {
				doc =new Document(result.getInt("ID"),result.getString("Author"),result.getString("Name"),result.getString("Type"),result.getString("CreationDate"),result.getString("Summary"),result.getInt("Folder"));
	
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
		return doc;
		
	}
    public Boolean checkDocName(String name,int rootFolder) throws SQLException {
    	String querry = "Select Name From document Where Name=? And Folder=? ";
    	PreparedStatement statement = null;
    	ResultSet result = null;
    	boolean isPresent = false;
    	try {
    		statement = connection.prepareStatement(querry);
    		statement.setString(1, name);
    		statement.setInt(2,rootFolder);
    		result = statement.executeQuery();
    		if(result.next()) {
    			isPresent = true;
    		}
    	}catch(SQLException e) {
    		throw new SQLException(e);
    	}finally {
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
    				throw new SQLException (e2);
    			}
    		}
    	}
    	return isPresent;
    	
    }
    public void addDoc(Document doc) throws SQLException {
    	String querry = "Insert Into document (Author,Name,Type,CreationDate,Summary,Folder) Values(?,?,?,?,?,?)";
    	PreparedStatement statement = null;
    	int result = 0;
    	try {
    		statement = connection.prepareStatement(querry);
    		statement.setString(1, doc.getAuthor());
    		statement.setString(2, doc.getName());
    		statement.setString(3, doc.getType());
    		statement.setString(4, doc.getCreationDate());
    		statement.setString(5, doc.Summary());
    		statement.setInt(6, doc.getFolder());
    		result = statement.executeUpdate();
    	}catch(SQLException e) {
    		throw new SQLException(e);
    	}finally {
    		if(statement!=null) {
    			try {
    				statement.close();
    			}catch(Exception e1) {
    				throw new SQLException(e1);
    			}
    		}
    	}
    }
    public boolean moveDOC(int docID, int folderID,String Username) throws SQLException{
	   String querry = "Update document Set Folder=? Where ID = ?";
	   PreparedStatement statement  = null;
	   Document doc = getDocumentInfo(docID,Username);
	   boolean updated = false;
	   boolean isPresent = checkDocName(doc.getName(),folderID);
	   if(isPresent==false) {
		   updated = true;
	   }
	   if(updated==true) {
		   try {
			   statement = connection.prepareStatement(querry);
		       statement.setInt(1, folderID);
		       statement.setInt(2, docID);
		       statement.executeUpdate();
	       }catch(SQLException e) {
		       throw new SQLException(e);
	       }finally{
	    		if(statement!=null) {
	    			try {
	    				statement.close();
	    			}catch(Exception e1) {
	    				throw new SQLException(e1);
	    			}
	    		}
	    	   
	       }
	   }
	   return updated;
   }
}
