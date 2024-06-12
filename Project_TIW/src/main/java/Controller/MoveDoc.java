package Controller;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

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

@WebServlet("/MoveDoc")
public class MoveDoc extends HttpServlet {
	private static final long serialVersionUID = 1L;
    private TemplateEngine templateEngine;
    private Connection connection;
    
    public MoveDoc() {
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
		if(session.isNew() || session.getAttribute("user")==null ){
			String path = getServletContext().getContextPath() + "/login.html";
		    response.sendRedirect(path);		
	    } 
		int docID = 0;
		
		try {
			docID = Integer.parseInt(request.getParameter("documentID"));
		}catch(NumberFormatException e) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST,"Parameter must be an integer");
		}
		String username = (String) session.getAttribute("user");
		DocumentDAO docdao = new DocumentDAO(connection);
		Document doc = null;
		try {
			doc = docdao.getDocumentInfo(docID,username);
			if(doc==null) {
				response.sendError(HttpServletResponse.SC_BAD_REQUEST,"Document doesn't exist");
				return;
			}
		}catch(SQLException e) {
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,"Error while checking document existence");
			return;
		}
		FolderDAO folderDAO = new FolderDAO(connection);
		List<Folder> allFolders = new ArrayList<Folder>();
		try {
			allFolders = folderDAO.getRootFolders(username);
		}catch(SQLException e) {
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,"Error while getting folders");

		}
		ServletContext context = getServletContext();
		WebContext ctx = new WebContext(request,response,context,request.getLocale());
		ctx.setVariable("rootFolders", allFolders);
		ctx.setVariable("doc",doc);
		templateEngine.process("/WEB-INF/HomePage.html",ctx,response.getWriter());
		
	}
	public void destroy() {
		try {
			connection.close();
			
		}catch(SQLException e) {
			
		}
	}
}
