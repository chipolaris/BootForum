package com.github.chipolaris.bootforum;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.context.event.EventListener;
import org.springframework.security.web.util.matcher.IpAddressMatcher;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.github.chipolaris.bootforum.domain.RemoteIPFilterOption;
import com.github.chipolaris.bootforum.domain.RemoteIPFilterOption.FilterType;
import com.github.chipolaris.bootforum.event.RemoteIPFilterOptionLoadEvent;

@Component
public class RemoteIPFilter extends OncePerRequestFilter {
	
	private Set<IpAddressMatcher> ipAddressMatchers;
	
	private RemoteIPFilterOption remoteIPFilterOption;
	
	@EventListener
	private void handleRemoteIPFilterOptionLoadEvent(RemoteIPFilterOptionLoadEvent event) {
		this.remoteIPFilterOption = event.getRemoteIPFilterOption();
		
		if(remoteIPFilterOption.getFilterType() != FilterType.NONE) {
			this.ipAddressMatchers = new HashSet<>();
			
			for(String ipAddressSubnet : remoteIPFilterOption.getIpAddresses()) {
				try {
					this.ipAddressMatchers.add(new IpAddressMatcher(ipAddressSubnet));
				} 
				catch (Exception e) {
					// not a valid IP address
				}
			}
		}
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) 
			throws IOException, ServletException {
		
		boolean allowed = true;
		
		/*
		 * If filterType is DENY, loop through to find a match
		 * if found, stop. Otherwise, not allow the request to continue
		 */
		if(this.remoteIPFilterOption.getFilterType() == FilterType.DENY) {
			
			for(IpAddressMatcher matcher : ipAddressMatchers) {
				
				if(matcher.matches(request)) {
					
					// found an DENY entry
					allowed = false;
					
					break;
				}
			}
		}
		/*
		 * If filterType is ALLOW, loop through to find a match
		 * if found, allow the request to continue. Otherwise, stop the request
		 */
		else if(this.remoteIPFilterOption.getFilterType() == FilterType.ALLOW) {
			
			allowed = false;
			
			for(IpAddressMatcher matcher : ipAddressMatchers) {
				
				if(matcher.matches(request)) {
					
					// found an ALLOW entry
					allowed = true;
					
					break;
				}
			}
		}
		
		if(allowed) {
			filterChain.doFilter(request, response);
		}
		else {
			response.sendError(HttpServletResponse.SC_FORBIDDEN);
		}
	}
}
