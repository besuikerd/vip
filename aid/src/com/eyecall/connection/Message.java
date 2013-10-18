<<<<<<< HEAD
package com.eyecall.connection;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Model for a message being sent across a {@link Connection}
 * @author Nicker
 *
 */
public class Message {
	
	private static final Logger logger = LoggerFactory.getLogger(Message.class);
	
	/**
	 * each message has a name to identify the type of the message
	 */
	private String name;
	
	
	/**
	 * list of named parameters
	 */
	private Map<String, Object> params;
	
	/**
	 * Constructor needed for Jackson ObjectMapper
	 */
	public Message() {
		this.name = "";
		params = new HashMap<String, Object>();
	}
	
	/**
	 * constructs a new Message with the given name
	 * @param name
	 */
	public Message(String name) {
		this();
		this.name = name;
	}
	
	/**
	 * add a parameter to this Message
	 * @param key parameter key
	 * @param value parameter value
	 * @return <code>this</code>, for easy method chaining
	 */
	public Message add(String key, Object value){
		params.put(key, value);
		return this;
	}
	
	/**
	 * 
	 * @return
	 */
	public String getName() {
		return name;
	}
	
	public Map<String, Object> getParams() {
		return params;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public void setParams(Map<String, Object> params) {
		this.params = params;
	}
	
	public boolean hasParam(String key){
		return params.containsKey(key);
	}
	
	public Object getParam(String key){
		return params.get(key);
	}
	
	public <E> E getParam(String key, Class<E> cls){
		if(params.containsKey(key)){
			Object val = getParam(key);
			if(val.getClass().isAssignableFrom(cls)){
				return cls.cast(val);
			} else{
				logger.warn("unable to cast message param from {} to {}", val.getClass().getCanonicalName(), cls.getCanonicalName());
			}
		}
		return null;
	}

	@Override
	public String toString() {
		return "Message [name=" + name + ", params=" + params + "]";
	}
}
=======
package com.eyecall.connection;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Model for a message being sent across a {@link Connection}
 * @author Nicker
 *
 */
public class Message {
	
	private static final Logger logger = LoggerFactory.getLogger(Message.class);
	
	/**
	 * each message has a name to identify the type of the message
	 */
	private String name;
	
	
	/**
	 * list of named parameters
	 */
	private Map<String, Object> params;
	
	/**
	 * Constructor needed for Jackson ObjectMapper
	 */
	public Message() {
		this.name = "";
		params = new HashMap<String, Object>();
	}
	
	/**
	 * constructs a new Message with the given name
	 * @param name
	 */
	public Message(String name) {
		this();
		this.name = name;
	}
	
	/**
	 * add a parameter to this Message
	 * @param key parameter key
	 * @param value parameter value
	 * @return <code>this</code>, for easy method chaining
	 */
	public Message add(String key, Object value){
		params.put(key, value);
		return this;
	}
	
	/**
	 * 
	 * @return
	 */
	public String getName() {
		return name;
	}
	
	public Map<String, Object> getParams() {
		return params;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public void setParams(Map<String, Object> params) {
		this.params = params;
	}
	
	public boolean hasParam(String key){
		return params.containsKey(key);
	}
	
	public Object getParam(String key){
		return params.get(key);
	}
	
	public <E> E getParam(String key, Class<E> cls){
		if(params.containsKey(key)){
			Object val = getParam(key);
			if(val.getClass().isAssignableFrom(cls)){
				return cls.cast(val);
			} else{
				logger.warn("unable to cast message param from {} to {}", val.getClass().getCanonicalName(), cls.getCanonicalName());
			}
		}
		return null;
	}

	@Override
	public String toString() {
		return "Message [name=" + name + ", params=" + params + "]";
	}
}
>>>>>>> Tanja
