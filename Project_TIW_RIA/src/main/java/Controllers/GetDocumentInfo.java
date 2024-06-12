package Controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;
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


@WebServlet("/GetDocumentInfo")
public class GetDocumentInfo extends HttpServlet {
	private static final long serialVersionUID = 1L;
    private Connection connection;
    
    
   
    public GetDocumentInfo() {
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
		int docID = 0;
		if(session.isNew() || session.getAttribute("username")==null) {
			response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
			return;
		}
		String username = (String)session.getAttribute("username");
		try {
		    docID =Integer.parseInt(request.getParameter("docID"));
		}catch(NumberFormatException e) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().println("The parameter of the document ID must be an int");
			return;
		}
		Document doc = null;
		DocumentDAO DocDao = new DocumentDAO(connection);
		try {
			doc = DocDao.getDocumentInfo(docID,username);
		}catch(SQLException e) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			response.getWriter().println("Server error. Please try later");
			return;
		}
		if(doc==null) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().println("Document not found");
			return;
		}
		Folder folder=null;
		try {
			folder = new FolderDAO(connection).getFolder(doc.getFolder(), username);
		}catch(SQLException e) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			response.getWriter().println("Server error. Please try later");
			return;
		}
		Map<String,Object> docInfo = new HashMap<String,Object>();
		docInfo.put("document", doc);
		docInfo.put("fold", folder);
		Gson gson = new GsonBuilder().create();
		String json = gson.toJson(docInfo);
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		response.setStatus(HttpServletResponse.SC_OK);
		response.getWriter().println(json);
		
		
		
	}
	
	public void destroy() {
		try {
			connection.close();
		}catch(SQLException e ) {
			
		}
	}

	
	

}
