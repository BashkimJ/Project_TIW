package Beans;

import java.util.ArrayList;
import java.util.List;

public class Folder {
	private int ID;
	private String date;
	private String name;
	private String author;
	private boolean isTop;
	private boolean isEmpty;
	private List<Folder> subfolders;
	
	public Folder(int ID, String name, String date, String author,boolean isTop) {
		this.ID = ID;
		this.name = name;
		this.date = date;
		this.author = author;
		subfolders = new ArrayList<Folder>();
		this.isTop = isTop;
		isEmpty = false;
	}
	
	public String getDate() {
		return this.date;
	}
	public String getAuthor() {
		return this.author;
	}
	public String name() {
		return this.name;
	}
	public int getID() {
		return this.ID;
	}
	
	public List<Folder> getSubFold(){
		return this.subfolders;
	}
	public void addSubFolder(Folder sub) {
		subfolders.add(sub);
	}
	public boolean isTop() {
		return this.isTop;
	}
	
	public boolean isEmpty() {
		if (subfolders.isEmpty()){
			this.isEmpty=true;
		}
		return this.isEmpty;
	}

}
