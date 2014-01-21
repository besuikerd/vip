import java.net.UnknownHostException;

import com.eyecall.connection.Connection;
import com.eyecall.connection.ProtocolHandler;
import com.eyecall.connection.State;
import com.eyecall.vip.VIPState;
import com.eyecall.volunteer.VolunteerProtocolHandler;
import com.eyecall.volunteer.VolunteerState;


public class Utils {

	public static Connection checkConnection(Connection connection, ProtocolHandler<?> handler) throws UnknownHostException{
		return checkConnection(connection, handler, null);
	}
	
	public static Connection checkConnection(Connection connection, ProtocolHandler<?> handler, State state) throws UnknownHostException{
		if(connection==null || connection.isClosed()){
			if(state==null){
				if(handler instanceof VolunteerProtocolHandler){
					state = VolunteerState.IDLE;
				}else{
					state = VIPState.IDLE;
				}
			}
			connection = new Connection("localhost", TestConstants.PORT, handler, state);
			connection.init(false);
			
		}
		return connection;
	}
}
