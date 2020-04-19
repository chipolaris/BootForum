package com.github.chipolaris.bootforum.enumeration;

public enum UserRole {

	ADMIN ("Administrator"),
	USER ("User");

	private String label;
	
	UserRole(String name) {

		this.label = name;
	}
	
    public String getLabel() {
    	return label;
    }
}