/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package genericnode;

import java.io.IOException;
import java.net.MalformedURLException;
import java.rmi.AlreadyBoundException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.AbstractMap.SimpleEntry;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import genericnode.client.TCPClient;
import genericnode.client.UDPClient;
import genericnode.server.TCPServer;
import genericnode.server.UDPServer;

/**
 * @author wlloyd
 */
public class GenericNode {
	/**
	 * @param args the command line arguments
	 */

	public interface Generic extends Remote {
		String put(String key, String value) throws Exception;

		String get(String key) throws Exception;

		String del(String key) throws Exception;

		String store() throws Exception;

		String exit() throws Exception;
	}

	public static class Server implements Generic {

		Map<String, String> map = new HashMap<>();

		public Server() {
		}

		@Override
		public String put(String key, String value) {
			map.put(key, value);
			map = Collections.synchronizedMap(map);
			return "server response: put key=" + key;
		}

		@Override
		public String get(String key) {
			map = Collections.synchronizedMap(map);
			String val = map.get(key);
			return "server response: get key=" + key + " val=" + val;
		}

		@Override
		public String del(String key) {
			map.remove(key);
			map = Collections.synchronizedMap(map);
			return "server response: delete key=" + key;
		}

		@Override
		public String store() {
			map = Collections.synchronizedMap(map);
			return this.toString();
		}

		@Override
		public String exit() {
			System.exit(1);
			return "closing client...";
		}

		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder(256);
			sb.append("server response: \n");
			Iterator<String> itr = map.keySet().iterator();
			int length = 0;
			boolean trimmed = false;
			while (itr.hasNext()) {
				String key = itr.next();
				String value = map.get(key);
				String s = "key:" + key + "value:" + value;
				length += s.length();
				sb.append(s);
				if (itr.hasNext() == true)
					sb.append("\n");
				if (length >= 65000 && trimmed == false) {
					trimmed = true;
					sb.append("TRIMMED: \n");
				}
			}
			return sb.toString();
		}
	}

	public static class RMIServer extends Server {
		Map<String, String> map;
		Generic stub;
		Registry registry;

		public RMIServer() {
			this.map = new HashMap<>();
		}

		@Override
		public String put(String key, String value) {
			return super.put(key, value);
		}

		@Override
		public String get(String key) {
			return super.get(key);
		}

		@Override
		public String del(String key) {
			return super.del(key);
		}

		@Override
		public String store() {
			return super.store();
		}

		@Override
		public String toString() {
			return super.toString();
		}

		@Override
		public String exit() {
			try {
				Naming.unbind("Generic");
				UnicastRemoteObject.unexportObject(this, true);
			} catch (NotBoundException | RemoteException e) {
				System.exit(1);
			} catch (MalformedURLException ex) {
				Logger.getLogger(GenericNode.class.getName()).log(Level.SEVERE, null, ex);
			}
			return "closing client...";
		}

	}

	public static class Client {
		public Client() {
		}

	}

	public static void main(String[] args) throws IOException, Exception, AlreadyBoundException, RemoteException {

		if (args.length > 0) {
			if (args[0].equals("rmis")) {
				System.out.println("RMI SERVER");
				try {
					// insert code to start RMI Server
					Generic server = new RMIServer();
					Generic stub = (Generic) UnicastRemoteObject.exportObject(server, 0);

					// Bind the remote object's stub in the registry
					Naming.rebind("Generic", stub);
					System.err.println("Server ready");
				} catch (MalformedURLException | RemoteException e) {
				        e.printStackTrace();	
                                        System.out.println("Error initializing RMI server.");
				}
			}
			if (args[0].equals("rmic")) {
				//System.out.println("RMI CLIENT");
				String addr = args[1];
				String cmd = args[2];
				String key = (args.length > 3) ? args[3] : "";
				String val = (args.length > 4) ? args[4] : "";
				// insert code to make RMI client request 
				// String host = (args.length < 1) ? null : args[0];
				try {
					//Registry registry = LocateRegistry.getRegistry(addr);
					Generic stub = (Generic) Naming.lookup("Generic");
					String response;
					if (null == cmd)
						throw new Exception("Invalid operation");
					else
						switch (cmd) {
							case "put":
								response = stub.put(key, val);
								break;
							case "get":
								response = stub.get(key);
								break;
							case "del":
								response = stub.del(key);
								break;
							case "store":
								response = stub.store();
								break;
							case "exit":
								response = stub.exit();
								break;
							default:
								throw new Exception("Invalid operation");
						}
					System.out.println(response);
				} catch (NotBoundException | RemoteException e) {
					System.err.println("Client exception: " + e.toString());
				}
			}
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
				// insert code to start TCP server on port
				new TCPServer(port).start();
			}
			if (args[0].equals("uc")) {
				System.out.println("UDP CLIENT");
				String addr = args[1];
				int sendport = Integer.parseInt(args[2]);
				int recvport = sendport + 1;
				String cmd = args[3];
				String key = (args.length > 4) ? args[4] : "";
				String val = (args.length > 5) ? args[5] : "";
				SimpleEntry<String, String> se = new SimpleEntry<>(key, val);
				// insert code to make UDP client request to server at addr:send/recvport
				UDPClient.send(addr, sendport, String.format("%s %s %s", cmd, key, val));
			}
			if (args[0].equals("us")) {
				System.out.println("UDP SERVER");
				int port = Integer.parseInt(args[1]);
				// insert code to start UDP server on port
				new UDPServer(port).start();
			}

		} else {
			String msg = "GenericNode Usage:\n\n" +
					"Client:\n" +
					"uc/tc <address> <port> put <key> <msg>  UDP/TCP CLIENT: Put an object into store\n" +
					"uc/tc <address> <port> get <key>  UDP/TCP CLIENT: Get an object from store by key\n" +
					"uc/tc <address> <port> del <key>  UDP/TCP CLIENT: Delete an object from store by key\n" +
					"uc/tc <address> <port> store  UDP/TCP CLIENT: Display object store\n" +
					"uc/tc <address> <port> exit  UDP/TCP CLIENT: Shutdown server\n" +
					"rmic <address> put <key> <msg>  RMI CLIENT: Put an object into store\n" +
					"rmic <address> get <key>  RMI CLIENT: Get an object from store by key\n" +
					"rmic <address> del <key>  RMI CLIENT: Delete an object from store by key\n" +
					"rmic <address> store  RMI CLIENT: Display object store\n" +
					"rmic <address> exit  RMI CLIENT: Shutdown server\n\n" +
					"Server:\n" +
					"us/ts <port>  UDP/TCP SERVER: run udp or tcp server on <port>.\n" +
					"rmis  run RMI Server.\n";
			System.out.println(msg);
		}

	}

}
