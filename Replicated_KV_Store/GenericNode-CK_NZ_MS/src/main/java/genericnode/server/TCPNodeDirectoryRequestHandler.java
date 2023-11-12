package genericnode.server;

import static genericnode.client.TCPClient.END_OF_LINE;
import static genericnode.common.KeyValueStoreCommand.DEL;
import static genericnode.common.KeyValueStoreCommand.GET;
import static genericnode.common.KeyValueStoreCommand.PUT;
import static genericnode.common.KeyValueStoreCommand.valueOf;
import static genericnode.server.KeyValueStoreService.get;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

import genericnode.common.KeyValueStoreCommand;

public class TCPNodeDirectoryRequestHandler implements RequestHandler, Runnable {
	private final Request request;
	private static final String COMMAND_SEPARATOR = " ";

	TCPNodeDirectoryRequestHandler(Request request) {
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
				KeyValueStoreCommand command = valueOf(dataFromClient[0].toUpperCase());
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

	private void handlePut(String[] dataFromClient) throws IOException {
		get().put(dataFromClient[1].trim(), dataFromClient[2].trim());
		writeResponse(PUT.getCommand() + " key=" + dataFromClient[1].trim());
	}

	private void handleGet(String[] dataFromClient) throws IOException {
		String response = GET.getCommand() + " key=" + dataFromClient[1].trim();
		response += " " + GET.getCommand() + " val=" + get().get(dataFromClient[1].trim());
		writeResponse(response);
	}

	private void handleStore() throws IOException {
		writeResponse(get().store());
	}

	private void handleDelete(String[] dataFromClient) throws IOException {
		get().del(dataFromClient[1].trim());
		writeResponse(DEL.getCommand() + " key=" + dataFromClient[1].trim());
	}

	private String getInput(Socket clientSocket) throws IOException {
		return getInputFromClient(clientSocket);
	}

	static String getInputFromClient(Socket clientSocket) throws IOException {
		BufferedReader in = new BufferedReader(
				new InputStreamReader(clientSocket.getInputStream(), StandardCharsets.UTF_8));
		String input;
		StringBuilder fullInput = new StringBuilder();
		while ((input = in.readLine()) != null && !END_OF_LINE.equalsIgnoreCase(input)) {
			fullInput.append(input);
		}

		return fullInput.toString();
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
		return dataFromClient[1];
	}
}
