package genericnode.server;

import genericnode.common.KeyValueStore;

public interface DistributedKeyValueStore extends KeyValueStore {

    String distributedPut1(String key, String value);

    String distributedPut2(String key, String value);

    String distributedPutAbort(String key, String value);

    String distributedDelete1(String key, String value);

    String distributedDelete2(String key, String value);

    String distributedDeleteAbort(String key, String value);
}
