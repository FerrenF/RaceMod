package core.gfx;

import java.awt.Point;
import java.util.HashMap;
import java.util.Map;

import core.gfx.texture.TexKey;
import core.gfx.texture.TextureManager;
import core.gfx.texture.AsyncTextureLoader.TextureLocation;
import core.race.parts.BodyPart;
import core.race.parts.RaceLookParts;
import helpers.DebugHelper;
import helpers.DebugHelper.MESSAGE_TYPE;
import necesse.engine.GlobalData;
import necesse.gfx.gameTexture.GameTexture;
import necesse.gfx.gameTexture.GameTexture.BlendQuality;

public class GameParts {
	
	public static String path_sep = System.getProperty("file.separator");
	protected static final Map<Class<? extends RaceLookParts>, Map<String, GamePart>> parts = new HashMap<>();	
	protected static final TextureManager texManager = new TextureManager("texCache", 500);
				
	protected static Point wigAccessoryLocation = new Point(0, 5);
	
	public static GameTexture getFullTexture(BodyPart part, int sideID, int textureID, int colorID, float scale, GameTexture.BlendQuality blendQuality, TextureLocation fromWhereIfNotCached) {
		return texManager.getTexture(new GamePart(part)
				.getTexKey(sideID, scale, textureID+1, colorID+1, blendQuality), fromWhereIfNotCached);
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
	
	private static GameTexture safeSubtexture(GameTexture source, int x, int y, int w, int h) {
	    if (w <= 0 || h <= 0 || x < 0 || y < 0 || x + w > source.getWidth() || y + h > source.getHeight()) {
	        throw new IllegalArgumentException("Attempted to extract invalid subtexture region.");
	    }
	    return new GameTexture(source, x, y, w, h);
	}		
	public static GameTexture getWigTexture(BodyPart part, int textureID, int colorID, int sideNumber) {
		GamePart p = new GamePart(part);
		TexKey spriteTex = p.getWigTexKey(sideNumber, textureID + 1, colorID + 1);
		String wigStoragePath = GlobalData.rootPath() + spriteTex.texturePath;

		GameTexture t = texManager.getTexture(spriteTex, TextureLocation.FROM_FILE);
		if (t == TextureManager.BLANK_TEXTURE) {
			// Extract from full texture
			GameTexture fullTex = getFullTexture(part, textureID+1, colorID + 1);
			
			if(fullTex == TextureManager.BLANK_TEXTURE) {
				return t;
			}
			
			int spriteSX = fullTex.getWidth() / p.accessoryMapSize().x;
			int spriteSY = fullTex.getHeight() / p.accessoryMapSize().y;
			
			int spriteX = spriteSX * wigAccessoryLocation.x;
			int spriteY = spriteSY * wigAccessoryLocation.y; // assuming origin bottom-left
			GameTexture nWigT = safeSubtexture(fullTex, spriteX, spriteY, spriteSX, spriteSY);
			try {
				nWigT.saveTextureImage(wigStoragePath);
				DebugHelper.handleFormattedDebugMessage("Created wig for texture %s at %s", 60, MESSAGE_TYPE.DEBUG, new Object[] { t.debugName, wigStoragePath });
				DebugHelper.handleFormattedDebugMessage("Full texture: %dx%d", 30, MESSAGE_TYPE.DEBUG,
					    new Object[] {fullTex.getWidth(), fullTex.getHeight()});
				
			} catch (Exception e) {
				DebugHelper.handleFormattedDebugMessage("Error creating wig for texture %s at %s: %s", 60, MESSAGE_TYPE.ERROR, new Object[] { t.debugName, wigStoragePath, e.getMessage() });
				e.printStackTrace();
			}
			return nWigT;
		}
		return t;
	}
	
}