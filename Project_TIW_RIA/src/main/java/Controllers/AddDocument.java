package Controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.time.LocalDate;
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


@WebServlet("/AddDocument")
public class AddDocument extends HttpServlet {
	private static final long serialVersionUID = 1L;
    private Connection connection;   
    public AddDocument() {
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


	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		HttpSession session = request.getSession();
		if(session.isNew() || session.getAttribute("username")==null) {
			response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
			return;
		}
		String name = request.getParameter("docname");
		String type = request.getParameter("type");
		int folderID = 0;
		try {
			folderID = Integer.parseInt(request.getParameter("folder"));
		}catch(NumberFormatException e) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().println("Folder ID must be an int");
			return;
		}
		String summary = request.getParameter("summary");
		String user = (String) session.getAttribute("username");
		LocalDate creationDate = LocalDate.now();
		String date = creationDate.toString();
		
		//Check for null or empty values
		if(name==null || type==null || summary==null || user==null || date==null || name.isEmpty() || type.isEmpty() || summary.isEmpty() || user.isEmpty() || date.isEmpty() ) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().println("No null parameters allowed");
			return;
		}
		if(summary.length()>200) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().println("No more than 200 chars in the summary");
			return;
		}
		if(!type.equals("pdf") && !type.equals("pptx") && !type.equals("doc") && !type.equals("txt") && !type.equals("xlsm") && !type.equals("html")) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().println("Invalid doc type");
			return; 		
		}
		if(name.length()>45) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().println("Too many characters for your doc name");
			return; 
		}
		Folder rootFolder = null;
		//Check that the folder exists
		
			try {
				rootFolder = new FolderDAO(connection).getFolder(folderID,user);
			} catch (SQLException e) {
				response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
				response.getWriter().println("Server error.Please try later");
				return;
			}
			if(rootFolder==null) {
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				response.getWriter().println("Folder doen't exist");
				return;
			}
		
		DocumentDAO docDAO = new DocumentDAO(connection);
		//Check if any document with the same name is present in the folder
		
			try {
				if(docDAO.checkDocName(name, folderID)) {
					response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
					response.getWriter().println("Choose another name for your document");
					return;
				}
			}catch(SQLException e) {
				response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
				response.getWriter().println("Try later");
				return;
			}
			
		//Add the document
			Document addDoc = new Document(6,user,name,type,date,summary,folderID);
				try {
				docDAO.addDoc(addDoc);
			}catch(SQLException e) {
				response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
				response.getWriter().println("Try later");
				return;
				}
			response.setStatus(HttpServletResponse.SC_OK);
		
		
	}
	
	public void destroy() {
		
		try {
			connection.close();
		}catch(SQLException e) {
			
		}
	}

}
