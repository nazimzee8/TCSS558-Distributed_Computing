package genericnode.client;

import static genericnode.common.KeyValueStoreCommand.EXIT;
import static genericnode.common.KeyValueStoreCommand.STORE;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

import genericnode.common.KeyValueStoreCommand;

public class UDPClient {
	private static final String SERVER_RESPONSE = "server response:";

	public static void send(String hostName, int port, String message) throws IOException {
		// get a datagram socket
		DatagramSocket socket = new DatagramSocket();

		// send request
		InetAddress address = InetAddress.getByName(hostName);
		DatagramPacket packet = new DatagramPacket(message.getBytes("UTF-8"), message.length(), address, port);
		socket.send(packet);

		byte[] buf = new byte[65_000];
		// get response
		packet = new DatagramPacket(buf, buf.length);
		socket.receive(packet);

		// display response
		String received = new String(packet.getData(), 0, packet.getLength());
		if (message.startsWith(STORE.getCommand())) {
			System.out.println(SERVER_RESPONSE);
			System.out.println(received);
		} else if (message.startsWith(EXIT.getCommand())) {
			System.out.println("<the server then exits> ");
		} else {
			System.out.println(SERVER_RESPONSE + received);
		}

		socket.close();
	}
}
