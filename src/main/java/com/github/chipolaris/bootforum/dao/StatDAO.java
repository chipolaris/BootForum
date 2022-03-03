package com.github.chipolaris.bootforum.dao;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
	public Number countThumbnail_deprecated(Discussion discussion) {
		TypedQuery<Number> typedQuery = entityManager.createQuery("SELECT COALESCE(SUM(SIZE(c.thumbnails)), 0) FROM Comment c WHERE c.discussion = :discussion", Number.class);
		typedQuery.setParameter("discussion", discussion);
		
		return typedQuery.getSingleResult();
	}
	
	/**
	 * This method identical as the method above, using native SQL query to avoid SQL Server issue as noted above
	 */
	public Number countThumbnails(Discussion discussion) {
		
		String nativeQuery = "SELECT COUNT(1) FROM COMMENT_THUMBNAIL_T CT"
				+ " LEFT JOIN COMMENT_T C ON CT.COMMENT_ID = C.ID"
				+ " WHERE C.DISCUSSION_ID = ?1";
		
		Query query = entityManager.createNativeQuery(nativeQuery).setParameter(1, discussion.getId());
		
		/* 
		 * Note: the query above returns Long in Postgresql and BigInteger in SQL Server 
		 * So, the compromise is to downcast to Number first,
		 */
		return (Number) query.getSingleResult();
	}
	
	/**
	 * 
	 * @param discussion
	 * @return
	 * 
	 * @deprecated This method will not work with SQL Server due to SUM(SIZE()) usage
	 */
	@Deprecated
	public Number countAttachment_deprecated(Discussion discussion) {
		TypedQuery<Number> typedQuery = entityManager.createQuery("SELECT COALESCE(SUM(SIZE(c.attachments)), 0) FROM Comment c WHERE c.discussion = :discussion", Number.class);
		typedQuery.setParameter("discussion", discussion);
		
		return typedQuery.getSingleResult().longValue();
	}
	
	/**
	 * This method identical as the method above, using native SQL query to avoid SQL Server issue as noted above
	 */
	public Number countAttachments(Discussion discussion) {
		
		String nativeQuery = "SELECT COUNT(1) FROM COMMENT_ATTACHMENT_T CA"
				+ " LEFT JOIN COMMENT_T C ON CA.COMMENT_ID = C.ID"
				+ " WHERE C.DISCUSSION_ID = ?1";
		
		Query query = entityManager.createNativeQuery(nativeQuery).setParameter(1, discussion.getId());
		
		/* 
		 * Note: the query above returns Long in Postgresql and BigInteger in SQL Server 
		 * So, the compromise is to downcast to Number first
		 */
		return (Number) query.getSingleResult();
	}
	
	public Number countComment(Forum forum) {
		
		TypedQuery<Number> typedQuery = entityManager.createQuery("SELECT COUNT(c) FROM Comment c WHERE c.discussion.forum = :forum", Number.class);
		typedQuery.setParameter("forum", forum);
		
		return typedQuery.getSingleResult();
	}
	
	public Number countDiscussion(Forum forum) {
		
		TypedQuery<Number> typedQuery = entityManager.createQuery("SELECT COUNT(d) FROM Discussion d WHERE d.forum = :forum", Number.class);
		typedQuery.setParameter("forum", forum);
		
		return typedQuery.getSingleResult();
	}
	
	/**
	 * Get latest comment of the given forum
	 * @param forum
	 * @return
	 */
	public Comment latestComment(Forum forum) {
		
		String queryStr = "SELECT c FROM Comment c WHERE c.discussion.forum = :forum ORDER BY c.id DESC";
		
		TypedQuery<Comment> typedQuery = entityManager.createQuery(queryStr, Comment.class);
		typedQuery.setParameter("forum", forum);
		
		List<Comment> resultList = typedQuery.setMaxResults(1).getResultList();
		
		return resultList.isEmpty() ? null : resultList.get(0);
	}
	
	/**
	 * get commentInfo from the latest discussion (one with the latest commentDate) of the given forum
	 * @param forum
	 * @return
	 */
	public CommentInfo latestCommentInfo(Forum forum) {
		
		String queryStr = "SELECT d.stat.lastComment FROM Discussion d WHERE d.forum = :forum ORDER BY d.stat.lastComment.commentDate DESC";
		
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
		
		TypedQuery<CommentInfo> typedQuery = entityManager.createQuery("FROM CommentInfo ci ORDER BY ci.commentDate DESC", CommentInfo.class);
		
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
		
		TypedQuery<String> typedQuery = entityManager.createQuery("SELECT u.username FROM User u ORDER BY u.id DESC", String.class);
		typedQuery.setMaxResults(1);
		
		List<String> resultList = typedQuery.setMaxResults(1).getResultList();
		
		return resultList.isEmpty() ? null : resultList.get(0);
	}
	
	public Date getLastUserRegisteredDate() {
		TypedQuery<Date> typedQuery = entityManager.createQuery("SELECT u.createDate FROM User u ORDER BY u.id DESC", Date.class);
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

	public Number countComment(Discussion discussion) {

		TypedQuery<Number> typedQuery = entityManager.createQuery("SELECT COUNT(c) FROM Comment c WHERE c.discussion = :discussion", Number.class);
		typedQuery.setParameter("discussion", discussion);
		
		return typedQuery.getSingleResult();
	}
	
	public Number countComment(Discussion discussion, String username) {
		
		TypedQuery<Number> typedQuery = entityManager.createQuery("SELECT COUNT(c) FROM Comment c WHERE c.discussion = :discussion AND c.createBy = :username", Number.class);
		typedQuery.setParameter("discussion", discussion).setParameter("username", username);
		
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
	public Number countCommentThumbnails_Deprecated(String username) {
		
		TypedQuery<Number> typedQuery = entityManager.createQuery("SELECT COALESCE(SUM(SIZE(c.thumbnails)), 0) FROM Comment c WHERE c.createBy = :createBy", Number.class);
		typedQuery.setParameter("createBy", username);
		
		return typedQuery.getSingleResult();
	}
	
	/**
	 * This method identical as the method above, using native SQL query to avoid SQL Server issue as noted above
	 */
	public Number countCommentThumbnails(String username) {
		
		String nativeQuery = "SELECT COUNT(1) FROM COMMENT_THUMBNAIL_T CT"
				+ " LEFT JOIN COMMENT_T C ON CT.COMMENT_ID = C.ID"
				+ " WHERE C.CREATE_BY = ?1";
		
		Query query = entityManager.createNativeQuery(nativeQuery).setParameter(1, username);
		
		/* 
		 * Note: the query above returns Long in Postgresql and BigInteger in SQL Server 
		 * So, the compromise is to downcast to Number first, then return longValue
		 */
		return ((Number) query.getSingleResult());
	}	
	
	/**
	 *  @deprecated: this method does not work with SQL Server due to SUM(SIZE()) call
	 * @param username
	 * @return
	 * 
	 *
	 */
	@Deprecated
	public Number countCommentAttachments_Deprecated(String username) {
		
		TypedQuery<Number> typedQuery = entityManager.createQuery("SELECT COALESCE(SUM(SIZE(c.attachments)), 0) FROM Comment c WHERE c.createBy = :createBy", Number.class);
		typedQuery.setParameter("createBy", username);
		
		return typedQuery.getSingleResult();
	}
	
	/**
	 * This method identical as the method above, using native SQL query to avoid SQL Server issue as noted above
	 */
	public Number countCommentAttachments(String username) {
		
		String nativeQuery = "SELECT COUNT(1) FROM COMMENT_ATTACHMENT_T CA"
				+ " LEFT JOIN COMMENT_T C ON CA.COMMENT_ID = C.ID"
				+ " WHERE C.CREATE_BY = ?1";
		
		Query query = entityManager.createNativeQuery(nativeQuery).setParameter(1, username);
		
		/* 
		 * Note: the query above returns Long in Postgresql and BigInteger in SQL Server 
		 * So, the compromise is to downcast to Number first, then return longValue
		 */
		return (Number) query.getSingleResult();
	}
	
	public Number countComment(String username) {
		TypedQuery<Number> typedQuery = entityManager.createQuery("SELECT COUNT(c) FROM Comment c WHERE c.createBy = :username", Number.class);
		typedQuery.setParameter("username", username);
		
		return typedQuery.getSingleResult();
	}
	
	public Number countDiscussion(String username) {

		TypedQuery<Number> typedQuery = entityManager.createQuery("SELECT COUNT(d) FROM Discussion d WHERE d.createBy = :username", Number.class);
		typedQuery.setParameter("username", username);
		
		return typedQuery.getSingleResult();
	}

	public Comment getLatestComment(Discussion discussion) {
		TypedQuery<Comment> typedQuery = entityManager.createQuery("SELECT c FROM Comment c WHERE c.discussion = :discussion ORDER BY c.id DESC", Comment.class);
		typedQuery.setParameter("discussion", discussion);
		
		List<Comment> resultList = typedQuery.setMaxResults(1).getResultList();
		
		return resultList.isEmpty() ? null : resultList.get(0);
	}
	
	/**
	 * Get last comment for the given username name
	 * @param username
	 * @return
	 */
	public Comment getLatestComment(String username) {
		TypedQuery<Comment> typedQuery = entityManager.createQuery("SELECT c FROM Comment c WHERE c.createBy = :username ORDER BY c.id DESC", Comment.class);
		typedQuery.setParameter("username", username);
		
		List<Comment> resultList = typedQuery.setMaxResults(1).getResultList();
		
		return resultList.isEmpty() ? null : resultList.get(0);
	}

	/**
	 * Not used, keep here for reference
	 * Get first maxResult distinct createBy (commentor) on the given discussion
	 * @param discussion
	 * @param maxResult
	 * @return
	 */
	public List<String> getFirstCommentors(Discussion discussion, int maxResult) {
		
		TypedQuery<String> typedQuery = entityManager.createQuery("SELECT DISTINCT c.createBy FROM Comment c WHERE c.discussion = :discussion"
				+ " GROUP BY c.createBy ORDER BY min(c.id) ASC, c.createBy", String.class);
		typedQuery.setParameter("discussion", discussion);
		
		return typedQuery.setMaxResults(maxResult).getResultList();
	}
	
	public Map<String, Integer> getCommentorMap(Discussion discussion) {
		
		Map<String, Integer> commentors = new HashMap<>();
		
		Query query = entityManager.createQuery("SELECT c.createBy, count(c) FROM Comment c WHERE c.discussion = :discussion"
				+ " GROUP BY c.createBy");
		query.setParameter("discussion", discussion);
		
		@SuppressWarnings("unchecked")
		List<Object[]> resultList = query.getResultList();
		
		for(Object[] objectArray : resultList) {
			commentors.put((String)objectArray[0], ((Number)objectArray[1]).intValue());
		}
		
		return commentors;
	}
	
	public Map<String, Integer> getMostCommentsForums(Date since, Integer maxResult) {
		
		Map<String, Integer> forums = new HashMap<>();
		
		Query query = entityManager.createQuery("Select d.forum.title, COALESCE(SUM(d.stat.commentCount), 0) totalCommentCount from Discussion d"
				+ " WHERE d.createDate >= :since GROUP BY d.forum ORDER BY totalCommentCount");
		
		query.setParameter("since", since);
		
		if(maxResult != null) {
			query.setMaxResults(maxResult);
		}
		
		@SuppressWarnings("unchecked")
		List<Object[]> resultList = query.getResultList();
		
		for(Object[] objectArray : resultList) {
			forums.put((String)objectArray[0], ((Number)objectArray[1]).intValue());
		}
		
		return forums;
	}
	
	public Map<String, Integer> getMostViewsForums(Date since, Integer maxResult) {
		
		Map<String, Integer> forums = new HashMap<>();
		
		Query query = entityManager.createQuery("Select d.forum.title, COALESCE(SUM(d.stat.viewCount), 0) totalViewCount from Discussion d"
				+ " WHERE d.createDate >= :since GROUP BY d.forum ORDER BY totalViewCount");
		
		query.setParameter("since", since);
		
		if(maxResult != null) {
			query.setMaxResults(maxResult);
		}
		
		@SuppressWarnings("unchecked")
		List<Object[]> resultList = query.getResultList();
		
		for(Object[] objectArray : resultList) {
			forums.put((String)objectArray[0], ((Number)objectArray[1]).intValue());
		}
		
		return forums;
	}
	
	public Map<String, Integer> getMostCommentsTags(Date since, Integer maxResult) {
		
		Map<String, Integer> tags = new HashMap<>();
		
		Query query = entityManager.createQuery("Select t.label, COALESCE(SUM(d.stat.commentCount), 0) totalCommentCount from Discussion d,"
				+ " d.tags t WHERE d.createDate >= :since GROUP BY t ORDER BY totalCommentCount ");
		
		query.setParameter("since", since);
		
		if(maxResult != null) {
			query.setMaxResults(maxResult);
		}
		
		@SuppressWarnings("unchecked")
		List<Object[]> resultList = query.getResultList();
		
		for(Object[] objectArray : resultList) {
			tags.put((String)objectArray[0], ((Number)objectArray[1]).intValue());
		}
		
		return tags;
	}	
	
	public Map<String, Integer> getMostViewsTags(Date since, Integer maxResult) {
		
		Map<String, Integer> tags = new HashMap<>();
		
		Query query = entityManager.createQuery("Select t.label, COALESCE(SUM(d.stat.viewCount), 0) totalViewCount from Discussion d,"
				+ " d.tags t WHERE d.createDate >= :since GROUP BY t ORDER BY totalViewCount");
		
		query.setParameter("since", since);
		
		if(maxResult != null) {
			query.setMaxResults(maxResult);
		}
		
		@SuppressWarnings("unchecked")
		List<Object[]> resultList = query.getResultList();
		
		for(Object[] objectArray : resultList) {
			tags.put((String)objectArray[0], ((Number)objectArray[1]).intValue());
		}
		
		return tags;
	}
	
	public Map<String, Integer> getMostVotedUpUsers(Date since, Integer maxResult) {
		
		Map<String, Integer> users = new HashMap<>();
		
		Query query = entityManager.createQuery("SELECT c.createBy, COALESCE(SUM(c.commentVote.voteUpCount), 0) voteUpCount"
				+ " FROM Comment c WHERE c.createDate >= :since GROUP BY c.createBy ORDER BY voteUpCount DESC");
		
		query.setParameter("since", since);
		
		if(maxResult != null) {
			query.setMaxResults(maxResult);
		}
		
		@SuppressWarnings("unchecked")
		List<Object[]> resultList = query.getResultList();
		
		for(Object[] objectArray : resultList) {
			users.put((String)objectArray[0], ((Number)objectArray[1]).intValue());
		}
		
		return users;
	}
	
	public Map<String, Integer> getMostVotedDownUsers(Date since, Integer maxResult) {
		
		Map<String, Integer> users = new HashMap<>();
		
		Query query = entityManager.createQuery("SELECT c.createBy, COALESCE(SUM(c.commentVote.voteDownCount), 0) voteDownCount"
				+ " FROM Comment c WHERE c.createDate >= :since GROUP BY c.createBy ORDER BY voteDownCount DESC");
		
		query.setParameter("since", since);
		
		if(maxResult != null) {
			query.setMaxResults(maxResult);
		}
		
		@SuppressWarnings("unchecked")
		List<Object[]> resultList = query.getResultList();
		
		for(Object[] objectArray : resultList) {
			users.put((String)objectArray[0], ((Number)objectArray[1]).intValue());
		}
		
		return users;
	}
	
	public Map<String, Integer> getMostVotedUpComments(Date since, Integer maxResult) {
		
		Map<String, Integer> comments = new HashMap<>();
		
		Query query = entityManager.createQuery("Select c.title, c.commentVote.voteUpCount FROM Comment c"
				+ " WHERE c.createDate >= :since ORDER BY c.commentVote.voteUpCount DESC");
		
		query.setParameter("since", since);
		
		if(maxResult != null) {
			query.setMaxResults(maxResult);
		}
		
		@SuppressWarnings("unchecked")
		List<Object[]> resultList = query.getResultList();
		
		for(Object[] objectArray : resultList) {
			comments.put((String)objectArray[0], ((Number)objectArray[1]).intValue());
		}
		
		return comments;
	}
	
	public Map<String, Integer> getMostVotedDownComments(Date since, Integer maxResult) {
		
		Map<String, Integer> comments = new HashMap<>();
		
		Query query = entityManager.createQuery("Select c.title, c.commentVote.voteDownCount FROM Comment c"
				+ " WHERE c.createDate >= :since ORDER BY c.commentVote.voteDownCount DESC");
		
		query.setParameter("since", since);
		
		if(maxResult != null) {
			query.setMaxResults(maxResult);
		}
		
		@SuppressWarnings("unchecked")
		List<Object[]> resultList = query.getResultList();
		
		for(Object[] objectArray : resultList) {
			comments.put((String)objectArray[0], ((Number)objectArray[1]).intValue());
		}
		
		return comments;
	}
}
