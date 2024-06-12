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
import Beans.Document;
import Beans.Folder;
import DAO.DocumentDAO;
import DAO.FolderDAO;


@WebServlet("/MoveToFolder")
public class MoveToFolder extends HttpServlet {
	private static final long serialVersionUID = 1L;
    private Connection connection;
    
    public MoveToFolder() {
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
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		HttpSession session = request.getSession();
		
		if(session.isNew() || session.getAttribute("username")==null) {
			response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
			return;
		}
		int docID = 0;
		int folderID = 0;
		String username = (String) session.getAttribute("username");
		try {
			docID = Integer.parseInt(request.getParameter("docID"));
			folderID = Integer.parseInt(request.getParameter("folderID"));
		}catch(NumberFormatException e) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().println("Parameters must be integers");
			return;
		}
		
		
		FolderDAO folderDAO = new FolderDAO(connection);
		Folder folder =  null;
		
		try {
			folder = folderDAO.getFolder(folderID, (String)session.getAttribute("username"));
		}catch(SQLException e) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			response.getWriter().println("Internal error.Please try later...");
			return;
			}
		
		if(folder==null) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().println("Folder doesn't exist");
			return;
		}
		DocumentDAO docDAO = new DocumentDAO(connection);
		Document doc = null;
		try {
			doc = docDAO.getDocumentInfo(docID,username);
		}catch(SQLException e) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			response.getWriter().println("Internal error.Please try later...");
			return;
		}
		if(doc==null) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().println("Document doesn't exist");
			return;
		}
		
		
		boolean moved = false;
		try {
			moved = docDAO.moveDOC(docID,folderID,username);
		}catch(SQLException e) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			response.getWriter().println("Internal error.Please try later...");
			return;
		}
		if(moved==true) { 
			response.setStatus(HttpServletResponse.SC_OK);
			return;
	
		}
		else {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().println("There is already a document with that name");
			return;
		}
		
		
		
	}
	public void destroy() {
		try {
			connection.close();
		}catch(SQLException e) {
			e.printStackTrace();
		}
	}

	
}
