package com.github.chipolaris.bootforum.messaging;

import java.util.Calendar;
import java.util.Date;

public class ConnectedUser {
	
	public ConnectedUser(String username) {
		this.username = username;
		this.connectedDate = Calendar.getInstance().getTime();
		this.setSessionCount(1);
	}
	
	private String username;
	private Date connectedDate;
	private int sessionCount;
	
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}

	public Date getConnectedDate() {
		return connectedDate;
	}
	public void setConnectedDate(Date connectedDate) {
		this.connectedDate = connectedDate;
	}
	
	public int getSessionCount() {
		return sessionCount;
	}
	public void setSessionCount(int sessionCount) {
		this.sessionCount = sessionCount;
	}
	public void addSessionCount(int value) {
		this.sessionCount += value;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((username == null) ? 0 : username.hashCode());
		return result;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ConnectedUser other = (ConnectedUser) obj;
		
		if (username == null) {
			if (other.username != null)
				return false;
		} 
		else if (!username.equals(other.username)) {
			return false;
		}
		return true;
	}
}
