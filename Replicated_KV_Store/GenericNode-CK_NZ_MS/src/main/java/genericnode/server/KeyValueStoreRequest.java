package genericnode.server;

import java.net.Socket;

public class KeyValueStoreRequest implements Request {
    private final Socket clientSocket;
    private final INode serverNode;

    KeyValueStoreRequest(Socket clientSocket, INode serverNode) {
        this.clientSocket = clientSocket;
        this.serverNode = serverNode;
    }

    @Override
    public Socket getClientSocket() {
        return clientSocket;
    }

    @Override
    public INode getServerNode() {
        return serverNode;
    }
}
