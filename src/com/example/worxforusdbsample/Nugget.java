package com.example.worxforusdbsample;

public class Nugget {
	String type="";
	int id =0;
	
	public static final String IRON = "Iron"; 
	public static final String GOLD = "Gold"; 
	public static final String DIAMOND = "Diamond"; 

	public String getType() {
		return type;
	}
	
	public void setType(String type) {
		this.type = type;
	}

	public void setType(int type) {
		if (type == 1) 
			setType(GOLD);
		else if (type == 2) 
			setType(DIAMOND);
		else //set remaining to Iron
			setType(IRON);
	}
	
	public int getId() {
		return id;
	}
	
	public void setId(int id) {
		this.id = id;
	}
	
	public String getDescription() {
		return type+" nugget";
	}
}
