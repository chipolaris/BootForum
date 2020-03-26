package com.github.chipolaris.bootforum.security;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Resource;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.github.chipolaris.bootforum.dao.GenericDAO;
import com.github.chipolaris.bootforum.domain.User;

@Service
@Transactional
public class AppUserDetailsService implements UserDetailsService {

	@Resource
	private GenericDAO genericDAO;
	
	@Override
	@Transactional(readOnly=true)
	public UserDetails loadUserByUsername(String username)
			throws UsernameNotFoundException {
		
		Map<String, Object> equalAttrs = new HashMap<String, Object>();
		equalAttrs.put("username", username);
		
		User user = genericDAO.getEntity(User.class, equalAttrs);
		
		if(user == null) {
			throw new UsernameNotFoundException("Can't find username: " + username);
		}
		
		return new AppUserDetails(user);
	}
}
