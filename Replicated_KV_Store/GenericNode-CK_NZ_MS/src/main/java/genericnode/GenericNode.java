/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package genericnode;

import genericnode.client.TCPClient;
import genericnode.server.FileBasedNodeDirectory;
import genericnode.server.NodeDirectory;
import genericnode.server.TCPBasedNodeDirectory;
import genericnode.server.TCPServer;

import java.util.AbstractMap.SimpleEntry;
import java.util.Optional;

/**
 * @author wlloyd
 */
public class GenericNode {
	private static NodeDirectory nodeDirectory;

	public static void main(String[] args) throws Exception {
		if (args.length > 0) {
			if (args[0].equals("tc")) {
				System.out.println("TCP CLIENT");
				String addr = args[1];
				int port = Integer.parseInt(args[2]);
				String cmd = args[3];
				String key = (args.length > 4) ? args[4] : "";
				String val = (args.length > 5) ? args[5] : "";
				SimpleEntry<String, String> se = new SimpleEntry<>(key, val);
				// insert code to make TCP client request to server at addr:port
				TCPClient.send(addr, port, String.format("%s %s %s", cmd, key, val));
			}
			if (args[0].equals("ts")) {
				System.out.println("TCP SERVER");
				int port = Integer.parseInt(args[1]);
				if (args.length == 3) {
					// Implement File based Node Directory and initialize, not need to implement register and deregister as it is a static file
					// Implement file based NodeDirectory and initialize nodeDirectory
					nodeDirectory = new FileBasedNodeDirectory();
				} else if (args.length == 5) {
					String protocol = args[2]; // tcp or udp
					String nodeDirectoryHostName = args[3];
					int nodeDirectoryPortNumber = Integer.parseInt(args[4]);

					if ("tcp".equalsIgnoreCase(protocol)) {
						System.out.println(
								"TCP based NodeDirectory - hostName : " + nodeDirectoryHostName +
										", nodeDirectoryPortNumber : " +
										nodeDirectoryPortNumber);
						nodeDirectory = new TCPBasedNodeDirectory(nodeDirectoryHostName, nodeDirectoryPortNumber);

					} else {
						System.out.println(
								"UDP based NodeDirectory - hostName : " + nodeDirectoryHostName +
										", nodeDirectoryPortNumber : " +
										nodeDirectoryPortNumber);
						// Implement UDPBased Node Directory and initialize
						//nodeDirectory = new TCPBasedNodeDirectory(nodeDirectoryHostName, nodeDirectoryPortNumber);
					}
				}
				// insert code to start TCP server on port
				new TCPServer(port, true).start();
			}
			if (args[0].equals("ts_cs")) {
				System.out.println("Centralized TCP SERVER");
				int port = Integer.parseInt(args[1]);
				// insert code to start TCP server on port
				new TCPServer(port, false).start();
			}
			if (args[0].equals("udp_cs")) {
				System.out.println("Centralized UDP SERVER");
				int port = Integer.parseInt(args[1]);
				// Start UDP Server

			}
		} else {
			String msg = "GenericNode Usage:\n\n" +
					"Client:\n" +
					"tc <address> <port> put <key> <msg>  TCP CLIENT: Put an object into store\n" +
					"tc <address> <port> get <key>  TCP CLIENT: Get an object from store by key\n" +
					"tc <address> <port> del <key>  TCP CLIENT: Delete an object from store by key\n" +
					"tc <address> <port> store  TCP CLIENT: Display object store\n" +
					"tc <address> <port> exit  TCP CLIENT: Shutdown server\n" +
					"Server:\n" +
					"ts <port>  TCP SERVER: run tcp server on <port>.\n";
			System.out.println(msg);
		}
	}

	public static Optional<NodeDirectory> getNodeDirectory() {
		return Optional.ofNullable(nodeDirectory);
	}
}
