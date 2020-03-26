package com.github.chipolaris.bootforum.dao;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

import org.springframework.stereotype.Repository;

import com.github.chipolaris.bootforum.domain.ForumGroup;

@Repository
public class ForumDAO {

	// no need to specify the unitName if there is only one PersistenceContext configured
	@PersistenceContext//(unitName = "BootForumPersistenceUnit")
	protected EntityManager entityManager;
	
	public List<ForumGroup> getLeafForumGroups() {
		
		String query = "SELECT fg FROM ForumGroup fg WHERE fg.subGroups IS EMPTY";
		
		TypedQuery<ForumGroup> typedQuery = entityManager.createQuery(query, ForumGroup.class);
		
		return typedQuery.getResultList();
	}
}
