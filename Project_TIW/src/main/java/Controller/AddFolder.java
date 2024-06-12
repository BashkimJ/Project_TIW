package Controller;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.time.LocalDate;
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

import Beans.Folder;
import DAO.FolderDAO;


@WebServlet("/AddFolder")
public class AddFolder extends HttpServlet {
	private static final long serialVersionUID = 1L;
    private Connection connection;
    private TemplateEngine templateEngine;
    
    public AddFolder() {
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


	
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		HttpSession session = request.getSession();
		//Control if the session is valid
		if(session.isNew() || session.getAttribute("user")==null) {
			String path = getServletContext().getContextPath() +"/login.html";
			response.sendRedirect(path);
		}
		//Get all the parameters
		String err = "";
		String user = (String) session.getAttribute("user");
		LocalDate currentDate = LocalDate.now();
		String date = currentDate.toString();
		int rootFolder = 0;
		String name = request.getParameter("foldername");
		try {
		      rootFolder = Integer.parseInt(request.getParameter("folder"));
		}catch(NumberFormatException e ) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST,"Wrong parameter format");
			return;
		}
		
		//Control any possible empty value
		if(user==null || date==null || name==null || name.isEmpty() || date.isEmpty() || user.isEmpty()) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST,"No null parameters");
			return;
		}
		
		if(name.length()>45) {
			err = err  + "Too many characters for your folder";
		}
		
		//Get the rootFolder with ID rootFolder
		FolderDAO folderDAO = new FolderDAO(connection);
		Folder folder=null;
		try {
			folder = folderDAO.getFolder(rootFolder,user);
		}catch(SQLException e1) {
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,"Error while checking for the existence of the root folder");
			return;
		}
		
		//Control if the root folder exists
		if(err=="" && folder==null && rootFolder!=-1) {
			err=err + "No such folder";
		}
		
		//Control if the name of the folder already exists in the specified rootFolder
		if(err=="") {
		    try {
			    if(folderDAO.checkFolderName(rootFolder, name,user)) {
				     err = err + "  Choose another name for you folder. This name already exists";
			    }
		    } catch (SQLException e2) {
			    response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,"Error while checking for dublications");
			    return;
		    }
		}
		
		//Add the folder and go to the homepage
		if(err=="") {
			try {
				folderDAO.addFolder(user, name, date, rootFolder);
			} catch (SQLException e3) {
				response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,"Error while adding the folder");
				return;
			}
			response.sendRedirect("GoToHomePage");
		}
		
		//Go to the GestioneContenuti page and print all possible errors
		else {
			
			List<Folder> allFolders = new ArrayList<Folder>();
			try {
				allFolders = new FolderDAO(connection).getAllFolders(user);
			}catch(SQLException e4){
				response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error while connecting to the database");
				return;
			}
			
			ServletContext context = getServletContext();
			WebContext ctx = new WebContext(request,response,context,request.getLocale());
			ctx.setVariable("folders", allFolders);
			ctx.setVariable("errorFold", err);
			templateEngine.process("/WEB-INF/GestioneContenuti.html",ctx,response.getWriter());
			
		}
		
		
	}
	
	public void destroy() {
		try {
			connection.close();
		}catch(SQLException e) {
			
		}
	}

}
