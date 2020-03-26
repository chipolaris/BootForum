package com.github.chipolaris.bootforum.enumeration;

public enum UserRole {

	ADMIN 			(1, "Administrator"),
	USER 			(2, "User");

	private int id;
	private String name;
	
	UserRole(int id, String name) {
		this.id = id;
		this.name = name;
	}
	
	public int getId() {
		return id;
	}
	
    public static UserRole valueOf(int id) {
    	UserRole[] allValues = UserRole.values();
    	if(id > 0 && id <= allValues.length) {
    		return allValues[id - 1];
    	}
    	return null;
    }
	
	public static UserRole getValueOf(String id) {
		try {
			int num = Integer.parseInt(id);
			return valueOf(num);
		}
		catch(Exception e) {
			return null;
		}
	}
	
    public String getName() {
    	return name;
    }
}
