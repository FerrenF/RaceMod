package core.gfx;

import core.gfx.AsyncTextureLoader.TextureLocation;
import core.gfx.cache.TextureCache;
import core.gfx.cache.TextureCache.TexCacheElement;
import necesse.gfx.GameSkinColors;
import necesse.gfx.gameTexture.GameTexture;

import java.awt.Color;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;
import java.util.function.Function;

import javax.imageio.ImageIO;

public class TextureManager {
 
	public static final GameTexture BLANK_TEXTURE = new GameTexture("texBlank", getBlankTextureBytes()); 
	private final AsyncTextureLoader loader;
    private final TextureCache textureDataCache;
    private final Set<TexKey> loading = ConcurrentHashMap.newKeySet();
    private final GameTextureLinkedMap gameTextureCache;
    private final Map<Integer, Point> paletteData = new HashMap<>();
    
    public TextureManager(String cachePath, int maxCacheSize) {    
    	this.gameTextureCache = new GameTextureLinkedMap(500);
    	
        this.loader = new AsyncTextureLoader();
        this.textureDataCache = new TextureCache(cachePath, loader);
        loader.setCache(textureDataCache);
    }
    
    
    public TexCacheElement getTextureElement(TexKey key) {
    	return this.textureDataCache.get(key.hashCode());
    }
   
    public Point getPaletteDimensions(TexKey key, TextureLocation fromWhereIfNotCached) {
    	if(this.paletteData.containsKey(key.texturePath.hashCode())) return this.paletteData.get(key.texturePath.hashCode());
    	
    	GameTexture palTex;
		try {
			palTex = fromWhereIfNotCached == TextureLocation.FROM_JAR 
					? GameTexture.fromFile(key.palettePath) : GameTexture.fromFileRawOutside(key.palettePath);
		} catch (FileNotFoundException e) {		
			e.printStackTrace();
			return null;
		}
    	paletteData.put(key.texturePath.hashCode(), new Point(palTex.getWidth()-1, palTex.getHeight()));
    	return paletteData.get(key.texturePath.hashCode()); 
    	
    }
    
    public GameTexture getTexture(TexKey key, TextureLocation fromWhereIfNotCached, BiFunction<HashSet<Color>, GameTexture, GameTexture> additionalProcessing) {    	
    	 	
    	GameTexture result = gameTextureCache.get(key);
    	if(result != null) return result;    	
    	    	
    	// if its null, then we check the textureCache for informmation.	
    	TexCacheElement textureDataCheck = getTextureElement(key);
    	if(textureDataCheck != null) {  
    		// If the textureDataCache has the texture data, then we make a new GameTexture for it and add it to the cache;
    		
    		result = new GameTexture("tex"+String.valueOf(key.hashCode()), textureDataCheck.textureData);
    		HashSet<Color> appliedColors = new HashSet<>();
    		if(key.hasPalette() && textureDataCheck.paletteData != null) {
    			GameTexture palTex = new GameTexture("pal"+String.valueOf(key.hashCode()), textureDataCheck.paletteData);
    			GameSkinColors pColors = new GameSkinColors();
    			paletteData.put(key.texturePath.hashCode(), new Point(palTex.getWidth()-1, palTex.getHeight()));
    			pColors.addBaseColors(palTex, 0, 1, palTex.getWidth() - 1);    			
    			pColors.replaceColors(result, key.paletteId, appliedColors);
    			palTex.delete();
    		}    	
    		
    		if(additionalProcessing != null) {
    			result = additionalProcessing.apply(appliedColors,result);
    		}
    		
    		if(key.scale != 1.0F) {
    			GameTexture scaled = result.resize((int)(result.getWidth() * key.scale), (int)(result.getHeight() * key.scale));
    			result.delete();
    			result = scaled;
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
    	   if (!loading.add(key)) return;
    	    
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
    	 byte[] bytes = getBlankTextureBytes();
    	    if (bytes == null) {
    	        // fallback to a 1x1 opaque pixel or log and throw as needed
    	        BufferedImage fallback = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
    	        fallback.setRGB(0, 0, 0xFFFFFFFF); // white
    	        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
    	            ImageIO.write(fallback, "png", baos);
    	            return baos.toByteArray();
    	        } catch (IOException e) {
    	            throw new RuntimeException("Failed to generate fallback texture bytes!", e);
    	        }
    	    }
    	    return bytes;
    }
    
}
