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

import core.gfx.texture.AsyncTextureLoader;
import helpers.DebugHelper;
import helpers.DebugHelper.MESSAGE_TYPE;
import necesse.engine.GameCache;
import necesse.gfx.ui.GameTextureData;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class TextureCache {
	public final String path;	
	
	private Map<Integer, CacheIndexEntry> indexMap = new HashMap<>();
	private File dataFile;
	private File indexFile;
	private RandomAccessFile dataAccess;
	private AsyncTextureLoader loader;
	protected HashSet<Integer> queriedKeys = new HashSet<Integer>();
	private final Set<Integer> requestedKeys = Collections.synchronizedSet(new HashSet<>());
	protected Map<Integer, TexCacheElement> loaded;

	protected boolean isDirty = false;

	public TextureCache(String path, AsyncTextureLoader loader) {
		this.loaded = Collections.synchronizedMap(new TextureDataLinkedMap(500));
		this.path = path;
		this.dataFile = GameCache.getCacheFile(path + ".bin");	
		this.indexFile = GameCache.getCacheFile(path + ".idx");
		this.loader = loader;		
		this.loadCache();
	}

	@SuppressWarnings("unchecked")
	public void loadCache() {
	    try {
	        this.indexMap = (Map<Integer, CacheIndexEntry>) GameCache.getObject(path + ".idx", HashMap.class);
	    } catch (Exception e) {
	    	DebugHelper.handleFormattedDebugMessage("No cache index found at %s. Creating a new one.", 30, MESSAGE_TYPE.DEBUG,
	    			new Object[] {GameCache.cachePath() + path + ".idx"});	
	        this.indexMap = new HashMap<>();
	    }

	    try {
	        this.dataAccess = new RandomAccessFile(dataFile, "rw");
	    } catch (IOException e) {
	    	DebugHelper.handleFormattedDebugMessage("Failed to create a random data access file for cache at %s", 30, MESSAGE_TYPE.ERROR,
	    			new Object[] {dataFile});	
	        this.dataAccess = null;
	    }
	}

	public void saveCache() {
		
		if(this.indexMap == null) {
			DebugHelper.handleDebugMessage("Failed to save cache because indexMap is null.", 30, MESSAGE_TYPE.ERROR);	
			return;
		}
	    try (RandomAccessFile raf = new RandomAccessFile(dataFile, "rw")) {
	        indexMap.clear();
	        long offset = 0;

	        for (Integer key : queriedKeys) {
	            TexCacheElement element = loaded.get(key);
	            try {
	                byte[] bytes = serializeObject(element);
	                raf.seek(offset);
	                raf.write(bytes);
	                indexMap.put(key, new CacheIndexEntry(offset, bytes.length, element.hash));
	                offset += bytes.length;
	            } catch (IOException e) {
	            	DebugHelper.handleFormattedDebugMessage("Failed to serialize texture for key %s", 30, MESSAGE_TYPE.ERROR, new Object[] {key});	            
	                e.printStackTrace();
	            }
	        }

	        // Save index
	        GameCache.cacheObject(indexMap, path + ".idx");
	        this.loaded.clear();
	        this.queriedKeys.clear();
	        this.isDirty = false;
	    } catch (IOException e) {
	        e.printStackTrace();
	    }
	}

	
	public void set(int hash, GameTextureData texture, GameTextureData palette) {
		TexCacheElement element = new TexCacheElement(hash, texture, palette);
	    this.loaded.put(hash, element);
	    this.queriedKeys.add(hash);
	    this.isDirty = true;
	}
	
	public void clearLoadedCache() {
	    this.loaded.clear();
	}
	
	public boolean containsLoadedKey(Integer key) {
		return this.loaded.containsKey(key);
	}
	
	private int saveInterval = 30;
	private long lastCacheSaveEvent = 0;
	public TexCacheElement get(Integer key) {	
		
	    this.queriedKeys.add(key);
	    
	    if(this.isDirty && ((System.currentTimeMillis() / 1000) - lastCacheSaveEvent) > saveInterval) {
	    	DebugHelper.handleDebugMessage("Saved cache interval triggered.", 60, MESSAGE_TYPE.DEBUG);	
	    	lastCacheSaveEvent = (System.currentTimeMillis() / 1000);
	    	this.saveCache();
	    }
	    
	    TexCacheElement element = this.loaded.get(key);
	    if (element != null) return element;

	    if (indexMap != null) {
		    CacheIndexEntry entry = indexMap.get(key);
		    if (entry == null || dataAccess == null) return null;
	
		    synchronized (requestedKeys) {
		        if (!requestedKeys.contains(key)) {
		            requestedKeys.add(key);
		            try {
		                loader.requestLoadFromCache(key);
		            } catch (Exception e) {
		                e.printStackTrace();
		                requestedKeys.remove(key); // Clean up on failure
		            }
		        }
		    }
	    }
	    return null;
	}
	public void markLoadComplete(int key) {
	    requestedKeys.remove(key);
	}
	public TexCacheElement loadDiskCacheElement(int hashCode) {
		  CacheIndexEntry entry = indexMap.get(hashCode);
		  try {
		        byte[] bytes = new byte[entry.length];
		        dataAccess.seek(entry.offset);
		        dataAccess.readFully(bytes);
		        return (TexCacheElement) deserializeObject(bytes);
		    } catch (IOException | ClassNotFoundException e) {
		    	DebugHelper.handleFormattedDebugMessage("Failed to read from %s at index %d with length %d: %s", 30,
		    				MESSAGE_TYPE.ERROR, new Object[] {dataFile.getAbsolutePath(), entry.offset, entry.length, e.getMessage()});	
		        e.printStackTrace();
		        return null;
		    }	  
	}
	
	private static byte[] serializeObject(Object obj) throws IOException {
	    try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
	         ObjectOutputStream oos = new ObjectOutputStream(baos)) {
	        oos.writeObject(obj);
	        return baos.toByteArray();
	    }
	}	
	
	private static Object deserializeObject(byte[] data) throws IOException, ClassNotFoundException {
	    try (ByteArrayInputStream bais = new ByteArrayInputStream(data);
	         ObjectInputStream ois = new ObjectInputStream(bais)) {
	        return ois.readObject();
	    }
	}
	
	public class TextureDataLinkedMap extends LinkedHashMap<Integer, TexCacheElement>{
    	
    	private final int maxCacheSize;
		private static final long serialVersionUID = 1L;
		
		public TextureDataLinkedMap(int maxCacheSize) {			
			super(50, 0.75f, true);
			this.maxCacheSize = maxCacheSize;
		}
		
		@Override
        protected boolean removeEldestEntry(Map.Entry<Integer, TexCacheElement> eldest) {
			   if (size() > maxCacheSize) {
			        TexCacheElement element = eldest.getValue();
			        if (element != null) {
			            this.onEvict(eldest);
			        }
			        return true;
			    }
			    return false;
        }

		private void onEvict(java.util.Map.Entry<Integer, TexCacheElement> eldest) {		
			
		}
    }
	
	public static class TexCacheElement implements Serializable {
		/**
		 * 
		 */
		private static final long serialVersionUID = 155L;
		public int hash;
		public GameTextureData textureData;
		public GameTextureData paletteData;
		
		public TexCacheElement(int hash, GameTextureData textureData, GameTextureData paletteData) {
			this.hash = hash;
			this.textureData = textureData;
			this.paletteData = paletteData;
		}
	}

	
}
