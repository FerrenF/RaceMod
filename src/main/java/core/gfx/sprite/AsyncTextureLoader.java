package core.gfx.sprite;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

import javax.imageio.ImageIO;

import org.lwjgl.BufferUtils;

public class AsyncTextureLoader {
 private final ExecutorService executor = Executors.newFixedThreadPool(2);
 private final BlockingQueue<PendingTexture> uploadQueue = new LinkedBlockingQueue<>();

 public void requestLoad(SpriteKey key) {
     executor.submit(() -> {
         try {
             BufferedImage image = loadImageFromJar(key.path());
             ByteBuffer buffer = toByteBuffer(image);
             uploadQueue.add(new PendingTexture(key, buffer, image.getWidth(), image.getHeight()));
         } catch (Exception e) {
             e.printStackTrace();
         }
     });
 }

 public void flushUploads(SpriteManager spriteManager) {
     while (!uploadQueue.isEmpty()) {
         PendingTexture pending = uploadQueue.poll();
     //    int texId = spriteManager.uploadTexture(pending.key(), pending.buffer(), pending.width(), pending.height());
     //    spriteManager.insert(pending.key(), texId);
     }
 }

 private BufferedImage loadImageFromJar(String path) throws IOException {
     try (InputStream stream = getClass().getClassLoader().getResourceAsStream(path)) {
         if (stream == null) throw new IOException("Missing texture: " + path);
         return ImageIO.read(stream);
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

 public static class PendingTexture{
	 private final SpriteKey key;
	 private final ByteBuffer buffer;
	 private final int width;
	 private final int height;
	 PendingTexture(SpriteKey key, ByteBuffer buffer, int width, int height) {
		 this.key=key;
		 this.buffer=buffer;
		 this.width=width;
		 this.height=height;
	 }
 	public SpriteKey getKey() {
		return key;
	}
	public ByteBuffer getBuffer() {
		return buffer;
	}
	public int getWidth() {
		return width;
	}
	public int getHeight() {
		return height;
	}
 }
}

