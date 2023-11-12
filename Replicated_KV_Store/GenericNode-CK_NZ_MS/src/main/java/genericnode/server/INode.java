package genericnode.server;

public interface INode {
	String getHost();

	int getPort();

	default String getIdentifier() {
		return getHost() + "_" + getPort();
	}
}
