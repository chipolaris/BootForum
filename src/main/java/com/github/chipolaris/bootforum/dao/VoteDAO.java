package com.github.chipolaris.bootforum.dao;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.TypedQuery;

import org.springframework.stereotype.Repository;

import com.github.chipolaris.bootforum.domain.CommentVote;
import com.github.chipolaris.bootforum.domain.Vote;

@Repository
public class VoteDAO {

	@PersistenceContext
	protected EntityManager entityManager;
	
	public Vote getVote(CommentVote commentVote, String voterName) {
		
		String queryStr = "SELECT v FROM CommentVote cv, cv.votes v WHERE cv = :commentVote and v.voterName = :voterName";
		
		TypedQuery<Vote> typedQuery = entityManager.createQuery(queryStr, Vote.class);
		typedQuery.setParameter("commentVote", commentVote);
		typedQuery.setParameter("voterName", voterName);
	
		// note: it is expected to get at most 1 in the resultList
		List<Vote> resultList = typedQuery.getResultList();
	
		return resultList.isEmpty() ? null : resultList.get(0);
	}
	
	public Map<String, Long> getReputation4EveryUsers() {
		
		Map<String, Long> results = new HashMap<>();
		
		// use COALESCE(X, Y) function to handle the case when there is no vote
		String queryStr = "SELECT c.createBy, COALESCE(SUM(v.voteValue), 0) FROM Comment c, c.commentVote.votes v GROUP BY c.createBy";
		
		Query query = entityManager.createQuery(queryStr);
		
		@SuppressWarnings("unchecked")
		List<Object[]> resultList = query.getResultList();
		
		for(Object[] objArr : resultList) {
			
			results.put((String)objArr[0], (Long)(objArr[1]));
		}
		
		return results;
	}
	
	public Number getReputation4User(String username) {
		
		// use COALESCE(X, Y) function to handle the case when there is no vote
		String queryStr = "SELECT COALESCE(SUM(v.voteValue), 0) FROM Comment c, c.commentVote.votes v WHERE c.createBy = :username";
		
		TypedQuery<Number> typedQuery = entityManager.createQuery(queryStr, Number.class);
		typedQuery.setParameter("username", username);
		
		return (Number) typedQuery.getSingleResult();
	}
}
