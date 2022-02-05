package com.github.chipolaris.bootforum.service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.github.chipolaris.bootforum.dao.GenericDAO;
import com.github.chipolaris.bootforum.dao.GenericQuery;
import com.github.chipolaris.bootforum.dao.QueryMeta;
import com.github.chipolaris.bootforum.dao.QuerySpec;
import com.github.chipolaris.bootforum.dao.SortSpec;

@Service @Transactional
public class GenericService {

	@Resource
	private GenericDAO genericDAO;
	
	@Resource 
	private GenericQuery genericQuery;
	
	/**
	 * Save new instance of the given entity
	 * @param <E>: entity type must be a subclass of BaseEntity
	 * @param entity: object to be saved
	 * @return
	 */
	@Transactional(readOnly=false)
	public <E> ServiceResponse<Long> saveEntity(E entity) {
		
		ServiceResponse<Long> response = new ServiceResponse<Long>();
		genericDAO.persist(entity);
		
		return response;
	}
	
	/**
	 * Find entity with the given type/class and entityId
	 * 	Note that this method is referred over the {@link #getEntity(Class, Long)} method.
	 *  However, make sure entityId is not null when invoking this method
	 * @param entityClass
	 * @param entityId (must not be null)
	 * @return
	 */
	@Transactional(readOnly=true)
	public <E> ServiceResponse<E> findEntity(Class<E> entityClass, Object entityId) {
		
		ServiceResponse<E> response = new ServiceResponse<E>();
		
		response.setDataObject(genericDAO.find(entityClass, entityId));
		
		return response;
	}
	
	/**
	 * Get entity with the given type/class and entityId
	 * @param entityClass
	 * @param entityId
	 * @return
	 */
	@Transactional(readOnly=true)
	public <E> ServiceResponse<E> getEntity(Class<E> entityClass, Object entityId) {
		
		ServiceResponse<E> response = new ServiceResponse<E>();
		
		response.setDataObject(genericDAO.getEntity(entityClass, 
				Collections.singletonMap("id", entityId)));
		
		return response;
	}
	
	/**
	 * Update the given entity
	 * @param <E>: entity type must be a subclass of BaseEntity
	 * @param entity: object to be updated
	 * @return
	 */
	@Transactional(readOnly=false)
	public <E> ServiceResponse<E> updateEntity(E entity) {
		
		ServiceResponse<E> response = new ServiceResponse<>();
		E mergedEntity = genericDAO.merge(entity);
		response.setDataObject(mergedEntity);
		
		return response;
	}
	
	/**
	 * 
	 * @param <E>
	 * @param entity
	 * @return
	 */
	@Transactional(readOnly=false)
	public <E>ServiceResponse<Void> deleteEntity(E entity) {
		
		ServiceResponse<Void> response = 
			new ServiceResponse<Void>();
		
		genericDAO.remove(entity);
		
		return response;
	}	
	
	/**
	 * Get all entities of the given entityClass
	 * Useful for retrieving all entities of a type to populate drop down list
	 * 
	 * @param <E>
	 * @param entityClass
	 * @return
	 */
	@Transactional(readOnly=true)
	public <E> ServiceResponse<List<E>> getAllEntities(Class<E> entityClass) {
		
		ServiceResponse<List<E>> response = new ServiceResponse<List<E>>();
		response.setDataObject((List<E>) genericDAO.findAll(entityClass));
		
		return response;
	}
	
	public <E> ServiceResponse<List<E>> getEntities(Class<E> entityClass,
			Map<String, Object> filters) {
	
		ServiceResponse<List<E>> response = new ServiceResponse<List<E>>();
		
		response.setDataObject(genericDAO.getEntities(entityClass, filters));
		
		return response;
	}
	
	@Transactional(readOnly=true)
	public <E> ServiceResponse<List<E>> getEntities(Class<E> entityClass, Map<String, Object> filters,
			String sortField, boolean descending) {
		
		ServiceResponse<List<E>> response = new ServiceResponse<>();
		
		response.setDataObject(genericDAO.getEntities(entityClass, filters, 
				new SortSpec(sortField != null ? sortField : "id", descending == true ? SortSpec.Direction.DESC : SortSpec.Direction.ASC)));
		
		return response;
	}
	
	public <E> ServiceResponse<List<E>> getEntities(Class<E> entityClass,
			Map<String, Object> equalAttrs, int startPosition, int maxResult, String sortField, boolean descending) {
	
		ServiceResponse<List<E>> response = new ServiceResponse<List<E>>();
		
		response.setDataObject(genericDAO.getEntities(entityClass, equalAttrs, startPosition, maxResult, 
				new SortSpec(sortField != null ? sortField : "id", descending == true ? SortSpec.Direction.DESC : SortSpec.Direction.ASC)));
		
		return response;
	}
	
	/**
	 * Count number of entities of a given type
	 * 
	 * @param <E>
	 * @param entityClass: entity type to count
	 * @return
	 */
	@Transactional(readOnly=true)
	public <E> ServiceResponse<Long> countEntities(Class<E> entityClass) {
		
		ServiceResponse<Long> response = new ServiceResponse<Long>();
		response.setDataObject(genericDAO.countEntities(entityClass).longValue());
		
		return response;
	}
	
	@Transactional(readOnly=true)
	public <E> ServiceResponse<Long> countEntities(Class<E> entityClass, Map<String, Object> filters) {
		
		ServiceResponse<Long> response = new ServiceResponse<Long>();
		response.setDataObject(genericDAO.countEntities(entityClass, filters).longValue());
		
		return response;
	}
	
	@Transactional(readOnly = true) 
	public <E> ServiceResponse<Number> getMaxNumber(Class<E> entityClass, String targetPath, Map<String, Object> filters) {
		
		ServiceResponse<Number> response = new ServiceResponse<>();
		
		response.setDataObject(genericDAO.getMaxNumber(entityClass, targetPath, filters));
		
		return response;
	}
	
	/*
	 * Methods that use Builder API 
	 */
	@Transactional(readOnly=true)
	public <E> ServiceResponse<Long> countEntities(QuerySpec<E> querySpec) {
		
		ServiceResponse<Long> response = new ServiceResponse<Long>();
		response.setDataObject(genericDAO.countEntities(querySpec).longValue());
		
		return response;
	}
	
	public <E> ServiceResponse<List<E>> getEntities(QuerySpec<E> querySpec) {
	
		ServiceResponse<List<E>> response = new ServiceResponse<List<E>>();
		
		response.setDataObject(genericDAO.getEntities(querySpec));
		
		return response;
	}
	
	@Transactional(readOnly=true)
	public <E> ServiceResponse<Long> countEntities2(QueryMeta<E> queryMeta) {
		
		ServiceResponse<Long> response = new ServiceResponse<Long>();
		response.setDataObject(genericQuery.countEntities(queryMeta).longValue());
		
		return response;
	}
		
	public <E> ServiceResponse<List<E>> getEntities2(QueryMeta<E> queryMeta) {
		
		ServiceResponse<List<E>> response = new ServiceResponse<List<E>>();
		
		response.setDataObject(genericQuery.getEntities(queryMeta));
		
		return response;
	}
	
	public <E> ServiceResponse<Stream<E>> streamEntities(Class<E> entityClass) {
		
		ServiceResponse<Stream<E>> response = new ServiceResponse<>();
		
		response.setDataObject(genericDAO.getEntityStream(entityClass));
		
		return response;
	}
}
