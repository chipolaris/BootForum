package com.github.chipolaris.bootforum;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.github.benmanes.caffeine.cache.Caffeine;

@Configuration
@EnableCaching
public class CachingConfig {
	
	public static final String DISCUSSION_BREAD_CRUMB = "discussionBreadCrumbCache";
	public static final String FORUM_BREAD_CRUMB = "forumBreadCrumbCache";
	public static final String AVATAR_BASE_64 = "avatarBase64Cache";
	public static final String USER_STAT = "userStatCache";
	public static final String FORUM_STAT = "forumStatCache";
	public static final String AVATAR_EXISTS = "avatarExists";
	public static final String ACTIVE_TAGS = "activeTags";
	public static final String DISCCUSIONS_FOR_TAG = "discussionForTag";
	public static final String DISCCUSIONS = "discussionCache";
	
    @Bean
    public CacheManager cacheManager() {
    	
    	/**
		 * ConcurrentMapCacheManager is the simple built-in cache implementation from Spring
    	 */
        // return new ConcurrentMapCacheManager(DISCUSSION_BREAD_CRUMB, FORUM_BREAD_CRUMB, AVATAR_BASE_64);
    	
    	
    	/**
    	 * CaffeineCacheManager is the wrapper around Caffeine cache implementation
    	 * 
    	 * <!-- https://mvnrepository.com/artifact/com.github.ben-manes.caffeine/caffeine -->
		 *	<dependency>
		 *	    <groupId>com.github.ben-manes.caffeine</groupId>
		 *	    <artifactId>caffeine</artifactId>
		 *	</dependency>
		 *
    	 */
    	
    	// discussionBreadCrumbCache
    	CaffeineCache discussionBreadCrumbCache = 
    			buildAccessBasedCache(DISCUSSION_BREAD_CRUMB, discussionBreadCrumbMaximumSize, discussionBreadCrumbExpireAfterAccessMinutes);
    	
    	// discussionBreadCrumbCache
    	CaffeineCache forumBreadCrumbCache = 
    			buildAccessBasedCache(FORUM_BREAD_CRUMB, forumBreadCrumbMaximumSize, forumBreadCrumbExpireAfterAccessMinutes);
    	
    	// forumBreadCrumbCache
    	CaffeineCache avatarBase64Cache = 
    			buildAccessBasedCache(AVATAR_BASE_64, avatarBase64MaximumSize, avatarBase64ExpireAfterAccessMinutes);
    	
    	// avatarExists
    	CaffeineCache avatarExistsCache =
    			buildAccessBasedCache(AVATAR_EXISTS, 1000, 60);
    	
    	// userStatCache
    	CaffeineCache userStatCache = 
    			buildAccessBasedCache(USER_STAT, userStatMaximumSize, userStatExpireAfterAccessMinutes);
    	
    	// forumStatCache
    	CaffeineCache forumStatCache = 
    			buildAccessBasedCache(FORUM_STAT, forumStatMaximumSize, forumStatExpireAfterWriteMinutes);
    	
    	// activeTags, a list of active tags, no expiration
    	CaffeineCache activeTagsCache = buildTimeBasedCache(ACTIVE_TAGS, 1, Integer.MAX_VALUE);
    	
    	// discussionsForTag
    	CaffeineCache discussionsForTagCache = buildTimeBasedCache(DISCCUSIONS_FOR_TAG, 100, Integer.MAX_VALUE);
    	
    	// discussionCache, used to mainly serve the home/welcome page, expire 5 minutes after write, 
    	// TODO: make the expire value configurable
    	CaffeineCache discussionsCache = buildTimeBasedCache(DISCCUSIONS, 10, 5);
    	
    	SimpleCacheManager cacheManager = new SimpleCacheManager();
    	cacheManager.setCaches(Arrays.asList(discussionBreadCrumbCache, forumBreadCrumbCache, 
    			avatarBase64Cache, avatarExistsCache, userStatCache, forumStatCache, activeTagsCache, discussionsForTagCache, discussionsCache));
    	
    	return cacheManager;
    }
    
    private CaffeineCache buildAccessBasedCache(String name, int maximumSize, int expireAfterAccessMinute) {
        return new CaffeineCache(name, Caffeine.newBuilder()
        			.maximumSize(maximumSize)
                    .expireAfterAccess(expireAfterAccessMinute, TimeUnit.MINUTES)
                    .build());
    }
    
    private CaffeineCache buildTimeBasedCache(String name, int maximumSize, int expireAfterWriteMinute) {
        return new CaffeineCache(name, Caffeine.newBuilder()
        			.maximumSize(maximumSize)
                    .expireAfterWrite(expireAfterWriteMinute, TimeUnit.MINUTES)
                    .build());
    }
    
    /**
     * injected properties from application.properties file
     */
    @Value("${Cache.discussionBreadCrumb.maximumSize}")
    private Integer discussionBreadCrumbMaximumSize;
    
    @Value("${Cache.discussionBreadCrumb.expireAfterAccessMinutes}")
    private Integer discussionBreadCrumbExpireAfterAccessMinutes;
    
    @Value("${Cache.forumBreadCrumb.maximumSize}")
    private Integer forumBreadCrumbMaximumSize;
    
    @Value("${Cache.forumBreadCrumb.expireAfterAccessMinutes}")
    private Integer forumBreadCrumbExpireAfterAccessMinutes;
    
    @Value("${Cache.avatarBase64.maximumSize}")
    private Integer avatarBase64MaximumSize;
    
    @Value("${Cache.avatarBase64.expireAfterAccessMinutes}")
    private Integer avatarBase64ExpireAfterAccessMinutes;
    
    @Value("${Cache.userStat.maximumSize}")
    private Integer userStatMaximumSize;
    
    @Value("${Cache.userStat.expireAfterAccessMinutes}")
    private Integer userStatExpireAfterAccessMinutes;
    
    @Value("${Cache.forumStat.maximumSize}")
    private Integer forumStatMaximumSize;
    
    @Value("${Cache.forumStat.expireAfterWriteMinutes}")
    private Integer forumStatExpireAfterWriteMinutes;
}
