package core.gfx.texture;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import javax.imageio.ImageIO;

import core.RaceMod;
import core.gfx.GameParts;
import core.gfx.cache.TextureCache;
import core.gfx.cache.TextureCache.TexCacheElement;
import necesse.engine.GlobalData;
import necesse.engine.modLoader.LoadedMod;
import necesse.gfx.gameTexture.GameTexture;
import necesse.gfx.res.ResourceEncoder;
import necesse.gfx.ui.GameTextureData;

public class AsyncTextureLoader {
 public static String rawTextureLocationRoot = GlobalData.rootPath();
 private static final ExecutorService executor = Executors.newFixedThreadPool(2);
 private TextureCache textureCache;
 public enum TextureLocation{
	 FROM_JAR,
	 FROM_FILE,
	 FROM_CACHE,
	 DONT_LOAD
 }
 
 public AsyncTextureLoader() {	 
	 this.textureCache = null;
 }
 
 public AsyncTextureLoader(TextureCache textureCache) {
	 this.textureCache = textureCache;
 }
 
 public void requestLoad(TexKey key, TextureLocation location) throws Exception {
	 if(this.textureCache == null) throw new Exception("Cache wasn't initialized at the time of resource request.");
     executor.submit(() -> {
         try {                	
             switch(location) {
	             case FROM_JAR: 
	            	 	loadImageFromJar(key, this.textureCache);
	            	 break;
	             case FROM_FILE:
	            	 	loadImageFromFile(key, this.textureCache);
	            	 break;
	             case FROM_CACHE:
	            	 	loadImageFromCache(key, this.textureCache);
	            	 break;
	             default:
	            	 throw new IllegalArgumentException("Can't request to load an image without a valid location.");
             }  
  
         } catch (Exception e) {
             e.printStackTrace();
         }
     });
 }
 
 public void requestLoadFromCache(Integer key) throws Exception {
	 if(this.textureCache == null) throw new Exception("Cache wasn't initialized at the time of resource request.");
     executor.submit(() -> {
         try {              	 
	          loadImageFromCache(key, this.textureCache);
         } catch (Exception e) {
             e.printStackTrace();
         }
     });
 }
 
 private void loadImageFromFile(TexKey key, TextureCache toCache) {
	 try {
        GameTextureData textureData = loadGameTextureDataFromFile(rawTextureLocationRoot + key.texturePath + ".png", key.blendQuality);
        GameTextureData paletteData = loadGameTextureDataFromFile(rawTextureLocationRoot + key.palettePath + ".png", null);
        toCache.set(key.hashCode(), textureData, paletteData);
        toCache.markLoadComplete(key.hashCode());
    } catch (IOException e) {
        System.err.println("Failed to load texture from file for "+rawTextureLocationRoot + key.texturePath +" key " + key);
        
    }
}

private GameTextureData loadGameTextureDataFromFile(String path, GameTexture.BlendQuality blendQuality) throws IOException {
	
	File f = new java.io.File(path);
    BufferedImage img = ImageIO.read(f);
    if (img == null) throw new IOException("Failed to decode image from file: " + path);  
    GameTextureData result = new GameTextureData(img.getWidth(), img.getHeight(), imageToByteArray(img), false, blendQuality);
    img.flush();
    return result;
}
	
private void loadImageFromCache(TexKey key, TextureCache toCache) throws Exception {
	  loadImageFromCache(key.hashCode(), toCache);
}

private void loadImageFromCache(Integer key, TextureCache toCache) throws Exception {
    if (toCache.containsLoadedKey(key)) {  
        return;
    }    
    TexCacheElement diskElement = toCache.loadDiskCacheElement(key.hashCode());
    if (diskElement != null) {
        toCache.set(key, diskElement.textureData, diskElement.paletteData);
        toCache.markLoadComplete(key);
    } else {
        throw new IOException("Could not load texture from disk cache for key: " + key);
    }
}

private void loadImageFromJar(TexKey key, TextureCache toCache) throws IOException {
    GameTextureData textureData = loadGameTextureDataFromJar(key.texturePath,  key.blendQuality);
    GameTextureData paletteData = loadGameTextureDataFromJar(key.palettePath, null);
    toCache.set(key.hashCode(), textureData, paletteData);
    toCache.markLoadComplete(key.hashCode());
}

private GameTextureData loadGameTextureDataFromJar(String path, GameTexture.BlendQuality blendQuality) throws IOException {
    JarFile jar = RaceMod.modJar;

    // Normalize path, just in case
    String normalizedPath = path.startsWith("/") ? path.substring(1) : path;

    normalizedPath = "resources/" + normalizedPath + ".png";
    JarEntry entry = jar.getJarEntry(normalizedPath );
    if (entry == null) {
        throw new IOException("Missing resource: " + normalizedPath);
    }

    try (InputStream stream = jar.getInputStream(entry)) {
        BufferedImage img = ImageIO.read(stream);
        if (img == null) throw new IOException("Failed to decode image: " + path);

        GameTextureData result = new GameTextureData(
            img.getWidth(),
            img.getHeight(),
            imageToByteArray(img),
            false,
            blendQuality
        );
        img.flush();
        return result;
    }
}
	
private byte[] imageToByteArray(BufferedImage bufferedImage) {
	ByteArrayOutputStream baos = new ByteArrayOutputStream();
	try {
		ImageIO.write(bufferedImage, "png", baos);
	} catch (IOException e) {
		 throw new RuntimeException(e);
	}
	return baos.toByteArray();
}
public void setCache(TextureCache textureDataCache) {
		this.textureCache = textureDataCache;
	}

	public static void shutdownThreads() {
		executor.shutdownNow();
		
	}

}

