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

import Beans.Document;
import Beans.Folder;
import DAO.DocumentDAO;
import DAO.FolderDAO;


@WebServlet("/AddDocument")
public class AddDocument extends HttpServlet {
	private static final long serialVersionUID = 1L;
    private Connection connection;
    private TemplateEngine templateEngine;
    
    public AddDocument() {
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
		String err="";
		if(session.isNew() || session.getAttribute("user")==null) {
			String path =getServletContext().getContextPath() + "/login.html";
			response.sendRedirect(path);
		}
		//Get values
		String name = request.getParameter("docname");
		String type = request.getParameter("type");
		int folderID = 0;
		try {
			folderID = Integer.parseInt(request.getParameter("folder"));
		}catch(NumberFormatException e) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST,"Parameter for folder must be integer");
		    return;
		}
		String summary = request.getParameter("summary");
		String user = (String) session.getAttribute("user");
		LocalDate creationDate = LocalDate.now();
		String date = creationDate.toString();
		
		//Check for null or empty values
		if(name==null || type==null || summary==null || user==null || date==null || name.isEmpty() || type.isEmpty() || summary.isEmpty() || user.isEmpty() || date.isEmpty() ) {
			err = err + "All fields must be compiled";
		}
		if(err=="" && summary.length()>200) {
			err = err + "No more than 200 characters!";
		}
		if(err=="" && name.length()>45) {
			err = err + "Too many characters for your document name";
		}
		if(err=="" && !type.equals("pdf") && !type.equals("pptx") && !type.equals("doc") && !type.equals("txt") && !type.equals("xlsm") && !type.equals("html")) {
	        err = err + "Invalid format of document"; 		
		}
		Folder rootFolder = null;
		//Check that the folder exists
		if(err=="") {
			try {
				rootFolder = new FolderDAO(connection).getFolder(folderID,user);
			} catch (SQLException e) {
				response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,"Error while trying to find the folder");
			    return; 
			}
			if(rootFolder==null) {
				err = err+"Folder doesn't exist";
			}
		}
		DocumentDAO docDAO = new DocumentDAO(connection);
		//Check if any document with the same name is present in the folder
		if(err=="") {
			try {
				if(docDAO.checkDocName(name, folderID)) {
					err = err + "Choose another name for your document. This already exists";
				}
			}catch(SQLException e) {
				response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,"Error while checking the doc's name");
			    return;
			}
			
			
		}
		//Add the document
		if(err=="") {
			Document addDoc = new Document(6,user,name,type,date,summary,folderID);
			
			try {
				docDAO.addDoc(addDoc);
			}catch(SQLException e) {
				response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,"Error while adding the document");
				return;
				}
		}
		//Ritorna nella pagina GestioneContenuti
		if(err=="") {
			String path = getServletContext().getContextPath() + "/GoToManageContentPage";
			response.sendRedirect(path);
		}
		
		//Stampa l'errore
		else {
			List<Folder> allfolders = new ArrayList<Folder>();
			try {
			       allfolders = new FolderDAO(connection).getAllFolders(user);
			}catch(SQLException e) {
				response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,"Error while trying to find all folders");
			}
			
			ServletContext context = getServletContext();
			WebContext ctx = new WebContext(request,response,context,request.getLocale());
			ctx.setVariable("folders", allfolders);
			ctx.setVariable("errorDoc",err);
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
