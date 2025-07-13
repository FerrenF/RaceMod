package core.gfx;

import java.awt.Point;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import core.gfx.texture.TexKey;
import core.gfx.texture.TextureManager;
import core.RaceMod;
import core.gfx.texture.AsyncTextureLoader.TextureLocation;
import core.race.parts.BodyPart;
import core.race.parts.RaceLookParts;
import helpers.DebugHelper;
import helpers.DebugHelper.MESSAGE_TYPE;
import necesse.engine.GameCache;
import necesse.gfx.gameTexture.GameTexture;
import necesse.gfx.gameTexture.GameTexture.BlendQuality;

public class GameParts {

	public static String path_sep = System.getProperty("file.separator");
	protected static final Map<Class<? extends RaceLookParts>, Map<String, GamePart>> parts = new HashMap<>();
	protected static Point wigAccessoryLocation = new Point(0, -1);
	public static String wigStoragePathRoot = GameCache.cachePath();

	protected static final ConcurrentHashMap<TexKey, Object> wigLocks = new ConcurrentHashMap<>();

	public static GameTexture getFullTexture(BodyPart part, int sideID, int textureID, int colorID, float scale, GameTexture.BlendQuality blendQuality, TextureLocation fromWhereIfNotCached) {	
		 if(textureID == -1) {
		    	return TextureManager.BLANK_TEXTURE;
		    }
		return RaceMod.raceTextureManager.getTexture(new GamePart(part)
				.getTexKey(sideID, scale, textureID+1, colorID+1, blendQuality), true, fromWhereIfNotCached);
	}

	public static GameTexture getFullTexture(BodyPart part, int textureID, int colorID, int sideNumber, TextureLocation fromWhere) {
		return getFullTexture(part, sideNumber, textureID, colorID, 1.0F, BlendQuality.NEAREST, fromWhere);
	}

	public static GameTexture getFullTexture(BodyPart part, int textureID, int colorID, int sideNumber) {
		return getFullTexture(part, sideNumber, textureID, colorID, 1.0F, BlendQuality.NEAREST, TextureLocation.FROM_JAR);
	}

	public static GameTexture getFullTexture(BodyPart part, int textureID, int colorID) {
		return getFullTexture(part, 0, textureID, colorID, 1.0F, BlendQuality.NEAREST, TextureLocation.FROM_JAR);
	}


	public static GameTexture getWigTexture(BodyPart part, int textureID, int colorID, int sideNumber) {		
		
	    GamePart p = new GamePart(part);
	    
	    if(textureID == -1) {
	    	return RaceMod.TEX_DISABLE_PART;
	    }
	    TexKey spriteTex = p.getWigTexKey(sideNumber, textureID + 1, colorID + 1);

	    DebugHelper.handleFormattedDebugMessage("[getWigTexture] Requesting wig texture for: %s", 85, MESSAGE_TYPE.INFO,
	            new Object[]{spriteTex});

	    GameTexture t = RaceMod.raceTextureManager.getTexture(spriteTex, false, TextureLocation.NONE);
	    if (t != TextureManager.BLANK_TEXTURE) {
	        DebugHelper.handleFormattedDebugMessage("[getWigTexture] Cache HIT for: %s", 85, MESSAGE_TYPE.INFO,
	                new Object[]{spriteTex});
	        return t;
	    }

	    DebugHelper.handleFormattedDebugMessage("[getWigTexture] Cache MISS — loading full texture from jar...", 85, MESSAGE_TYPE.INFO, null);
	    t = getFullTexture(part, textureID, colorID, sideNumber, TextureLocation.FROM_JAR);
	    if (t == TextureManager.BLANK_TEXTURE) {
	        DebugHelper.handleFormattedDebugMessage("[getWigTexture] Failed to load full texture for: %s", 85, MESSAGE_TYPE.WARNING,
	                new Object[]{spriteTex});
	        return t;
	    }

	    DebugHelper.handleFormattedDebugMessage("[getWigTexture] Full texture loaded: %dx%d", 85, MESSAGE_TYPE.INFO,
	            new Object[]{t.getWidth(), t.getHeight()});

	    int spriteSX =  p.accessoryMapSize().x;
	    int spriteSY =  p.accessoryMapSize().y;
	    int spriteX = wigAccessoryLocation.x < 0 ?
	    		t.getWidth() + (spriteSX * wigAccessoryLocation.x)
	    		: (spriteSX * wigAccessoryLocation.x);
	    int spriteY = wigAccessoryLocation.y < 0 ?
	    		t.getHeight() + (spriteSY * wigAccessoryLocation.y)
	    		: (spriteSY * wigAccessoryLocation.y);

	    DebugHelper.handleFormattedDebugMessage("[getWigTexture] Extracting sprite at (%d, %d) with size %dx%d", 85, MESSAGE_TYPE.INFO,
	            new Object[]{spriteX, spriteY, spriteSX, spriteSY});

	    GameTexture wig = new GameTexture(t, spriteX, spriteY, spriteSX, spriteSY);

	    DebugHelper.handleFormattedDebugMessage("[getWigTexture] Caching wig texture for key: %s", 85, MESSAGE_TYPE.INFO,
	            new Object[]{spriteTex});

	    RaceMod.raceTextureManager.cacheTexture(spriteTex, wig);

	    return wig;
	}


}
