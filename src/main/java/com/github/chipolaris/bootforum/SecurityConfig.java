package com.github.chipolaris.bootforum;

import javax.annotation.Resource;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;

@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {
    
	@Value("${RememberMe.tokenValiditySeconds}")
	private Integer rememberMeTokenValiditySeconds;
	
	@Value("${RememberMe.tokenHashKey}")
	private String rememberMeTokenHashKey;
	
	@Value("${RememberMe.requestParameter}")
	private String rememberMeRequestParameter;
	
	@Bean
	public PasswordEncoder passwordEncoder() {
	    return new BCryptPasswordEncoder();
	}
	
	/*
	 * note: AppUserDetailsService implements UserDetailService. 
	 * 		AppUserDetailsService is marked with @Transactional annotation
	 * 
	 * 		--> must use the interface here: UserDetailsService
	 * 
	 * 		otherwise, would get bean-not-of-type error: 
	 * 			http://stackoverflow.com/questions/6586727/spring-error-beannotofrequiredtypeexception
	 */
    @Resource(name="appUserDetailsService")
    private UserDetailsService userDetailService;
    
    @Resource
    public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
    	
        auth.userDetailsService(userDetailService).passwordEncoder(passwordEncoder());
    }
    
    @Override
    public void configure(WebSecurity web) throws Exception {
      web
        .ignoring()
           .antMatchers("/resources/**");
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
      http
      	.csrf().disable() // see this thread: http://stackoverflow.com/questions/20602436/in-spring-security-with-java-config-why-does-httpbasic-post-want-csrf-token
        .authorizeRequests()
        .antMatchers("/admin/**").hasRole("ADMIN")
        .antMatchers("/secure/**", "/chat/**", "/stomp-connect/**").hasAnyRole("ADMIN","USER")        
        .antMatchers("/**").permitAll()
        .anyRequest().authenticated()
        .and()
/*      .requiresChannel().anyRequest().requiresSecure() // require https
      .and()*/
      .formLogin()
          .loginPage("/login.xhtml")
          .usernameParameter("username") // username is default since SS v.3.2
          .passwordParameter("password")  // password is default since SS v.3.2
          .loginProcessingUrl("/login") // default since SS v.3.2
          .failureUrl("/login.xhtml?login_error=1" )
          .defaultSuccessUrl("/secure/index.xhtml", false)
          .permitAll()
          .and()
       .rememberMe().key(rememberMeTokenHashKey).rememberMeParameter(rememberMeRequestParameter).tokenValiditySeconds(rememberMeTokenValiditySeconds)
       	  .and()
       .logout()
       	  .logoutUrl("/logout") // default since SS v.3.2
       	  .logoutSuccessUrl("/login.xhtml?logout_success=1")
       	  .deleteCookies("JSESSIONID")
       	  .permitAll()
       	  // the following 2 lines added 11/30/2014 to facilitating retrieving all logged in users through Spring Security:
       	  // http://stackoverflow.com/questions/11271449/how-can-i-have-list-of-all-users-logged-in-via-spring-secuirty-my-web-applicat
       	  .and()
       .sessionManagement().maximumSessions(1).sessionRegistry(new SessionRegistryImpl()); 
    }
}