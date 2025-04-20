package core.gfx.texture;

import java.awt.Point;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

import necesse.gfx.gameTexture.GameTexture;

public class RaceModTextureUtils {
    private static final ConcurrentHashMap<Object, Object> wigLocks = new ConcurrentHashMap<>();

    public static GameTexture getOrGenerateWigTexture(
            Object uniqueKey,
            Supplier<GameTexture> loadFromFile,
            Supplier<GameTexture> loadFullTexture,
            Point accessoryMapSize,
            Point accessoryLocation
    ) {
        GameTexture t = loadFromFile.get();
        if (t != TextureManager.BLANK_TEXTURE) {
            return t;
        }

        Object lock = wigLocks.computeIfAbsent(uniqueKey, k -> new Object());
        synchronized (lock) {
            t = loadFromFile.get();
            if (t != TextureManager.BLANK_TEXTURE) {
                return t;
            }

            GameTexture fullTex = loadFullTexture.get();
            if (fullTex == TextureManager.BLANK_TEXTURE) {
                return t;
            }

            int spriteSX = fullTex.getWidth() / accessoryMapSize.x;
            int spriteSY = fullTex.getHeight() / accessoryMapSize.y;
            int spriteX = spriteSX * accessoryLocation.x;
            int spriteY = spriteSY * accessoryLocation.y;

            GameTexture wig = new GameTexture(fullTex, spriteX, spriteY, spriteSX, spriteSY);
            return wig;
        }
    }
}
