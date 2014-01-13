package com.eyecall.database;

import java.io.Serializable;
import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import org.hibernate.service.ServiceRegistryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eyecall.server.ServerProtocolHandler;

/**
 * abstraction for the database
 * 
 * @author Nicker
 * 
 */
public class Database {
	private static Database instance;
	private static final Logger logger = LoggerFactory
			.getLogger(Database.class);
	private SessionFactory factory;

	private Database() {
		Configuration cfg = new Configuration()
				.addAnnotatedClass(Volunteer.class)
				.addAnnotatedClass(Location.class).configure();
		factory = cfg.buildSessionFactory(new ServiceRegistryBuilder()
				.applySettings(cfg.getProperties()).buildServiceRegistry());
	}

	/**
	 * get the Singleton instance of the Database
	 * 
	 * @return the database
	 */
	public static Database getInstance() {
		if (instance == null) {
			instance = new Database();
		}
		return instance;
	}

	public Session startSession() {
		return factory.openSession();
	}

	/**
	 * insert the given objects into the database. The object needs to be
	 * annotated with the correct Hibernate annotations and there must be an
	 * existing table for that Object.
	 * 
	 * @param insertions
	 * @return
	 */
	public boolean insertTransaction(Object... insertions) {
		Session s = startSession();
		Transaction tx = null;
		try {
			tx = s.beginTransaction();
			for (Object o : insertions) {
				s.save(o);
			}
			tx.commit();
		} catch (HibernateException e) {
			if (tx != null)
				tx.rollback();
			e.printStackTrace();
			s.close();
			return false;
		}
		s.close();
		return true;
	}

	/**
	 * query for a list of results in the database. The result will be cast to a
	 * list of the given Class.
	 * 
	 * @param query the query to execute
	 * @param cls class to cast to
	 * @param params bound parameters for PreparedStatements
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public <E> List<E> queryForList(String query, Class<E> cls,
			Object... params) {
		Session s = startSession();
		Query q = s.createQuery(query);
		for (int i = 0; i < params.length; i++) {
			Object param = params[i];
			q.setParameter(i, param);
		}
		logger.debug("Query:");
		logger.debug(q.getQueryString());
		List<E> result = q.list();
		s.close();
		return result;
	}

	/**
	 * query for a single record in the database. The result will be cast to a
	 * list of the given Class.
	 * @param query the query to execute
	 * @param cls class to cast to
	 * @param params bound parameters for PreparedStatements
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public <E> E query(String query, Class<E> cls, Object... params) {
		Session s = startSession();
		Query q = s.createQuery(query);
		for (int i = 0; i < params.length; i++) {
			Object param = params[i];
			q.setParameter(i, param);
		}
		E result = (E) q.uniqueResult();
		s.close();
		return result;
	}

	/**
	 * Delete the given Objects from the database
	 * @param deletions Objects to delete
	 * @return is the transaction was successful
	 */
	public boolean deleteTransaction(Object... deletions) {
		Session s = startSession();
		Transaction tx = null;
		try {
			tx = s.beginTransaction();
			for (Object o : deletions) {
				s.delete(o);
			}
			tx.commit();
		} catch (HibernateException e) {
			if (tx != null)
				tx.rollback();
			e.printStackTrace();
			s.close();
			return false;
		}
		s.close();
		return true;
	}

	/**
	 * Update the given Objects in the database
	 * @param updates Objects to update
	 * @return if the transaction was successful
	 */
	public boolean updateTransaction(Object... updates) {
		Session s = startSession();
		Transaction tx = null;
		try {
			tx = s.beginTransaction();
			for (Object o : updates) {
				s.update(o);
			}
			tx.commit();
		} catch (HibernateException e) {
			if (tx != null)
				tx.rollback();
			e.printStackTrace();
			s.close();
			return false;
		}
		s.close();
		return true;
	}

	public <E> E get(Class<E> cls, Serializable key) {
		Session s = startSession();
		E result = cls.cast(startSession().get(cls, key));
		s.close();
		return result;
	}
}
