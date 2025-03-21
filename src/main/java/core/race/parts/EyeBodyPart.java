package core.race.parts;

import java.awt.Point;
import java.util.List;

import core.gfx.GameParts;
import core.gfx.TextureReplacer;
import necesse.gfx.gameTexture.GameTexture;
import core.gfx.EyeTypeGamePart;
public class EyeBodyPart extends BodyPart {

	private String skinColorPath;
	public EyeBodyPart(String name, String labelKey, int totalOptions) {
		super(name, labelKey, totalOptions);
	}

	public EyeBodyPart(Class<? extends RaceLookParts> belongsToClass, String name, String labelCategory,
			String labelKey, String skinColorPath, String colorPath, String texturePath,
			Point spriteMapSize, Point accessoryTextureMapSize, TextureReplacer replacer, int stylistCost,
			boolean costIsShards) {
		
		super(belongsToClass, name, labelCategory, labelKey, true, false, true, false,
				false, false, colorPath, texturePath, spriteMapSize,
				accessoryTextureMapSize, replacer, stylistCost, costIsShards);
		this.skinColorPath = skinColorPath;
	}
	
	public String getSkinColorPath() 				{	return skinColorPath;				}	
	
	public List<GameTexture> getOpenTextures(int eyeType, int skinColor, int eyeColor) {
		return ((EyeTypeGamePart)GameParts.getPart(this.getOwnerClass(), this.getPartName())).getOpenColorTextures(eyeType, eyeColor, skinColor);	
	}
	
	public List<GameTexture> getClosedTextures(int eyeType, int skinColor, int eyeColor) {
		return ((EyeTypeGamePart)GameParts.getPart(this.getOwnerClass(), this.getPartName())).getClosedColorTextures(eyeType, eyeColor, skinColor);	
	}
	
	public int getTotalColorOptions() {
		return ((EyeTypeGamePart)GameParts.getPart(this.getOwnerClass(), this.getPartName())).getEyeColorCount();
	}
	public int getTotalTextureOptions() {
		return ((EyeTypeGamePart)GameParts.getPart(this.getOwnerClass(), this.getPartName())).getTextureCount();
	}
}
