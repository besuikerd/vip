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


public class Database {
	private static Database instance;
	private static final Logger logger = LoggerFactory.getLogger(Database.class);
	private SessionFactory factory;
	
	private Database() {
		Configuration cfg = new Configuration()
		.addAnnotatedClass(Volunteer.class)
		.addAnnotatedClass(Location.class)
		.configure(); 
		factory = cfg.buildSessionFactory(new ServiceRegistryBuilder().applySettings(cfg.getProperties()).buildServiceRegistry());
	}
	
	public static Database getInstance(){
		if(instance == null){
			instance = new Database();
		}
		return instance;
	}
	
	public Session startSession(){
		return factory.openSession();
	}
	
	public boolean insertTransaction(Object... insertions){
		Session s = startSession();
		Transaction tx = null;
		try{
			tx = s.beginTransaction();
			for(Object o : insertions){
				s.save(o);
			}
			tx.commit();
		} catch(HibernateException e){
			if(tx != null) tx.rollback();
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	@SuppressWarnings("unchecked")
	public <E> List<E> queryForList(String query, Class<E> cls, Object... params){
		Query q = startSession().createQuery(query);
		for(int i = 0 ; i < params.length ; i++){
			Object param = params[i];
			q.setParameter(i, param);
		}
		logger.debug("Query:");
		logger.debug(q.getQueryString());
		return q.list();
	}
	
	@SuppressWarnings("unchecked")
	public <E> E query(String query, Class<E> cls, Object... params){
		Query q = startSession().createQuery(query);
		for(int i = 0 ; i < params.length ; i++){
			Object param = params[i];
			q.setParameter(i, param);
		}
		return (E) q.uniqueResult();
	}
	
	public boolean deleteTransaction(Object... deletions){
		Session s = startSession();
		Transaction tx = null;
		try{
			tx = s.beginTransaction();
			for(Object o : deletions){
				s.delete(o);
			}
			tx.commit();			
		} catch(HibernateException e){
			if(tx != null) tx.rollback();
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	public boolean updateTransaction(Object... updates){
		Session s = startSession();
		Transaction tx = null;
		try{
			tx = s.beginTransaction();
			for(Object o : updates){
				s.update(o);
			}
			tx.commit();
		} catch(HibernateException e){
			if(tx != null) tx.rollback();
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	public <E> E get(Class<E> cls, Serializable key){
		return cls.cast(startSession().get(cls, key));
	}
	
	public static void main(String[] args) {
		Database d = Database.getInstance();
	}
}
