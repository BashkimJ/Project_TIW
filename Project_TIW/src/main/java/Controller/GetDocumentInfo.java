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
import org.thymeleaf.context.WebContext;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ServletContextTemplateResolver;

import Beans.Document;
import Beans.Folder;
import DAO.DocumentDAO;
import DAO.FolderDAO;


@WebServlet("/GetDocumentInfo")
public class GetDocumentInfo extends HttpServlet {
	private static final long serialVersionUID = 1L;
    private Connection connection;
    TemplateEngine templateEngine;
    
   
    public GetDocumentInfo() {
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
		int docID = 0;
		if(session.isNew() || session.getAttribute("user")==null) {
			String path = getServletContext().getContextPath() + "/login.html";
			response.sendRedirect(path);
		}
		String username = (String)session.getAttribute("user");
		try {
		    docID =Integer.parseInt(request.getParameter("documentID"));
		}catch(NumberFormatException e) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST,"Parameters must be integers");
		}
		Document doc = null;
		DocumentDAO DocDao = new DocumentDAO(connection);
		try {
			doc = DocDao.getDocumentInfo(docID,username);
		}catch(SQLException e) {
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,"Error while connecting to the database");
		}
		if(doc==null) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST,"This document doesn't exist");
			return;
		}
		Folder folder=null;
		try {
		folder = new FolderDAO(connection).getFolder(doc.getFolder(), (String)session.getAttribute("user"));
		}catch(SQLException e) {
		}
		ServletContext context = getServletContext();
		WebContext ctx = new WebContext(request,response,context,request.getLocale());
		ctx.setVariable("doc",doc);
		ctx.setVariable("folder", folder);
		templateEngine.process("/WEB-INF/Documento.html",ctx,response.getWriter());
		
		
	}
	
	public void destroy() {
		try {
			connection.close();
		}catch(SQLException e ) {
			e.printStackTrace();
		}
	}

	
	

}
