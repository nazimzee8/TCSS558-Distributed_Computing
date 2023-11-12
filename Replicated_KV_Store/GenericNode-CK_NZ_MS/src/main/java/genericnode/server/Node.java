package genericnode.server;

import java.util.Objects;

public class Node implements INode {
	private final String host;
	private final int port;

	public Node(String host, int port) {
		this.host = host;
		this.port = port;
	}

	@Override
	public String getHost() {
		if (host.equalsIgnoreCase("0.0.0.0"))
			return "localhost";

		return host;
	}

	@Override
	public String toString() {
		return "Node{" +
				"host='" + host + '\'' +
				", port=" + port +
				'}';
	}

	@Override
	public int getPort() {
		return port;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || !(o instanceof INode))
			return false;
		INode node = (INode) o;
		return port == node.getPort() &&
				Objects.equals(host, node.getHost());
	}

	@Override
	public int hashCode() {
		return Objects.hash(host, port);
	}
}
