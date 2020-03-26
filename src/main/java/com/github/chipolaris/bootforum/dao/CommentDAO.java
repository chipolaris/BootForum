package com.github.chipolaris.bootforum.dao;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

import org.springframework.stereotype.Repository;

import com.github.chipolaris.bootforum.domain.Comment;
import com.github.chipolaris.bootforum.domain.Discussion;

@Repository
public class CommentDAO {
	
	@PersistenceContext//(unitName = "BootForumPersistenceUnit")
	protected EntityManager entityManager;
	
	public List<String> getCommentorsForDiscussion(Discussion discussion) {
		
		String queryStr = "SELECT DISTINCT c.createBy FROM Comment c WHERE c.discussion = :discussion";
		
		TypedQuery<String> typedQuery = entityManager.createQuery(queryStr, String.class);
		typedQuery.setParameter("discussion", discussion);
		
		return typedQuery.getResultList();
	}
	
	public List<Long> getCommentIdsForDiscussion(Discussion discussion) {
		
		String queryStr = "SELECT c.id FROM Comment c WHERE c.discussion = :discussion";
		
		TypedQuery<Long> typedQuery = entityManager.createQuery(queryStr, Long.class);
		typedQuery.setParameter("discussion", discussion);
		
		return typedQuery.getResultList();
	}
	
	public List<String> getAttachmentPathsForDiscussion(Discussion discussion) {
		
		String queryStr = "SELECT f.path FROM FileInfo f, Comment c WHERE f MEMBER OF c.attachments and c.discussion = :discussion";
		
		TypedQuery<String> typedQuery = entityManager.createQuery(queryStr, String.class);
		typedQuery.setParameter("discussion", discussion);
		
		return typedQuery.getResultList();
	}
	
	public List<String> getThumnailPathsForDiscussion(Discussion discussion) {
		
		String queryStr = "SELECT f.path FROM FileInfo f, Comment c WHERE f MEMBER OF c.thumbnails and c.discussion = :discussion";
		
		TypedQuery<String> typedQuery = entityManager.createQuery(queryStr, String.class);
		typedQuery.setParameter("discussion", discussion);
		
		return typedQuery.getResultList();
	}
	
	public int deleteCommentsForDiscussion(Discussion discussion) {
		
		String queryStr = "DELETE FROM Comment c WHERE c.discussion = :discussion";
		
		return entityManager.createQuery(queryStr).setParameter("discussion", discussion).executeUpdate();
	}
	
	public List<Comment> getLatestCommentsForUser(String username, int maxResult) {
		
		String queryStr = "SELECT c FROM Comment c WHERE c.createBy = :username ORDER BY c.createDate DESC";
		TypedQuery<Comment> typedQuery = entityManager.createQuery(queryStr, Comment.class);
		typedQuery.setParameter("username", username);
		typedQuery.setMaxResults(maxResult);
		
		return typedQuery.getResultList();
	}
}
