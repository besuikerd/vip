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
	 * The maximum amount of volunteers who will receive a new request in one timer cycle
	 */
	public static final int REQUEST_GROUP_SIZE = 1;

	/**
	 * Query used for finding a new group of volunteers
	 */
	public static final String VOLUNTEER_QUERY = 
			"(select distinct l.user_id\r\n" + 
			"from (select *\r\n" + 
			"    from location l1\r\n" + 
			"    where not exists (\r\n" + 
			"        select *\r\n" + 
			"        from location l2\r\n" + 
			"        where l2.user_id=l1.user_id and l2.preferred=FALSE and abs(sqrt(pow(l2.latitude-:latitude,2)+pow(l2.longitude-:longitude,2)))<=l2.radius\r\n" + 
			"        ) \r\n" + 
			"    ) l\r\n" + 
			"where preferred=True\r\n" + 
			"group by l.user_id\r\n" + 
			"order by preferred DESC, abs(sqrt(pow(l.latitude-:latitude,2)+pow(l.longitude-:longitude,2))) ASC)\r\n" + 
			"UNION\r\n" + 
			"(select distinct l.user_id\r\n" + 
			"from\r\n" + 
			"    (select *\r\n" + 
			"    from location l\r\n" + 
			"    where not exists (\r\n" + 
			"      select *\r\n" + 
			"      from location l2\r\n" + 
			"      where l.user_id=l2.user_id AND l2.preferred=True)) as l\r\n" + 
			"where not exists \r\n" + 
			"        (\r\n" + 
			"        select *\r\n" + 
			"        from location l2\r\n" + 
			"        where l2.user_id=l.user_id and l2.preferred=FALSE and abs(sqrt(pow(l2.latitude-:latitude,2)+pow(l2.longitude-:longitude,2)))<=l2.radius\r\n" + 
			"        ))\r\n" + 
			"UNION\r\n" + 
			"(select v.id\r\n" + 
			"from volunteer v\r\n" + 
			"where not exists (\r\n" + 
			"  select *\r\n" + 
			"  from location l\r\n" + 
			"  where l.user_id=v.id));\r\n" + 
			"";

	/** 
	 * Query used to delete a location
	 */
	public static final String DELETE_QUERY = "delete from Location where id=:locationId";
}
