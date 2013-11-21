package com.eyecall.test;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

import com.eyecall.connection.Connection;
import com.eyecall.connection.Message;
import com.eyecall.protocol.ProtocolField;
import com.eyecall.protocol.ProtocolName;


public class VIPSimulation {
	public static void main(String[] args) throws UnknownHostException, IOException {
		Connection c = new Connection(new Socket("localhost", 5000));
		c.init(false);
		c.send(new Message(ProtocolName.REQUEST_HELP).add(ProtocolField.LATITUDE, 52d).add(ProtocolField.LONGITUDE, 7d));
	}
}
