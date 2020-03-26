package com.github.chipolaris.bootforum.dao;

import java.util.List;
import java.util.Set;
import java.util.TreeMap;

import javax.annotation.PostConstruct;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Tuple;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import com.github.chipolaris.bootforum.domain.User;

@Repository
public class UserDAO {

	private static final Logger logger = LoggerFactory.getLogger(UserDAO.class);
	
	@PersistenceContext
	protected EntityManager entityManager;
	
	/*
	 * Design decision: use in-memory cache for [username <-> email] bi-direction map
	 * to enhance performance for the following functionalities:
	 * 		- listing all usernames
	 * 		- listing all emails
	 *		- is username exists
	 *		- is email exists 
	 *		- get email for username
	 *		- get username for email
	 *
	 *	The cache need to be updated when:
	 *		- new user is registered
	 *		- a user is deleted
	 *		- existing user.person is updated (which might include change in email)
	 */ 
	private BiMap<String, String> username2EmailMap; 
	
	@PostConstruct
	private void postConstruct() {
		buildCache();
		logger.info("UserDAO initialized");
	}
	
	/**
	 * search usernames using wildcard search
	 * @param userStr
	 * @return
	 */
	public List<String> searchUsernames(String userStr) {
		
		String queryStr = "SELECT usr.username FROM User usr WHERE usr.username LIKE :userStr";
		
		TypedQuery<String> typedQuery = entityManager.createQuery(queryStr, String.class);
		typedQuery.setParameter("userStr", "%" + userStr + "%");
		
		return typedQuery.getResultList();
	}
	
	private void buildCache() {
		
		/*
		String queryStr = "SELECT usr.username, usr.person.email FROM User usr";

		TypedQuery<Tuple> query = entityManager.createQuery(queryStr, Tuple.class);

		List<Tuple> resultList = query.getResultList();
		  
		username2EmailMap = new BiMap<>();
		for (Tuple tuple : resultList) {
			username2EmailMap.put(tuple.get(0, String.class), tuple.get(1, String.class));
		}
		*/
		
		/*
		 * sample code from https://www.logicbig.com/tutorials/java-ee-tutorial/jpa/criteria-api-tuple.html
		 */
		
		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();

		CriteriaQuery<Tuple> query = criteriaBuilder.createTupleQuery();
		Root<User> user = query.from(User.class);

		// the following two lines produce the same result: (also mentioned in the URL link above)
		/* query.select(criteriaBuilder.tuple(user.get("username"), user.get("person").get("email"))); */
		query.multiselect(user.get("username"), user.get("person").get("email"));

		List<Tuple> resultList = entityManager.createQuery(query).getResultList();
		
		username2EmailMap = new BiMap<>();
		for (Tuple tuple : resultList) {
			username2EmailMap.put(tuple.get(0, String.class), tuple.get(1, String.class));
		}
		 
	}
	
	/**
	 * determine if username exists
	 * @param username
	 * @return
	 */
	// Note: used in:
	// 		PrivateMessageService#createMessage
	// 		UserService#checkUsernameExist
	// 		UserService#validateUser
	public boolean usernameExists(String username) {
		
		return username2EmailMap.containsKey(username);
	}
	
	/**
	 * determine if email exists
	 * @param email
	 * @return
	 */
	// Note: used in:
	// 		PasswordResetService#sendPasswordResetEmail 
	// 		UserService#checkEmailExist, 
	// 		UserService#validateUser
	public boolean emailExists(String email) {
		
		return username2EmailMap.containsValue(email);
	}
	
	/**
	 * return email for username
	 * @param username
	 * @return
	 */
	// Note: not used in the project
	public String getEmailForUsername(String username) {
		
		return username2EmailMap.get(username);
	}
	
	/**
	 * return username for email
	 * @param email
	 * @return
	 */
	// Note: used in PasswordService#emailPasswordReset
	public String getUsernameForEmail(String email) {
		
		return username2EmailMap.getKey(email);
	}
	
	// used in UserService#addUser(User)
	public void createUser(User user) {
		
		entityManager.persist(user);
		username2EmailMap.put(user.getUsername(), user.getPerson().getEmail());
	}
	
	public void deleteUser(User user) {
		entityManager.remove(user);
		username2EmailMap.remove(user.getUsername());
	}
	
	// update personInfo, make sure not to update user itself since the user contain 
	// password that might not have been properly encrypted
	// Note: used in UserService#updatePersonalInfo(User user)
	public void updateUserPersonInfo(User user) {
		
		entityManager.merge(user.getPerson());
		
		// remove, then re-add the username/email pair from cache map since the email might get updated
		username2EmailMap.remove(user.getUsername());
		username2EmailMap.put(user.getUsername(), user.getPerson().getEmail());
	}
	
	/**
	 * get all usernames in the system
	 * @return
	 */
	// used in UserService#getAllUsernames(
	public Set<String> getAllUsernames() {
		
		return username2EmailMap.keySet();
	}
	
	/**
	 * get all emails in the system
	 * @return
	 */
	// Note: used in UserService#getAllEmails
	public Set<String> getAllEmails() {
		
		return username2EmailMap.valueSet();
	}
	
	/**
	 * 
	 * A simple bi-directional map used to store username/email pair
	 * 
	 * Modified from (original) code: https://stackoverflow.com/questions/9783020/bidirectional-map
	 *
	 * @param <K>
	 * @param <V>
	 */
	class BiMap<K,V> {

		// Note: use TreeMap instead of HashMap to have the key ordered
	    TreeMap<K, V> map = new TreeMap<K, V>();
	    TreeMap<V, K> inversedMap = new TreeMap<V, K>();

	    void put(K k, V v) {
	        map.put(k, v);
	        inversedMap.put(v, k);
	    }

	    public void remove(K k) {
			
			V v = map.remove(k);
			if(v != null) inversedMap.remove(v);
		}

		V get(K k) {
	        return map.get(k);
	    }

	    K getKey(V v) {
	        return inversedMap.get(v);
	    }
	    
	    boolean containsKey(K k) {
	    	return map.containsKey(k);
	    }
	    
	    boolean containsValue(V v) {
	    	return inversedMap.containsKey(v);
	    }
	    
	    Set<K> keySet() {
	    	return map.keySet();
	    }
	    
	    Set<V> valueSet() {
	    	return inversedMap.keySet();
	    }
	}
}
