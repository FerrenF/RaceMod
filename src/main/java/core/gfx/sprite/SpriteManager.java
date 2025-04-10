package core.gfx.sprite;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.opengl.GL30;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.imageio.ImageIO;

public class SpriteManager {
  
	private final AsyncTextureLoader loader;
    private final SpriteKeyLinkedMap cache;
    private final Set<SpriteKey> loading = ConcurrentHashMap.newKeySet();
    
    public SpriteManager(AsyncTextureLoader loader, int maxCacheSize) {       
        this.cache = new SpriteKeyLinkedMap(500);
        this.loader = loader;
    }
    
    public Integer getTexture(SpriteKey key) {
        return cache.get(key); // May be null if not ready
    }

    public void requestOrLoad(SpriteKey key) {
        if (!cache.containsKey(key) && loading.add(key)) {
            loader.requestLoad(key);
        }
    }
    
    public int getOrLoad(SpriteKey key) {
        return cache.computeIfAbsent(key, this::loadTexture);
    }
    
    public int getOrCreateTexture(SpriteKey key, BufferedImage baseImage) {
    	
        if (cache.containsKey(key)) {
            return cache.get(key);
        }

        BufferedImage scaledImage = scaleImage(baseImage, key.scale);
        int textureId = uploadToOpenGL(scaledImage);

        cache.put(key, textureId);
        return textureId;
    }

    public void clear() {
        for (int textureId : cache.values()) {
            GL11.glDeleteTextures(textureId);
        }
        cache.clear();
    }
    
    private int loadTexture(SpriteKey key) {
        try (InputStream stream = getClass().getClassLoader().getResourceAsStream(key.path())) {
            if (stream == null) throw new IOException("Texture not found: " + key.path());
            BufferedImage image = ImageIO.read(stream);
            ByteBuffer buffer = toByteBuffer(image);

            int texID = GL11.glGenTextures();
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, texID);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL12.GL_CLAMP_TO_EDGE);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL12.GL_CLAMP_TO_EDGE);
            GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA8, image.getWidth(), image.getHeight(), 0,
                    GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, buffer);

            return texID;
        } catch (IOException e) {
            e.printStackTrace();
            return 0; // or a fallback texture ID
        }
    }
    
    private ByteBuffer toByteBuffer(BufferedImage image) {
        int[] pixels = new int[image.getWidth() * image.getHeight()];
        image.getRGB(0, 0, image.getWidth(), image.getHeight(), pixels, 0, image.getWidth());
        ByteBuffer buffer = BufferUtils.createByteBuffer(image.getWidth() * image.getHeight() * 4);

        for (int pixel : pixels) {
            buffer.put((byte)((pixel >> 16) & 0xFF)); // Red
            buffer.put((byte)((pixel >> 8) & 0xFF));  // Green
            buffer.put((byte)(pixel & 0xFF));         // Blue
            buffer.put((byte)((pixel >> 24) & 0xFF)); // Alpha
        }

        buffer.flip();
        return buffer;
    }
    
    private BufferedImage scaleImage(BufferedImage image, float scale) {
        int newWidth = Math.round(image.getWidth() * scale);
        int newHeight = Math.round(image.getHeight() * scale);

        BufferedImage scaled = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = scaled.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        g.drawImage(image, 0, 0, newWidth, newHeight, null);
        g.dispose();

        return scaled;
    }

    private int uploadToOpenGL(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();

        int[] pixels = new int[width * height];
        image.getRGB(0, 0, width, height, pixels, 0, width);

        ByteBuffer buffer = ByteBuffer.allocateDirect(width * height * 4);
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int pixel = pixels[y * width + x];
                buffer.put((byte) ((pixel >> 16) & 0xFF)); // Red
                buffer.put((byte) ((pixel >> 8) & 0xFF));  // Green
                buffer.put((byte) (pixel & 0xFF));         // Blue
                buffer.put((byte) ((pixel >> 24) & 0xFF)); // Alpha
            }
        }
        buffer.flip();

        int textureId = GL11.glGenTextures();
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureId);

        GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA8, width, height,
                0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, buffer);

        // Set texture parameters (important!)
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL12.GL_CLAMP_TO_EDGE);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL12.GL_CLAMP_TO_EDGE);

        GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
        return textureId;
    }
    
    public class SpriteKeyLinkedMap extends LinkedHashMap<SpriteKey, Integer>{
    	
    	private final int maxCacheSize;
		private static final long serialVersionUID = 1L;
		
		public SpriteKeyLinkedMap(int maxCacheSize) {			
			super(16, 0.75f, true);
			this.maxCacheSize = maxCacheSize;
		}
		
		@Override
        protected boolean removeEldestEntry(Map.Entry<SpriteKey, Integer> eldest) {
            if (size() > this.maxCacheSize) {
                GL11.glDeleteTextures(eldest.getValue());
                return true;
            }
            return false;
        }
    }
}
