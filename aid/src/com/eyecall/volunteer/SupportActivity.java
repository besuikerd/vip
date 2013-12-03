package com.eyecall.volunteer;



import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.SparseIntArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TabHost;
import android.widget.Toast;
import android.widget.TabHost.OnTabChangeListener;

import com.eyecall.eventbus.Event;
import com.eyecall.eventbus.EventBus;
import com.eyecall.eventbus.EventListener;
import com.eyecall.protocol.ProtocolField;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class SupportActivity extends FragmentActivity implements EventListener, OnTabChangeListener, OnPageChangeListener{
	
	private static final Logger logger = LoggerFactory.getLogger(SupportActivity.class);
	
	private static final String TAG_VIDEO = "video";
	private static final String TAG_MAP = "map";
	
	private VideoFragment videoFragment;
	private MapFragment mapFragment;
	private ViewPager pager;
	private TabHost tabHost;
	
	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		setContentView(R.layout.activity_support);
		this.pager = (ViewPager) findViewById(R.id.pager); 
		pager.setAdapter(new SupportPagerAdapter(getSupportFragmentManager()));
		pager.setOnPageChangeListener(this);
		this.videoFragment = new VideoFragment();
		this.mapFragment = new MapFragment();
		
		//initialize and attach tabhost
		this.tabHost = (TabHost) getLayoutInflater().inflate(R.layout.tabhost, null);
		((ViewGroup) findViewById(R.id.layout_tabs)).addView(tabHost);
		tabHost.setup();
		tabHost.addTab(tabHost.newTabSpec(TAG_VIDEO).setIndicator(getString(R.string.title_video)).setContent(R.id.video));
		tabHost.addTab(tabHost.newTabSpec(TAG_MAP).setIndicator(getString(R.string.title_map)).setContent(R.id.map));
		logger.debug("tab count: {}", tabHost.getTabWidget().getTabCount());
		tabHost.setOnTabChangedListener(this);
		
		EventBus.getInstance().subscribe(this);
	}
	
	public static class VideoFragment extends Fragment{
		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			return inflater.inflate(R.layout.fragment_video, container, false);
		}
	}
	
	public static class MapFragment extends Fragment{
		private GoogleMap map;
		private Marker marker;
		
		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			return inflater.inflate(R.layout.fragment_map, container, false);
		}
		
		@Override
		public void onViewCreated(View view, Bundle savedInstanceState) {
			super.onViewCreated(view, savedInstanceState);
			
			map = ((SupportMapFragment) getFragmentManager().findFragmentById(R.id.fragment_map)).getMap();
			Bundle b = getActivity().getIntent().getExtras();
			if(b!= null){
				LatLng pos = new LatLng(b.getDouble(ProtocolField.LATITUDE.getName(), 0d), b.getDouble(ProtocolField.LONGITUDE.getName(), 0d));
				logger.debug("setting map marker to ({},{})", pos.latitude, pos.longitude);
				marker = map.addMarker(new MarkerOptions().title("VIP location").position(pos));
				marker.setTitle(String.format("(%f, %f)", marker.getPosition().latitude, marker.getPosition().longitude));
				map.moveCamera(CameraUpdateFactory.newLatLngZoom(pos, 18f));
			}
		}
		
		public void updateLocation(double lat, double lng){
			if(map != null){
				logger.debug("updating coordinates: ({},{})", lat, lng);
				LatLng pos = new LatLng(lat, lng);
				if(marker == null){
					marker = map.addMarker(new MarkerOptions().title("VIP position").position(pos));
				} else{
					marker.setPosition(pos);
					marker.setTitle(String.format("(%f, %f)", marker.getPosition().latitude, marker.getPosition().longitude));
				}
			}
		}
	}
	
	private class SupportPagerAdapter extends FragmentPagerAdapter{

		private SparseIntArray titles;
		
		public SupportPagerAdapter(FragmentManager fm) {
			super(fm);
			titles = new SparseIntArray(){{
				append(0, R.string.title_video);
				append(1, R.string.title_map);
			}};
		}

		@Override
		public CharSequence getPageTitle(int position) {
			return getString(titles.valueAt(position));
		}
		
		@Override
		public Fragment getItem(int pos) {
			switch(pos){
			case 0:
				return videoFragment;
			case 1:
				return mapFragment;
			default:
				return null;
			}
		}

		@Override
		public int getCount() {
			return titles.size();
		}
	}

	@Override
	public void onTabChanged(String tabId) {
		logger.debug("Tab changed: {}", tabId);
		pager.setCurrentItem(tabId.equals(TAG_VIDEO) ? 0 : 1);
	}
	
	@Override
	public void onPageSelected(int index) {
		tabHost.setCurrentTab(index);
	}

	@Override
	public void onPageScrollStateChanged(int arg0) {
	}

	@Override
	public void onPageScrolled(int arg0, float arg1, int arg2) {
	}

	@Override
	public void onEvent(Event e) {
		switch(EventTag.lookup(e.getTag())){
		case LOCATION_UPDATE:
			final LatLng coords = (LatLng) e.getData();
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					Toast.makeText(getApplicationContext(), String.format("location update! (%f,%f)", coords.latitude, coords.longitude), Toast.LENGTH_SHORT).show();
					mapFragment.updateLocation(coords.latitude, coords.longitude);
				}
			});
		}
	}
}
