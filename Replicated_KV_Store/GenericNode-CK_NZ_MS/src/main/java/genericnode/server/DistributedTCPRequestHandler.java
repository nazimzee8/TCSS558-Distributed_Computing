package genericnode.server;

import genericnode.GenericNode;
import genericnode.common.KeyValueStoreCommand;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;

import static genericnode.client.TCPClient.send;
import static genericnode.common.DistributedCommandFormat.*;
import static genericnode.common.DistributedCommandTransactionState.ABORT;
import static genericnode.common.DistributedCommandTransactionState.ACK;
import static genericnode.common.KeyValueStoreCommand.*;
import static genericnode.server.KeyValueStoreService.get;
import static genericnode.server.TCPNodeDirectoryRequestHandler.getInputFromClient;
import static java.lang.String.format;

public class DistributedTCPRequestHandler implements RequestHandler, Runnable {
	private final Request request;
	private static final String COMMAND_SEPARATOR = " ";
	private int retriesLeft = 10;

	DistributedTCPRequestHandler(Request request) {
		this.request = request;
	}

	@Override
	public void handle() {
		boolean exitServer = false;
		try {
			Socket clientSocket = request.getClientSocket();
			String input = getInput(clientSocket);

			if (input != null) {
				String[] dataFromClient = input.split(COMMAND_SEPARATOR);
				KeyValueStoreCommand command = valueOf(getCommand(dataFromClient).toUpperCase());
				log("handle - " + input);
				switch (command) {
					case PUT:
						handlePut(dataFromClient);
						break;
					case GET:
						handleGet(dataFromClient);
						break;
					case STORE:
						handleStore();
						break;
					case DEL:
						handleDelete(dataFromClient);
						break;
					case DPUT1:
						handlePutOne(dataFromClient);
						break;
					case DPUT2:
						handlePutTwo(dataFromClient);
						break;
					case DDEL1:
						handleDeleteOne(dataFromClient);
						break;
					case DDEL2:
						handleDeleteTwo(dataFromClient);
						break;
					case DPUTABORT:
						handlePutAbort(dataFromClient);
						break;
					case DDELABORT:
						handleDelAbort(dataFromClient);
						break;
					case EXIT:
						exitServer = true;
						writeResponse("<the server then exits>");
						break;

					default:
						throw new IllegalStateException("Unexpected value: " + dataFromClient[0]);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (exitServer)
				TCPServer.exit();
		}
	}

	private void handleDelAbort(String[] dataFromClient) throws IOException {
		String key = getKey(dataFromClient);
		log("handleDelAbort: for key: " + key);
		if (get().unlock(key)) {
			log("handleDelAbort: unlocked key : " + key + ", sending ACK.");
			writeResponse(ACK.name());
		} else {
			log("handleDelAbort: unable to unlock : " + key + ", sending ABORT. This should not happen.");
			writeResponse(ABORT.name());
		}
	}

	private void handleDeleteTwo(String[] dataFromClient) throws IOException {
		get().del(getKey(dataFromClient));
		writeResponse(ACK.name());
		get().unlock(getKey(dataFromClient));
	}

	private void handleDeleteOne(String[] dataFromClient) throws IOException {
		String key = getKey(dataFromClient);
		if (get().lockIfAvailable(key)) {
			log("handleDeleteOne - lockAvailable for key : " + key + ", sending ACK");
			writeResponse(ACK.name());
		} else {
			log("handleDeleteOne - lockNotAvailable for key : " + key + ", sending ABORT");
			writeResponse(ABORT.name());
		}
	}

	private void handlePutAbort(String[] dataFromClient) throws IOException {
		String key = getKey(dataFromClient);
		log("handlePutAbort: for key: " + key);
		if (get().unlock(key)) {
			log("handlePutAbort: unlocked key : " + key + ", sending ACK.");
			writeResponse(ACK.name());
		} else {
			log("handlePutAbort: unable to unlock : " + key + ", sending ABORT. This should not happen.");
			writeResponse(ABORT.name());
		}
	}

	private void log(String message) {
		System.out.println(new Date() + " - " + request.getServerNode().getIdentifier() + ": " + message);
	}

	private void handlePutTwo(String[] dataFromClient) throws IOException {
		get().put(getKey(dataFromClient), getValue(dataFromClient));
		writeResponse(ACK.name());
		get().unlock(getKey(dataFromClient));
	}

	private void handlePutOne(String[] dataFromClient) throws IOException {
		String key = getKey(dataFromClient);
		if (get().lockIfAvailable(key)) {
			log("handlePutOne - lockAvailable for key : " + key + ", sending ACK");
			writeResponse(ACK.name());
		} else {
			log("handlePutOne - lockNotAvailable for key : " + key + ", sending ABORT");
			writeResponse(ABORT.name());
		}
	}

	private void handlePut(String[] dataFromClient) throws IOException {
		if (retriesLeft >= 0) {
			DistributedCommandResponse put1Response = broadCastDistributedPutOne(dataFromClient);
			if (ACK.equals(put1Response.getTransactionState())) {
				DistributedCommandResponse put2Response = broadCastDistributedPutTwo(
						dataFromClient);

				if (ACK.equals(put2Response.getTransactionState())) {
					log("handlePut - broadCastDistributedPutTwo ACK - response - " + put2Response);
					get().put(getKey(dataFromClient), getValue(dataFromClient));
					writeResponse(getPutCommandResponse(dataFromClient));
				} else {
					log("handlePut - broadCastDistributedPutTwo ABORT - response - " + put2Response);
					writeResponse("ERROR-FAILED to execute command dput2. This should not happen. Command - " +
							getPutCommandResponse(dataFromClient));
				}
				return;
			} else {
				log("handlePut - got ABORT from " + put1Response);
				broadCastDistributedPutAbort(put1Response);
				retriesLeft--;
				handlePut(dataFromClient);
			}
		} else {
			writeResponse("ERROR-FAILED to execute command - " + getPutCommandResponse(dataFromClient));
		}
	}

	private String getPutCommandResponse(String[] dataFromClient) {
		return PUT.getCommand() + " key=" + getKey(dataFromClient);
	}

	private void broadCastDistributedPutAbort(DistributedCommandResponse put1Response) {
		String distributeAbortCommand = format(DISTRIBUTED_PUT_ABORT, put1Response.getKey(), put1Response.getValue());
		broadCastCommand(put1Response.getNodes(), put1Response.getKey(), put1Response.getValue(),
				distributeAbortCommand);
	}

	private DistributedCommandResponse broadCastDistributedPutOne(String[] dataFromClient) {
		String command = getDistributedPutCommand(dataFromClient, 1);
		log("broadCastDistributedPutOne - " + command);
		if (GenericNode.getNodeDirectory().isPresent())
			return broadCastCommand(GenericNode.getNodeDirectory().get().getNodes(), getKey(dataFromClient),
					getValue(dataFromClient), command);

		return new DistributedCommandResponse(getKey(dataFromClient), getValue(dataFromClient), command);
	}

	private DistributedCommandResponse broadCastDistributedPutTwo(String[] dataFromClient) {
		String command = getDistributedPutCommand(dataFromClient, 2);
		DistributedCommandResponse distributedCommandResponse = new DistributedCommandResponse(getKey(dataFromClient),
				getValue(dataFromClient), command);

		log("broadCastDistributedPutTwo - " + command);
		if (GenericNode.getNodeDirectory().isPresent())
			return broadCastCommand(GenericNode.getNodeDirectory().get().getNodes(), getKey(dataFromClient),
					getValue(dataFromClient), command);

		return distributedCommandResponse;
	}

	private void handleGet(String[] dataFromClient) throws IOException {
		String response = GET.getCommand() + " key=" + getKey(dataFromClient);
		response += " " + GET.getCommand() + " val=" + get().get(getKey(dataFromClient));
		writeResponse(response);
	}

	private void handleStore() throws IOException {
		writeResponse(get().store());
	}

	private void handleDelete(String[] dataFromClient) throws IOException {
		if (retriesLeft >= 0) {
			DistributedCommandResponse del1Response = broadCastDistributedDelOne(dataFromClient);
			if (ACK.equals(del1Response.getTransactionState())) {
				DistributedCommandResponse del2Response = broadCastDistributedDelTwo(
						dataFromClient);

				if (ACK.equals(del2Response.getTransactionState())) {
					log("handleDelete - broadCastDistributedDelTwo ACK - response - " + del2Response);
					get().del(getKey(dataFromClient));
					writeResponse(getDelCommandResponse(dataFromClient));
				} else {
					log("handleDelete - broadCastDistributedDelTwo ABORT - response - " + del2Response);
					writeResponse("ERROR-FAILED to execute command ddel2. This should not happen. Command - " +
							getDelCommandResponse(dataFromClient));
				}
				return;
			} else {
				log("handleDelete - got ABORT from " + del1Response);
				broadCastDistributedDelAbort(del1Response);
				retriesLeft--;
				log("handleDelete - retrying handleDeletaAgain - retriesLeft : " + retriesLeft);
				handleDelete(dataFromClient);
			}
		} else {
			writeResponse("ERROR-FAILED to execute command - " + getDelCommandResponse(dataFromClient));
		}
	}

	private void broadCastDistributedDelAbort(DistributedCommandResponse del1Response) {
		String distributeDelAbortCommand = format(DISTRIBUTED_DEL_ABORT, del1Response.getKey());
		broadCastCommand(del1Response.getNodes(), del1Response.getKey(), del1Response.getValue(),
				distributeDelAbortCommand);
	}

	private String getDelCommandResponse(String[] dataFromClient) {
		return DEL.getCommand() + " key=" + getKey(dataFromClient);
	}

	private DistributedCommandResponse broadCastDistributedDelTwo(String[] dataFromClient) {
		String distributedDeleteOneCommand = getDistributedDeleteCommand(dataFromClient, 2);
		log("broadCastDistributedDelTwo - " + distributedDeleteOneCommand);
		if (GenericNode.getNodeDirectory().isPresent())
			return broadCastCommand(GenericNode.getNodeDirectory().get().getNodes(), getKey(dataFromClient),
					getValue(dataFromClient), distributedDeleteOneCommand);

		return new DistributedCommandResponse(getKey(dataFromClient), getValue(dataFromClient), distributedDeleteOneCommand);
	}

	private DistributedCommandResponse broadCastDistributedDelOne(String[] dataFromClient) {
		String distributedDeleteOneCommand = getDistributedDeleteCommand(dataFromClient, 1);
		log("broadCastDistributedDelOne - " + distributedDeleteOneCommand);
		if (GenericNode.getNodeDirectory().isPresent())
			return broadCastCommand(GenericNode.getNodeDirectory().get().getNodes(), getKey(dataFromClient),
					getValue(dataFromClient), distributedDeleteOneCommand);

		return new DistributedCommandResponse(getKey(dataFromClient), getValue(dataFromClient), distributedDeleteOneCommand);
	}

	private DistributedCommandResponse broadCastCommand(List<INode> nodes, String key, String value, String command) {
		DistributedCommandResponse distributedCommandResponse = new DistributedCommandResponse(key, value, command);
		log("broadCastCommand - from leader -  " + request.getServerNode());
		for (INode node : nodes) {
			log("broadCastCommand: command: " + command + ", to node : " + node);
			if (!node.equals(request.getServerNode())) {
				String response = send(node.getHost(), node.getPort(), command, false);
				if (ACK.name().equalsIgnoreCase(response)) {
					distributedCommandResponse.addNode(node);
					distributedCommandResponse.setTransactionState(ACK);
				} else {
					distributedCommandResponse.setTransactionState(ABORT);
				}
				log("broadCastCommand - response : " + distributedCommandResponse);

			}
		}
		return distributedCommandResponse;
	}

	private String getDistributedDeleteCommand(String[] dataFromClient, int number) {
		if (number == 1) {
			return format(DISTRIBUTED_DEL_ONE, getKey(dataFromClient));
		} else {
			return format(DISTRIBUTED_DEL_TWO, getKey(dataFromClient));
		}
	}

	private String getDistributedPutCommand(String[] dataFromClient, int number) {
		if (number == 1) {
			return format(DISTRIBUTED_PUT_ONE, getKey(dataFromClient),
					getValue(dataFromClient));
		} else {
			return format(DISTRIBUTED_PUT_TWO, getKey(dataFromClient),
					getValue(dataFromClient));
		}
	}

	private String getInput(Socket clientSocket) throws IOException {
		return getInputFromClient(clientSocket);
	}

	private void writeResponse(String response) throws IOException {
		Socket clientSocket = request.getClientSocket();
		PrintWriter out =
				new PrintWriter(new OutputStreamWriter(clientSocket.getOutputStream(), StandardCharsets.UTF_8), true);
		out.println(response);
		out.close();
	}

	@Override
	public void run() {
		handle();
	}

	private String getKey(String[] dataFromClient) {
		return dataFromClient[1].trim();
	}

	private String getValue(String[] dataFromClient) {
		if (dataFromClient.length > 2)
			return dataFromClient[2].trim();

		return "";
	}

	private String getCommand(String[] dataFromClient) {
		return dataFromClient[0].trim();
	}
}
