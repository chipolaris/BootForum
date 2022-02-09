package com.github.chipolaris.bootforum.jsf.bean;

import java.util.Set;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.github.chipolaris.bootforum.domain.Comment;
import com.github.chipolaris.bootforum.domain.DisplayOption;
import com.github.chipolaris.bootforum.domain.EmailOption;
import com.github.chipolaris.bootforum.domain.RegistrationOption;
import com.github.chipolaris.bootforum.domain.RemoteIPFilterOption;
import com.github.chipolaris.bootforum.jsf.util.JSFUtils;
import com.github.chipolaris.bootforum.service.AckCodeType;
import com.github.chipolaris.bootforum.service.GenericService;
import com.github.chipolaris.bootforum.service.IndexService;
import com.github.chipolaris.bootforum.service.ServiceResponse;
import com.github.chipolaris.bootforum.service.StatService;
import com.github.chipolaris.bootforum.service.SystemConfigService;
import com.github.chipolaris.bootforum.service.SystemInfoService;

@Component
@Scope("application")
public class SystemMaintenance {

	@SuppressWarnings("unused")
	private static final Logger logger = LoggerFactory.getLogger(SystemMaintenance.class);
	
	@Resource
	private GenericService genericService;
	
	@Resource
	private IndexService indexService;
	
	@Resource
	private SystemConfigService systemConfigService;
	
	@Resource
	private SystemInfoService systemInfoService;
	
	@Resource
	private StatService statService;

	
	public SystemInfoService.Statistics getStatistics() {
		return systemInfoService.getStatistics().getDataObject();
	}
	
    public int getSessionCount() {
        return systemInfoService.getSessionCount().getDataObject();
    }
    
	public Set<String> getLoggedOnUsers() {
		return systemInfoService.getLoggedOnUsers().getDataObject();
	}
	
	public Boolean isUserLoggedOn(String username) {
		
		return systemInfoService.isUserLoggedOn(username).getDataObject();
	}
	
	public DisplayOption getDisplayOption() {
		
		return systemConfigService.getDisplayOption().getDataObject();
	}

	public EmailOption getEmailOption() {
		
		return systemConfigService.getEmailOption().getDataObject();
	}
	
	public RegistrationOption getRegistrationOption() {
		
		return systemConfigService.getRegistrationOption().getDataObject();
	}
	
	public RemoteIPFilterOption getRemoteIPFilterOption() {
		
		return systemConfigService.getRemoteIPFilterOption().getDataObject();
	}
	
	/**
	 * synch System Stat
	 */
	public void synchSystemStatistics() {
		systemInfoService.refreshStatistics();
		
		JSFUtils.addInfoStringMessage("systemStatForm:synchSystemStatButton", "System statistics synchronization completed");
	}
	
	/**
	 * synchForumStats
	 */
	public void synchAllStatistics() {
		
		statService.synchForumStats();
		
		statService.synchDiscussionStats();
		  
		statService.synchUserStats();
				
		JSFUtils.addInfoStringMessage("forumStatForm:synchForumStatButton", "Forum statistics synchronization completed");
	}
	
	public void synchCommentIndex() {
		
		ServiceResponse<Void> clearCommentIndexResponse = indexService.clearCommentIndex(true);
		
		if(clearCommentIndexResponse.getAckCode() != AckCodeType.SUCCESS) {
			
			JSFUtils.addErrorStringMessage("commentIndexForm:synchCommentIndexButton", 
					"Unable to clear existing comment data to re-index");
		}
		else {
			ServiceResponse<Void> indexCommentStreamResponse = 
					indexService.indexCommentStream(genericService.streamEntities(Comment.class).getDataObject());
				
			if(indexCommentStreamResponse.getAckCode() == AckCodeType.SUCCESS) {
				JSFUtils.addInfoStringMessage("commentIndexForm:synchCommentIndexButton", "Comment data re-indexed");
			}
			else {
				JSFUtils.addErrorStringMessage("commentIndexForm:synchCommentIndexButton", "Unable to re-index comment data");
			}			
		}
	}
}
