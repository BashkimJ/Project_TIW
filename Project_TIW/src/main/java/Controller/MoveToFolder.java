package Controller;

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

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ServletContextTemplateResolver;

import Beans.Document;
import Beans.Folder;
import DAO.DocumentDAO;
import DAO.FolderDAO;


@WebServlet("/MoveToFolder")
public class MoveToFolder extends HttpServlet {
	private static final long serialVersionUID = 1L;
    private Connection connection;
    private TemplateEngine templateEngine;
    
    public MoveToFolder() {
        super();
       
    }

	public void init() {
	  ServletContext servletcontext = getServletContext();
      ServletContextTemplateResolver templateResolver = new ServletContextTemplateResolver(servletcontext);
  	  templateResolver.setTemplateMode(TemplateMode.HTML);
  	  this.templateEngine = new TemplateEngine();
  	  this.templateEngine.setTemplateResolver(templateResolver);
  	  templateResolver.setSuffix(".html");

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
		
		if(session.isNew() || session.getAttribute("user")==null) {
			String path = getServletContext().getContextPath() + "/login.html";
			response.sendRedirect(path);
		}
		int docID = 0;
		int folderID = 0;
		String username = (String) session.getAttribute("user");
		try {
			docID = Integer.parseInt(request.getParameter("docID"));
			folderID = Integer.parseInt(request.getParameter("folderID"));
		}catch(NumberFormatException e) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST,"Parameters must be integers");
			return;
		}
		
		
		FolderDAO folderDAO = new FolderDAO(connection);
		Folder folder =  null;
		
		try {
			folder = folderDAO.getFolder(folderID, (String)session.getAttribute("user"));
		}catch(SQLException e) {
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,"Error while checking the existence of the folder");
			return;
		}
		
		if(folder==null) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST,"Folder doesn't exist");
			return;
		}
		DocumentDAO docDAO = new DocumentDAO(connection);
		Document doc = null;
		try {
			doc = docDAO.getDocumentInfo(docID,username);
		}catch(SQLException e) {
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,"Error while checking the existence of the document");
			return;
		}
		if(doc==null) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST,"Document doesn't exist");
			return;
		}
		
		
		boolean moved = false;
		try {
			moved = docDAO.moveDOC(docID,folderID,username);
		}catch(SQLException e) {
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,"Couldn't move document");
			return;
		}
		if(moved==true) { 
			String path = getServletContext().getContextPath() + "/GetFolderContent?folderID=" + folderID;
			response.sendRedirect(path);
	
		}
		else {
			String path = getServletContext().getContextPath() + "/MoveDoc?documentID=" + docID;
			response.sendRedirect(path);
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
