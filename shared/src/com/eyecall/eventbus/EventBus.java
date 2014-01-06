package com.eyecall.eventbus;

import java.util.HashMap;
import java.util.Map;

public class EventBus {
	private static EventBus instance;
	private Map<Class<?>, EventListener> listeners;
	
	private EventBus(){
		this.listeners = new HashMap<Class<?>, EventListener>();
	}
	
	public void subscribe(EventListener listener){
		listeners.put(listener.getClass(), listener);
	}
	
	public void unsubscribe(EventListener listener){
		listeners.remove(listener.getClass());
	}
	
	public void post(Event e){
		for(EventListener listener : listeners.values()){
			listener.onEvent(e);
		}
	}
	
	public static EventBus getInstance(){
		if(instance == null){
			instance = new EventBus();
		}
		return instance;
	}
}
