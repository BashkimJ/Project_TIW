package Controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang.StringEscapeUtils;

import Beans.User;
import DAO.*;

/**
 * Servlet implementation class checkLogin
 */
@WebServlet("/CheckLogin")
@MultipartConfig
public class CheckLogin extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Connection connection = null;
	  
    public CheckLogin() {
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
    	String err= "";
    	String username = null;
    	String password = null;
    	username = StringEscapeUtils.escapeJava(request.getParameter("username"));
    	password = StringEscapeUtils.escapeJava(request.getParameter("password"));
    	if(username==null || password==null || username.isEmpty() || password.isEmpty()) {
    		err = "You must fill all the fields";
    	}
    	if(err.equals("")) {
    		User user = null;
    		UserDao dao = new UserDao(connection);
    		try {
    			user = dao.checkUser(username, password);
    			if(user==null) {
    				err = "Wrong Credentials";
    			}
      		}catch(SQLException e) {
    			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    			response.getWriter().println("Internal Error. Please try later");
    			return;
    		}
    	}
    	if(err.equals("")) {
    		request.getSession().setAttribute("username",username);
    		response.setStatus(HttpServletResponse.SC_OK);
    		response.getWriter().println(username);
    		return;
    	}
    	else {
    		response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
    		response.getWriter().println(err);
    		return;
    	}
   
    
    }

    
    public void destroy(){
		try {
			if(connection!=null) {
			connection.close();}
		}catch(SQLException e) {
			e.printStackTrace();
		}
		
	}

}
