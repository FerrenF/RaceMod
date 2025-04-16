package core.gfx.texture;

import core.gfx.cache.TextureCache;
import core.gfx.cache.TextureCache.TexCacheElement;
import core.gfx.texture.AsyncTextureLoader.TextureLocation;
import helpers.DebugHelper;
import helpers.DebugHelper.MESSAGE_TYPE;
import necesse.gfx.GameSkinColors;
import necesse.gfx.gameTexture.GameTexture;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.BiFunction;

import javax.imageio.ImageIO;

public class TextureManager {
 
	public static final GameTexture BLANK_TEXTURE = new GameTexture("texBlank", getBlankTextureBytes()); 
	private final AsyncTextureLoader loader;
    private final TextureCache textureDataCache;
    private final GameTextureLinkedMap gameTextureCache;
    public final String cachePath;
    public TextureManager(String cachePath, int maxCacheSize) {    
    	this.gameTextureCache = new GameTextureLinkedMap(maxCacheSize);    	
        this.loader = new AsyncTextureLoader();
        this.textureDataCache = new TextureCache(cachePath, loader);
        loader.setCache(textureDataCache);
        this.cachePath = cachePath;       
    }
    
    public TexCacheElement getTextureElement(TexKey key) {
    	
    	TexCacheElement result = this.textureDataCache.get(key.hashCode());
    	if( result == null ) {
    		DebugHelper.handleFormattedDebugMessage("Requested a cache texture element for %s but cache returned null.", 70, MESSAGE_TYPE.DEBUG,
    				new Object[] {key.texturePath});
    	}
    	return result;
    }
   
   
    public GameTexture getTexture(TexKey key, TextureLocation fromWhereIfNotCached, BiFunction<HashSet<Color>, GameTexture, GameTexture> additionalProcessing) {    	
    	 	
    	// First, try the gameTextureCache.
    	GameTexture result = gameTextureCache.get(key);
    	if(result != null) {
    		DebugHelper.handleFormattedDebugMessage("Requested a cache texture element for %s and texture was found in gameTextureCache.", 75, MESSAGE_TYPE.DEBUG,
    				new Object[] {key.texturePath});
    		return result;    	
    	}
    	    	
    	// if its null, then we check the textureDataCache to see if the bytes have been loaded.	
    	TexCacheElement textureDataCheck = getTextureElement(key);
    	if(textureDataCheck != null && textureDataCheck.textureData != null) {  
    		
    		// If the textureDataCache has the texture data, then we make a new GameTexture for it and add it to the gameTextureCache cache;    		
    		result = new GameTexture("tex"+String.valueOf(key.hashCode()), textureDataCheck.textureData);
    		GameTexture palTex = null;
    		// appliedColors is used when we have multiple palette to process with additional steps.
    		HashSet<Color> appliedColors = new HashSet<>();
    		if(key.hasPalette() && textureDataCheck.paletteData != null) {
    			
    			palTex = new GameTexture("pal"+String.valueOf(key.hashCode()), textureDataCheck.paletteData);
    			GameSkinColors pColors = new GameSkinColors();
    			
    			pColors.addBaseColors(palTex, 0, 1, palTex.getWidth() - 1);    			
    			pColors.replaceColors(result, key.paletteId, appliedColors);
    			
    		}    	
    		
    		if(additionalProcessing != null) {
    			result = additionalProcessing.apply(appliedColors, result);
    		}
    		
    		if(key.scale != 1.0F) {
    			GameTexture scaled = result.resize((int)(result.getWidth() * key.scale), (int)(result.getHeight() * key.scale));
    			result.delete();
    			result = scaled;
    		}
    		
    		this.textureDataCache.set(key.hashCode(), result.getData(), palTex.getData());
    		if(palTex != null) {	
    			palTex.delete();
    		}    		
    		this.gameTextureCache.put(key, result);
    		return this.gameTextureCache.get(key);
    	}
    	
    	// if THAT is null, then we return a blank texture while we load the requested texture's data into the textureDataCache
    	this.requestOrLoad(key, fromWhereIfNotCached);
        return BLANK_TEXTURE;
    }
    
    public GameTexture getTexture(TexKey key, TextureLocation fromWhereIfNotCached) {    	
    		return getTexture(key, fromWhereIfNotCached, null);
    }
   
    public void requestOrLoad(TexKey key, TextureLocation fromWhere) {    	
    	   if (gameTextureCache.containsKey(key)) return;
    	   if (textureDataCache.get(key.hashCode()) != null) return;
    	    
    	    try {
				loader.requestLoad(key, fromWhere);
			} catch (Exception e) {
				e.printStackTrace();
			}
    }       
   
    public void clear() {
        this.gameTextureCache.forEach((key, texture)->texture.delete());
        this.textureDataCache.clearLoadedCache();
    }  
    
    public class GameTextureLinkedMap extends LinkedHashMap<TexKey, GameTexture>{
    	
    	private final int maxCacheSize;
		private static final long serialVersionUID = 1L;
		
		public GameTextureLinkedMap(int maxCacheSize) {			
			super(16, 0.75f, true);
			this.maxCacheSize = maxCacheSize;
		}
		
		@Override
		protected boolean removeEldestEntry(Map.Entry<TexKey, GameTexture> eldest) {
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
	        BufferedImage fallback = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
	        fallback.setRGB(0, 0, 0xFFFFFFFF); // white
	        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
	            ImageIO.write(fallback, "png", baos);
	            return baos.toByteArray();
	        } catch (IOException e) {
	            throw new RuntimeException("Failed to generate fallback texture bytes!", e);
	        }
    }
    
}
