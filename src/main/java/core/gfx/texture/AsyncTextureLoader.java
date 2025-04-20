package core.gfx.texture;

import java.awt.image.BufferedImage;
import java.io.*;
import java.util.concurrent.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import javax.imageio.ImageIO;

import core.RaceMod;
import core.gfx.cache.TextureByteCache;
import core.gfx.cache.TextureByteCache.TexCacheElement;
import necesse.gfx.gameTexture.GameTexture;
import necesse.gfx.ui.GameTextureData;

public class AsyncTextureLoader {

    public static String rawTextureLocationRoot = "gamecache/";

    private static final ExecutorService executor = Executors.newFixedThreadPool(4);
    private final ConcurrentHashMap<Integer, Boolean> loadingFlags = new ConcurrentHashMap<>();

    private TextureByteCache textureCache;

    public enum TextureLocation {
        FROM_JAR,
        FROM_FILE,
        NONE
    }

    public AsyncTextureLoader() {}

    public AsyncTextureLoader(TextureByteCache textureCache) {
        this.textureCache = textureCache;
    }

    public void requestLoad(String texturePath, TextureLocation location, int cacheKey,
                            GameTexture.BlendQuality blendQuality, Runnable onComplete) throws Exception {
        if (this.textureCache == null)
            throw new Exception("Cache not initialized at time of resource request.");
        if (location == TextureLocation.NONE) return;

        if (loadingFlags.putIfAbsent(cacheKey, true) != null) return;

        executor.submit(() -> {
            try {
                switch (location) {
                    case FROM_JAR: loadImageFromJar(texturePath, cacheKey, blendQuality); break;
                    case FROM_FILE: loadImageFromFile(texturePath, cacheKey, blendQuality); break;
                    default: throw new IllegalArgumentException("Unsupported texture location: " + location);
                }
                if (onComplete != null) onComplete.run();
            } catch (Exception e) {
                logError("Failed to load texture from " + location + ": " + texturePath, e);
                textureCache.markLoadFailed(cacheKey);
            } finally {
                loadingFlags.remove(cacheKey);
            }
        });
    }

    public void requestLoadFromCache(int cacheKey, Runnable onComplete) throws Exception {
        if (this.textureCache == null)
            throw new Exception("Cache not initialized at time of disk cache request.");

        if (loadingFlags.putIfAbsent(cacheKey, true) != null) return;

        executor.submit(() -> {
            try {
                if (textureCache.containsLoadedKey(cacheKey) || textureCache.isLoading(cacheKey))
                    return;

                if (!textureCache.indexContainsKey(cacheKey))
                    return;

                TexCacheElement diskElement = textureCache.loadDiskCacheElement(cacheKey);
                if (diskElement != null) {
                    textureCache.set(cacheKey, diskElement);
                    textureCache.markLoadComplete(cacheKey);
                    if (onComplete != null) onComplete.run();
                } else {
                    throw new IOException("Could not load texture from disk cache for key: " + cacheKey);
                }
            } catch (Exception e) {
                logError("Failed to load texture from disk cache for key: " + cacheKey, e);
                textureCache.markLoadFailed(cacheKey);
            } finally {
                loadingFlags.remove(cacheKey);
            }
        });
    }

    private void loadImageFromFile(String texturePath, int cacheKey, GameTexture.BlendQuality blendQuality) throws IOException {
        String fullPath = rawTextureLocationRoot + normalizeTexturePath(texturePath);
        GameTextureData textureData = loadGameTextureDataFromFile(fullPath, blendQuality);
        textureCache.set(cacheKey, new TexCacheElement(cacheKey, textureData));
        textureCache.markLoadComplete(cacheKey);
    }

    private void loadImageFromJar(String texturePath, int cacheKey, GameTexture.BlendQuality blendQuality) throws IOException {
        GameTextureData textureData = loadGameTextureDataFromJar(texturePath, blendQuality);
        textureCache.set(cacheKey, new TexCacheElement(cacheKey, textureData));
        textureCache.markLoadComplete(cacheKey);
    }

    private GameTextureData loadGameTextureDataFromFile(String path, GameTexture.BlendQuality blendQuality) throws IOException {
        File file = new File(path);
        if (!file.exists()) throw new FileNotFoundException("File not found: " + path);
        BufferedImage img = ImageIO.read(file);
        if (img == null) throw new IOException("Failed to decode image: " + path);
        GameTextureData result = new GameTextureData(img.getWidth(), img.getHeight(), imageToByteArray(img), false, blendQuality);
        img.flush();
        return result;
    }

    private GameTextureData loadGameTextureDataFromJar(String texturePath, GameTexture.BlendQuality blendQuality) throws IOException {
        JarFile jar = RaceMod.modJar;
        String entryPath = "resources/" + normalizeTexturePath(texturePath);
        JarEntry entry = jar.getJarEntry(entryPath);
        if (entry == null) throw new IOException("Missing resource in jar: " + entryPath);

        try (InputStream stream = jar.getInputStream(entry)) {
            BufferedImage img = ImageIO.read(stream);
            if (img == null) throw new IOException("Failed to decode image: " + entryPath);
            GameTextureData result = new GameTextureData(img.getWidth(), img.getHeight(), imageToByteArray(img), false, blendQuality);
            img.flush();
            return result;
        }
    }

    private String normalizeTexturePath(String path) {
        path = path.startsWith("/") ? path.substring(1) : path;
        return path.endsWith(".png") ? path : path + ".png";
    }

    private byte[] imageToByteArray(BufferedImage bufferedImage) {
        int width = bufferedImage.getWidth();
        int height = bufferedImage.getHeight();
        byte[] pixels = new byte[width * height * 4]; // RGBA

        int[] argbPixels = bufferedImage.getRGB(0, 0, width, height, null, 0, width);

        for (int i = 0; i < argbPixels.length; i++) {
            int argb = argbPixels[i];
            int baseIndex = i * 4;
            pixels[baseIndex]     = (byte)((argb >> 16) & 0xFF); // R
            pixels[baseIndex + 1] = (byte)((argb >> 8) & 0xFF);  // G
            pixels[baseIndex + 2] = (byte)(argb & 0xFF);         // B
            pixels[baseIndex + 3] = (byte)((argb >> 24) & 0xFF); // A
        }

        return pixels;
    }

    public void setCache(TextureByteCache textureCache) {
        this.textureCache = textureCache;
    }

    public static void shutdownThreads() {
        executor.shutdown();
        try {
            if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    private void logError(String message, Exception e) {
        System.err.println(message);
        e.printStackTrace(System.err);
    }
}
