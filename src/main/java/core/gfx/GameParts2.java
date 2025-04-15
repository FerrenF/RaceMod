package core.gfx;

import java.awt.Point;
import java.util.HashMap;
import java.util.Map;
import core.gfx.AsyncTextureLoader.TextureLocation;
import core.race.parts.BodyPart;
import core.race.parts.RaceLookParts;
import helpers.DebugHelper;
import helpers.DebugHelper.MESSAGE_TYPE;
import necesse.engine.GlobalData;
import necesse.gfx.gameTexture.GameTexture;
import necesse.gfx.gameTexture.GameTexture.BlendQuality;

public class GameParts2 {
	
	static String path_sep = System.getProperty("file.separator");
	protected static final Map<Class<? extends RaceLookParts>, Map<String, GamePart>> parts = new HashMap<>();	
	protected static final TextureManager texManager = new TextureManager(GlobalData.appDataPath()+ path_sep+ "cache"+path_sep+"texCache", 500);
				
	protected static Point wigAccessoryLocation = new Point(0, -1);
	
	public static GameTexture getFullTexture(BodyPart part, int sideID, int textureID, int colorID, float scale, GameTexture.BlendQuality blendQuality, TextureLocation fromWhereIfNotCached) {
		return texManager.getTexture(new GamePart(part)
				.getTexKey(sideID, scale, textureID, colorID, blendQuality), fromWhereIfNotCached);
	}
	
	public static GameTexture getFullTexture(BodyPart part, int textureID, int colorID, int sideNumber, TextureLocation fromWhere) {
		return getFullTexture(part, sideNumber, textureID, colorID, 1.0F, BlendQuality.NEAREST, fromWhere);
	}
	
	public static GameTexture getFullTexture(BodyPart part, int textureID, int colorID, int sideNumber) {
		return getFullTexture(part, sideNumber, textureID, colorID, 1.0F, BlendQuality.NEAREST, TextureLocation.FROM_JAR);
	}
	
	public static GameTexture getFullTexture(BodyPart part, int textureID, int colorID) {
		return getFullTexture(part, 1, textureID, colorID, 1.0F, BlendQuality.NEAREST, TextureLocation.FROM_JAR);
	}
	
	
	public static GameTexture getWigTexture(BodyPart part, int textureID, int colorID, int sideNumber) {
		
		GamePart p = new GamePart(part);
		TexKey spriteTex = p.getWigTexKey(sideNumber, textureID, colorID);
		String wigStoragePath = spriteTex.texturePath;
		
		GameTexture t = texManager.getTexture(spriteTex, TextureLocation.FROM_FILE);
		
		if(t == TextureManager.BLANK_TEXTURE) {
			// Make the wig texture.			
			int spriteSX = t.getWidth() / p.accessoryMapSize().x;
			int spriteSY = t.getHeight() / p.accessoryMapSize().y;
			int spriteY = t.getHeight() + spriteSY * wigAccessoryLocation.y;
			int spriteX = 0;
			GameTexture nWigT = new GameTexture(getFullTexture(part, textureID, colorID), spriteX, spriteY, spriteSX, spriteSY);
			try {
				nWigT.saveTextureImage(wigStoragePath);
				DebugHelper.handleFormattedDebugMessage("Created wig for texture %s at %s",60, MESSAGE_TYPE.DEBUG, new Object[] {t.debugName, wigStoragePath});
			}
			catch(Exception e) {
				DebugHelper.handleFormattedDebugMessage("Error creating wig for texture %s at %s: %s",60, MESSAGE_TYPE.ERROR, new Object[] {t.debugName, wigStoragePath, e.getMessage()});
				e.printStackTrace();
			}
			
			return nWigT;
		}
		else {
			return t;
		}
	}
	
}