package com.github.chipolaris.bootforum.dao;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.TypedQuery;

import org.springframework.stereotype.Repository;

import com.github.chipolaris.bootforum.domain.Discussion;
import com.github.chipolaris.bootforum.domain.Forum;
import com.github.chipolaris.bootforum.domain.Tag;

@Repository
public class DiscussionDAO {

	@PersistenceContext
	private EntityManager entityManager;
	
	public List<Discussion> findByTag(Tag tag, Integer startPosition, Integer maxResult, String sortField, Boolean descending) {
		
		String queryStr = "FROM Discussion d WHERE :tag MEMBER OF d.tags";
		
		if(sortField != null && descending != null) {
			queryStr += " ORDER BY d." + sortField + (descending ? " DESC" : " ASC");
		}
		else {
			queryStr += " ORDER BY d.createDate DESC";
		}
		
		TypedQuery<Discussion> typedQuery = entityManager.createQuery(queryStr, Discussion.class);
		typedQuery.setParameter("tag", tag);
		
		if(startPosition != null) {
			typedQuery.setFirstResult(startPosition);
		}
		if(maxResult != null) {
			typedQuery.setMaxResults(maxResult);
		}
		
		return typedQuery.getResultList();
	}

	/*
	 * Note: 
	 * 
	 * The following JPQL query:
	 * 		"SELECT COALESCE(SUM(SIZE(d.comments)), 0) FROM Discussion d WHERE :tag MEMBER OF d.tags"
	 * 
	 * Will work fine in Postgresql and H2, but not in SQL Server.
	 * 
	 * In SQL Server, it Would result in the error:
	 *
	 *	com.microsoft.sqlserver.jdbc.SQLServerException: Cannot perform an aggregate function on an expression containing
	 *	 an aggregate or a subquery. Error Code: 130 Call: 
	 *	 		SELECT COALESCE(SUM((SELECT COUNT(t3.ID) FROM COMMENT_T t3 WHERE (t3.DISCUSSION_ID = t0.ID))),? ) 
	 *			FROM DISCUSSION_T t0, DISCUSSION_TAG_T t2, TAG_T t1 WHERE ((? = t1.ID) 
	 *			AND ((t2.DISCUSSION_ID = t0.ID) AND (t1.ID = t2.TAG_ID))) 
	 *
	 * So the work around for now is to use a native SQL query
	 */
	public Long countCommentsForTag(Tag tag) {
		
		String queryStr = "SELECT COALESCE(SUM(SIZE(d.comments)), 0) FROM Discussion d WHERE :tag MEMBER OF d.tags";
		
		TypedQuery<BigDecimal> typedQuery = entityManager.createQuery(queryStr, BigDecimal.class);
		typedQuery.setParameter("tag", tag);
		
		return typedQuery.getSingleResult().longValue();
	}
	
	/**
	 * This method identical as the method above, using native SQL query to avoid SQL Server issue as noted above
	 */
	public Long countCommentsForTag(Long tagId) {
		
		String nativeQuery = "SELECT COUNT(1) FROM COMMENT_T C"
				+ " LEFT JOIN DISCUSSION_TAG_T DT ON DT.DISCUSSION_ID = C.DISCUSSION_ID"
				+ " WHERE DT.TAG_ID = ?1";
		
		Query query = entityManager.createNativeQuery(nativeQuery).setParameter(1, tagId);
		
		/* 
		 * Note: the query above returns Long in Postgresql and BigInteger in SQL Server 
		 * So, the compromise is to downcast to Number first, then return longValue
		 */
		return ((Number) query.getSingleResult()).longValue();
	}
	
	public Long countDiscusionsForTag(Tag tag) {
		
		String queryStr = "SELECT COUNT(d) FROM Discussion d WHERE :tag MEMBER OF d.tags";
		
		TypedQuery<Long> typedQuery = entityManager.createQuery(queryStr, Long.class);
		typedQuery.setParameter("tag", tag);
		
		return typedQuery.getSingleResult();
	}

	public List<Discussion> getLatestDiscussions(Integer maxResult) {

		String queryStr = "FROM Discussion d ORDER BY d.createDate DESC";
		
		TypedQuery<Discussion> typedQuery = entityManager.createQuery(queryStr, Discussion.class);
		
		if(maxResult != null) {
			typedQuery.setMaxResults(maxResult);
		}
		
		return typedQuery.getResultList();
	}
	
	public List<Discussion> getMostViewsDiscussions(Date since, Integer maxResult) {

		String queryStr = "FROM Discussion d WHERE d.createDate >= :since ORDER BY d.stat.viewCount DESC";
		
		TypedQuery<Discussion> typedQuery = entityManager.createQuery(queryStr, Discussion.class);
		
		typedQuery.setParameter("since", since);
		
		if(maxResult != null) {
			typedQuery.setMaxResults(maxResult);
		}
		
		return typedQuery.getResultList();
	}
	
	public List<Discussion> getMostCommentsDiscussions(Date since, Integer maxResult) {

		String queryStr = "FROM Discussion d WHERE d.createDate >= :since ORDER BY d.stat.commentCount DESC";
		
		TypedQuery<Discussion> typedQuery = entityManager.createQuery(queryStr, Discussion.class);
		
		typedQuery.setParameter("since", since);
		
		if(maxResult != null) {
			typedQuery.setMaxResults(maxResult);
		}
		
		return typedQuery.getResultList();
	}

	public Integer assignForum(List<Discussion> discussions, Forum forum) {

		String queryStr = "UPDATE Discussion d SET d.forum = :forum WHERE d in :discussions";
		
		Query query = entityManager.createQuery(queryStr);
		
		return query.setParameter("forum", forum).setParameter("discussions", discussions).executeUpdate();
	}
}
