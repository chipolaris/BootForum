package com.github.chipolaris.bootforum.dao;

import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

import org.springframework.stereotype.Repository;

import com.github.chipolaris.bootforum.domain.Comment;
import com.github.chipolaris.bootforum.domain.CommentInfo;
import com.github.chipolaris.bootforum.domain.Discussion;
import com.github.chipolaris.bootforum.domain.Forum;
import com.github.chipolaris.bootforum.domain.UserStat;

@Repository
public class StatDAO {
	
	// no need to specify the unitName if there is only one PersistenceContext configured
	@PersistenceContext//(unitName = "BootForumPersistenceUnit")
	protected EntityManager entityManager;

	public Long countThumbnail(Discussion discussion) {
		TypedQuery<Long> typedQuery = entityManager.createQuery("SELECT SUM(SIZE(c.thumbnails)) FROM Comment c WHERE c.discussion = :discussion", Long.class);
		typedQuery.setParameter("discussion", discussion);
		
		return typedQuery.getSingleResult();
	}
	
	public Long countAttachment(Discussion discussion) {
		TypedQuery<Long> typedQuery = entityManager.createQuery("SELECT SUM(SIZE(c.attachments)) FROM Comment c WHERE c.discussion = :discussion", Long.class);
		typedQuery.setParameter("discussion", discussion);
		
		return typedQuery.getSingleResult();
	}
	
	public Long countComment(Forum forum) {
		
		TypedQuery<Long> typedQuery = entityManager.createQuery("SELECT COUNT(c) FROM Comment c WHERE c.discussion.forum = :forum", Long.class);
		typedQuery.setParameter("forum", forum);
		
		return typedQuery.getSingleResult();
	}
	
	public Long countDiscussion(Forum forum) {
		
		TypedQuery<Long> typedQuery = entityManager.createQuery("SELECT COUNT(d) FROM Discussion d WHERE d.forum = :forum", Long.class);
		typedQuery.setParameter("forum", forum);
		
		return typedQuery.getSingleResult();
	}
	
	public CommentInfo latestCommentInfo(Forum forum) {
		
		String queryStr = "SELECT d.stat.lastComment FROM Discussion d WHERE d.forum = :forum ORDER BY d.stat.lastComment.updateDate DESC";
		
		TypedQuery<CommentInfo> typedQuery = entityManager.createQuery(queryStr, CommentInfo.class);
		typedQuery.setParameter("forum", forum);
		
		List<CommentInfo> resultList = typedQuery.setMaxResults(1).getResultList();
		
		return resultList.isEmpty() ? null : resultList.get(0);
	}
	
	public CommentInfo latestCommentInfo() {
		
/*		CriteriaBuilder builder = entityManager.getCriteriaBuilder();
		
		CriteriaQuery<CommentInfo> query = builder.createQuery(CommentInfo.class);
		
		Root<CommentInfo> root = query.from(CommentInfo.class);
		
		query.select(builder.max(root.get("createDate")));*/
		
		TypedQuery<CommentInfo> typedQuery = entityManager.createQuery("FROM CommentInfo ci ORDER BY ci.updateDate DESC", CommentInfo.class);
		
		/*
		 * A note about TypedQuery.getSingleResult() vs.Typedquery.getResultList(0):
		 * 
		 *  The getSingleResult() method throws NonUniqueResultException
		 *  
		 *  refs: 
		 *  	- https://stackoverflow.com/questions/8500031/what-is-better-getsingleresult-or-getresultlist-jpa
		 *  	- http://sysout.be/2011/03/09/why-you-should-never-use-getsingleresult-in-jpa/
		 */
		
		List<CommentInfo> resultList = typedQuery.setMaxResults(1).getResultList();
		
		return resultList.isEmpty() ? null : resultList.get(0);
	}
	
	public String getLatestUsername() {
		
		TypedQuery<String> typedQuery = entityManager.createQuery("SELECT u.username FROM User u ORDER BY u.createDate DESC", String.class);
		typedQuery.setMaxResults(1);
		
		List<String> resultList = typedQuery.setMaxResults(1).getResultList();
		
		return resultList.isEmpty() ? null : resultList.get(0);
	}
	
	public Date getLastUserRegisteredDate() {
		TypedQuery<Date> typedQuery = entityManager.createQuery("SELECT u.createDate FROM User u ORDER BY u.createDate DESC", Date.class);
		typedQuery.setMaxResults(1);
		
		List<Date> resultList = typedQuery.setMaxResults(1).getResultList();
		
		return resultList.isEmpty() ? null : resultList.get(0);
	}
	
	public UserStat getUserStat(String username) {
		
		TypedQuery<UserStat> typedQuery = entityManager.createQuery("SELECT u.stat FROM User u where u.username = :username", UserStat.class);
		typedQuery.setParameter("username", username);
		
		List<UserStat> resultList = typedQuery.setMaxResults(1).getResultList();
		
		return resultList.isEmpty() ? null : resultList.get(0);
	}

	public Long countComment(Discussion discussion) {

		TypedQuery<Long> typedQuery = entityManager.createQuery("SELECT COUNT(c) FROM Comment c WHERE c.discussion = :discussion", Long.class);
		typedQuery.setParameter("discussion", discussion);
		
		return typedQuery.getSingleResult();
	}

	public Long countCommentThumbnails(String username) {
		
		TypedQuery<Long> typedQuery = entityManager.createQuery("SELECT SUM(SIZE(c.thumbnails)) FROM Comment c WHERE c.createBy = :createBy", Long.class);
		typedQuery.setParameter("createBy", username);
		
		return typedQuery.getSingleResult();
	}
	
	public Long countCommentAttachments(String username) {
		
		TypedQuery<Long> typedQuery = entityManager.createQuery("SELECT SUM(SIZE(c.attachments)) FROM Comment c WHERE c.createBy = :createBy", Long.class);
		typedQuery.setParameter("createBy", username);
		
		return typedQuery.getSingleResult();
	}
	
	public Long countComment(String username) {
		TypedQuery<Long> typedQuery = entityManager.createQuery("SELECT COUNT(c) FROM Comment c WHERE c.createBy = :createBy", Long.class);
		typedQuery.setParameter("createBy", username);
		
		return typedQuery.getSingleResult();
	}
	
	public Long countDiscussion(String username) {

		TypedQuery<Long> typedQuery = entityManager.createQuery("SELECT COUNT(d) FROM Discussion d WHERE d.createBy = :username", Long.class);
		typedQuery.setParameter("username", username);
		
		return typedQuery.getSingleResult();
	}

	public Comment getLatestComment(Discussion discussion) {
		TypedQuery<Comment> typedQuery = entityManager.createQuery("SELECT c FROM Comment c WHERE c.discussion = :discussion ORDER BY c.createDate DESC", Comment.class);
		typedQuery.setParameter("discussion", discussion);
		
		List<Comment> resultList = typedQuery.setMaxResults(1).getResultList();
		
		return resultList.isEmpty() ? null : resultList.get(0);
	}
	
	/**
	 * get last comment for the given commentor name
	 * @param commentor
	 * @return
	 */
	public Comment getLatestComment(String commentor) {
		TypedQuery<Comment> typedQuery = entityManager.createQuery("SELECT c FROM Comment c WHERE c.createBy = :commentor ORDER BY c.createDate DESC", Comment.class);
		typedQuery.setParameter("commentor", commentor);
		
		List<Comment> resultList = typedQuery.setMaxResults(1).getResultList();
		
		return resultList.isEmpty() ? null : resultList.get(0);
	}
}
