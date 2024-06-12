package Controllers;

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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import Beans.Folder;
import DAO.FolderDAO;

/**
 * Servlet implementation class GetGolders
 */
@WebServlet("/GetFolders")
public class GetFolders extends HttpServlet {
	private static final long serialVersionUID = 1L;
    private Connection connection;   
   
    public GetFolders() {
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
		String user = (String) session.getAttribute("username");
		List<Folder> folders = new ArrayList<Folder>();
		FolderDAO folderdao = new FolderDAO(connection);
		try {
			folders = folderdao.getRootFolders(user);
			
		}catch(SQLException e) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			response.getWriter().println("Internal server error. Please try later!");
			return;
		}
		Gson gson = new GsonBuilder().setDateFormat("yyyy/MM/dd").create();
		String json = gson.toJson(folders);
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		response.setStatus(HttpServletResponse.SC_OK);
		response.getWriter().println(json);

	}
	
	public void destroy() {
		try {
			if(connection!=null) {
			connection.close();}
		}catch(SQLException e) {
			e.printStackTrace();
		}
	}

}
