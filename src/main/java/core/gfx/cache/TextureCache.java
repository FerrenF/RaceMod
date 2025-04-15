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

import core.gfx.AsyncTextureLoader;
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
	protected Map<Integer, TexCacheElement> loaded;
	protected Map<Integer, TexCacheElement> synchronizedMap = new HashMap<Integer, TexCacheElement>();
	protected boolean isDirty = false;

	public TextureCache(String path, AsyncTextureLoader loader) {
		this.loaded = Collections.synchronizedMap(new TextureDataLinkedMap(500));
		this.path = path;
		this.dataFile = GameCache.getCacheFile(path + ".bin");
		this.indexFile = GameCache.getCacheFile(path + ".idx");
		this.loader = loader;
	}

	@SuppressWarnings("unchecked")
	public void loadCache() {
	    try {
	        this.indexMap = (Map<Integer, CacheIndexEntry>) GameCache.getObject(path + ".idx", HashMap.class);
	    } catch (Exception e) {
	        this.indexMap = new HashMap<>();
	    }

	    try {
	        this.dataAccess = new RandomAccessFile(dataFile, "r");
	    } catch (IOException e) {
	        this.dataAccess = null;
	    }
	}

	public void saveCache() {
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
	                System.err.println("Failed to serialize texture for key " + key);
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
	
	public TexCacheElement get(Integer key) {
	    this.queriedKeys.add(key);
	    TexCacheElement element = this.loaded.get(key);
	    if (element != null) return element;

	    CacheIndexEntry entry = indexMap.get(key);
	    if (entry == null || dataAccess == null) return null;

	    try {
	        byte[] bytes = new byte[entry.length];
	        dataAccess.seek(entry.offset);
	        dataAccess.readFully(bytes);
	        element = (TexCacheElement) deserializeObject(bytes);	      
	        return element;
	    } catch (IOException | ClassNotFoundException e) {
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
			super(16, 0.75f, true);
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
