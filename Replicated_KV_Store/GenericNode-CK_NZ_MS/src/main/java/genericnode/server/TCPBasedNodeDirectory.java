package genericnode.server;

import java.util.ArrayList;
import java.util.List;

import static genericnode.client.TCPClient.send;

public class TCPBasedNodeDirectory implements NodeDirectory {
	private final String host;
	private final int port;

	public TCPBasedNodeDirectory(String host, int port) {
		this.host = host;
		this.port = port;
	}

	@Override
	public boolean register(INode node) {
		send(host, port, "put " + node.getIdentifier() + " " + node.getIdentifier());
		return true;
	}

	@Override
	public boolean deRegister(INode node) {
		send(host, port, "del " + node.getIdentifier());
		return true;
	}

	@Override
	public List<INode> getNodes() {
		String store = send(host, port, "store", false);
		List<INode> nodes = new ArrayList<>();
		if (store != null) {
			String lines[] = store.split("\\r?\\n");
			for (int i = 0; i < lines.length; i++) {
				final String[] split = lines[i].split(":value:");
				if (split.length == 2) {
					String value = split[1];
					String[] hostNameAndPort = value.split("_");
					if (hostNameAndPort.length == 2)
						nodes.add(new Node(hostNameAndPort[0].replace("key:", ""), Integer.parseInt(hostNameAndPort[1])));

				}
			}
		}
		return nodes;
	}
}
