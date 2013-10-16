package com.eyecall.database;

import java.io.Serializable;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import org.hibernate.service.ServiceRegistryBuilder;


public class Database {
	private static Database instance;
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
	
	public void insertTransaction(Object... insertions){
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
		}
	}
	
	public void deleteTransaction(Object... deletions){
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
		}
	}
	
	public void updateTransaction(Object... updates){
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
		}
	}
	
	public <E> E get(Class<E> cls, Serializable key){
		return cls.cast(startSession().get(cls, key));
	}
	
	public static void main(String[] args) {
		Database d = Database.getInstance();
	}
}
