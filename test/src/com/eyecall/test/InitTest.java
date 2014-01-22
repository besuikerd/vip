package com.eyecall.test;
import java.net.BindException;
import java.util.List;

import org.apache.log4j.Level;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eyecall.connection.Connection;
import com.eyecall.database.Database;
import com.eyecall.database.Volunteer;
import com.eyecall.server.Server;
import com.eyecall.vip.VIPProtocolHandler;
import com.eyecall.volunteer.Location;
import com.eyecall.volunteer.VolunteerProtocolHandler;

/**
 * This class test the registration and initialization steps of the system
 */
public class InitTest {
	private static final Logger logger = LoggerFactory.getLogger(InitTest.class);
	
	private Thread serverThread;
	private Server server;
	
	private VIPProtocolHandler vipHandler1;
	private Connection vipConnection1;
	private VIPProtocolHandler vipHandler2;
	private Connection vipConnection2;
	private VolunteerProtocolHandler volunteerHandler1;
	private Connection volunteerConnection1;
	private VolunteerProtocolHandler volunteerHandler2;
	private Connection volunteerConnection2;
	private VolunteerProtocolHandler volunteerHandler3;
	private Connection volunteerConnection3;

	private boolean registered = false;
	
	@Before
	public void setUp() throws Exception {
		org.apache.log4j.Logger.getLogger("org.hibernate").setLevel(Level.INFO);
		
		// Start server
		server = new Server(TestConstants.PORT);
		serverThread = new Thread(){
			public void run() {
				try {
					server.start();
				} catch (BindException e) {
					e.printStackTrace();
				}
			};
		};
		serverThread.start();
		
		// Wait for server
		while(server.getServerSocket()==null || server.getServerSocket().isClosed()){
			Thread.sleep(10);
		}
		Assert.assertFalse("Server connection", server.getServerSocket().isClosed());
		
		// Init connections
		vipHandler1 = new VIPProtocolHandler();
		vipHandler2 = new VIPProtocolHandler();
		volunteerHandler1 = new VolunteerProtocolHandler();
		volunteerHandler2 = new VolunteerProtocolHandler();
		volunteerHandler3 = new VolunteerProtocolHandler();
		
		vipConnection1 = Utils.checkConnection(vipConnection1, vipHandler1);
		vipConnection2 = Utils.checkConnection(vipConnection2, vipHandler2);
		volunteerConnection1 = Utils.checkConnection(volunteerConnection1, volunteerHandler1);
		volunteerConnection2 = Utils.checkConnection(volunteerConnection2, volunteerHandler2);
		volunteerConnection3 = Utils.checkConnection(volunteerConnection3, volunteerHandler3);
		
		if(!registered) testRegistration();
	}
	
	@SuppressWarnings("deprecation")
	@After
	public void tearDown() throws Exception {
		if(!vipConnection1.isClosed()) vipConnection1.close();
		if(!vipConnection2.isClosed()) vipConnection2.close();
		if(!volunteerConnection1.isClosed()) volunteerConnection1.close();
		if(!volunteerConnection2.isClosed()) volunteerConnection2.close();
		if(!volunteerConnection3.isClosed()) volunteerConnection3.close();
		server.getServerSocket().close();
		// Wait for server to close
		while(server.getServerSocket()!=null && !server.getServerSocket().isClosed()){
			Thread.sleep(10);
		}
		serverThread.stop();
	}

	
	/**
	 * Test the registration process by sending empty, normal and too long ids to
	 * the server
	 * @throws Exception
	 */
	public void testRegistration() throws Exception {
		String emptyId = "";
		String tooLongId = "2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b";
		
		// Register volunteer
		VolunteerProtocolHandler.sendKeyToServer(volunteerConnection1, emptyId);
		VolunteerProtocolHandler.sendKeyToServer(volunteerConnection2, tooLongId);
		VolunteerProtocolHandler.sendKeyToServer(volunteerConnection3, TestConstants.VOLUNTEER_ID_3);
		System.out.println("Waiting for server to register...");
		Thread.sleep(2000);
		
		Assert.assertNotNull("Volunteer normal id", Database.getInstance().query("from Volunteer where id=?", Volunteer.class, TestConstants.VOLUNTEER_ID_3));
		Assert.assertNull("Volunteer empty Id", Database.getInstance().query("from Volunteer where id=?", Volunteer.class, emptyId));
		Assert.assertNull("Volunteer too long Id", Database.getInstance().query("from Volunteer where id=?", Volunteer.class, tooLongId));
	
		volunteerConnection1 = Utils.checkConnection(volunteerConnection1, volunteerHandler1);
		volunteerConnection2 = Utils.checkConnection(volunteerConnection2, volunteerHandler2);
		
		Assert.assertNotNull(volunteerConnection1);
		Assert.assertFalse(volunteerConnection1.isClosed());
		
		VolunteerProtocolHandler.sendKeyToServer(volunteerConnection1, TestConstants.VOLUNTEER_ID_1);
		VolunteerProtocolHandler.sendKeyToServer(volunteerConnection2, TestConstants.VOLUNTEER_ID_2);
		System.out.println("Waiting for server to register...");
		Thread.sleep(2000);
		
		Assert.assertNotNull("Volunteer normal id", Database.getInstance().query("from Volunteer where id=?", Volunteer.class, TestConstants.VOLUNTEER_ID_1));
		Assert.assertNotNull("Volunteer normal id", Database.getInstance().query("from Volunteer where id=?", Volunteer.class, TestConstants.VOLUNTEER_ID_2));
	
		registered = true;
	}
	
	/**
	 * Test the addition of locations by volunteers
	 * @throws Exception
	 */
	@Test
	public void testLocationsAdd() throws Exception {
		// Check connections
		volunteerConnection1 = Utils.checkConnection(volunteerConnection1, volunteerHandler1);
		volunteerConnection2 = Utils.checkConnection(volunteerConnection2, volunteerHandler2);
		volunteerConnection3 = Utils.checkConnection(volunteerConnection3, volunteerHandler3);
		
		// Create locations
		Location location1 = new Location();
		Location location2 = new Location();
		Location location3 = new Location();
		Location location4 = new Location();
		location1.setPreferred(true);
		location2.setPreferred(true);
		location3.setPreferred(true);
		location2.setLatitude(10.0);
		location3.setLatitude(20.0);
		location4.setLatitude(20.5);
		location4.setRadius(1.0);
		
		// Add locations
		VolunteerProtocolHandler.addLocation(volunteerConnection1, TestConstants.VOLUNTEER_ID_1, location1);
		VolunteerProtocolHandler.addLocation(volunteerConnection2, TestConstants.VOLUNTEER_ID_2, location2);
		VolunteerProtocolHandler.addLocation(volunteerConnection3, TestConstants.VOLUNTEER_ID_3, location3);
		volunteerConnection1.close();
		volunteerConnection1 = Utils.checkConnection(volunteerConnection1, volunteerHandler1);
		VolunteerProtocolHandler.addLocation(volunteerConnection1, TestConstants.VOLUNTEER_ID_1, location4);
		System.out.println("Waiting for database to add locations...");
		Thread.sleep(2000);
		
		List<com.eyecall.database.Location> locations;
		com.eyecall.database.Location location;
		
		// Get locations from database
		locations = Database.getInstance().queryForList("FROM Location WHERE volunteer.id=?", com.eyecall.database.Location.class, TestConstants.VOLUNTEER_ID_1);
		location = null;
		for(com.eyecall.database.Location l : locations){
			if(l.getLatitude()==0.0 && l.getLongitude()==0.0) location = l;
		}
		Assert.assertNotNull(location);
		location = null;
		for(com.eyecall.database.Location l : locations){
			if(l.getLatitude()==20.5 && l.getLongitude()==0.0  && l.getRadius()==1.0 && !l.isPreferred()) location = l;
		}
		Assert.assertNotNull(location);
		
		locations = Database.getInstance().queryForList("FROM Location WHERE volunteer.id=?", com.eyecall.database.Location.class, TestConstants.VOLUNTEER_ID_2);
		location = null;
		for(com.eyecall.database.Location l : locations){
			if(l.getLatitude()==10.0 && l.getLongitude()==0.0) location = l;
		}
		Assert.assertNotNull(location);
		
		locations = Database.getInstance().queryForList("FROM Location WHERE volunteer.id=?", com.eyecall.database.Location.class, TestConstants.VOLUNTEER_ID_3);
		location = null;
		for(com.eyecall.database.Location l : locations){
			if(l.getLatitude()==20.0 && l.getLongitude()==0.0) location = l;
		}
		Assert.assertNotNull(location);
	}

}
