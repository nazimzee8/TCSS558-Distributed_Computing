package genericnode.server;

import static genericnode.server.KeyValueStoreService.get;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

import genericnode.common.KeyValueStoreCommand;

public class UDPServer extends Thread {
	private final DatagramSocket socket;
	private BufferedReader in;
	protected boolean exit = false;

	public UDPServer(int portNumber) throws SocketException {
		super("UDPServer-TCSS558");
		socket = new DatagramSocket(portNumber);
	}

	public void run() {

		while (!exit) {
			try {
				byte[] buf = new byte[65_000];

				// receive request
				DatagramPacket packet = new DatagramPacket(buf, buf.length);
				socket.receive(packet);

				String receivedCommand = new String(packet.getData());
				String response = "";
				if (receivedCommand != null) {
					final String[] dataFromClient = receivedCommand.split(" ");
					KeyValueStoreCommand command = KeyValueStoreCommand.valueOf(dataFromClient[0].toUpperCase());
					switch (command) {
						case PUT:
							get().put(dataFromClient[1].trim(), dataFromClient[2].trim());
							response = "put key=" + dataFromClient[1].trim();
							break;
						case GET:
							response = "get key=" + dataFromClient[1].trim();
							response += " get val=" + get().get(dataFromClient[1].trim());
							break;
						case STORE:
							response = get().store();
							break;
						case DEL:
							get().del(dataFromClient[1].trim());
							response = "delete key=" + dataFromClient[1].trim();
							break;
						case EXIT:
							exit = true;
							response = "<the server then exits>";
							break;

						default:
							throw new IllegalStateException("Unexpected value: " + dataFromClient[0]);
					}
				}

				if (response != null) {
					// send the response to the client at "address" and "port"
					InetAddress address = packet.getAddress();
					int port = packet.getPort();
					packet = new DatagramPacket(response.getBytes("UTF-8"), response.length(), address, port);
					socket.send(packet);
				}
			} catch (IOException e) {
				e.printStackTrace();
				exit = true;
			}
		}
		socket.close();
	}
}
