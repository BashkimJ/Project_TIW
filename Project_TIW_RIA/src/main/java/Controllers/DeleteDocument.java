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
import DAO.DocumentDAO;


@WebServlet("/DeleteDocument")
public class DeleteDocument extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Connection connection;  
  
    public DeleteDocument() {
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
		if(session==null || session.getAttribute("username")==null) {
			response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
			return;
		}
		String username = (String) session.getAttribute("username");
		int docID = 1;
		try {
			docID = Integer.parseInt(request.getParameter("docID"));
		}catch(NumberFormatException e) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().println("The document id must an integer");
			return;
		}
		Document docDelete = null;
		DocumentDAO docDAO = new DocumentDAO(connection);
		
		try {
			docDelete = docDAO.getDocumentInfo(docID,username);
		}catch(SQLException e) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			response.getWriter().println("Internal error. Please try later!");
			return;
		}
		
		if(docDelete==null) {
			response.setStatus(HttpServletResponse.SC_NOT_FOUND);
			response.getWriter().println("Document doesn't exist");
			return;
		}
		else {
			try {
				docDAO.deleteDocuments(docID, 0, username);
			}catch(SQLException e) {
				response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
				response.getWriter().println("Internal error. Please try later!");
				return;
			}
			response.setStatus(HttpServletResponse.SC_OK);
			return;
		}
			
	}
	
	public void destroy() {
		try {
			connection.close();
		}catch(SQLException e ) {
			
		}
	}


	
	

}
