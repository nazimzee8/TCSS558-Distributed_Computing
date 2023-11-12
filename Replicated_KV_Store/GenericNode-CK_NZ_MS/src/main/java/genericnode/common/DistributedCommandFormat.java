package genericnode.common;

import static genericnode.common.KeyValueStoreCommand.DDEL1;
import static genericnode.common.KeyValueStoreCommand.DDEL2;
import static genericnode.common.KeyValueStoreCommand.DDELABORT;
import static genericnode.common.KeyValueStoreCommand.DPUT1;
import static genericnode.common.KeyValueStoreCommand.DPUT2;
import static genericnode.common.KeyValueStoreCommand.DPUTABORT;

public class DistributedCommandFormat {

	public static String DISTRIBUTED_PUT_ONE = DPUT1.command + " %s %s";
	public static String DISTRIBUTED_PUT_TWO = DPUT2.command + " %s %s";
	public static String DISTRIBUTED_PUT_ABORT = DPUTABORT.command + " %s %s";

	public static String DISTRIBUTED_DEL_ONE = DDEL1.command + " %s";
	public static String DISTRIBUTED_DEL_TWO = DDEL2.command + " %s";
	public static String DISTRIBUTED_DEL_ABORT = DDELABORT.command + " %s";
}
