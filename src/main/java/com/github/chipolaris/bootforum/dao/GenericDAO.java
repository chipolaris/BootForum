package com.github.chipolaris.bootforum.dao;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaDelete;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.springframework.stereotype.Repository;

import com.github.chipolaris.bootforum.dao.SortSpec.Direction;
import com.github.chipolaris.bootforum.domain.BaseEntity;

/**
 * A generic DAO class to use in basic CRUD operations
 * for any entity
 */
@Repository
public class GenericDAO {
	
	@PersistenceContext//(unitName = "BootForumPersistenceUnit")
	protected EntityManager entityManager;
	
	/**
	 * Save an entity
	 * @param entity
	 */
    public void persist(Object entity) { 
    	entityManager.persist(entity);
    } 

    /**
     * Delete an entity
     * @param entity
     */
    public void remove(Object entity) { 
    	Object toBeDeleted = entityManager.merge(entity);
    	entityManager.remove(toBeDeleted);
    } 
    
    /**
     * Update an entity
     * @param entity
     * @return
     */
    public <E> E merge(E entity) { 
        return entityManager.merge(entity); 
    } 
    
    /**
     * Refresh an entity, useful to make
     * sure the data is the latest
     * @param entity
     */
    public void refresh(Object entity) { 
    	entityManager.refresh(entity); 
    }
    
    /**
     * Synchronize the persistence context to the underlying database.
 	 * Seldom used. But useful when need to explicitly write cached 
 	 * data to database. Noted the write will not be committed until
 	 * the commit is issued (e.g., by the transaction manager)
     */
    public void flush() {
    	entityManager.flush();
    }
    
    /**
     * Find the entity given the entityType (class name, and ID)
     * @param entityClass
     * @param id
     * @return
     */
	public <E> E find(Class<E> entityClass, Object id) { 
        return entityManager.find(entityClass, id); 
    }

    /** TODO: candidate for removal
     * Find the entity given the entityType (class name, and ID)
     * 	Also eager fetch the specified properties, this is useful if
     *  the properties was configured as lazy fetched
     *  
     * @param <E>
     * @param entityClass
     * @param id
     * @param eagerFetchProperties
     * @return
     */
    public <E> E findById(Class<E> entityClass, Object id, List<String> eagerFetchProperties) {
		
    	CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
    	CriteriaQuery<E> criteriaQuery = criteriaBuilder.createQuery(entityClass);
    	
    	Root<E> entity = criteriaQuery.from(entityClass);
    	criteriaQuery.select(entity);
    	
    	Predicate condition = criteriaBuilder.equal(entity.get("id"), id);
    	
    	criteriaQuery.where(condition);
    	
    	// fetch specified properties
    	for(String property : eagerFetchProperties) {
    		entity.fetch(property, JoinType.LEFT);
    	}
    	
    	TypedQuery<E> typedQuery = entityManager.createQuery(criteriaQuery);
    	
    	List<E> resultList = typedQuery.getResultList();
    	
    	if(resultList.isEmpty()) {
    		return null;
    	}
    	
    	return resultList.get(0);
    }
    
    /**
     * Find all entities of the given entity type
     * @param entityClass
     * @return
     */
	public <E> List<E> findAll(Class<E> entityClass) {
    	TypedQuery<E> query = entityManager.createQuery("SELECT e FROM " + entityClass.getSimpleName() + " e", entityClass);
        return query.getResultList();
    }
	
	/**
	 * Find all entities of the given entity type
	 * Also eager fetch the specified properties
	 * @param <E>
	 * @param entityClass
	 * @param joinFetchProperties
	 * @return
	 */
    public <E> List<E> findAll(Class<E> entityClass, List<String> joinFetchProperties) {
		
    	CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
    	CriteriaQuery<E> criteriaQuery = criteriaBuilder.createQuery(entityClass);
    	
    	Root<E> entity = criteriaQuery.from(entityClass);
    	
    	// fetch specified properties
    	for(String property : joinFetchProperties) {
    		entity.fetch(property, JoinType.LEFT);
    	}
    	
    	TypedQuery<E> typedQuery = entityManager.createQuery(criteriaQuery);
    	
    	return typedQuery.getResultList();
    }
    
    /** TODO: candidate for removal
     * Get entities of the given entityClass and 
     * within the pagination defined by the startPosition and the maxResult
     * 
     * @param <E>
     * @param entityClass
     * @param startPosition
     * @param maxResult
     * @return
     */
    public <E> List<E> findEntitiesInBatch(Class<E> entityClass, int startPosition, int maxResult) {
    	
    	TypedQuery<E> query = entityManager.createQuery("SELECT e FROM " 
    			+ entityClass.getSimpleName() + " e order by e.id desc", entityClass);
    	
    	query.setFirstResult(startPosition);
    	query.setMaxResults(maxResult);
    	
    	return query.getResultList();
    }
    
    /** TODO: candidate for removal
     * Get entities of the given entityClass and 
     * within the pagination defined by the startPosition and the maxResult
     * Also eager fetch the specified properties
     * 
     * Also note that this method uses Criteria Query instead of JPQL
     * 
     * @param <E>
     * @param entityClass
     * @param startPosition
     * @param maxResult
     * @param eagerFetchProperties
     * @return
     */
    public <E> List<E> findEntitiesInBatch(Class<E> entityClass, int startPosition, 
    		int maxResult, List<String> eagerFetchProperties) {
    	
    	CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
    	CriteriaQuery<E> criteriaQuery = criteriaBuilder.createQuery(entityClass);
    	
    	Root<E> entity = criteriaQuery.from(entityClass);
    	
    	// order by id desc
    	criteriaQuery.orderBy(criteriaBuilder.desc(entity.get("id")));
    	
    	// fetch specified properties
    	for(String property : eagerFetchProperties) {
    		entity.fetch(property, JoinType.LEFT);
    	}
    	
    	TypedQuery<E> typedQuery = entityManager.createQuery(criteriaQuery);
    	
    	typedQuery.setFirstResult(startPosition);
    	typedQuery.setMaxResults(maxResult);
    	
    	return typedQuery.getResultList();
    }
    
    /**
     * Count all entities of the given entity type
     * @param entityClass
     * @return
     */
	public <E> long countEntities(Class<E> entityClass) {
    	TypedQuery<Long> typedQuery = entityManager.createQuery("SELECT count(e) FROM " + entityClass.getSimpleName() + " e", Long.class);
    	return typedQuery.getSingleResult();
    }
	
	/**
	 * 
	 * @param <E>
	 * @param entityClass
	 * @param filters
	 * @return
	 */
	public <E> E getEntity(Class<E> entityClass,
			Map<String, Object> filters) {
		
		CriteriaBuilder builder = entityManager.getCriteriaBuilder();
		CriteriaQuery<E> query = builder.createQuery(entityClass);

		Root<E> root = query.from(entityClass);
		query.select(root);
		
		Predicate[] predicates = buildPredicates(builder, root, filters);
		query.where(predicates);
		
		TypedQuery<E> typedQuery = entityManager.createQuery(query);
		typedQuery.setMaxResults(1);
		
		List<E> resultList = typedQuery.getResultList();
		
		if(resultList.isEmpty()) {
			return null;
		}
		
		return resultList.get(0);
	}

	/**
	 * TODO: candidate for removal
	 * @param <E>
	 * @param entityClass
	 * @param equalAttrs
	 * @param eagerFetchProperties: attributes to eager fetch
	 * @return
	 */
	public <E> E getEntity(Class<E> entityClass,
			Map<String, Object> equalAttrs, List<String> eagerFetchProperties) {
		
		CriteriaBuilder builder = entityManager.getCriteriaBuilder();
		CriteriaQuery<E> query = builder.createQuery(entityClass);

		Root<E> obj = query.from(entityClass);
		query.select(obj);
		
		for(String property : eagerFetchProperties) {
			obj.fetch(property, JoinType.LEFT);
		}
		
		List<Predicate> predicateList = new ArrayList<Predicate>();
		for (String paramName : equalAttrs.keySet()) {
			Object value = equalAttrs.get(paramName);
			Predicate equalPredicate = builder.equal(getPathGeneric(obj, paramName), value);
			predicateList.add(equalPredicate);
		}
		Predicate[] predicates = new Predicate[predicateList.size()];
		predicateList.toArray(predicates);
		query.where(predicates);
		
		TypedQuery<E> typedQuery = entityManager.createQuery(query);
		typedQuery.setMaxResults(1);
		
		List<E> resultList = typedQuery.getResultList();
		
		if(resultList.isEmpty()) {
			return null;
		}
		
		return resultList.get(0);
	}
	
	/**
	 * 
	 * @param <T>
	 * @param <R>
	 * @param targetClass
	 * @param targetPath
	 * @param entityClass
	 * @param filters
	 * @return
	 */
	public <T, R> List<T> getEntities(Class<T> targetClass, String targetPath, 
			Class<R> entityClass, Map<String, Object> filters) {
		
		CriteriaBuilder builder = entityManager.getCriteriaBuilder();
		CriteriaQuery<T> query = builder.createQuery(targetClass);

		Root<R> root = query.from(entityClass);
		query.select(getPathGeneric(root, targetPath));
		
		Predicate[] predicates = buildPredicates(builder, root, filters);
		query.where(predicates);
		
		return entityManager.createQuery(query).getResultList();
	}
	
	/**
	 * Get all entities of the given type and have attributes
	 * fit the filters parameter
	 * @param <E>
	 * @param entityClass
	 * @param filters
	 * @return
	 */
	public <E> List<E> getEntities(Class<E> entityClass,
			Map<String, Object> filters) {
		
		CriteriaBuilder builder = entityManager.getCriteriaBuilder();
		CriteriaQuery<E> query = builder.createQuery(entityClass);

		Root<E> root = query.from(entityClass);
		query.select(root);		
		
		Predicate[] predicates = buildPredicates(builder, root, filters);
		query.where(predicates);
		
		return entityManager.createQuery(query).getResultList();
	}
	
	/**
	 * @param entityClass
	 * @param equalAttrs
	 * @param startPosition
	 * @param maxResult
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public <E> List<E> getEntitiesIgnoreCase(Class<E> entityClass,
			Map<String, Object> equalAttrs, int startPosition, int maxResult, SortSpec sortSpec) {
		
		CriteriaBuilder builder = entityManager.getCriteriaBuilder();
		CriteriaQuery<E> query = builder.createQuery(entityClass);

		Root<E> obj = query.from(entityClass);
		query.select(obj);
		List<Predicate> predicateList = new ArrayList<Predicate>();
		for (String paramName : equalAttrs.keySet()) {
			Object value = equalAttrs.get(paramName);
			
			Predicate predicate = null;
			
			if(value instanceof String) {
				predicate = builder.equal(builder.lower(getPathGeneric(obj, paramName)), 
						builder.lower(builder.literal((String)value)));
			}
			else if(value instanceof Entry) {
				@SuppressWarnings("rawtypes")
				Entry<Comparable, Comparable> valuePair = (Entry<Comparable, Comparable>) value;
				@SuppressWarnings("rawtypes")
				Comparable value1 = valuePair.getKey();
				@SuppressWarnings("rawtypes")
				Comparable value2 = valuePair.getValue();

				predicate = builder.between(getPathGeneric(obj, paramName), value1, value2);
			}
			else {
				predicate = builder.equal(getPathGeneric(obj, paramName), value);
			}
			
			predicateList.add(predicate);
		}
		Predicate[] predicates = new Predicate[predicateList.size()];
		predicateList.toArray(predicates);
		query.where(predicates);
		
		query.orderBy(sortSpec.dir == Direction.ASC ?
				builder.asc(getPathGeneric(obj, sortSpec.field)) : builder.desc(getPathGeneric(obj, sortSpec.field)));
		
    	TypedQuery<E> typedQuery = entityManager.createQuery(query);
    	
    	typedQuery.setFirstResult(startPosition);
    	typedQuery.setMaxResults(maxResult);
		
		return typedQuery.getResultList();
	}
	
	/**
	 * Similar method as the above {@link #getEntitiesIgnoreCase(Class, Map, int, int, SortSpec)},
	 *  but adding an additional parameter notEqualAttrs, which is a map of key/value pair to check
	 *  for the not-equal criteria
	 *  
	 * @param entityClass
	 * @param equalAttrs
	 * @param notEqualAttrs
	 * @param startPosition
	 * @param maxResult
	 * @param sortSpec
	 * @return
	 */
	public <E> List<E> getEntitiesIgnoreCase(Class<E> entityClass, Map<String, Object> equalAttrs, 
			Map<String, Object> notEqualAttrs, int startPosition, int maxResult, SortSpec sortSpec) {
		
		CriteriaBuilder builder = entityManager.getCriteriaBuilder();
		CriteriaQuery<E> query = builder.createQuery(entityClass);

		Root<E> obj = query.from(entityClass);
		query.select(obj);
		List<Predicate> predicateList = new ArrayList<Predicate>();
		
		for (String paramName : equalAttrs.keySet()) {
			Object value = equalAttrs.get(paramName);
			
			Predicate predicate = null;
			
			if(value instanceof String) {
				predicate = builder.equal(builder.lower(getPathGeneric(obj, paramName)), 
						builder.lower(builder.literal((String)value)));
			}
			else if(value instanceof Entry) {
				@SuppressWarnings("rawtypes")
				Entry<Comparable, Comparable> valuePair = (Entry<Comparable, Comparable>) value;
				@SuppressWarnings("rawtypes")
				Comparable value1 = valuePair.getKey();
				@SuppressWarnings("rawtypes")
				Comparable value2 = valuePair.getValue();

				predicate = builder.between(getPathGeneric(obj, paramName), value1, value2);
			}
			else {
				predicate = builder.equal(getPathGeneric(obj, paramName), value);
			}
			
			predicateList.add(predicate);
		}
		
		for (String paramName : notEqualAttrs.keySet()) {
			Object value = notEqualAttrs.get(paramName);
			
			Predicate predicate = null;
			
			if(value instanceof String) {
				predicate = builder.notEqual(builder.lower(getPathGeneric(obj, paramName)), 
						builder.lower(builder.literal((String)value)));
			}
			else if(value instanceof Entry) {
				@SuppressWarnings("rawtypes")
				Entry<Comparable, Comparable> valuePair = (Entry<Comparable, Comparable>) value;
				@SuppressWarnings("rawtypes")
				Comparable value1 = valuePair.getKey();
				@SuppressWarnings("rawtypes")
				Comparable value2 = valuePair.getValue();

				predicate = builder.not(builder.between(getPathGeneric(obj, paramName), value1, value2));
			}
			else {
				predicate = builder.notEqual(getPathGeneric(obj, paramName), value);
			}
			
			predicateList.add(predicate);
		}
		
		Predicate[] predicates = new Predicate[predicateList.size()];
		predicateList.toArray(predicates);
		query.where(predicates);
		
		query.orderBy(sortSpec.dir == Direction.ASC ?
				builder.asc(getPathGeneric(obj, sortSpec.field)) : builder.desc(getPathGeneric(obj, sortSpec.field)));
		
    	TypedQuery<E> typedQuery = entityManager.createQuery(query);
    	
    	typedQuery.setFirstResult(startPosition);
    	typedQuery.setMaxResults(maxResult);
		
		return typedQuery.getResultList();
	}

	/**
	 * @param entityClass
	 * @param filters
	 * @param startPosition
	 * @param maxResult
	 * @return
	 */	
	public <E> List<E> getEntities(Class<E> entityClass,
			Map<String, Object> filters, int startPosition, int maxResult, SortSpec sortSpec) {
		
		CriteriaBuilder builder = entityManager.getCriteriaBuilder();
		CriteriaQuery<E> query = builder.createQuery(entityClass);

		Root<E> root = query.from(entityClass);
		query.select(root);
		
		Predicate[] predicates = buildPredicates(builder, root, filters);
		query.where(predicates);
		
		query.orderBy(sortSpec.dir == Direction.ASC ?
				builder.asc(getPathGeneric(root, sortSpec.field)) : builder.desc(getPathGeneric(root, sortSpec.field)));
		
    	TypedQuery<E> typedQuery = entityManager.createQuery(query);
    	
    	typedQuery.setFirstResult(startPosition);
    	typedQuery.setMaxResults(maxResult);
		
		return typedQuery.getResultList();
	}
	
	public <E> List<E> getEntities(Class<E> entityClass, Map<String, Object> equalAttrs, 
			Map<String, Object> notEqualAttrs, int startPosition, int maxResult, SortSpec sortSpec) {
		
		CriteriaBuilder builder = entityManager.getCriteriaBuilder();
		CriteriaQuery<E> query = builder.createQuery(entityClass);

		Root<E> obj = query.from(entityClass);
		query.select(obj);
		List<Predicate> predicateList = new ArrayList<Predicate>();
		
		for (String paramName : equalAttrs.keySet()) {
			Object value = equalAttrs.get(paramName);
			
			Predicate predicate = null;
			
			if(value instanceof Entry) {
				@SuppressWarnings("rawtypes")
				Entry<Comparable, Comparable> valuePair = (Entry<Comparable, Comparable>) value;
				@SuppressWarnings("rawtypes")
				Comparable value1 = valuePair.getKey();
				@SuppressWarnings("rawtypes")
				Comparable value2 = valuePair.getValue();

				predicate = builder.between(getPathGeneric(obj, paramName), value1, value2);
			}
			else {
				predicate = builder.equal(getPathGeneric(obj, paramName), value);
			}
			
			
			predicateList.add(predicate);
		}
		
		for (String paramName : notEqualAttrs.keySet()) {
			Object value = notEqualAttrs.get(paramName);
			
			Predicate predicate = null;
			
			if(value instanceof Entry) {
				@SuppressWarnings("rawtypes")
				Entry<Comparable, Comparable> valuePair = (Entry<Comparable, Comparable>) value;
				@SuppressWarnings("rawtypes")
				Comparable value1 = valuePair.getKey();
				@SuppressWarnings("rawtypes")
				Comparable value2 = valuePair.getValue();

				predicate = builder.not(builder.between(getPathGeneric(obj, paramName), value1, value2));
			}
			else {
				predicate = builder.notEqual(getPathGeneric(obj, paramName), value);
			}
			
			predicateList.add(predicate);
		}
		
		Predicate[] predicates = new Predicate[predicateList.size()];
		predicateList.toArray(predicates);
		query.where(predicates);
		
		query.orderBy(sortSpec.dir == Direction.ASC ?
				builder.asc(getPathGeneric(obj, sortSpec.field)) : builder.desc(getPathGeneric(obj, sortSpec.field)));
		
    	TypedQuery<E> typedQuery = entityManager.createQuery(query);
    	
    	typedQuery.setFirstResult(startPosition);
    	typedQuery.setMaxResults(maxResult);
		
		return typedQuery.getResultList();
	}
	
	/**
	 * Get all entities of the given type and have attributes
	 * fit the notEqualAttrs
	 * @param <E>
	 * @param entityClass
	 * @param notEqualAttrs
	 * @return
	 */
	public <E> List<E> getNotEqualEntities(Class<E> entityClass,
			Map<String, Object> notEqualAttrs) {
		
		CriteriaBuilder builder = entityManager.getCriteriaBuilder();
		CriteriaQuery<E> query = builder.createQuery(entityClass);

		Root<E> obj = query.from(entityClass);
		query.select(obj);
		List<Predicate> predicateList = new ArrayList<Predicate>();
		for (String paramName : notEqualAttrs.keySet()) {
			Object value = notEqualAttrs.get(paramName);
			Predicate notEqualPredicate = builder.notEqual(getPathGeneric(obj, paramName), value);
			predicateList.add(notEqualPredicate);
		}
		Predicate[] predicates = new Predicate[predicateList.size()];
		predicateList.toArray(predicates);
		query.where(predicates);
		
		return entityManager.createQuery(query).getResultList();
	}

	/**
	 * 
	 * @param <E>
	 * @param entityClass
	 * @param equalAttrs
	 * @return
	 */
	public <E> List<E> getEntities(Class<E> entityClass,
			Map<String, Object> equalAttrs, Map<String, Object> notEqualAttrs) {
		
		CriteriaBuilder builder = entityManager.getCriteriaBuilder();
		CriteriaQuery<E> query = builder.createQuery(entityClass);

		Root<E> obj = query.from(entityClass);
		query.select(obj);
		List<Predicate> predicateList = new ArrayList<Predicate>();
		for (String paramName : equalAttrs.keySet()) {
			Object value = equalAttrs.get(paramName);
			Predicate equalPredicate = builder.equal(getPathGeneric(obj, paramName), value);
			predicateList.add(equalPredicate);
		}
		for (String paramName : notEqualAttrs.keySet()) {
			Object value = notEqualAttrs.get(paramName);
			Predicate notEqualPredicate = builder.notEqual(getPathGeneric(obj, paramName),
					value);
			predicateList.add(notEqualPredicate);
		}
		
		Predicate[] predicates = new Predicate[predicateList.size()];
		predicateList.toArray(predicates);
		query.where(predicates);
		
		return entityManager.createQuery(query).getResultList();
	}
		
	@SuppressWarnings("unchecked")
	public <E> long countEntitiesIgnoreCase(Class<E> entityClass,
			Map<String, Object> equalAttrs) {
		CriteriaBuilder builder = entityManager.getCriteriaBuilder();
		CriteriaQuery<Long> query = builder.createQuery(Long.class);

		Root<E> obj = query.from(entityClass);
		query.select(builder.count(obj));
		List<Predicate> predicateList = new ArrayList<Predicate>();
		
		for (String paramName : equalAttrs.keySet()) {
			Object value = equalAttrs.get(paramName);
			
			Predicate predicate = null;
			if(value instanceof String) {
				predicate = builder.equal(builder.lower(getPathGeneric(obj, paramName)), builder.lower(builder.literal((String)value)));
			}
			else if(value instanceof Entry) {
				@SuppressWarnings("rawtypes")
				Entry<Comparable, Comparable> valuePair = (Entry<Comparable, Comparable>) value;
				@SuppressWarnings("rawtypes")
				Comparable value1 = valuePair.getKey();
				@SuppressWarnings("rawtypes")
				Comparable value2 = valuePair.getValue();
				
				predicate = builder.between(getPathGeneric(obj, paramName), value1, value2);
			}
			else {
				predicate = builder.equal(getPathGeneric(obj, paramName), value);
			}
			
			predicateList.add(predicate);
		}
		
		Predicate[] predicates = new Predicate[predicateList.size()];
		predicateList.toArray(predicates);
		query.where(predicates);
		return entityManager.createQuery(query).getSingleResult();
	}
	
	/**
	 * Same method as the above method {{@link #countEntitiesIgnoreCase(Class, Map)}
	 *  but add additional parameter notEqualAttrs
	 * 
	 * @param entityClass
	 * @param equalAttrs
	 * @param notEqualAttrs
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public <E> long countEntitiesIgnoreCase(Class<E> entityClass,
			Map<String, Object> equalAttrs, Map<String, Object> notEqualAttrs) {
		CriteriaBuilder builder = entityManager.getCriteriaBuilder();
		CriteriaQuery<Long> query = builder.createQuery(Long.class);

		Root<E> obj = query.from(entityClass);
		query.select(builder.count(obj));
		List<Predicate> predicateList = new ArrayList<Predicate>();
		
		for (String paramName : equalAttrs.keySet()) {
			Object value = equalAttrs.get(paramName);
			
			Predicate predicate = null;
			if(value instanceof String) {
				predicate = builder.equal(builder.lower(getPathGeneric(obj, paramName)), builder.lower(builder.literal((String)value)));
			}
			else if(value instanceof Entry) {
				@SuppressWarnings("rawtypes")
				Entry<Comparable, Comparable> valuePair = (Entry<Comparable, Comparable>) value;
				@SuppressWarnings("rawtypes")
				Comparable value1 = valuePair.getKey();
				@SuppressWarnings("rawtypes")
				Comparable value2 = valuePair.getValue();
				
				predicate = builder.between(getPathGeneric(obj, paramName), value1, value2);
			}
			else {
				predicate = builder.equal(getPathGeneric(obj, paramName), value);
			}
			
			predicateList.add(predicate);
		}
		
		for(String paramName : notEqualAttrs.keySet()) {
			Object value = notEqualAttrs.get(paramName);
			
			Predicate predicate = null;
			if(value instanceof String) {
				predicate = builder.notEqual(builder.lower(getPathGeneric(obj, paramName)), builder.lower(builder.literal((String)value)));
			}
			else {
				predicate = builder.notEqual(getPathGeneric(obj, paramName), value);
			}
			
			predicateList.add(predicate);
		}
		
		Predicate[] predicates = new Predicate[predicateList.size()];
		predicateList.toArray(predicates);
		query.where(predicates);
		return entityManager.createQuery(query).getSingleResult();
	}
	
	/**
	 * 
	 * Count all entities of the given entity type and having the attributes and
	 * correspond values specified in the given attrs map
	 * 
	 * <p>
	 * This function is inspired by the following JPA's criteria query example:
	 * <a href="http://city81.blogspot.com/2011/01/criteriabuilder-and-dynamic-queries-in.html">
	 * 	http://city81.blogspot.com/2011/01/criteriabuilder-and-dynamic-queries-in.html
	 * </a>
	 * </p>
	 * <p>
	 * Example use:
	 * </p>
	 * 
	 * <pre>
	 * // count how many client's entity with the given name:
	 * Map&lt;String, Object&gt; attrs = new HashMap&lt;String, Object&gt;();
	 * attrs.put(&quot;name&quot;, client.getSimpleName());
	 * clientCount = genericDAO.countEntities(Client.class, attrs);
	 * </pre>
	 * 
	 * @param <E>
	 * @param entityClass
	 *            the type of entity to be query (Client.class, Contact.class,
	 *            etc)
	 * @param filters
	 *            map that hold key/value to be applied in the query
	 * @return number of entities matching the given entity type and predicates
	 */
	public <E> long countEntities(Class<E> entityClass,
			Map<String, Object> filters) {
		CriteriaBuilder builder = entityManager.getCriteriaBuilder();
		CriteriaQuery<Long> query = builder.createQuery(Long.class);

		Root<E> root = query.from(entityClass);
		query.select(builder.count(root));
		
		Predicate[] predicates = buildPredicates(builder, root, filters);
		query.where(predicates);
		return entityManager.createQuery(query).getSingleResult();
	}

	/**
	 * Count all entities of the given entity type and having the attributes and
	 * correspond values specified in the given equalAttrs map and having the
	 * attributes and correspond not equal values specified in the given
	 * notEqualAttrs map
	 * <p>
	 * This function is inspired by the following JPA's criteria query example:
	 * <a href="http://city81.blogspot.com/2011/01/criteriabuilder-and-dynamic-queries-in.html"> 
	 * 	http://city81.blogspot.com/2011/01/criteriabuilder-and-dynamic-queries-in.html 
	 * </a>
	 * </p>
	 * <p>
	 * Example use:
	 * </p>
	 * 
	 * <pre>
	 * // count how many client's entity with the given name, but with the difference
	 * // id
	 * Map&lt;String, Object&gt; equalAttrs = new HashMap&lt;String, Object&gt;();
	 * attrs.put(&quot;name&quot;, client.getSimpleName());
	 * Map&lt;String, Object&gt; notEqualAttrs = new HashMap&lt;String, Object&gt;();
	 * notEqualAttrs.put(&quot;id&quot;, client.getId());
	 * clientCount = genericDAO.countEntities(Client.class, equalAttrs, notEqualAttrs);
	 * </pre>
	 * 
	 * the format of the equalAttrs and notEqualAttrs maps is [key, value] pair
	 * 
	 * @param <E>
	 * @param entityClass
	 * @param equalAttrs
	 * @param notEqualAttrs
	 * @return
	 * 
	 */
	public <E> long countEntities(Class<E> entityClass,
			Map<String, Object> equalAttrs, Map<String, Object> notEqualAttrs) {
		
		CriteriaBuilder builder = entityManager.getCriteriaBuilder();
		CriteriaQuery<Long> query = builder.createQuery(Long.class);

		Root<E> obj = query.from(entityClass);
		query.select(builder.count(obj));
		List<Predicate> predicateList = new ArrayList<Predicate>();
		
		for (String paramName : equalAttrs.keySet()) {
			Object value = equalAttrs.get(paramName);
			
			Predicate predicate = null;
			
			if(value instanceof Entry) {
				@SuppressWarnings("rawtypes")
				Entry<Comparable, Comparable> valuePair = (Entry<Comparable, Comparable>) value;
				@SuppressWarnings("rawtypes")
				Comparable value1 = valuePair.getKey();
				@SuppressWarnings("rawtypes")
				Comparable value2 = valuePair.getValue();

				predicate = builder.between(getPathGeneric(obj, paramName), value1, value2);
			}
			else {
				predicate = builder.equal(getPathGeneric(obj, paramName), value);
			}
			
			predicateList.add(predicate);
		}
		
		for (String paramName : notEqualAttrs.keySet()) {
			Object value = notEqualAttrs.get(paramName);
			
			Predicate predicate = null;
			
			predicate = builder.notEqual(getPathGeneric(obj, paramName), value);
			
			predicateList.add(predicate);
		}
		
		Predicate[] predicates = new Predicate[predicateList.size()];
		predicateList.toArray(predicates);
		query.where(predicates);
		return entityManager.createQuery(query).getSingleResult();
	}
	
	/**
	 * TODO: candidate for removal
	 * @param <E>
	 * @param entityClass
	 * @param id
	 * @param fetchProperties
	 * @return
	 */
	public <E> E load(Class<E> entityClass, Long id, List<String> fetchProperties) {
		return load(entityClass, id, (String[])fetchProperties.toArray(new String[fetchProperties.size()]));
	}
	
	/**
	 * TODO: candidate for removal
	 * @param <E>
	 * @param entityClass
	 * @param id
	 * @param fetchProperties
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	public <E> E load(Class<E> entityClass, Long id, String[] fetchProperties) {
		
		E entity = entityManager.find(entityClass, id);
		
		if(entity != null) {
			Map objectTree = buildTreeMap(fetchProperties);
			
			loadLazyProperties(entity, objectTree);
		}
		
		return entity;
	}
	
	/**
	 * 
	 * @param <E>
	 * @param entityClass
	 * @param text
	 * @param fieldPaths
	 * @return
	 */
	public <E> Long searchTextCount(Class<E> entityClass, String text, List<String> fieldPaths) {
		
		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		
		CriteriaQuery<Long> criteriaQuery = criteriaBuilder.createQuery(Long.class);
		
		Root<E> root = criteriaQuery.from(entityClass);
		
		Predicate[] predicates = buildPredicates4TextSearch(criteriaBuilder, root, fieldPaths, text);
		criteriaQuery.select(criteriaBuilder.countDistinct(root));
		criteriaQuery.where(criteriaBuilder.or(predicates));
		
		TypedQuery<Long> typedQuery = entityManager.createQuery(criteriaQuery);
		
		return typedQuery.getSingleResult();
	}
	
	/**
	 * Search for entities (from entityClass) that contains the text in its structure
	 * 	(specified by the fieldPaths list)
	 * @param <E>
	 * @param entityClass
	 * @param text
	 * @param fieldPaths
	 * @param firstIndex
	 * @param pageSize
	 * @return
	 */
	public <E> List<E> searchText(Class<E> entityClass, String text, List<String> fieldPaths, int firstIndex, int pageSize) {
		
		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		
		CriteriaQuery<E> criteriaQuery = criteriaBuilder.createQuery(entityClass);
		
		Root<E> root = criteriaQuery.from(entityClass);
		
		Predicate[] predicates = buildPredicates4TextSearch(criteriaBuilder, root, fieldPaths, text);
		criteriaQuery.where(criteriaBuilder.or(predicates));
		criteriaQuery.distinct(true);
		
		TypedQuery<E> typedQuery = entityManager.createQuery(criteriaQuery);
		
		typedQuery.setFirstResult(firstIndex);
		typedQuery.setMaxResults(pageSize);
		
		return typedQuery.getResultList();
	}
	
	@SuppressWarnings("rawtypes")
	public <E> Integer deleteEquals(Class<E> entityClass, Map<String, Comparable> equalAttributes) {
		
		CriteriaBuilder builder = entityManager.getCriteriaBuilder();
		
		CriteriaDelete<E> delete = builder.createCriteriaDelete(entityClass);
		
		// from
		Root<E> root = delete.from(entityClass);
		
		List<Predicate> predicateList = new ArrayList<Predicate>();
		
		// where
		for (String paramName : equalAttributes.keySet()) {
			
			Comparable value = equalAttributes.get(paramName);
			
			Predicate predicate = builder.equal(getPathGeneric(root, paramName), value);
			
			predicateList.add(predicate);
		}
		
		Predicate[] predicates = new Predicate[predicateList.size()];
		predicateList.toArray(predicates);
		delete.where(predicates);
		
		Query query = entityManager.createQuery(delete);
		
		return query.executeUpdate();
	}
	
	@SuppressWarnings("rawtypes")
	public <E> Integer deleteLessThan(Class<E> entityClass, Map<String, Comparable> lessThanAttributes) {
		
		CriteriaBuilder builder = entityManager.getCriteriaBuilder();
		
		CriteriaDelete<E> delete = builder.createCriteriaDelete(entityClass);
		
		// from
		Root<E> root = delete.from(entityClass);
		
		List<Predicate> predicateList = new ArrayList<Predicate>();
		
		// where
		for (String paramName : lessThanAttributes.keySet()) {
			
			Comparable value = lessThanAttributes.get(paramName);
			
			Predicate predicate = builder.lessThan(getPathGeneric(root, paramName), value);
			
			predicateList.add(predicate);
		}
		
		Predicate[] predicates = new Predicate[predicateList.size()];
		predicateList.toArray(predicates);
		delete.where(predicates);
		
		Query query = entityManager.createQuery(delete);
		
		return query.executeUpdate();
	}
	
	@SuppressWarnings("rawtypes")
	public <E> Integer deleteGreaterThan(Class<E> entityClass, Map<String, Comparable> greaterThanAttributes) {
		
		CriteriaBuilder builder = entityManager.getCriteriaBuilder();
		
		CriteriaDelete<E> delete = builder.createCriteriaDelete(entityClass);
		
		// from
		Root<E> root = delete.from(entityClass);
		
		List<Predicate> predicateList = new ArrayList<Predicate>();
		
		// where
		for (String paramName : greaterThanAttributes.keySet()) {
			
			Comparable value = greaterThanAttributes.get(paramName);
			
			Predicate predicate = builder.greaterThan(getPathGeneric(root, paramName), value);
			
			predicateList.add(predicate);
		}
		
		Predicate[] predicates = new Predicate[predicateList.size()];
		predicateList.toArray(predicates);
		delete.where(predicates);
		
		Query query = entityManager.createQuery(delete);
		
		return query.executeUpdate();
	}
	
	/**
	 * Helper method to create array of Predicate (	Predicate[] )
	 * @param <E>
	 * @param criteriaBuilder
	 * @param root
	 * @param searchText
	 * @param fieldPaths
	 * @return
	 */
	private <E> Predicate[] buildPredicates4TextSearch(CriteriaBuilder criteriaBuilder, Root<E> root,
			List<String> fieldPaths, String searchText) {
		
		List<Predicate> predicateList = new ArrayList<>();
		Predicate predicate = null;
		
		for(String fieldPath : fieldPaths) {
			predicate = criteriaBuilder.like(criteriaBuilder.upper(getPathGeneric(root, fieldPath)), "%"+searchText.toLowerCase()+"%");
			predicateList.add(predicate);
		}
		
		Predicate[] predicates = new Predicate[predicateList.size()];
		predicateList.toArray(predicates);
		
		return predicates;
	}
	
	/**
	 * Helper method
	 * @param <E>
	 * @param filters
	 * @param builder
	 * @param root
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private <E>Predicate[] buildPredicates(CriteriaBuilder builder, Root<E> root, Map<String, Object> filters) {
		List<Predicate> predicateList = new ArrayList<>();
		
		for (String paramName : filters.keySet()) {
			Object value = filters.get(paramName);
			
			Predicate predicate = null;
			
			if(value instanceof Entry) {
				@SuppressWarnings("rawtypes")
				Entry<Comparable, Comparable> valuePair = (Entry<Comparable, Comparable>) value;
				@SuppressWarnings("rawtypes")
				Comparable value1 = valuePair.getKey();
				@SuppressWarnings("rawtypes")
				Comparable value2 = valuePair.getValue();

				predicate = builder.between(getPathGeneric(root, paramName), value1, value2);
			}
			else {
				predicate = builder.equal(getPathGeneric(root, paramName), value);
			}
			
			predicateList.add(predicate);
		}
		
		Predicate[] predicates = new Predicate[predicateList.size()]; 
		predicateList.toArray(predicates);
		
		return predicates;
	}
	
	/**
	 * Helper method to build Path expression, given the root object and 
	 * 	expression string
	 * @param <E>
	 * @param root
	 * @param pathExpression
	 * @return
	 */
	private <E> Path<E> getPathGeneric(Root<?> root, String pathExpression) {
		
		String[] paths = pathExpression.split("\\."); 
		if(paths.length > 1) {
			String lastPathEntry = paths[paths.length - 1]; // last entry
			Join<?,?> join = null;
			for(int i = 0; i < paths.length - 1; i++) {
				if(join == null) {
					join = root.join(paths[i]);
				}
				else {
					join = join.join(paths[i]);
				}
			}
			
			return join.<E>get(lastPathEntry);
		}
		
		return root.<E>get(pathExpression);
	}
	
	
	@SuppressWarnings("rawtypes")
	private void loadLazyProperties(Object entity, Map objectTree) {
		
		for(Object key : objectTree.keySet()) {	
			
			// load child property with name key
			Object child = invokeGetter(entity, (String)key);
			
			if(child != null) {
				
				// load grand children 
				if(child instanceof Collection) {
					Collection collection = (Collection) child;
					collection.size(); // force load children
					for(Object element : collection) {
						if ( element != null ) {
							loadLazyProperties(element, (Map)objectTree.get(key));
						}
					}
				}
				else {
					// force load attributes on the entity:
					// note that getId on BaseDomain would
					// not work since Id is the primary key
					// of the entity so it is already loaded
					if(child instanceof BaseEntity) {
						((BaseEntity)child).getCreateDate();
					}
					loadLazyProperties(child, (Map)objectTree.get(key));
				}
			}
		}
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private Object invokeGetter(Object object, String fieldName) {
		if ( object==null || fieldName==null ) {
			String msg =  String.format(
					"invokeGetter(Object object, String fieldName): both parameters required. " +
					"object==%s, fieldName=%s",
					(object==null ? "null" : object.toString()), (fieldName==null ? "null" : fieldName)
					);
			throw new IllegalArgumentException(msg);
		}
		Class entityClass = object.getClass();
		
		try {
			//capitalize the first letter, retrieve getter method, then invoke it 
			Method getterMethod = entityClass.getDeclaredMethod("get" 
					+ Character.toUpperCase(fieldName.charAt(0)) 
					+ fieldName.substring(1));
			return getterMethod.invoke(object); // invoke no-args getter
		} 
		catch (Exception e) {
			// whatever, should swallow it
			e.printStackTrace();
		}
		
		return null;
	}

	/**
	 * helper method build the tree map to represent the object graphs
	 * 
	 * input is in the following formats:
	 * 
	 * 		String[] fetchProperties = { 
	 * 			"logNo",
	 *			"fundingOpp.programFY.program",
	 *			"fundingOpp.awardType.mechanismComponents",
	 *			"preApp.applicationContacts.personContact"
	 *			}
	 *			
	 *	output map would look like:
	 *
	 * 		{
	 * 			logNo={}, 
	 * 			preApp={
	 * 				applicationContacts={
	 * 					personContact={}
	 * 				}
	 * 			}, 
	 * 			fundingOpp={
	 * 				awardType={
	 * 					mechanismComponents={}
	 * 			}, 
	 * 				programFY={
	 * 					program={}
	 * 				}
	 * 			}
	 * 		}
	 * 
	 * Notice that the last level (node without children)
	 * 	would have an empty map
	 * 
	 * @param fetchProperties
	 * @return
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private Map buildTreeMap(String[] fetchProperties) {
		
		Map topLevelMap = new HashMap();
				
		for(String fetchProperty : fetchProperties) {
			Map currMap = topLevelMap;
			String[] tokens = fetchProperty.split("\\.");
			/* 
			 * tokens[0] is top level property
			 * tokens[1] is second level property... and so on
			 */ 
			for(String token : tokens) {
				Map map = (Map)currMap.get(token);
				if(currMap.get(token) == null) {
					map = new HashMap();
					currMap.put(token, map);
				}
				currMap = map;
			}
		}
		
		return topLevelMap;
	}
}
