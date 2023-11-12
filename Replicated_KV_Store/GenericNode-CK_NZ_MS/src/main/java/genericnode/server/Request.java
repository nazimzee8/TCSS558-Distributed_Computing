package genericnode.server;

import java.net.Socket;

public interface Request {
	Socket getClientSocket();

	INode getServerNode();
}
