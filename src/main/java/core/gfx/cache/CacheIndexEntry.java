package core.gfx.cache;

import java.io.Serializable;

public class CacheIndexEntry implements Serializable {
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public final long offset;
    public final int length;
    public final int hash;

    public CacheIndexEntry(long offset, int length, int hash) {
        this.offset = offset;
        this.length = length;
        this.hash = hash;
    }
}
