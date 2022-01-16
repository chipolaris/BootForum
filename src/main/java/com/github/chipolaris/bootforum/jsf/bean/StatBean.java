package com.github.chipolaris.bootforum.jsf.bean;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import com.github.chipolaris.bootforum.CachingConfig;
import com.github.chipolaris.bootforum.domain.Forum;
import com.github.chipolaris.bootforum.domain.ForumStat;
import com.github.chipolaris.bootforum.service.GenericService;

// application scope JSF backing bean
@Component
public class StatBean {
	
	private static final Logger logger = LoggerFactory.getLogger(StatBean.class);

	@Resource
	private GenericService genericService;
	
	@Cacheable(value=CachingConfig.FORUM_STAT, key="#forum.stat.id")
	public ForumStat getForumStat(Forum forum) {
		logger.warn("getForumStat: " + forum.getStat().getId());
		return genericService.getEntity(ForumStat.class, forum.getStat().getId()).getDataObject();
	}
}
