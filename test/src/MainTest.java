import java.net.BindException;

import org.apache.log4j.Level;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eyecall.connection.Connection;
import com.eyecall.database.Volunteer;
import com.eyecall.server.Constants;
import com.eyecall.server.Request;
import com.eyecall.server.RequestPool;
import com.eyecall.server.Server;
import com.eyecall.vip.VIPProtocolHandler;
import com.eyecall.volunteer.VolunteerProtocolHandler;

/**
 * This class tests the methods involving requests, finding volunteers and
 * accepting/rejecting these requests
 * 
 * It is assumed that InitTest has run before successfully, so all volunteers
 * are already registered and locations have been added.
 */
public class MainTest {
	private static final Logger logger = LoggerFactory.getLogger(MainTest.class);
	
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
	
	@Test
	/**
	 * Test the find volunteers method
	 * @throws Exception
	 */
	public void testFindVolunteers() throws Exception {
		// Group size needs to be 2 for this test
		Assert.assertEquals(Constants.REQUEST_GROUP_SIZE, 2);
		
		Request request = RequestPool.getInstance().setup(vipConnection1, 0.0, 0.0);
		request.findNewVolunteers();
		logger.debug("Waiting to find volunteers...");
		Thread.sleep(2000); // Wait for database
		Volunteer volunteer1 = null;
		Volunteer volunteer2 = null;
		Volunteer volunteer3 = null;
		for(Volunteer v : request.getPendingVolunteers()){
			if(v.getId().equals(TestConstants.VOLUNTEER_ID_1)) volunteer1 = v;
			if(v.getId().equals(TestConstants.VOLUNTEER_ID_2)) volunteer2 = v;
			if(v.getId().equals(TestConstants.VOLUNTEER_ID_3)) volunteer3 = v;
		}
		// Volunteer 1 and 2 should be pending
		Assert.assertTrue(request.isValid());
		Assert.assertEquals(request.getPendingVolunteers().size(), 2);
		Assert.assertEquals(request.getRejectedVolunteers().size(), 0);
		Assert.assertNotNull(volunteer1);
		Assert.assertNotNull(volunteer2);
		Assert.assertNull(volunteer3);
		
		// Volunteer 1 and 2 should not be free (for other request)
		Assert.assertFalse(RequestPool.getInstance().isFree(volunteer1));
		Assert.assertFalse(RequestPool.getInstance().isFree(volunteer2));
		
		// Now reject both volunteers
		request.rejectPendingVolunteer(TestConstants.VOLUNTEER_ID_1);
		request.rejectPendingVolunteer(TestConstants.VOLUNTEER_ID_2);
		
		// The request should now automaticly start searching for new volunteers,
		// as there are no pending volunteers left.
		logger.debug("Waiting to find volunteers...");
		Thread.sleep(2000); // Wait for database
		// Volunteer 1 and 2 should be rejected, 3 should be pending
		Assert.assertTrue(request.isValid());
		Assert.assertEquals(request.getPendingVolunteers().size(), 1);
		Assert.assertEquals(request.getRejectedVolunteers().size(), 2);
		
		// Check rejected
		volunteer1 = null;
		volunteer2 = null;
		volunteer3 = null;
		for(Volunteer v : request.getRejectedVolunteers()){
			if(v.getId().equals(TestConstants.VOLUNTEER_ID_1)) volunteer1 = v;
			if(v.getId().equals(TestConstants.VOLUNTEER_ID_2)) volunteer2 = v;
			if(v.getId().equals(TestConstants.VOLUNTEER_ID_3)) volunteer3 = v;
		}
		Assert.assertNotNull(volunteer1);
		Assert.assertNotNull(volunteer2);
		Assert.assertNull(volunteer3);
		
		// Check pending
		volunteer1 = null;
		volunteer2 = null;
		volunteer3 = null;
		for(Volunteer v : request.getPendingVolunteers()){
			if(v.getId().equals(TestConstants.VOLUNTEER_ID_1)) volunteer1 = v;
			if(v.getId().equals(TestConstants.VOLUNTEER_ID_2)) volunteer2 = v;
			if(v.getId().equals(TestConstants.VOLUNTEER_ID_3)) volunteer3 = v;
		}
		Assert.assertNull(volunteer1);
		Assert.assertNull(volunteer2);
		Assert.assertNotNull(volunteer3);
		
		// Reject last volunteer
		request.rejectPendingVolunteer(TestConstants.VOLUNTEER_ID_3);
		
		// Request should now automaticly search for new volunteers
		// However these cannnot be found, so the request should close and
		// invalidate itself
		logger.debug("Waiting to find volunteers...");
		Thread.sleep(2000); // Wait for database
		
		Assert.assertFalse(request.isValid());
		Assert.assertEquals(0, request.getPendingVolunteers().size());
		Assert.assertEquals(3, request.getRejectedVolunteers().size());
		
		// Check rejected
		volunteer1 = null;
		volunteer2 = null;
		volunteer3 = null;
		for(Volunteer v : request.getRejectedVolunteers()){
			if(v.getId().equals(TestConstants.VOLUNTEER_ID_1)) volunteer1 = v;
			if(v.getId().equals(TestConstants.VOLUNTEER_ID_2)) volunteer2 = v;
			if(v.getId().equals(TestConstants.VOLUNTEER_ID_3)) volunteer3 = v;
		}
		Assert.assertNotNull(volunteer1);
		Assert.assertNotNull(volunteer2);
		Assert.assertNotNull(volunteer3);
	}
	
	@Test
	/**
	 * Test if the finding process still works if there are multiple
	 * request simultaniously
	 * @throws Exception
	 */
	public void testMultipleRequests() throws Exception {
		// Group size needs to be 2 for this test
		Assert.assertEquals(Constants.REQUEST_GROUP_SIZE, 2);
		
		// Setup requests
		Request request1 = RequestPool.getInstance().setup(vipConnection1, 0.0, 0.0);
		Request request2 = RequestPool.getInstance().setup(vipConnection1, 0.0, 0.0);
		
		// Find volunteers for first request
		request1.findNewVolunteers();
		logger.debug("Waiting to find volunteers...");
		Thread.sleep(2000); // Wait for database
		Volunteer volunteer1 = null;
		Volunteer volunteer2 = null;
		Volunteer volunteer3 = null;
		for(Volunteer v : request1.getPendingVolunteers()){
			if(v.getId().equals(TestConstants.VOLUNTEER_ID_1)) volunteer1 = v;
			if(v.getId().equals(TestConstants.VOLUNTEER_ID_2)) volunteer2 = v;
			if(v.getId().equals(TestConstants.VOLUNTEER_ID_3)) volunteer3 = v;
		}
		// Volunteer 1 and 2 should be pending
		Assert.assertTrue(request1.isValid());
		Assert.assertEquals(request1.getPendingVolunteers().size(), 2);
		Assert.assertEquals(request1.getRejectedVolunteers().size(), 0);
		Assert.assertNotNull(volunteer1);
		Assert.assertNotNull(volunteer2);
		Assert.assertNull(volunteer3);
		
		// Now find volunteers for request 2
		request2.findNewVolunteers();
		logger.debug("Waiting to find volunteers...");
		Thread.sleep(2000); // Wait for database
		volunteer1 = null;
		volunteer2 = null;
		volunteer3 = null;
		for(Volunteer v : request2.getPendingVolunteers()){
			if(v.getId().equals(TestConstants.VOLUNTEER_ID_1)) volunteer1 = v;
			if(v.getId().equals(TestConstants.VOLUNTEER_ID_2)) volunteer2 = v;
			if(v.getId().equals(TestConstants.VOLUNTEER_ID_3)) volunteer3 = v;
		}
		// Volunteer 3 should be pending
		Assert.assertTrue(request1.isValid());
		Assert.assertTrue(request2.isValid());
		Assert.assertEquals(request2.getPendingVolunteers().size(), 1);
		Assert.assertEquals(request2.getRejectedVolunteers().size(), 0);
		Assert.assertNull(volunteer1);
		Assert.assertNull(volunteer2);
		Assert.assertNotNull(volunteer3);
		
		
		// Now reject volunteer 1
		request1.rejectPendingVolunteer(TestConstants.VOLUNTEER_ID_1);
		
		request2.findNewVolunteers();
		logger.debug("Waiting to find volunteers...");
		Thread.sleep(2000); // Wait for database
		volunteer1 = null;
		volunteer2 = null;
		volunteer3 = null;
		for(Volunteer v : request2.getPendingVolunteers()){
			if(v.getId().equals(TestConstants.VOLUNTEER_ID_1)) volunteer1 = v;
			if(v.getId().equals(TestConstants.VOLUNTEER_ID_2)) volunteer2 = v;
			if(v.getId().equals(TestConstants.VOLUNTEER_ID_3)) volunteer3 = v;
		}
		// Volunteer 1 and 3 should be pending
		Assert.assertTrue(request1.isValid());
		Assert.assertTrue(request2.isValid());
		Assert.assertEquals(request2.getPendingVolunteers().size(), 2);
		Assert.assertEquals(request2.getRejectedVolunteers().size(), 0);
		Assert.assertNotNull(volunteer1);
		Assert.assertNull(volunteer2);
		Assert.assertNotNull(volunteer3);
		
		request1.invalidate();
		request1.close();
		request2.invalidate();
		request2.close();
	}
	
	@Test
	/**
	 * Test if the finding process also works if there is an non-preferred location
	 * within radius
	 * @throws Exception
	 */
	public void testFindVolunteersNotPreferred() throws Exception {
		// Group size needs to be 2 for this test
		Assert.assertEquals(Constants.REQUEST_GROUP_SIZE, 2);
		
		Request request = RequestPool.getInstance().setup(vipConnection1, 0.0, 20.0);
		request.findNewVolunteers();
		logger.debug("Waiting to find volunteers...");
		Thread.sleep(2000); // Wait for database
		Volunteer volunteer1 = null;
		Volunteer volunteer2 = null;
		Volunteer volunteer3 = null;
		for(Volunteer v : request.getPendingVolunteers()){
			if(v.getId().equals(TestConstants.VOLUNTEER_ID_1)) volunteer1 = v;
			if(v.getId().equals(TestConstants.VOLUNTEER_ID_2)) volunteer2 = v;
			if(v.getId().equals(TestConstants.VOLUNTEER_ID_3)) volunteer3 = v;
		}
		// Volunteer 2 and 3 should be pending
		Assert.assertTrue(request.isValid());
		Assert.assertEquals(request.getPendingVolunteers().size(), 2);
		Assert.assertEquals(request.getRejectedVolunteers().size(), 0);
		Assert.assertNull(volunteer1);
		Assert.assertNotNull(volunteer2);
		Assert.assertNotNull(volunteer3);
		
		// Now reject both volunteers
		request.rejectPendingVolunteer(TestConstants.VOLUNTEER_ID_2);
		request.rejectPendingVolunteer(TestConstants.VOLUNTEER_ID_3);
		
		// The request should now automaticly start searching for new volunteers,
		// as there are no pending volunteers left.
		logger.debug("Waiting to find volunteers...");
		Thread.sleep(2000); // Wait for database
		// Volunteer 3 and 2 should be rejected, but 1 should not be pending as it 
		// has an non-preferred locations
		Assert.assertFalse(request.isValid());
		Assert.assertEquals(request.getPendingVolunteers().size(), 0);
		Assert.assertEquals(request.getRejectedVolunteers().size(), 2);
		
		// Check rejected
		volunteer1 = null;
		volunteer2 = null;
		volunteer3 = null;
		for(Volunteer v : request.getRejectedVolunteers()){
			if(v.getId().equals(TestConstants.VOLUNTEER_ID_1)) volunteer1 = v;
			if(v.getId().equals(TestConstants.VOLUNTEER_ID_2)) volunteer2 = v;
			if(v.getId().equals(TestConstants.VOLUNTEER_ID_3)) volunteer3 = v;
		}
		Assert.assertNotNull(volunteer2);
		Assert.assertNotNull(volunteer3);
	}
	
	@Test
	/**
	 * Test the acceptation of an request
	 * @throws Exception
	 */
	public void testAcceptRequest() throws Exception{
		// Group size needs to be 2 for this test
		Assert.assertEquals(Constants.REQUEST_GROUP_SIZE, 2);
		
		Request request = RequestPool.getInstance().setup(vipConnection1, 0.0, 0.0);
		request.findNewVolunteers();
		logger.debug("Waiting to find volunteers...");
		Thread.sleep(2000); // Wait for database
		
		// Accept request
		VolunteerProtocolHandler.sendAcceptRequest(volunteerConnection1, TestConstants.VOLUNTEER_ID_1, request.getId());
		logger.debug("Waiting for accepting...");
		Thread.sleep(1000);
		
		Volunteer volunteer1 = null;
		Volunteer volunteer2 = null;
		Volunteer volunteer3 = null;
		Assert.assertEquals(request.getVolunteerId(), TestConstants.VOLUNTEER_ID_1);
		System.out.println(request.getPendingVolunteers());
		Assert.assertEquals(0, request.getPendingVolunteers().size());
		
	}

}
