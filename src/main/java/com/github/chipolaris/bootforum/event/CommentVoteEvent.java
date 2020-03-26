package com.github.chipolaris.bootforum.event;

import org.springframework.context.ApplicationEvent;

import com.github.chipolaris.bootforum.domain.Comment;

public class CommentVoteEvent extends ApplicationEvent {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private short voteValue;
	private Comment comment;

	public CommentVoteEvent(Object source, Comment comment, short voteValue) {
		super(source);
		this.setVoteValue(voteValue);
		this.setComment(comment);
	}

	public short getVoteValue() {
		return voteValue;
	}
	public void setVoteValue(short voteValue) {
		this.voteValue = voteValue;
	}

	public Comment getComment() {
		return comment;
	}
	public void setComment(Comment comment) {
		this.comment = comment;
	}
}
