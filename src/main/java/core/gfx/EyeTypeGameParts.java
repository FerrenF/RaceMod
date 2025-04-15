package core.gfx;

import core.gfx.AsyncTextureLoader.TextureLocation;
import core.race.parts.EyeBodyPart;
import helpers.DebugHelper;
import helpers.DebugHelper.MESSAGE_TYPE;
import necesse.gfx.GameSkinColors;
import necesse.gfx.gameTexture.GameTexture;
import necesse.gfx.gameTexture.GameTexture.BlendQuality;

public class EyeTypeGameParts extends GameParts2 {
	
	protected static GameTexture EYES_CLOSED_TEXTURE;	
	protected static final String EYE_CLOSED_TEXTURE_NAME = "eyes_closed";	
	
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
	
	public static GameTexture getFullClosedTexture(EyeBodyPart part, int skinColorID, int textureID, int colorID, float scale, GameTexture.BlendQuality blendQuality, TextureLocation fromWhereIfNotCached) {
		
		EyeTypeGamePart p = new EyeTypeGamePart(part);
		EyeTexKey spriteTex = p.getClosedTexKey(scale, skinColorID, textureID, colorID, blendQuality);	
		
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
			
	public static GameTexture getWigTexture(EyeBodyPart part, float scale, int textureID, int colorID, int skinColorId) {
		
		EyeTypeGamePart p = new EyeTypeGamePart(part);
		EyeTexKey spriteTex = p.getOpenWigTexKey(scale, textureID, colorID, skinColorId, BlendQuality.NEAREST);
		String wigStoragePath = spriteTex.texturePath;
		
		GameTexture t = texManager.getTexture(spriteTex, TextureLocation.FROM_FILE);
		
		if(t == TextureManager.BLANK_TEXTURE) {
			// Make the wig texture.			
			int spriteSX = t.getWidth() / p.accessoryMapSize().x;
			int spriteSY = t.getHeight() / p.accessoryMapSize().y;
			int spriteY = t.getHeight() + spriteSY * wigAccessoryLocation.y;
			int spriteX = 0;
			GameTexture nWigT = new GameTexture(getFullOpenTexture(part, skinColorId, textureID, colorID, 1.0F, BlendQuality.NEAREST, TextureLocation.FROM_FILE), spriteX, spriteY, spriteSX, spriteSY);
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
