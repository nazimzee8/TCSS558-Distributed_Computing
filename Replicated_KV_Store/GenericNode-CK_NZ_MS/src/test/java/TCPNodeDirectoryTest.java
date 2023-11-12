import genericnode.server.TCPBasedNodeDirectory;

public class TCPNodeDirectoryTest {
    public static void main(String[] args) {
        TCPBasedNodeDirectory tcpBasedNodeDirectory = new TCPBasedNodeDirectory("localhost", 1234);
        tcpBasedNodeDirectory.getNodes();
    }
}
