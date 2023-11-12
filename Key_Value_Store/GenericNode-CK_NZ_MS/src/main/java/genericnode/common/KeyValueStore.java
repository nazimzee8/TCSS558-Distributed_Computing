package genericnode.common;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Key Value Store
 */
public interface KeyValueStore extends Remote {
	/**
	 * put <key> <value>
	 * Put a value to the key-value store. The key uniquely identifies the object
	 * to store. A unique key maps to only one object which has a unique
	 * value. If multiple clients write to the same key, writes should be
	 * synchronized
	 *
	 * @param key
	 * @param value
	 */
	void put(String key, String value) throws RemoteException;

	/**
	 * get <key>
	 * Returns the value stored at <key>.
	 *
	 * @param key
	 * @return
	 */
	String get(String key) throws RemoteException;

	/**
	 * del <key>
	 * Deletes the value stored at <key> from the key value store.
	 *
	 * @param key
	 */
	void del(String key) throws RemoteException;

	/**
	 * store
	 * Prints the contents of the entire key-value store. You may optionally
	 * truncate the output after returning 65,000 characters. If the contents of
	 * the key value store exceed 65,000 by
	 *
	 * @return
	 */
	String store() throws RemoteException;

	/**
	 * Exit
	 * When the exit command is sent by the client, the server is shutdown.
	 */
	void exit() throws RemoteException;
}
