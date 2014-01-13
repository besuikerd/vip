import java.net.BindException;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.eyecall.connection.Connection;
import com.eyecall.database.Database;
import com.eyecall.database.Volunteer;
import com.eyecall.server.Server;
import com.eyecall.vip.VIPProtocolHandler;
import com.eyecall.vip.VIPState;
import com.eyecall.volunteer.VolunteerProtocolHandler;
import com.eyecall.volunteer.VolunteerState;


public class Main {
	
	private Thread serverThread;
	private Server server;
	
	private VIPProtocolHandler vipHandler;
	private Connection vipConnection;
	private VolunteerProtocolHandler volunteerHandler;
	private Connection volunteerConnection;
	
	@Before
	public void setUp() throws Exception {
		// Start server
		server = new Server(5555);
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
		vipHandler = new VIPProtocolHandler();
		vipConnection = new Connection("localhost", 5555, vipHandler, VIPState.IDLE);
		volunteerHandler = new VolunteerProtocolHandler();
		volunteerConnection = new Connection("localhost", 5555, volunteerHandler, VolunteerState.IDLE);
		
		vipConnection.init(false);
		volunteerConnection.init(false);
		
		// Register volunteer
		VolunteerProtocolHandler.sendKeyToServer(volunteerConnection, "1234");
		Assert.assertNotNull("Volunteer", Database.getInstance().query("Volunteer where id=?", Volunteer.class, "1234"));
	}

	@After
	public void tearDown() throws Exception {
		vipConnection.close();
		volunteerConnection.close();
		server.getServerSocket().close();
		// Wait for server to close
		while(server.getServerSocket()!=null && !server.getServerSocket().isClosed()){
			Thread.sleep(10);
		}
		serverThread.stop();
	}
	
	@Test
	public void simulateRequest() throws Exception {
		//VIPProtocolHandler.sendNewRequest(vipConnection, 0, 0);
		return;
	}

}
