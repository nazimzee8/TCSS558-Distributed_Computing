package genericnode.common;

public enum KeyValueStoreCommand {
	PUT("put"),
	GET("get"),
	DEL("del"),
	STORE("store"),
	EXIT("exit");
	String command;

	KeyValueStoreCommand(String command) {
		this.command = command;
	}

	public String getCommand() {
		return command;
	}
}
