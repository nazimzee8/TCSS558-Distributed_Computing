package genericnode.server;

import static genericnode.common.DistributedCommandTransactionState.ACK;

import java.util.ArrayList;
import java.util.List;

import genericnode.common.DistributedCommandTransactionState;

public class DistributedCommandResponse {
	private DistributedCommandTransactionState transactionState = ACK;
	private List<INode> nodes = new ArrayList<>();
	private final String key;
	private final String command;
	private final String value;

	public DistributedCommandResponse(String key, String value, String command) {
		this.key = key;
		this.command = command;
		this.value = value;
	}

	public void addNode(INode node) {
		nodes.add(node);
	}

	public void setTransactionState(DistributedCommandTransactionState transactionState) {
		this.transactionState = transactionState;
	}

	public DistributedCommandTransactionState getTransactionState() {
		return transactionState;
	}

	public List<INode> getNodes() {
		return nodes;
	}

	public String getKey() {
		return key;
	}

	@Override
	public String toString() {
		return "DistributedCommandResponse{" +
				"transactionState=" + transactionState +
				", nodes=" + getNodeAsString() +
				", key='" + key + '\'' +
				", command='" + command + '\'' +
				", value='" + value + '\'' +
				'}';
	}

	private String getNodeAsString() {
		StringBuilder stringBuilder = new StringBuilder();
		nodes.forEach((node) -> stringBuilder.append(node.getIdentifier()).append(", "));
		return stringBuilder.toString();
	}

	public String getCommand() {
		return command;
	}

	public String getValue() {
		return value;
	}
}
