package com.eyecall.eventbus;

import java.util.Collection;
import java.util.Stack;

public class EventBus {
	private static EventBus instance;
	private Collection<EventListener> listeners;
	
	private EventBus(){
		this.listeners = new Stack<EventListener>();
	}
	
	public void subscribe(EventListener listener){
		listeners.add(listener);
	}
	
	public void post(Event e){
		for(EventListener listener : listeners){
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
