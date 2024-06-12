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

import DAO.UserDao;

@MultipartConfig
@WebServlet("/CheckRegistration")
public class CheckRegistration extends HttpServlet {
	private static final long serialVersionUID = 1L;
    private Connection connection = null;
    
    public CheckRegistration() {
        super();
        // TODO Auto-generated constructor stub
    }
    
    public void init() throws ServletException{
          ServletContext context =getServletContext();
  	  try {
  		  String user = context.getInitParameter("dbUser");
  		  String pass = context.getInitParameter("dbPassword");
  		  String url = context.getInitParameter("dbUrl");
  		  String driver = context.getInitParameter("dbDriver");
  		  Class.forName(driver);
  		  connection = DriverManager.getConnection(url, user, pass);
  	  }catch(ClassNotFoundException e) {
  		  e.printStackTrace();
  	  }catch(SQLException e) {
  		  e.printStackTrace();
  	  }
    	
    }
	
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String email = StringEscapeUtils.escapeJava(request.getParameter("email"));
		String username = StringEscapeUtils.escapeJava(request.getParameter("username"));
		String pwd = StringEscapeUtils.escapeJava(request.getParameter("password"));
		String rpt = StringEscapeUtils.escapeJava(request.getParameter("reppassword"));
		if(email==null || username==null || pwd==null || rpt==null || email.isEmpty() || username.isEmpty() || pwd.isEmpty() || rpt.isEmpty()) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().println("Fill out all the fields");
			return;
		}
		if(!email.contains("@")){
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().println("Invalid email format");
			return;
		}
		if(!pwd.equals(rpt)) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().println("The passwords are not matching");
			return;
		}
		if(email.length()>40 || username.length()>40 || pwd.length()>40 ) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().println("Too many characters. Check to have less than 40 characters for every field.");
			return;
		}
		UserDao userDao = new UserDao(connection);
		boolean added = false;
		try {
			added = userDao.addUser(username, pwd, email);
			if(added==false) {
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				response.getWriter().println("Invalid username");
				return;
			}
			else {
				response.setStatus(HttpServletResponse.SC_OK);
				return;
			}
		}catch(SQLException e) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			response.getWriter().println("Server error. Try later");
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
