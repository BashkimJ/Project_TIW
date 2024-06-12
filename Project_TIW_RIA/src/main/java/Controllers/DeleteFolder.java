package Controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import Beans.Folder;
import DAO.FolderDAO;


@WebServlet("/DeleteFolder")
public class DeleteFolder extends HttpServlet {
	private static final long serialVersionUID = 1L;
    private Connection connection;
    public DeleteFolder() {
        super();
       
    }
    
    public void init() {
    	ServletContext servletcontext = getServletContext();
     	  try {
    		  String url = servletcontext.getInitParameter("dbUrl");
    		  String user= servletcontext.getInitParameter("dbUser");
       	  String driver = servletcontext.getInitParameter("dbDriver");
    		  String pass = servletcontext.getInitParameter("dbPassword");
    		  Class.forName(driver);
    		  connection = DriverManager.getConnection(url,user,pass);
    	  }catch (ClassNotFoundException e) {
    		  e.printStackTrace();
    	  }catch(SQLException e) {
    		  e.printStackTrace();
    	  }
    }
    
    

	
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException{
		HttpSession session = request.getSession();
		if(session==null || session.getAttribute("username")==null) {
			response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
			return;
		}
		String username = (String) session.getAttribute("username");
		int folderID = 1;
		try {
			folderID = Integer.parseInt(request.getParameter("folderID"));
		}catch(NumberFormatException e) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().println("The folder id must an integer");
			return;
		}
		FolderDAO folderDao  =new FolderDAO(connection);
		Folder folder = null;
		
		try {
			folder = folderDao.getFolder(folderID, username);
		}catch(SQLException e) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			response.getWriter().println("Internal error. Please try later!");
			return;
		}
		if(folder==null) {
			response.setStatus(HttpServletResponse.SC_NOT_FOUND);
			response.getWriter().println("Folder doesn't exist");
			return;
		}
		
		try {
			folderDao.deleteFolder(folderID, username);
		}catch(SQLException e) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			response.getWriter().println("Internal error. Please try later!");
			return;
		}
		response.setStatus(HttpServletResponse.SC_OK);
		return;
		
	}

	public void destroy() {
		try {
			connection.close();
		}catch(SQLException e ) {
			
		}
	}

	

}
