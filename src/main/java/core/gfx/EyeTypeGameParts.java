package core.gfx;

import core.gfx.texture.EyeTexKey;
import core.gfx.texture.RaceModTextureUtils;
import core.gfx.texture.TextureManager;

import java.awt.Color;
import java.util.HashSet;
import java.util.List;

import core.RaceMod;
import core.gfx.texture.AsyncTextureLoader.TextureLocation;
import core.race.parts.EyeBodyPart;
import helpers.DebugHelper;
import helpers.DebugHelper.MESSAGE_TYPE;
import necesse.gfx.GameSkinColors;
import necesse.gfx.gameTexture.GameTexture;
import necesse.gfx.gameTexture.GameTexture.BlendQuality;
import necesse.gfx.gameTexture.MergeFunction;

public class EyeTypeGameParts extends GameParts {
	
	protected static GameTexture EYES_CLOSED_TEXTURE;	
	protected GameTexture defaultClosedTexture;
	
	public static GameTexture getFullOpenTexture(EyeBodyPart part, int skinColorID, int textureID, int colorID, float scale, GameTexture.BlendQuality blendQuality, TextureLocation fromWhereIfNotCached) {
		
		EyeTypeGamePart p = new EyeTypeGamePart(part);
		EyeTexKey spriteTex = p.getOpenTexKey(scale, skinColorID, textureID + 1, colorID + 1, blendQuality);			
		GameTexture t = RaceMod.raceTextureManager.getTexture(spriteTex, TextureLocation.FROM_JAR, true, (textureList, paletteIds)->{
			
			if(textureList.size() < 3) {
				return textureList.get(0);
			}
			GameTexture baseElement = new GameTexture(textureList.get(0));
			GameTexture skinElement = new GameTexture(baseElement);
			GameTexture eyeColorPaletteElement = textureList.get(1);
			GameTexture skinColorPaletteElement = textureList.get(2);
			int eyeCol = paletteIds.get(0);
			int skinCol = paletteIds.get(1);
	        GameSkinColors pColors = new GameSkinColors();	     
	        GameSkinColors skinColors = new GameSkinColors();
	        skinColors.addBaseColors(skinColorPaletteElement, 0, 1, skinColorPaletteElement.getWidth() - 1);
	        pColors.addBaseColors(eyeColorPaletteElement, 0, 1, eyeColorPaletteElement.getWidth() - 1);
	        
	        HashSet<Color> excludedColors = new HashSet<>();	    
	        pColors.replaceColors(baseElement, eyeCol, excludedColors);
	        pColors.removeColors(baseElement, excludedColors, skinColors);
	        
	        excludedColors.clear();	        
	        skinColors.replaceColors(skinElement, skinCol, excludedColors);
	        skinColors.removeColors(skinElement, excludedColors, pColors);
	        
	        //GameTexture image, int applyX, int applyY, MergeFunction mergeFunction
	        baseElement.merge(skinElement, 0, 0, MergeFunction.NORMAL);
	        skinElement.delete();
            return baseElement;
		});
				
		return t;
	}
	
	public static GameTexture getFullClosedTexture(EyeBodyPart part, int skinColorID, int colorID, float scale, GameTexture.BlendQuality blendQuality, TextureLocation fromWhereIfNotCached) {
		
		EyeTypeGamePart p = new EyeTypeGamePart(part);
		EyeTexKey spriteTex = p.getClosedTexKey(scale, skinColorID + 1, colorID + 1, blendQuality);	
		GameTexture t = RaceMod.raceTextureManager.getTexture(spriteTex, TextureLocation.FROM_JAR, true, (textureList, paletteIds)->{
			
			if(textureList.size() < 3) {
				return textureList.get(0);
			}
			GameTexture baseElement = new GameTexture(textureList.get(0));
			GameTexture skinElement = new GameTexture(baseElement);
			GameTexture eyeColorPaletteElement = textureList.get(1);
			GameTexture skinColorPaletteElement = textureList.get(2);
			int eyeCol = paletteIds.get(0);
			int skinCol = paletteIds.get(1);
	        GameSkinColors pColors = new GameSkinColors();	     
	        GameSkinColors skinColors = new GameSkinColors();
	        skinColors.addBaseColors(skinColorPaletteElement, 0, 1, skinColorPaletteElement.getWidth() - 1);
	        pColors.addBaseColors(eyeColorPaletteElement, 0, 1, eyeColorPaletteElement.getWidth() - 1);
	        
	        HashSet<Color> excludedColors = new HashSet<>();	    
	        pColors.replaceColors(baseElement, eyeCol, excludedColors);
	        pColors.removeColors(baseElement, excludedColors, skinColors);
	        
	        excludedColors.clear();	        
	        skinColors.replaceColors(skinElement, skinCol, excludedColors);
	        skinColors.removeColors(skinElement, excludedColors, pColors);
	        
	        //GameTexture image, int applyX, int applyY, MergeFunction mergeFunction
	        baseElement.merge(skinElement, 0, 0, MergeFunction.NORMAL);
	        skinElement.delete();
            return baseElement;
		});
				
		return t;
	}
	public static GameTexture getWigTexture(EyeBodyPart part, float scale, int textureID, int colorID, int skinColorId) {
	    EyeTypeGamePart p = new EyeTypeGamePart(part);
	    EyeTexKey spriteTex = p.getOpenWigTexKey(scale, skinColorId, textureID + 1, colorID + 1, BlendQuality.NEAREST);
	
	    DebugHelper.handleFormattedDebugMessage("[getWigTexture] Requesting wig texture for: %s", 85, MESSAGE_TYPE.INFO,
	            new Object[]{spriteTex});

	    GameTexture t = RaceMod.raceTextureManager.getTexture(spriteTex, false, TextureLocation.NONE);
	    if (t != TextureManager.BLANK_TEXTURE) {
	        DebugHelper.handleFormattedDebugMessage("[getWigTexture] Cache HIT for: %s", 85, MESSAGE_TYPE.INFO,
	                new Object[]{spriteTex});
	        return t;
	    }

	    DebugHelper.handleFormattedDebugMessage("[getWigTexture] Cache MISS — loading full texture from jar...", 85, MESSAGE_TYPE.INFO, null);

	    t = getFullOpenTexture(part, skinColorId, textureID, colorID, scale, BlendQuality.NEAREST, TextureLocation.FROM_JAR);
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
