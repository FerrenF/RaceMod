package core.gfx.cache;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.Serializable;
import java.util.Collections;
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

	public void saveCache() {	
		
		if(this.indexMap == null) {
			DebugHelper.handleDebugMessage("Failed to save cache because indexMap is null.", 30, MESSAGE_TYPE.ERROR);	
			return;
		}
		
	    try (RandomAccessFile raf = new RandomAccessFile(dataFile, "rw")) {
	    	
	  
	        long offset = 0;
	        
	        for (Integer key : queriedKeys) {
	            TexCacheElement element = loaded.get(key);
	            if (element == null) continue;

	            try {
	                byte[] bytes = serializeTextureData(element.textureData);
	                raf.seek(offset);
	                raf.write(bytes);
	                indexMap.put(key, new CacheIndexEntry(offset, bytes.length, element.hash)); // Overwrite or insert
	                offset += bytes.length;
	            } catch (IOException e) {
	                DebugHelper.handleFormattedDebugMessage("Failed to serialize texture for key %s", 30, MESSAGE_TYPE.ERROR, new Object[] {key});            
	                e.printStackTrace();
	            }
	        }

	        // Save index
	        GameCache.cacheObject(indexMap, path + ".idx");
	        
	        if (!indexFile.exists() || indexMap.isEmpty()) {
	        	DebugHelper.handleFormattedDebugMessage("Warning: indexMap not written or is empty after save for %s", 30,
	        			MESSAGE_TYPE.WARNING, new Object[] { path + ".idx" });
	        }
	        
	        DebugHelper.handleFormattedDebugMessage("Saved %d entries to texture index map at %s", 30,
	                MESSAGE_TYPE.DEBUG, new Object[] { indexMap.size(), path + ".idx" });
	        this.loaded.clear();
	        this.queriedKeys.clear();
	        this.isDirty = false;
	        
	    } catch (IOException e) {
	        e.printStackTrace();
	    }
	}
	
	
	public boolean wasLoadFailed(int key) {
	    return failedKeys.contains(key);
	}
	
	public void set(int hash, TexCacheElement element) {	
		
		this.indexMap.put(hash, new CacheIndexEntry(-1, -1, element.hash)); // Placeholder until saved
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
	
	public void cleanupStaleEntries() {
	  
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
	private int cleanupInterval = 600; // seconds
	private void maybeCleanup() {
	    long now = System.currentTimeMillis() / 1000;
	    if (now - lastCleanupCheck > cleanupInterval) {
	        lastCleanupCheck = now;
	        cleanupStaleEntries();
	    }
	}
	
	private int saveInterval = 30;
	private long lastCacheSaveEvent = 0;
	public TexCacheElement get(Integer key) {	
		
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
		  try {
		        byte[] bytes = new byte[entry.length];
		        dataAccess.seek(entry.offset);
		        dataAccess.readFully(bytes);
		        return new TexCacheElement(hashCode, deserializeTextureData(bytes));
		    } catch (IOException e) {
		    	DebugHelper.handleFormattedDebugMessage("Failed to read from %s at index %d with length %d: %s", 30,
		    				MESSAGE_TYPE.ERROR, new Object[] {dataFile.getAbsolutePath(), entry.offset, entry.length, e.getMessage()});	
		        e.printStackTrace();
		        return null;
		    }	  
	}
	
	public <C extends TexCacheElement> C loadDiskCacheElement(int hashCode, Class<? extends C> expectedType) {
	    TexCacheElement result = loadDiskCacheElement(hashCode);
	    if (result == null || !expectedType.isInstance(result)) return null;
	    return expectedType.cast(result);
	}
	
	public static byte[] serializeTextureData(GameTextureData data) throws IOException {
	    ByteArrayOutputStream baos = new ByteArrayOutputStream();
	    DataOutputStream dos = new DataOutputStream(baos);
	    dos.writeInt(data.blendQuality.ordinal());
	    dos.writeInt(data.width);
	    dos.writeInt(data.height);
	    byte[] buffer = data.getBuffer(); // This must return the raw texture bytes
	    dos.writeInt(buffer.length);
	    dos.write(buffer);
	    
	    dos.flush();
	    return baos.toByteArray();
	}

	public static GameTextureData deserializeTextureData(byte[] data) throws IOException {
	    DataInputStream dis = new DataInputStream(new ByteArrayInputStream(data));

	    BlendQuality bq = BlendQuality.values()[dis.readInt()];
	    int width = dis.readInt();
	    int height = dis.readInt();
	    int length = dis.readInt();
	    byte[] buffer = new byte[length];
	    dis.readFully(buffer);
	    
	    return new GameTextureData(width, height, buffer, false, bq); // Replace with the actual constructor
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
