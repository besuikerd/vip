import java.net.BindException;
import java.net.UnknownHostException;

import org.apache.log4j.Level;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eyecall.connection.Connection;
import com.eyecall.connection.ProtocolHandler;
import com.eyecall.connection.State;
import com.eyecall.database.Database;
import com.eyecall.database.Volunteer;
import com.eyecall.eventbus.EventBus;
import com.eyecall.server.Server;
import com.eyecall.vip.MainActivity;
import com.eyecall.vip.VIPProtocolHandler;
import com.eyecall.vip.VIPState;
import com.eyecall.volunteer.VolunteerProtocolHandler;
import com.eyecall.volunteer.VolunteerState;


public class Main {
	private static final Logger logger = LoggerFactory.getLogger(Main.class);
	
	private static final int PORT = 5555;
	
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
		server = new Server(PORT);
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
		vipConnection1 = new Connection("localhost", PORT, vipHandler1, VIPState.IDLE);
		vipHandler2 = new VIPProtocolHandler();
		vipConnection2 = new Connection("localhost", PORT, vipHandler1, VIPState.IDLE);
		
		volunteerHandler1 = new VolunteerProtocolHandler();
		volunteerConnection1 = new Connection("localhost", PORT, volunteerHandler1, VolunteerState.IDLE);
		volunteerHandler2 = new VolunteerProtocolHandler();
		volunteerConnection2 = new Connection("localhost", PORT, volunteerHandler2, VolunteerState.IDLE);
		volunteerHandler3 = new VolunteerProtocolHandler();
		volunteerConnection3 = new Connection("localhost", PORT, volunteerHandler3, VolunteerState.IDLE);
		
		vipConnection1.init(false);
		vipConnection2.init(false);
		volunteerConnection1.init(false);
		volunteerConnection2.init(false);
		volunteerConnection3.init(false);
		
		if(!registered) testRegistration();
	}
	
	private Connection checkConnection(Connection connection, ProtocolHandler handler) throws UnknownHostException{
		return checkConnection(connection, handler, null);
	}
	
	private Connection checkConnection(Connection connection, ProtocolHandler handler, State state) throws UnknownHostException{
		if(connection==null || connection.isClosed()){
			logger.info("Resetting connection");
			if(state==null){
				if(handler instanceof VolunteerProtocolHandler){
					state = VolunteerState.IDLE;
				}else{
					state = VIPState.IDLE;
				}
			}
			connection = new Connection("localhost", PORT, handler, state);
			connection.init(false);
			
		}
		return connection;
	}

	@After
	public void tearDown() throws Exception {
		if(!vipConnection1.isClosed()) vipConnection1.close();
		if(!volunteerConnection1.isClosed()) volunteerConnection1.close();
		server.getServerSocket().close();
		// Wait for server to close
		while(server.getServerSocket()!=null && !server.getServerSocket().isClosed()){
			Thread.sleep(10);
		}
		serverThread.stop();
	}
	
	public void testRegistration() throws Exception {
		String emptyId = "";
		String tooLongId = "2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b2b";
		
		// Register volunteer
		VolunteerProtocolHandler.sendKeyToServer(volunteerConnection1, emptyId);
		VolunteerProtocolHandler.sendKeyToServer(volunteerConnection2, tooLongId);
		VolunteerProtocolHandler.sendKeyToServer(volunteerConnection3, "3c3c3c3c3c3c");
		System.out.println("Waiting for server to register...");
		Thread.sleep(2500);
		
		Assert.assertNotNull("Volunteer normal id", Database.getInstance().query("from Volunteer where id=?", Volunteer.class, "3c3c3c3c3c3c"));
		Assert.assertNull("Volunteer empty Id", Database.getInstance().query("from Volunteer where id=?", Volunteer.class, emptyId));
		Assert.assertNull("Volunteer too long Id", Database.getInstance().query("from Volunteer where id=?", Volunteer.class, tooLongId));
	
		volunteerConnection1 = checkConnection(volunteerConnection1, volunteerHandler1);
		volunteerConnection2 = checkConnection(volunteerConnection2, volunteerHandler2);
		
		Assert.assertNotNull(volunteerConnection1);
		Assert.assertFalse(volunteerConnection1.isClosed());
		
		VolunteerProtocolHandler.sendKeyToServer(volunteerConnection1, "1a1a1a1a1a1a");
		VolunteerProtocolHandler.sendKeyToServer(volunteerConnection2, "2b2b2b2b2b2b");
		System.out.println("Waiting for server to register...");
		Thread.sleep(5000);
		
		Assert.assertNotNull("Volunteer normal id", Database.getInstance().query("from Volunteer where id=?", Volunteer.class, "1a1a1a1a1a1a"));
		Assert.assertNotNull("Volunteer normal id", Database.getInstance().query("from Volunteer where id=?", Volunteer.class, "2b2b2b2b2b2b"));
	
		registered = true;
	}
	
	@Test
	public void simulateRequest() throws Exception {
		VIPProtocolHandler.sendNewRequest(vipConnection1, 0, 0);
		
		Thread.sleep(1000);
		
		EventBus.getInstance().
		
		return;
	}

}
