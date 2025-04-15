package core.race.parts;

import java.awt.Point;
import core.gfx.AsyncTextureLoader.TextureLocation;
import core.gfx.EyeTypeGameParts;
import core.gfx.TextureReplacer;
import necesse.gfx.gameTexture.GameTexture;
import necesse.gfx.gameTexture.GameTexture.BlendQuality;


public class EyeBodyPart extends BodyPart {

	private int _numSkinColors;
	private String skinColorPath;
	public EyeBodyPart(String name, String labelKey, int textureOptions, int colorOptions, int closedEyeOptions) {
		super(name, labelKey, textureOptions, colorOptions);
	}

	public EyeBodyPart(Class<? extends RaceLookParts> belongsToClass, String name, String labelCategory,
			String labelKey, String skinColorPath, String palettePath, String partTexturePath,
			int numTextures, int numColors, int numSkinColors,
			Point spriteMapSize, Point accessoryTextureMapSize, TextureReplacer replacer, int stylistCost,
			boolean costIsShards, int closedEyeOptions) {
		
		super(belongsToClass, name, labelCategory, labelKey, numTextures, 1, numColors, true, palettePath, partTexturePath, spriteMapSize,
				accessoryTextureMapSize, replacer, stylistCost, costIsShards);
		
		this.skinColorPath = skinColorPath;
		this._numSkinColors = numSkinColors;
	}

	
	public String getSkinColorPath() 				{	return skinColorPath;				}	
	public int numSkinColors() 						{	return _numSkinColors;				}	
	
	public GameTexture getOpenTexture(int eyeType, int skinColor, int eyeColor) {
		return EyeTypeGameParts.getFullOpenTexture(this, eyeType, skinColor, eyeColor, 1.0F, BlendQuality.NEAREST, TextureLocation.FROM_FILE);
	}
	
	public GameTexture getClosedTexture(int eyeType, int skinColor, int eyeColor) {
		return EyeTypeGameParts.getFullClosedTexture(this, eyeType, skinColor, eyeColor, 1.0F, BlendQuality.NEAREST, TextureLocation.FROM_FILE);
	}

}
