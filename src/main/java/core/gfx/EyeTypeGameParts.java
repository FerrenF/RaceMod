package core.gfx;

import core.gfx.texture.EyeTexKey;
import core.gfx.texture.TextureManager;
import core.gfx.texture.AsyncTextureLoader.TextureLocation;
import core.race.parts.EyeBodyPart;
import helpers.DebugHelper;
import helpers.DebugHelper.MESSAGE_TYPE;
import necesse.gfx.GameSkinColors;
import necesse.gfx.gameTexture.GameTexture;
import necesse.gfx.gameTexture.GameTexture.BlendQuality;

public class EyeTypeGameParts extends GameParts {
	
	protected static GameTexture EYES_CLOSED_TEXTURE;	
	protected GameTexture defaultClosedTexture;
	
	public static GameTexture getFullOpenTexture(EyeBodyPart part, int skinColorID, int textureID, int colorID, float scale, GameTexture.BlendQuality blendQuality, TextureLocation fromWhereIfNotCached) {
		
		EyeTypeGamePart p = new EyeTypeGamePart(part);
		EyeTexKey spriteTex = p.getOpenTexKey(scale, skinColorID, textureID, colorID, blendQuality);	
		
		GameTexture t = texManager.getTexture(spriteTex, TextureLocation.FROM_JAR, (appliedColors, result)->{	
			GameTexture skinPalTex = GameTexture.fromFile(part.getSkinColorPath());
			GameSkinColors pColors = new GameSkinColors();
			pColors.addBaseColors(skinPalTex, 0, skinPalTex.getWidth()-1, skinPalTex.getHeight());		
			pColors.replaceColors(result, skinColorID, appliedColors);
			skinPalTex.delete();			
			return result;
		});
				
		return t;
	}
	
	public static GameTexture getFullClosedTexture(EyeBodyPart part, int skinColorID, int colorID, float scale, GameTexture.BlendQuality blendQuality, TextureLocation fromWhereIfNotCached) {
		
		EyeTypeGamePart p = new EyeTypeGamePart(part);
		EyeTexKey spriteTex = p.getClosedTexKey(scale, skinColorID, colorID, blendQuality);	
		
		GameTexture t = texManager.getTexture(spriteTex, TextureLocation.FROM_JAR, (appliedColors, result)->{	
			GameTexture skinPalTex = GameTexture.fromFile(part.getSkinColorPath());
			GameSkinColors pColors = new GameSkinColors();
			pColors.addBaseColors(skinPalTex, 0, skinPalTex.getWidth()-1, skinPalTex.getHeight());		
			pColors.replaceColors(result, skinColorID, appliedColors);
			skinPalTex.delete();			
			return result;
		});
				
		return t;
	}
	private static GameTexture safeSubtexture(GameTexture source, int x, int y, int w, int h) {
	    if (w <= 0 || h <= 0 || x < 0 || y < 0 || x + w > source.getWidth() || y + h > source.getHeight()) {
	        throw new IllegalArgumentException("Attempted to extract invalid subtexture region.");
	    }
	    return new GameTexture(source, x, y, w, h);
	}		
	public static GameTexture getWigTexture(EyeBodyPart part, float scale, int textureID, int colorID, int skinColorId) {
		EyeTypeGamePart p = new EyeTypeGamePart(part);
		EyeTexKey spriteTex = p.getOpenWigTexKey(scale, skinColorId, textureID+1, colorID +1, BlendQuality.NEAREST);
		String wigStoragePath = spriteTex.texturePath;

		GameTexture t = texManager.getTexture(spriteTex, TextureLocation.FROM_FILE);

		if (t == TextureManager.BLANK_TEXTURE) {
			// Get the full eye texture
			GameTexture fullTex = getFullOpenTexture(part, skinColorId, textureID+1, colorID+1, 1.0F, BlendQuality.NEAREST, TextureLocation.FROM_FILE);

			int spriteSX = fullTex.getWidth() / p.accessoryMapSize().x;
			int spriteSY = fullTex.getHeight() / p.accessoryMapSize().y;
			int spriteX = spriteSX * wigAccessoryLocation.x;
			int spriteY = spriteSY * (wigAccessoryLocation.y - 1); // or adjust based on origin

			GameTexture nWigT = safeSubtexture(fullTex, spriteX, spriteY, spriteSX, spriteSY);
			try {
				nWigT.saveTextureImage(wigStoragePath);
				DebugHelper.handleFormattedDebugMessage("Created wig for texture %s at %s", 60, MESSAGE_TYPE.DEBUG, new Object[] { t.debugName, wigStoragePath });
			} catch (Exception e) {
				DebugHelper.handleFormattedDebugMessage("Error creating wig for texture %s at %s: %s", 60, MESSAGE_TYPE.ERROR, new Object[] { t.debugName, wigStoragePath, e.getMessage() });
				e.printStackTrace();
			}

			return nWigT;
		} else {
			return t;
		}
	}

}
