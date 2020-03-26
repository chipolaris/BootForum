package com.github.chipolaris.bootforum.util;

import java.util.Comparator;

import com.github.chipolaris.bootforum.domain.BaseEntity;

public class DomainEntitySorter {

	private static Comparator<BaseEntity> dateSorter;
	private static Comparator<BaseEntity> dateReverseSorter;
	private static Comparator<BaseEntity> idSorter;
	private static Comparator<BaseEntity> idReverseSorter;
	
	public static Comparator<BaseEntity> dateSorter() {
		
		if(dateSorter == null) {
			dateSorter = new Comparator<BaseEntity>() {

				@Override
				public int compare(BaseEntity o1, BaseEntity o2) {
					
					if(o1 != null && o2 != null) {
						return o1.getCreateDate().compareTo(o2.getCreateDate());
					}
					
					if(o1 != null) return 1;
					
					if(o2 != null) return -1;
					
					return 0;
				}
				
			};
		}
		
		return dateSorter; 
	}
	
	public static Comparator<BaseEntity> dateReverseSorter() {
		
		if(dateReverseSorter == null) {
			dateReverseSorter = new Comparator<BaseEntity>() {

				@Override
				public int compare(BaseEntity o1, BaseEntity o2) {
					
					if(o1 != null && o2 != null) {
						return - o1.getCreateDate().compareTo(o2.getCreateDate());
					}
					
					if(o1 != null) return 1;
					
					if(o2 != null) return -1;
					
					return 0;
				}
				
			};
		}
		
		return dateReverseSorter; 
	}
	
	public static Comparator<BaseEntity> idSorter() {
		
		if(idSorter == null) {
			idSorter = new Comparator<BaseEntity>() {

				@Override
				public int compare(BaseEntity o1, BaseEntity o2) {
					
					if(o1 != null && o2 != null) {
						return o1.getId().compareTo(o2.getId());
					}
					
					if(o1 != null) return 1;
					
					if(o2 != null) return -1;
					
					return 0;
				}
				
			};
		}
		
		return idSorter; 
	}
	
	public static Comparator<BaseEntity> idReverseSorter() {
		
		if(idReverseSorter == null) {
			idReverseSorter = new Comparator<BaseEntity>() {

				@Override
				public int compare(BaseEntity o1, BaseEntity o2) {
					
					if(o1 != null && o2 != null) {
						return - o1.getId().compareTo(o2.getId());
					}
					
					if(o1 != null) return 1;
					
					if(o2 != null) return -1;
					
					return 0;
				}
				
			};
		}
		
		return idReverseSorter; 
	}
}
