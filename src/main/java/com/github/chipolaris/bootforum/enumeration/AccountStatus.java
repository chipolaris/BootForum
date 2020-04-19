package com.github.chipolaris.bootforum.enumeration;

public enum AccountStatus {

	ACTIVE 			("Active"),
	LOCKED			("Locked"),
	INACTIVE		("Inactive");
	
	private String label;
	
	AccountStatus(String name) {
		this.label = name;
	}
	
    public String getLabel() {
    	return label;
    }
}
