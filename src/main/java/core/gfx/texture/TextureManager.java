package core.gfx.texture;

import core.gfx.cache.TextureByteCache;
import core.gfx.cache.TextureByteCache.TexCacheElement;
import core.gfx.cache.TextureByteCache.TexVariantCacheElement;
import core.gfx.texture.AsyncTextureLoader.TextureLocation;
import helpers.DebugHelper;
import helpers.DebugHelper.MESSAGE_TYPE;
import necesse.gfx.GameSkinColors;
import necesse.gfx.gameTexture.GameTexture;
import java.awt.Color;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

public class TextureManager {
 
	public static final GameTexture BLANK_TEXTURE = new GameTexture("texBlank", 1, 1, getBlankTextureBytes()); 
	private final AsyncTextureLoader loader;
    private final TextureByteCache textureDataCache;
    private final GameTextureLinkedMap gameTextureCache;
    public final String cachePath;
    
    public TextureManager(String cachePath, int maxCacheSize) {    
    	this.gameTextureCache = new GameTextureLinkedMap(maxCacheSize);    	
        this.loader = new AsyncTextureLoader();
        this.textureDataCache = new TextureByteCache(cachePath, loader);
        loader.setCache(textureDataCache);
        this.cachePath = cachePath;       
    }
    
    public TexCacheElement getTextureElement(int key) {  
    	TexCacheElement result = this.textureDataCache.get(key);
    	return result;
    }
    
    public TexVariantCacheElement getTextureVariantElement(int key) {  
    	TexVariantCacheElement result = this.textureDataCache.get(key, TexVariantCacheElement.class);
    	return result;
    }
   
    public GameTexture getTexture(TexKey key, TextureLocation fromWhereIfNotCached, boolean buildIfNotPresent, BiFunction<List<GameTexture>, List<Integer>, GameTexture> paletteProcessing) {
        DebugHelper.handleFormattedDebugMessage("[getTexture] Request started for key: %s", 85, MESSAGE_TYPE.DEBUG, new Object[]{key});

        GameTexture result;
        synchronized (gameTextureCache) {
            result = gameTextureCache.get(key);
            if (result != null) {
                return result;
            }
      
        
	        if (this.textureDataCache.indexContainsKey(key.hashCode())) {
	            TexCacheElement cT = this.textureDataCache.get(key.hashCode());
	            if (cT != null && cT.textureData != null) {
	                DebugHelper.handleFormattedDebugMessage("[getTexture] Found in textureDataCache: %s", 85, MESSAGE_TYPE.DEBUG, new Object[]{key.texturePath});
	                result = new GameTexture(String.valueOf(key.hashCode()), cT.textureData);
	                this.gameTextureCache.put(key, result);
	                return result;
	            } else {
	                DebugHelper.handleFormattedDebugMessage("[getTexture] Entry in textureDataCache was null or missing textureData: %s", 85, MESSAGE_TYPE.WARNING, new Object[]{key.texturePath});
	            }
	        } else {
	            DebugHelper.handleFormattedDebugMessage("[getTexture] Not found in textureDataCache index: %s", 85, MESSAGE_TYPE.DEBUG, new Object[]{key.texturePath});
	        }
        }
        
        if(!buildIfNotPresent) return BLANK_TEXTURE;
        
        List<String> requestedPaths = key.requiredPathsToBuild();
        int elementsNeededTotal = requestedPaths.size();
        DebugHelper.handleFormattedDebugMessage("[getTexture] Building texture variant. Total required paths: %d", 85, MESSAGE_TYPE.DEBUG, new Object[]{elementsNeededTotal});

        List<Boolean> loadStatus = new ArrayList<>(elementsNeededTotal);
        List<GameTexture> textureData = new ArrayList<>(elementsNeededTotal);
        List<TexCacheElement> cacheData = new ArrayList<>(elementsNeededTotal);

        
        for (int i = 0; i < elementsNeededTotal; i++) {
            String path = requestedPaths.get(i);
            BaseTexKey subTexKey = new BaseTexKey(path, 1.0F, null);               
            GameTexture texDataCheck = gameTextureCache.get(subTexKey);
            textureData.add(i, texDataCheck);

            if (texDataCheck != null) {
                DebugHelper.handleFormattedDebugMessage("[getTexture] Sub-texture found in gameTextureCache [%d]: %s", 85, MESSAGE_TYPE.DEBUG,
                        new Object[]{i, subTexKey});
                loadStatus.add(i, true);
                cacheData.add(i, null);
            } else {
                TexCacheElement cacheCheck = this.textureDataCache.get(subTexKey.hashCode());
                cacheData.add(i, cacheCheck);

                if (cacheCheck != null && cacheCheck.textureData != null) {
                    DebugHelper.handleFormattedDebugMessage("[getTexture] Sub-texture found in textureDataCache [%d]: %s", 85, MESSAGE_TYPE.DEBUG,
                            new Object[]{i, path});         
                    
                    byte[] buffer = cacheCheck.textureData.getBuffer();
                    int expectedLength = cacheCheck.textureData.width * cacheCheck.textureData.height * 4;

                    if (buffer == null) {
                        throw new IllegalStateException("TextureData buffer is null");
                    }

                    if (buffer.length != expectedLength) {
                        throw new IllegalStateException("TextureData buffer length mismatch: expected " + expectedLength + ", got " + buffer.length);
                    }
                    
                    GameTexture fromCache = new GameTexture(String.valueOf(subTexKey.hashCode()), cacheCheck.textureData);
                    // Thread-safe cache write
                    synchronized (gameTextureCache) {
                    	gameTextureCache.put(subTexKey, fromCache);
	                    textureData.set(i, fromCache);
	                    loadStatus.add(i, true);
                    }
                } else {
                    DebugHelper.handleFormattedDebugMessage("[getTexture] Sub-texture missing [%d]: %s", 85, MESSAGE_TYPE.DEBUG,
                            new Object[]{i, path});
                    loadStatus.add(i, false);
                }
            }
        }


        if (loadStatus.contains(false)) {
            DebugHelper.handleFormattedDebugMessage("[getTexture] Missing sub-textures, requesting load if needed...", 85, MESSAGE_TYPE.DEBUG, null);
            for (int i = 0; i < elementsNeededTotal; i++) {
            	if (!(loadStatus.get(i))) {
            	    TexCacheElement x = cacheData.get(i);
            	    String path = requestedPaths.get(i);
            	    BaseTexKey subTexKey = new BaseTexKey(path, 1.0F, null);      
            	    if (x == null) {
            	        try {
            	            DebugHelper.handleFormattedDebugMessage("[getTexture] Requesting loader to load missing path [%d]: %s", 85, MESSAGE_TYPE.DEBUG,
            	                    new Object[]{i, path});
            	            this.loader.requestLoad(path, fromWhereIfNotCached, subTexKey.hashCode(), key.blendQuality, null);
            	        } catch (Exception e) {
            	            DebugHelper.handleFormattedDebugMessage("[getTexture] Failed to request load for: %s (%s)", 85, MESSAGE_TYPE.ERROR,
            	                    new Object[]{path, e.getMessage()});
            	        }
            	    }
            	}

            }
            return BLANK_TEXTURE;
        }

        result = textureData.get(0);
        if (result == null) {
            DebugHelper.handleFormattedDebugMessage("[getTexture] ERROR: Base texture data was null for: %s", 85, MESSAGE_TYPE.ERROR, new Object[]{key.texturePath});
            return BLANK_TEXTURE;
        }
        
        result = new GameTexture(result);
       
        DebugHelper.handleFormattedDebugMessage("[getTexture] Applying palette recolors...", 85, MESSAGE_TYPE.DEBUG, null);
        
        if(paletteProcessing != null) {
        	// Something more complicated.
        	result = paletteProcessing.apply(textureData, key.palleteIdList());
        }
        else {
        	// Basic re-color.
	        GameSkinColors pColors = new GameSkinColors();	         
            GameTexture baseColorsElement = textureData.get(1);
            List<Integer> paletteIDs = key.palleteIdList();
            
            if(baseColorsElement != null) {
            	pColors.addBaseColors(textureData.get(1), 0, 1, baseColorsElement.getWidth() - 1);
            	Integer replacerIndex = paletteIDs.get(0) > 0 ? paletteIDs.get(0) - 1 :0;
            	
            	if(replacerIndex != null) {
            		 pColors.replaceColors(result, replacerIndex);
            	}
                DebugHelper.handleFormattedDebugMessage("[getTexture] Applied palette [%d] from texture: %s", 85, MESSAGE_TYPE.DEBUG, new Object[]{replacerIndex, requestedPaths.get(1)});
            }
	        
        }


        if (key.scale != 1.0F) {
            DebugHelper.handleFormattedDebugMessage("[getTexture] Scaling texture to scale: %.2f", 85, MESSAGE_TYPE.DEBUG, new Object[]{key.scale});
            GameTexture scaled = result.resize((int)(result.getWidth() * key.scale), (int)(result.getHeight() * key.scale));
            result.delete();
            result = scaled;
        }

        DebugHelper.handleFormattedDebugMessage("[getTexture] Finished. Texture for %s generated and cached.", 85, MESSAGE_TYPE.DEBUG, new Object[]{key.textureID});
        synchronized (gameTextureCache) {
        	this.textureDataCache.set(key.hashCode(), new TexCacheElement(key.hashCode(), result.getData()));
        	this.gameTextureCache.put(key, result);
        }
        return result;
    }

    public GameTexture getTexture(TexKey key, boolean buildIfNotPresent, TextureLocation fromWhereIfNotCached) {    	
    		return getTexture(key, fromWhereIfNotCached, buildIfNotPresent, null);
    }
   
    public void clear() {
    	synchronized (gameTextureCache) {
    		this.gameTextureCache.forEach((key, texture)->texture.delete());
    	}
        this.textureDataCache.clearLoadedCache();
    }  
    
    public class GameTextureLinkedMap extends LinkedHashMap<BaseTexKey, GameTexture>{
    	
    	private final int maxCacheSize;
		private static final long serialVersionUID = 1L;
		
		public GameTextureLinkedMap(int maxCacheSize) {			
			super(16, 0.75f, true);
			this.maxCacheSize = maxCacheSize;
		}
		
		@Override
		protected boolean removeEldestEntry(Map.Entry<BaseTexKey, GameTexture> eldest) {
			if (size() > maxCacheSize) {
				
				// Cleanup logic before removing
				GameTexture texture = eldest.getValue();
				if (texture != null) {
					texture.delete();
				}
				return true;
			}
			return false;
		}
    }
    
    public static byte[] getBlankTextureBytes() {
        // 1x1 transparent pixel = 4 bytes: R, G, B, A
        return new byte[] {(byte)255, (byte)255, (byte)255, (byte)0};
    }

	public void init_cache() {
		this.textureDataCache.loadCache();		
	}

	public void cacheTexture(BaseTexKey key, GameTexture generatedWigTex) {
		this.textureDataCache.set(key.hashCode(), new TexCacheElement(key.hashCode(), generatedWigTex.getData()));
		this.gameTextureCache.put(key, generatedWigTex);
	}
    
}
