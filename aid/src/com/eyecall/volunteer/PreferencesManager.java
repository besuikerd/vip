package com.eyecall.volunteer;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

public class PreferencesManager {

	private static final String LOCATIONS_FILE_NAME = "locations";
	private Context context;
	

	public PreferencesManager(Context context){
		this.context = context;
	}
	
	public List<Location> getLocations(){
		List<Location> result = new ArrayList<Location>();
		SharedPreferences prefs = context.getSharedPreferences(LOCATIONS_FILE_NAME, Context.MODE_PRIVATE);
		String[] ids = prefs.getString("ids", "").split(";");
		for(String string : ids){
			int id;
			try{
				id = Integer.parseInt(string);
			}catch(NumberFormatException e){
				Log.d(MainActivity.TAG, "Wrong save: '" + string + "' is not a valid number!");
				continue;
			}
			Location location = new Location();
			location.setId(id);
			location.setLatitude(prefs.getFloat(string + "-lat", 0));
			location.setLongitude(prefs.getFloat(string + "-long", 0));
			location.setPreferred(prefs.getBoolean(string + "-pref", true));
			location.setRadius(prefs.getInt(string + "-rad", 0));
			result.add(location);
		}
		return result;
	}
	
	public void saveLocation(Location location){
		
	}
	
	public void deleteLocation(Location location){
		
	}
}
