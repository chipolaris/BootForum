package com.github.chipolaris.bootforum.dao;

public class SortSpec {

	public enum Direction {
		ASC, DESC
	}
	
	public final String field;
	public final Direction dir;
	
	public SortSpec(String field, Direction dir) {
		this.dir = dir;
		this.field = field;
	}
}
