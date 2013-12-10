package com.eyecall.server;

public class Constants {
	/**
	 * Api key used for GCM
	 */
	public static final String API_KEY = "AIzaSyCcxMgHWw1VpH5x1NO69PtsxD1ycbc7NdQ";
	
	/**
	 * Time after which volunteers will automatically reject and a new group will be 
	 * requested (in milliseconds)
	 */
	public static final int REQUEST_TIMEOUT = 30000;

	/**
	 * Query used for finding a new group of volunteers
	 */
	public static final String VOLUNTEER_QUERY = "SELECT v FROM Volunteer v";

	/** 
	 * Query used to delete a location
	 */
	public static final String DELETE_QUERY = "delete from Location where id=:locationId";
}
