package com.github.chipolaris.bootforum.dao;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.TypedQuery;

import org.springframework.stereotype.Repository;

import com.github.chipolaris.bootforum.domain.Forum;
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
	
	public Integer moveDiscussions(Forum fromForum, Forum toForum) {
		
		String queryStr = "Update Discussion d SET d.forum = :toForum WHERE d.forum = :fromForum";
		
		Query query = entityManager.createQuery(queryStr);
		
		return query.setParameter("fromForum", fromForum).setParameter("toForum", toForum).executeUpdate();
	}
}
