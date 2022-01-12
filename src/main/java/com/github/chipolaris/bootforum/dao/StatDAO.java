package com.github.chipolaris.bootforum.dao;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
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

	/**
	 * 
	 * @param discussion
	 * @return
	 * 
	 * @deprecated This method will not work with SQL Server due to SUM(SIZE()) usage
	 */
	@Deprecated
	public Long countThumbnail(Discussion discussion) {
		TypedQuery<BigDecimal> typedQuery = entityManager.createQuery("SELECT COALESCE(SUM(SIZE(c.thumbnails)), 0) FROM Comment c WHERE c.discussion = :discussion", BigDecimal.class);
		typedQuery.setParameter("discussion", discussion);
		
		return typedQuery.getSingleResult().longValue();
	}
	
	/**
	 * 
	 * @param discussion
	 * @return
	 * 
	 * @deprecated This method will not work with SQL Server due to SUM(SIZE()) usage
	 */
	@Deprecated
	public Long countAttachment(Discussion discussion) {
		TypedQuery<BigDecimal> typedQuery = entityManager.createQuery("SELECT COALESCE(SUM(SIZE(c.attachments)), 0) FROM Comment c WHERE c.discussion = :discussion", BigDecimal.class);
		typedQuery.setParameter("discussion", discussion);
		
		return typedQuery.getSingleResult().longValue();
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

	/**
	 * 
	 * @param username
	 * @return
	 * 
	 * @deprecated: this method does not work with SQL Server due to SUM(SIZE()) call
	 */
	@Deprecated
	public Long countCommentThumbnails_Deprecated(String username) {
		
		TypedQuery<BigDecimal> typedQuery = entityManager.createQuery("SELECT COALESCE(SUM(SIZE(c.thumbnails)), 0) FROM Comment c WHERE c.createBy = :createBy", BigDecimal.class);
		typedQuery.setParameter("createBy", username);
		
		return typedQuery.getSingleResult().longValue();
	}
	
	/**
	 * This method identical as the method above, using native SQL query to avoid SQL Server issue as noted above
	 */
	public Long countCommentThumbnails(String username) {
		
		String nativeQuery = "SELECT COUNT(1) FROM COMMENT_THUMBNAIL_T CT"
				+ " LEFT JOIN COMMENT_T C ON CT.COMMENT_ID = C.ID"
				+ " WHERE C.CREATED_BY = ?1";
		
		Query query = entityManager.createNativeQuery(nativeQuery).setParameter(1, username);
		
		/* 
		 * Note: the query above returns Long in Postgresql and BigInteger in SQL Server 
		 * So, the compromise is to downcast to Number first, then return longValue
		 */
		return ((Number) query.getSingleResult()).longValue();
	}	
	
	/**
	 * 
	 * @param username
	 * @return
	 * 
	 * @deprecated: this method does not work with SQL Server due to SUM(SIZE()) call
	 */
	public Long countCommentAttachments_Deprecated(String username) {
		
		TypedQuery<BigDecimal> typedQuery = entityManager.createQuery("SELECT COALESCE(SUM(SIZE(c.attachments)), 0) FROM Comment c WHERE c.createBy = :createBy", BigDecimal.class);
		typedQuery.setParameter("createBy", username);
		
		return typedQuery.getSingleResult().longValue();
	}
	
	/**
	 * This method identical as the method above, using native SQL query to avoid SQL Server issue as noted above
	 */
	public Long countCommentAttachments(String username) {
		
		String nativeQuery = "SELECT COUNT(1) FROM COMMENT_ATTACHMENT_T CA"
				+ " LEFT JOIN COMMENT_T C ON CA.COMMENT_ID = C.ID"
				+ " WHERE C.CREATED_BY = ?1";
		
		Query query = entityManager.createNativeQuery(nativeQuery).setParameter(1, username);
		
		/* 
		 * Note: the query above returns Long in Postgresql and BigInteger in SQL Server 
		 * So, the compromise is to downcast to Number first, then return longValue
		 */
		return ((Number) query.getSingleResult()).longValue();
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
