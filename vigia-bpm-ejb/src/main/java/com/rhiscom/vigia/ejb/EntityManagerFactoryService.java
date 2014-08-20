package com.rhiscom.vigia.ejb;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

public class EntityManagerFactoryService {

	private static EntityManagerFactoryService instance = null;
	private static EntityManagerFactory entityManagerFactory;
	private static EntityManager entityManager;

	private EntityManagerFactoryService() {
		entityManagerFactory = Persistence.createEntityManagerFactory("PersistenceUnit");

	}

	public EntityManager getEntityManager() {
		entityManager = entityManagerFactory.createEntityManager();
		return entityManager;
	}

	public static EntityManagerFactoryService getInstance() {
		if (instance == null)
			instance = new EntityManagerFactoryService();
		return instance;

	}
}
