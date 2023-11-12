package genericnode.client;

import static genericnode.common.KeyValueStoreCommand.EXIT;
import static genericnode.common.KeyValueStoreCommand.STORE;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

public class TCPClient {
	private static final String SERVER_RESPONSE = "server response:";
	public static final String END_OF_LINE = "END_OF_LINE";

	public static void send(String hostName, int portNumber, String message) {
		send(hostName, portNumber, message, true);
	}

	public static String send(String hostName, int portNumber, String message, boolean printMessage) {
		String response = null;
		try (
				Socket clientSocket = new Socket(hostName, portNumber);
				PrintWriter out =
						new PrintWriter(new OutputStreamWriter(clientSocket.getOutputStream(), "UTF-8"), true);
				BufferedReader in =
						new BufferedReader(
								new InputStreamReader(clientSocket.getInputStream(), "UTF-8"));
		) {
			out.println(message);
			out.println(END_OF_LINE);

			StringBuilder responseFromServer = new StringBuilder();
			String responseSingleLine = in.readLine();
			while (responseSingleLine != null) {
				responseFromServer.append(responseSingleLine);
				responseSingleLine = in.readLine();
				if (responseSingleLine != null)
					responseFromServer.append("\n");
			}

			response = responseFromServer.toString();
			if (printMessage)
				printResponse(message, response);

			return response;
		} catch (UnknownHostException e) {
			System.err.println("Don't know about host " + hostName);
			System.exit(1);
		} catch (IOException e) {
			System.err.println("Couldn't get I/O for the connection to " +
					hostName);
			e.printStackTrace();
			System.exit(1);
		}
		return response;
	}

	private static void printResponse(String commamd, String responseFromServer) {
		if (commamd.startsWith(STORE.getCommand())) {
			System.out.println(SERVER_RESPONSE);
			System.out.println(responseFromServer);
		} else if (commamd.startsWith(EXIT.getCommand())) {
			System.out.println("<the server then exits> ");
		} else {
			System.out.println(SERVER_RESPONSE + responseFromServer);
		}
	}
}
