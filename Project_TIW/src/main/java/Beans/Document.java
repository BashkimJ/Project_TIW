package Beans;

public class Document {
	private int ID;
	private String Author;
	private String Name;
	private String Type;
	private String CreationDate;
	public String Summary;
	public int Folder;
	
	public Document(int ID,String Auth,String Name,String type,String CreationDate,String Summary,int Folder) {
		this.ID = ID;
		this.Author = Auth;
		this.Name= Name;
		this.Type = type;
		this.CreationDate = CreationDate;
		this.Summary = Summary;
		this.Folder = Folder;
	}
	
	public int getID() {
		return this.ID;
	}
	public String getAuthor() {
		return this.Author;
	}
	public String  getType() {
		return this.Type;
	}
	public String getCreationDate() {
		return this.CreationDate;
	}
	public String getName() {
		return this.Name;
	}
	public String Summary() {
		return this.Summary;
	}
	public int getFolder() {
		return this.Folder;
	}

}
