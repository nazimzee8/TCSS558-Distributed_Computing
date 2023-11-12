package genericnode.server;

import java.util.List;

public interface NodeDirectory {
	boolean register(INode node);

	boolean deRegister(INode node);

	List<INode> getNodes();
}
