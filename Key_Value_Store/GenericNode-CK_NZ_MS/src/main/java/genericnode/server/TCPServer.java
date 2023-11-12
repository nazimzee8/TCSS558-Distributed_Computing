package genericnode.server;

import static genericnode.client.TCPClient.END_OF_LINE;
import static genericnode.server.KeyValueStoreService.get;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

import genericnode.common.KeyValueStoreCommand;

public class TCPServer extends Thread {
	private boolean exit = false;
	private ServerSocket serverSocket;

	public TCPServer(int portNumber) throws IOException {
		serverSocket = new ServerSocket(portNumber);
	}

	@Override
	public void run() {
		while (!exit) {
			try {
				Socket clientSocket = serverSocket.accept();

				BufferedReader in = new BufferedReader(
						new InputStreamReader(clientSocket.getInputStream(), "UTF-8"));
				String input = null;
				StringBuilder fullInput = new StringBuilder();
				while ((input = in.readLine()) != null && !END_OF_LINE.equalsIgnoreCase(input)) {
					fullInput.append(input);
				}

				String response = "";
				if (fullInput != null) {
					final String[] dataFromClient = fullInput.toString().split(" ");
					KeyValueStoreCommand command = KeyValueStoreCommand.valueOf(dataFromClient[0].toUpperCase());
					switch (command) {
						case PUT:
							get().put(dataFromClient[1].trim(), dataFromClient[2].trim());
							response = "put key=" + dataFromClient[1].trim();
							break;
						case GET:
							response = "get key=" + dataFromClient[1].trim();
							response += " get val=" + get().get(dataFromClient[1].trim());
							break;
						case STORE:
							response = get().store();
							break;
						case DEL:
							get().del(dataFromClient[1].trim());
							response = "delete key=" + dataFromClient[1].trim();
							break;
						case EXIT:
							exit = true;
							response = "<the server then exits>";
							break;

						default:
							throw new IllegalStateException("Unexpected value: " + dataFromClient[0]);
					}
				}

				PrintWriter out =
						new PrintWriter(new OutputStreamWriter(clientSocket.getOutputStream(), "UTF-8"), true);
				out.println(response);
				out.close();

			} catch (IOException e) {
				exit = true;
				e.printStackTrace();
			}
		}
	}
}
