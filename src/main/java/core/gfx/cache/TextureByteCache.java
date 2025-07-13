package core.gfx.cache;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import core.gfx.texture.AsyncTextureLoader;
import helpers.DebugHelper;
import helpers.DebugHelper.MESSAGE_TYPE;
import necesse.engine.GameCache;
import necesse.gfx.gameTexture.GameTexture.BlendQuality;
import necesse.gfx.ui.GameTextureData;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;

public class TextureByteCache {
	public final String path;	
	
	private Map<Integer, CacheIndexEntry> indexMap = new HashMap<>();
	private File dataFile;
	private File indexFile;
	private RandomAccessFile dataAccess;
	private AsyncTextureLoader loader;
	protected HashSet<Integer> queriedKeys = new HashSet<Integer>();
	private final Set<Integer> requestedKeys = Collections.synchronizedSet(new HashSet<>());
	private final Set<Integer> failedKeys = ConcurrentHashMap.newKeySet();
	protected Map<Integer, TexCacheElement> loaded;

	protected boolean isDirty = false;

	public TextureByteCache(String path, AsyncTextureLoader loader) {
		this.loaded = Collections.synchronizedMap(new TextureDataLinkedMap(this,500));
		this.path = path;
		this.dataFile = GameCache.getCacheFile(path + ".bin");	
		this.indexFile = GameCache.getCacheFile(path + ".idx");
		this.loader = loader;		
	}

	@SuppressWarnings("unchecked")
	public void loadCache() {
		
        this.indexMap = (Map<Integer, CacheIndexEntry>) GameCache.getObject(path + ".idx", HashMap.class); 
        if (this.indexMap == null || this.indexMap.isEmpty()) {
            if (indexFile.length() > 0) {
                DebugHelper.handleFormattedDebugMessage("Warning: Index file at %s is not empty but could not be read. Possible corruption.", 30,
                    MESSAGE_TYPE.WARNING, new Object[] { indexFile.getAbsolutePath() });
            }
        	DebugHelper.handleFormattedDebugMessage("No cache index found at %s. Creating a new one.", 30, MESSAGE_TYPE.DEBUG,
	    			new Object[] {GameCache.cachePath() + path + ".idx"});	
        	this.indexMap = new HashMap<>();
        	try {
				this.indexFile.createNewFile();
			} catch (IOException e) {
				DebugHelper.handleFormattedDebugMessage("Failed to create a cache index file at %s", 30, MESSAGE_TYPE.ERROR,
		    			new Object[] {indexFile.getAbsolutePath()});	
			}
        }

	    try {
	        this.dataAccess = new RandomAccessFile(dataFile, "rw");
	    } catch (IOException e) {
	    	DebugHelper.handleFormattedDebugMessage("Failed to create a random data access file for cache at %s", 30, MESSAGE_TYPE.ERROR,
	    			new Object[] {dataFile.getAbsolutePath()});	
	        this.dataAccess = null;
	    }
	}
	
	private final Object ioLock = new Object();
	public void saveCache() {
	    synchronized (ioLock) {
	        if (dataAccess == null) return;

	        if (indexMap == null) {
	            DebugHelper.handleDebugMessage("Failed to save cache because indexMap is null.", 30, MESSAGE_TYPE.ERROR);
	            return;
	        }

	        try (RandomAccessFile raf = new RandomAccessFile(dataFile, "rw")) {

	            long currentOffset = dataFile.length(); // Always append

	            for (Integer key : queriedKeys) {
	                TexCacheElement element = loaded.get(key);
	                if (element == null) continue;

	                CacheIndexEntry indexEntry = indexMap.get(key);
	                boolean isPlaceholder = indexEntry == null || indexEntry.offset < 0 || indexEntry.length < 0;

	                if (!isPlaceholder) {
	                    // Already written to disk; no need to re-write
	                    continue;
	                }
	                
	                if (indexEntry != null && indexEntry.offset >= 0 && indexEntry.length >= 0 && indexEntry.hash == element.hash) {
	                    continue; // Already saved with same hash
	                }
	                
	                try {
	                    byte[] bytes = serializeTextureData(element.textureData);
	                    raf.seek(currentOffset);
	                    raf.write(bytes);

	                    indexMap.put(key, new CacheIndexEntry(currentOffset, bytes.length, element.hash));
	                    currentOffset += bytes.length;
	                } catch (IOException e) {
	                    DebugHelper.handleFormattedDebugMessage("Failed to serialize texture for key %s", 30,
	                            MESSAGE_TYPE.ERROR, key);
	                    e.printStackTrace();
	                }
	            }

	        } catch (IOException e) {
	            DebugHelper.handleDebugMessage("Cache failed to save: " + e.getMessage(), 30, MESSAGE_TYPE.ERROR);
	            e.printStackTrace();
	        }

	        // Save index
	        GameCache.cacheObject(indexMap, path + ".idx");

	        if (!indexFile.exists() || indexMap.isEmpty()) {
	            DebugHelper.handleFormattedDebugMessage("Warning: indexMap not written or is empty after save for %s", 30,
	                    MESSAGE_TYPE.WARNING, path + ".idx");
	        }

	        DebugHelper.handleFormattedDebugMessage("Saved %d entries to texture index map at %s", 30,
	                MESSAGE_TYPE.DEBUG, indexMap.size(), path + ".idx");

	        loaded.clear();
	        queriedKeys.clear();
	        isDirty = false;
	    }
	}

	public boolean wasLoadFailed(int key) {
	    return failedKeys.contains(key);
	}
	
	public void set(int hash, TexCacheElement element) {	
		
		 if (!indexMap.containsKey(hash)) {
		        // Only insert placeholder if this is truly a new entry
		        indexMap.put(hash, new CacheIndexEntry(-1, -1, element.hash));
		    }
	    this.loaded.put(hash, element);
	    failedKeys.remove(hash); 
	    this.queriedKeys.add(hash);
	    this.isDirty = true;
	    
	    if(this.isDirty && ((System.currentTimeMillis() / 1000) - lastCacheSaveEvent) > saveInterval) {
	    	DebugHelper.handleDebugMessage("Saved cache interval triggered.", 60, MESSAGE_TYPE.DEBUG);	
	    	lastCacheSaveEvent = (System.currentTimeMillis() / 1000);
	    	this.saveCache();
	    }	    
	}
	
	private boolean validateTextureData(GameTextureData data) {
	    if (data == null || data.width <= 0 || data.height <= 0) return false;
	    if (data.getBuffer() == null || data.getBuffer().length == 0) return false;
	    BlendQuality bq = data.blendQuality;
	    return bq == null || EnumSet.allOf(BlendQuality.class).contains(bq);
	}
	
	public void compactCache() {
	    synchronized (ioLock) {
	        DebugHelper.handleDebugMessage("Compacting cache files...", 50, MESSAGE_TYPE.INFO);

	        if (indexMap.size() <= 1) {
	            DebugHelper.handleDebugMessage("Index map is too small... Skipping compaction", 50, MESSAGE_TYPE.INFO);
	            return;
	        }

	        Map<Integer, CacheIndexEntry> newIndexMap = new HashMap<>();

	        try {
	            // Prepare byte output stream for new .bin file
	            ByteArrayOutputStream dataOut = new ByteArrayOutputStream();
	            long currentOffset = 0;

	            for (Map.Entry<Integer, CacheIndexEntry> entry : indexMap.entrySet()) {
	                int hashCode = entry.getKey();
	                CacheIndexEntry oldEntry = entry.getValue();
	                if (!isValidEntry(oldEntry)) continue;

	                try {
	                    TexCacheElement element = loadDiskCacheElement(hashCode);
	                    if (element != null && validateTextureData(element.textureData)) {
	                        byte[] serialized = serializeTextureData(element.textureData);
	                        dataOut.write(serialized);

	                        CacheIndexEntry newEntry = new CacheIndexEntry(currentOffset, serialized.length, hashCode);
	                        newIndexMap.put(hashCode, newEntry);

	                        currentOffset += serialized.length;
	                    }
	                } catch (IOException e) {
	                    DebugHelper.handleFormattedDebugMessage(
	                        "Skipping invalid entry %d during compaction: %s", 30,
	                        MESSAGE_TYPE.WARNING, new Object[]{hashCode, e.getMessage()}
	                    );
	                }
	            }

	            // Overwrite data file directly
	            Files.write(dataFile.toPath(), dataOut.toByteArray(),
	                    StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

	            // Save new index file directly
	            GameCache.cacheObject(newIndexMap, path + ".idx"); // make sure this overwrites

	            // Replace in-memory state
	            this.indexMap = newIndexMap;

	            // Reopen dataAccess for the rewritten file
	            if (dataAccess != null) {
	                dataAccess.close();
	            }
	            this.dataAccess = new RandomAccessFile(dataFile, "rw");

	            DebugHelper.handleFormattedDebugMessage(
	                "Texture cache compaction completed successfully: %s", 30,
	                MESSAGE_TYPE.INFO, new Object[]{path}
	            );

	        } catch (IOException e) {
	            DebugHelper.handleFormattedDebugMessage(
	                "Error during texture cache compaction: %s", 30,
	                MESSAGE_TYPE.ERROR, new Object[]{e.getMessage()}
	            );
	            e.printStackTrace();
	        }
	    }
	}

	
	public void clearLoadedCache() {
	    this.loaded.clear();
	}
	
	public boolean isLoading(Integer key) {
		return this.requestedKeys.contains(key);
	}
	public boolean indexContainsKey(Integer key) {
		return this.indexMap.containsKey(key);
	}
	
	public boolean containsLoadedKey(Integer key) {
		   return this.loaded.containsKey(key) && !failedKeys.contains(key);
	}
	private long lastCleanupCheck = 0;
	private int cleanupInterval = 1200;
	private void maybeCleanup() {
	    long now = System.currentTimeMillis() / 1000;
	    if (now - lastCleanupCheck > cleanupInterval) {
	        lastCleanupCheck = now;
	        compactCache();
	    }
	}
	
	private int saveInterval = 30;
	private long lastCacheSaveEvent = 0;
	public TexCacheElement get(Integer key) {	
		
		synchronized (ioLock) {
		    if (failedKeys.contains(key)) return null;
		    this.queriedKeys.add(key);
		    
		    maybeCleanup();
		    if(this.isDirty && ((System.currentTimeMillis() / 1000) - lastCacheSaveEvent) > saveInterval) {
		    	DebugHelper.handleDebugMessage("Saved cache interval triggered.", 60, MESSAGE_TYPE.DEBUG);	
		    	lastCacheSaveEvent = (System.currentTimeMillis() / 1000);
		    	this.saveCache();
		    }
		    
		    TexCacheElement element = this.loaded.getOrDefault(key, null);
		    if (element != null) return element;
	
		    if (indexMap != null) {
			    CacheIndexEntry entry = indexMap.get(key);
			    if (entry == null || dataAccess == null) return null;
		
			    synchronized (requestedKeys) {
			        if (!requestedKeys.contains(key) && !loaded.containsKey(key)) {
			            requestedKeys.add(key);
			            try {
			            	DebugHelper.handleDebugMessage("Requesting key " + key + " from disk cache loader.", 60, MESSAGE_TYPE.DEBUG);
			                loader.requestLoadFromCache(key, null);
			            } catch (Exception e) {
			                e.printStackTrace();
			                requestedKeys.remove(key);
			            }
			        }
			    }
		    }
		    return null;
		}
	}
	
	public <C extends TexCacheElement> C get(Integer key, Class<? extends C> expectedType) {	
		TexCacheElement result = this.get(key);
		if (result == null || !expectedType.isInstance(result)) return null;
		return expectedType.cast(result);
	}

	
	public void markLoadComplete(int key) {
	    requestedKeys.remove(key);
	}
	
	public void markLoadFailed(int cacheKey) {
		 requestedKeys.remove(cacheKey);
		
	}
	
	public TexCacheElement loadDiskCacheElement(int hashCode) {
	    CacheIndexEntry entry = indexMap.get(hashCode);

	    // Null check for safety
	    if (entry == null) {
	        DebugHelper.handleFormattedDebugMessage("Missing cache index entry for hash %d", 30,
	                MESSAGE_TYPE.ERROR, new Object[]{hashCode});
	        return null;
	    }

	    try {
	        // Ensure entry length is within reasonable bounds
	        if (entry.length <= 0 || entry.length > 10_000_000) {
	            DebugHelper.handleFormattedDebugMessage("Invalid entry length %d for hash %d", 30,
	                    MESSAGE_TYPE.ERROR, new Object[]{entry.length, hashCode});
	            return null;
	        }

	        
	        // Check that the offset + length is within the file size
	        long fileLength = dataAccess.length();
	        if (entry.offset < 0 || entry.offset + entry.length > fileLength) {
	            DebugHelper.handleFormattedDebugMessage(
	                    "Invalid offset/length for entry: offset=%d, length=%d, fileLength=%d",
	                    30,
	                    MESSAGE_TYPE.ERROR,
	                    new Object[]{entry.offset, entry.length, fileLength});
	            return null;
	        }

	        byte[] bytes = new byte[entry.length];
	        dataAccess.seek(entry.offset);
	        dataAccess.readFully(bytes);

	        return new TexCacheElement(hashCode, deserializeTextureData(bytes));

	    } catch (IOException e) {
	        DebugHelper.handleFormattedDebugMessage("Failed to read from %s at index %d with length %d: %s", 30,
	                MESSAGE_TYPE.ERROR,
	                new Object[]{dataFile.getAbsolutePath(), entry.offset, entry.length, e.getMessage()});
	        e.printStackTrace();
	        return null;
	    } catch (Exception e) {
	        DebugHelper.handleFormattedDebugMessage("Unexpected error during cache load for hash %d: %s", 30,
	                MESSAGE_TYPE.ERROR, new Object[]{hashCode, e.getMessage()});
	        e.printStackTrace();
	        return null;
	    }
	}
	
	public <C extends TexCacheElement> C loadDiskCacheElement(int hashCode, Class<? extends C> expectedType) {
	    TexCacheElement result = loadDiskCacheElement(hashCode);
	    if (result == null || !expectedType.isInstance(result)) return null;
	    return expectedType.cast(result);
	}
	
	private boolean isValidEntry(CacheIndexEntry entry) {
	    try {
	        if (entry.offset < 0 || entry.length <= 0) return false;
	        if (entry.offset + entry.length > dataAccess.length()) return false;
	        return true;
	    } catch (IOException e) {
	        return false;
	    }
	}
	
	public static byte[] serializeTextureData(GameTextureData data) throws IOException {
	    ByteArrayOutputStream baos = new ByteArrayOutputStream();
	    DataOutputStream dos = new DataOutputStream(baos);

	    // Serialize BlendQuality by name (or "null")
	    dos.writeUTF(data.blendQuality != null ? data.blendQuality.name() : "null");

	    dos.writeInt(data.width);
	    dos.writeInt(data.height);

	    byte[] buffer = data.getBuffer();
	    dos.writeInt(buffer.length);
	    dos.write(buffer);

	    dos.flush();
	    return baos.toByteArray();
	}

	public static GameTextureData deserializeTextureData(byte[] data) throws IOException {
	    DataInputStream dis = new DataInputStream(new ByteArrayInputStream(data));

	    // Deserialize BlendQuality by name
	    String bqName = dis.readUTF();
	    BlendQuality bq = null;
	    if (!"null".equals(bqName)) {
	        try {
	            bq = BlendQuality.valueOf(bqName);
	        } catch (IllegalArgumentException e) {
	            throw new IOException("Unknown BlendQuality name: " + bqName, e);
	        }
	    }

	    int width = dis.readInt();
	    int height = dis.readInt();
	    int length = dis.readInt();

	    if (length < 0 || length > 10_000_000) {
	        throw new IOException("Invalid texture buffer length: " + length);
	    }

	    byte[] buffer = new byte[length];
	    dis.readFully(buffer);

	    return new GameTextureData(width, height, buffer, false, bq);
	}
		
	public class TextureDataLinkedMap extends LinkedHashMap<Integer, TexCacheElement>{
		private final TextureByteCache parent;
    	private final int maxCacheSize;
		private static final long serialVersionUID = 1L;
		
		public TextureDataLinkedMap(TextureByteCache parent, int maxCacheSize) {
		    super(50, 0.75f, true);
		    this.maxCacheSize = maxCacheSize;
		    this.parent = parent;
		}
		
		@Override
        protected boolean removeEldestEntry(Map.Entry<Integer, TexCacheElement> eldest) {
			  if (size() > maxCacheSize) {
			        onEvict(eldest);
			        return true;
			    }
			    return false;
        }

		private void onEvict(Map.Entry<Integer, TexCacheElement> eldest) {
		    Integer key = eldest.getKey();
		    
		    parent.queriedKeys.remove(key);
		}
    }
	
	public static enum TexCacheElementType {
		TEX_ELEMENT,
		TEX_VARIANT_ELEMENT
	}
	
	public static class TexVariantCacheElement extends TexCacheElement {
		private static final long serialVersionUID = 156L;
		public GameTextureData paletteData;
		
		public TexVariantCacheElement(int hash, GameTextureData textureData, GameTextureData paletteData) {
			super(hash, textureData, TexCacheElementType.TEX_VARIANT_ELEMENT);
			this.paletteData = paletteData;
		}
	}
	
	public static class TexCacheElement implements Serializable {
		private static final long serialVersionUID = 155L;
		public int hash;
		public GameTextureData textureData;
		public final TexCacheElementType texCacheElementType;
		
		public TexCacheElement(int hash, GameTextureData textureData, TexCacheElementType elementType) {
			this.hash = hash;
			this.textureData = textureData;
			this.texCacheElementType = elementType;
		}
		public TexCacheElement(int hash, GameTextureData textureData) {
			this(hash, textureData, TexCacheElementType.TEX_ELEMENT);
		}
	}

	
}
