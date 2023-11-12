package genericnode.server;

import genericnode.common.KeyValueStore;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;

public class KeyValueStoreService implements KeyValueStore {
	private static final Map<String, String> dataMap = new ConcurrentHashMap<>();
	private static final Map<String, Boolean> locks = new ConcurrentHashMap();
	private static final int MAXIMUM_LENGTH_OF_STORE_OUTPUT = 65_000;
	private static final String TRIMMED = "TRIMMED:";

	private static final KeyValueStore INSTANCE = new KeyValueStoreService();

	static KeyValueStore get() {
		return INSTANCE;
	}

	@Override
	public void put(String key, String value) {
		dataMap.put(key, value);
	}

	@Override
	public String get(String key) {
		return dataMap.get(key);
	}

	@Override
	public void del(String key) {
		dataMap.remove(key);
	}

	@Override
	public String store() {
		StringBuilder keyValueDisplay = new StringBuilder();
		int counter = 0;
		synchronized (this) {
			for (Map.Entry<String, String> dataEntry : dataMap.entrySet()) {
				keyValueDisplay
						.append(java.lang.String.format("key:%s:value:%s", dataEntry.getKey(), dataEntry.getValue()));

				counter++;
				if (counter != dataMap.size())
					keyValueDisplay.append("\n");

				if (keyValueDisplay.length() >= MAXIMUM_LENGTH_OF_STORE_OUTPUT) {
					return TRIMMED + keyValueDisplay.substring(0, MAXIMUM_LENGTH_OF_STORE_OUTPUT);
				}
			}
		}

		return keyValueDisplay.toString();
	}

	@Override
	public void exit() {
		dataMap.clear();
	}

	@Override
	public boolean lockIfAvailable(String key) {
		return locks.compute(key, (k, v) -> {
			if (v == null)
				return TRUE;

			return FALSE;
		});
	}

	@Override
	public boolean unlock(String key) {
		return locks.remove(key);
	}
}
