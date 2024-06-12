package Controller;

import java.io.*;
import java.sql.*;
import javax.servlet.*;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ServletContextTemplateResolver;

import Beans.User;
import DAO.UserDao;




@WebServlet("/checkLogin")
public class checkLogin extends HttpServlet {
	private static final long serialVersionUID = 1L;
    Connection connection = null;
    private TemplateEngine templateEngine;
    public checkLogin() {
        super();
    }
    
    
    public void init(){
    	ServletContext servletcontext= getServletContext();
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
		String error="";
		
		//Read the values inserted from the user
		String user = request.getParameter("username");
		String password = request.getParameter("password");
		User login = null;
		
		//Control if they are empty
		if(user==null || password==null || user.isEmpty() || password.isEmpty()) {
			error = error + "All fields must be compiled";
		}
	
		//Find the user if present
		if(error=="") {
		    UserDao controll = new UserDao(connection);
		    try {
		      login = controll.checkUser(user,password);
		    }catch(SQLException e) {
			  response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,"Error while comunicating with the server");
		      }
		    if(login==null) {
		    	error = error+" Invalid Password or Username";
		    }
		 }
		if(login !=null) {
			request.getSession().setAttribute("user",login.getUsername());
			String path = getServletContext().getContextPath() + "/GoToHomePage";
			response.sendRedirect(path);
			
		}else{
			ServletContext context = getServletContext();
			WebContext ctx = new WebContext(request,response,context,request.getLocale());
			ctx.setVariable("errorMsg",error);
			templateEngine.process("login.html",ctx,response.getWriter());
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
