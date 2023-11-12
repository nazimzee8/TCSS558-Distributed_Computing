package genericnode.common;

public enum KeyValueStoreCommand {
	PUT("put"),
	GET("get"),
	DEL("del"),
	STORE("store"),
	EXIT("exit"),
	DPUT1("dput1"),
	DPUT2("dput2"),
	DDEL1("ddel1"),
	DDEL2("ddel2"),
	DPUTABORT("dputabort"),
	DDELABORT("ddelabort");
	String command;

	KeyValueStoreCommand(String command) {
		this.command = command;
	}

	public String getCommand() {
		return command;
	}

}
