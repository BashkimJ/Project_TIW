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

import DAO.DocumentDAO;
import DAO.FolderDAO;

import java.util.*;
import Beans.Document;
import Beans.Folder;


@WebServlet("/GetFolderContent")
public class GetFolderContent extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private TemplateEngine templateEngine =null;
	private Connection connection = null;
       
   
    public GetFolderContent() {
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
		int folderID = 0;
		HttpSession session = request.getSession();
		List<Document> documents = new ArrayList<Document>();
		List<Folder> folders = new ArrayList<Folder>();
		if(session.isNew() || session.getAttribute("user")==null) {
			String path = getServletContext().getContextPath() + "/login.html";
			response.sendRedirect(path);
		}
		String username  = (String)session.getAttribute("user");
		
		try {
			folderID = Integer.parseInt(request.getParameter("folderID"));
		}catch(NumberFormatException e) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST,"An integer must be provided as a parameter");
		}
		
		Folder folder = null;
		try {
			folder=new FolderDAO(connection).getFolder(folderID, username);
		}catch(SQLException e) {
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,"Error while connecting to the database");
			return;
		}
		if(folder==null) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST,"Folder doesn't exist");
			return;
		}
		
		DocumentDAO documentdao = new DocumentDAO(connection);
		try {
			documents = documentdao.getDocuments(folderID, username);
			
		}catch(SQLException e) {
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,"Error while connecting to the database");
		}
		String msg = "";
		if(documents.isEmpty()) {
			msg="No documents present";
		}
		
		try {
			folders = new FolderDAO(connection).getSubFolders(username,folderID);
		}catch(SQLException e) {
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,"Error while getting subfolders");
			return;
		}
		if(folder!=null) {
		ServletContext context = getServletContext();
		WebContext ctx = new WebContext(request,response,context,request.getLocale());
		ctx.setVariable("documents",documents);
		ctx.setVariable("folders", folders);
		ctx.setVariable("empty", msg);
		ctx.setVariable("folder", folder);
		ctx.setVariable("moveError", "");
		templateEngine.process("/WEB-INF/Contenuti.html",ctx,response.getWriter());
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
