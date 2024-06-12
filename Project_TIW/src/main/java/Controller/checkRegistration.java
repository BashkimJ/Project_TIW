package Controller;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ServletContextTemplateResolver;

import DAO.UserDao;


@WebServlet("/checkRegistration")
public class checkRegistration extends HttpServlet {
	private static final long serialVersionUID = 1L;
    private Connection connection;
    private TemplateEngine templateEngine;
    
    public checkRegistration() {
        super();
        // TODO Auto-generated constructor stub
    }
    
    public void init() throws ServletException{
      ServletContext context = getServletContext();
      ServletContextTemplateResolver templateResolver = new ServletContextTemplateResolver(context);
  	  templateResolver.setTemplateMode(TemplateMode.HTML);
  	  this.templateEngine = new TemplateEngine();
  	  this.templateEngine.setTemplateResolver(templateResolver);
  	  templateResolver.setSuffix(".html");
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
		String error="";
		String error1="";
		String error2="";
		String error3="";
		Boolean done = false;
		String user = request.getParameter("username");
		String email = request.getParameter("email");
		String pass = request.getParameter("password");
		String repeat = request.getParameter("repeatPass");
		
		//Control that no parameter is empty
		if(user==null || email==null || pass==null || repeat==null || user.isEmpty() || email.isEmpty() || pass.isEmpty() || repeat.isEmpty())
		{
			error = error + "You must fill all fields";
		}
		
		if(error=="" && (user.length()>40 || email.length()>40 || pass.length()>40)) {
			error = error +"You are exceding the maximum characters. The username,password or email must contain at most 40 characters";
		}
			
		//Control the email syntax
		if(!email.contains("@") && error=="") {
			error1= error1+"You must give the correct email address";
		}
		
		//Control password matching
		if(!pass.equals(repeat) && error=="") {
			error3 = error3 + "Passwords not matching";
		}
		
		if(error=="" && error1=="" && error3=="") {
			UserDao controll = new UserDao(connection);
			try {
				done = controll.addUser(user, pass, email);
			}catch(SQLException e) {
				response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,"Error while comunnicating with server");;
			}
			if(done==false) {
				error3 = error3 + "Invalid username";
			}
		}
		if(done==false) {
			ServletContext context = getServletContext();
			WebContext ctx = new WebContext(request,response,context,request.getLocale());
			ctx.setVariable("General",error);
			ctx.setVariable("error1",error1);
			ctx.setVariable("error2",error2);
			ctx.setVariable("error3",error3);
			templateEngine.process("Registration.html",ctx,response.getWriter());
			return;
		}
		else {
			response.sendRedirect("login.html");
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
