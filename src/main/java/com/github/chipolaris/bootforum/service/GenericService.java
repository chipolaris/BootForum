package com.github.chipolaris.bootforum.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.github.chipolaris.bootforum.dao.GenericDAO;
import com.github.chipolaris.bootforum.dao.SortSpec;

@Service @Transactional
public class GenericService {

	@Resource
	private GenericDAO genericDAO;
	
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
	 * 	Note that this method is referred over the {@link #getEntity(Class, Long)} method
	 * @param entityClass
	 * @param entityId
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
	 * 	Note that the method {@link #findEntity(Class, Object)} is preferred over this method
	 * @param entityClass
	 * @param entityId
	 * @return
	 */
	@Transactional(readOnly=true)
	public <E> ServiceResponse<E> getEntity(Class<E> entityClass, Long entityId) {
		
		ServiceResponse<E> response = new ServiceResponse<E>();

		Map<String, Object> equalAttrs = new HashMap<String, Object>();
		equalAttrs.put("id", entityId);
		
		response.setDataObject(genericDAO.getEntity(entityClass, equalAttrs));
		
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
	
	public <E> ServiceResponse<List<E>> getEntities(Class<E> entityClass,
			Map<String, Object> equalAttrs, int startPosition, int maxResult, String sortField, boolean descending) {
	
		ServiceResponse<List<E>> response = new ServiceResponse<List<E>>();
		
		response.setDataObject(genericDAO.getEntities(entityClass, equalAttrs, startPosition, maxResult, 
				new SortSpec(sortField != null ? sortField : "id", descending == true ? SortSpec.Direction.DESC : SortSpec.Direction.ASC)));
		
		return response;
	}
	
	public <E> ServiceResponse<List<E>> getEntitiesIgnoreCase(Class<E> entityClass,
			Map<String, Object> equalAttrs, int startPosition, int maxResult, String sortField, boolean descending) {
	
		ServiceResponse<List<E>> response = new ServiceResponse<List<E>>();
		
		response.setDataObject(genericDAO.getEntitiesIgnoreCase(entityClass, equalAttrs, startPosition, maxResult, 
				new SortSpec(sortField != null ? sortField : "id", descending == true ? SortSpec.Direction.DESC : SortSpec.Direction.ASC)));
		
		return response;
	}
	
	public <E> ServiceResponse<List<E>> getEntitiesIgnoreCase(Class<E> entityClass,
			Map<String, Object> equalAttrs, Map<String, Object> notEqualFilters, int startPosition, int maxResult, String sortField, boolean descending) {
	
		ServiceResponse<List<E>> response = new ServiceResponse<List<E>>();
		
		response.setDataObject(genericDAO.getEntitiesIgnoreCase(entityClass, equalAttrs, notEqualFilters, startPosition, maxResult, 
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
		response.setDataObject(genericDAO.countEntities(entityClass));
		
		return response;
	}
	
	@Transactional(readOnly=true)
	public <E> ServiceResponse<Long> countEntities(Class<E> entityClass, Map<String, Object> filters) {
		
		ServiceResponse<Long> response = new ServiceResponse<Long>();
		response.setDataObject(genericDAO.countEntities(entityClass, filters));
		
		return response;
	}
	
	@Transactional(readOnly=true)
	public <E> ServiceResponse<Long> countEntitiesIgnoreCase(Class<E> entityClass, Map<String, Object> filters) {
		
		ServiceResponse<Long> response = new ServiceResponse<Long>();
		response.setDataObject(genericDAO.countEntitiesIgnoreCase(entityClass, filters));
		
		return response;
	}
	
	@Transactional(readOnly=true)
	public <E> ServiceResponse<Long> countEntitiesIgnoreCase(Class<E> entityClass, Map<String, Object> filters, Map<String, Object> notEqualFilters) {
		
		ServiceResponse<Long> response = new ServiceResponse<Long>();
		response.setDataObject(genericDAO.countEntitiesIgnoreCase(entityClass, filters, notEqualFilters));
		
		return response;
	}
}
