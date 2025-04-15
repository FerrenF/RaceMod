package core.gfx.cache;

public class CacheIndexEntry {
    public final long offset;
    public final int length;
    public final int hash;

    public CacheIndexEntry(long offset, int length, int hash) {
        this.offset = offset;
        this.length = length;
        this.hash = hash;
    }
}
