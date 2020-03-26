package com.github.chipolaris.bootforum.enumeration;

public enum AccountStatus {

	ACTIVE 			(1, "Active"),
	LOCKED			(2, "Locked"),
	INACTIVE		(3, "Inactive");

	private int id;
	private String name;
	
	AccountStatus(int id, String name) {
		this.id = id;
		this.name = name;
	}
	
	public int getId() {
		return id;
	}
	
    public static AccountStatus valueOf(int id) {
    	AccountStatus[] allValues = AccountStatus.values();
    	if(id > 0 && id <= allValues.length) {
    		return allValues[id - 1];
    	}
    	return null;
    }
	
	public static AccountStatus getValueOf(String id) {
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
