package Beans;

public class User {
	private String email;
	private String password;
	private String username;
	
	public User(String em,String pass, String user) {
		this.email = em;
		this.username=user;
		this.password = pass;
	}
	
	public String getEmail() {
		return this.email;
	}
	
	public String getPassword() {
		return this.password;
	}
	public String getUsername() {
		return this.username;
	}
}
