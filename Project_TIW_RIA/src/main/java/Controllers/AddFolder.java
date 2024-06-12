package Controllers;

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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import Beans.Folder;
import DAO.FolderDAO;


@WebServlet("/AddFolder")
public class AddFolder extends HttpServlet {
	private static final long serialVersionUID = 1L;
    private Connection connection;
    
    public AddFolder() {
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
		//Controll if the session is valid
		if(session.isNew() || session.getAttribute("username")==null) {
			response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
			return;
		}
		
		//Get all the parameters
		String user = (String) session.getAttribute("username");
		LocalDate currentDate = LocalDate.now();
		String date = currentDate.toString();
		int rootFolder = 0;
		String name = request.getParameter("foldername");
		try {
		      rootFolder = Integer.parseInt(request.getParameter("rootFolder"));
		}catch(NumberFormatException e ) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().println("Folder ID must be an int");
			return;
		}
		
		//Control any possible empty value
		if(user==null || date==null || name==null || name.isEmpty() || date.isEmpty() || user.isEmpty()) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().println("No null parameters");
			return;
		}
		if(name.length()>45) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().println("No more than 45 characters for your folder name");
			return;
		}
		//Get the rootFolder with ID rootFolder
		FolderDAO folderDAO = new FolderDAO(connection);
		Folder folder=null;
		try {
			folder = folderDAO.getFolder(rootFolder,user);
		}catch(SQLException e1) {
		    response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			response.getWriter().println("Server error.Please try later");
			return;
		}
		
		//Control if the root folder exists
		if(folder==null && rootFolder!=-1) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().println("Root folder doesn't exist");
			return;
		}
		
		//Control if the name of the folder already exists in the specified rootFolder
		
		    try {
			    if(folderDAO.checkFolderName(rootFolder, name,user)) {
			    	response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
					response.getWriter().println("Choose another name for your folder");
					return; 
			    }
		    } catch (SQLException e2) {
		    	response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
				response.getWriter().println("Server error.Please try later");
				return;
		    }
		    
		
			try {
				folderDAO.addFolder(user, name, date, rootFolder);
			} catch (SQLException e3) {
				response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
				response.getWriter().println("Server error.Please try later");
				return;
			}
			List<Folder> folders = new ArrayList<Folder>();
			try {
				 folders = new FolderDAO(connection).getRootFolders(user);
			}catch(SQLException e ) {
				response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
				response.getWriter().println("Server error.Please try later");
				return;
			}
			Gson gson = new GsonBuilder().create();
			String json = gson.toJson(folders);
			response.setContentType("application/json");
			response.setCharacterEncoding("UTF-8");
			response.setStatus(HttpServletResponse.SC_OK);
		    response.getWriter().println(json);
	}
	
	public void destroy() {
		try {
			connection.close();
		}catch(SQLException e) {
			
		}
	}

}
