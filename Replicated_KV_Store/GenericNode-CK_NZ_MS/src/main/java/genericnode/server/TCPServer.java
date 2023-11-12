package genericnode.server;

import java.io.Closeable;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static genericnode.GenericNode.getNodeDirectory;

public class TCPServer extends Thread implements Closeable, INode {
	private static ServerSocket serverSocket;
	private final ExecutorService executorService = Executors.newFixedThreadPool(50);
	private final boolean distributedKeyValueStoreServer;

	public TCPServer(int portNumber, boolean distributedKeyValueStoreServer) throws IOException {
		if (distributedKeyValueStoreServer)
			System.out.println("TCPServer starting on portNumber : " + portNumber);
		else
			System.out.println("Centralized TCP Node Registry Server starting on portNumber : " + portNumber);

		serverSocket = new ServerSocket(portNumber);
		this.distributedKeyValueStoreServer = distributedKeyValueStoreServer;
	}

	@Override
	public void run() {
		if (distributedKeyValueStoreServer)
			getNodeDirectory().ifPresent(nodeDirectory -> nodeDirectory.register(this));

		while (!serverSocket.isClosed()) {
			try {
				Socket clientSocket = serverSocket.accept();
				if (distributedKeyValueStoreServer)
					executorService
							.submit(new DistributedTCPRequestHandler(new KeyValueStoreRequest(clientSocket, this)));
				else
					executorService
							.submit(new TCPNodeDirectoryRequestHandler(new KeyValueStoreRequest(clientSocket, this)));
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				if (serverSocket.isClosed() && distributedKeyValueStoreServer)
					getNodeDirectory().ifPresent(nodeDirectory -> nodeDirectory.deRegister(this));
			}
		}
		if (distributedKeyValueStoreServer)
			getNodeDirectory().ifPresent(nodeDirectory -> nodeDirectory.deRegister(this));
		System.exit(1);
	}

	public static void exit() {
		try {
			serverSocket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void close() throws IOException {
		serverSocket.close();
	}

	@Override
	public String getHost() {
		/*
		if (serverSocket.getInetAddress().getHostAddress().equalsIgnoreCase("0.0.0.0"))
			return "localhost";

		return serverSocket.getInetAddress().getHostAddress();*/
		try {
			String hostAddress = InetAddress.getLocalHost().getHostAddress();
			System.out.println("TCPServer.getHost - " + hostAddress);
			return hostAddress;
		} catch (UnknownHostException e) {
			e.printStackTrace();
			System.out.println("TCPServer.getHost - localhost");
			return "localhost";
		}
	}

	@Override
	public String toString() {
		return "TCPServer{" +
				"host='" + getHost() + '\'' +
				", port=" + getPort() +
				'}';
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || !(o instanceof INode))
			return false;
		INode node = (INode) o;
		return getPort() == node.getPort() &&
				Objects.equals(getHost(), node.getHost());
	}

	@Override
	public int getPort() {
		return serverSocket.getLocalPort();
	}
}
