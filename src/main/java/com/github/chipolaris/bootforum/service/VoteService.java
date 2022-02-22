package com.github.chipolaris.bootforum.service;

import java.util.Date;
import java.util.Map;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.github.chipolaris.bootforum.dao.GenericDAO;
import com.github.chipolaris.bootforum.dao.VoteDAO;
import com.github.chipolaris.bootforum.domain.CommentVote;
import com.github.chipolaris.bootforum.domain.Vote;

@Service 
@Transactional
public class VoteService {

	@Resource
	private VoteDAO voteDAO;
	
	@Resource
	private GenericDAO genericDAO;
	
	@Transactional(readOnly = false) 
	public ServiceResponse<Void> registerCommentVote(CommentVote commentVote, String voterName, short voteValue) {
		
		ServiceResponse<Void> response = new ServiceResponse<>();
		
		Vote vote = voteDAO.getVote(commentVote, voterName);
		if(vote == null) {
			
			vote = new Vote();
			vote.setVoterName(voterName);
			vote.setVoteValue(voteValue);
			
			genericDAO.persist(vote);
			
			// update commentVote
			commentVote.getVotes().add(vote);
			if(voteValue == 1) {
				commentVote.setVoteUpCount(commentVote.getVoteUpCount() + 1);
			}
			else if(voteValue == -1) {
				commentVote.setVoteDownCount(commentVote.getVoteDownCount() + 1);
			}
			
			genericDAO.merge(commentVote);
			
			response.addMessage("Vote on comment registered successfully for voter " + voterName);
		}
		else {
			response.addMessage("Voter already voted on the comment");
			response.setAckCode(AckCodeType.FAILURE);
		}
		
		return response;
	}
	
	@Transactional(readOnly = true)
	public ServiceResponse<Map<String, Long>> getReputation4AllUsers() {
	
		ServiceResponse<Map<String, Long>> response = new ServiceResponse<>();
		
		response.setDataObject(voteDAO.getReputation4AllUsers());
		
		return response;
	}
	
	@Transactional(readOnly = true)
	public ServiceResponse<Map<String, Integer>> getMostReputationUsers(Date since, Integer maxResult) {
		
		ServiceResponse<Map<String, Integer>> response = new ServiceResponse<>();
		
		response.setDataObject(voteDAO.getTopReputationUsers(since, maxResult));
		
		return response;
	}
}
