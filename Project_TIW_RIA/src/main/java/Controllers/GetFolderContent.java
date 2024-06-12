package Controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import Beans.Document;
import Beans.Folder;
import DAO.DocumentDAO;
import DAO.FolderDAO;

/**
 * Servlet implementation class GetFolderContent
 */
@WebServlet("/GetFolderContent")
public class GetFolderContent extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Connection connection;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public GetFolderContent() {
        super();
        // TODO Auto-generated constructor stub
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
		int folderID = 0;
		HttpSession session = request.getSession();
		List<Document> documents = new ArrayList<Document>();
		List<Folder> folders = new ArrayList<Folder>();
		if(session.isNew() || session.getAttribute("username")==null) {
			response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
			return;
		}
		String username =(String) session.getAttribute("username");
		try{
			folderID = Integer.parseInt(request.getParameter("folderID"));
		}catch(NumberFormatException e) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().println("An int must provide for the folderID");
			return;
		}
		Folder folder = null;
		try {
			folder=new FolderDAO(connection).getFolder(folderID, username);
		}catch(SQLException e) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			response.getWriter().println("Internal server error. Try later");
			return;
		}
		if(folder==null) {
			response.setStatus(HttpServletResponse.SC_NOT_FOUND);
			response.getWriter().println("Folder not found");
			return;
		}
		DocumentDAO documentdao = new DocumentDAO(connection);
		try {
			documents = documentdao.getDocuments(folderID, username);
			
		}catch(SQLException e) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			response.getWriter().println("Internal server error. Try later");
			return;		
		}
		try {
			folders = new FolderDAO(connection).getSubFolders(username,folderID);
		}catch(SQLException e) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			response.getWriter().println("Internal server error. Try later");
			return;	
		}
		Map<String, Object> content = new HashMap<String,Object>();
		content.put("documents", documents);
		content.put("folders", folders);
		Gson gson = new GsonBuilder().create();
		String json = gson.toJson(content);
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
