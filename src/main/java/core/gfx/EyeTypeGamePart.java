package core.gfx;

import core.gfx.texture.EyeTexKey;
import core.race.parts.EyeBodyPart;
import necesse.gfx.gameTexture.GameTexture;

public class EyeTypeGamePart extends GamePart{

	public EyeTypeGamePart(EyeBodyPart assignedPart) {
		super(assignedPart);
	}
	
	public EyeBodyPart getEyePart() {
		return (EyeBodyPart)this.assignedPart;
	}
	
	public String getSkinPalettePath() {
		return getEyePart().getSkinColorPath();
	}
	
	
	public EyeTexKey getOpenTexKey(float scale, int skinColorID, int textureNum, int partColorId, GameTexture.BlendQuality blendQuality) {
		return new EyeTexKey(getPartPath() + getPartName().toLowerCase() + "-" + String.valueOf(textureNum),
				getPartPalettePath(), getSkinPalettePath(), scale, partColorId, skinColorID, blendQuality);
	}
	
	public EyeTexKey getClosedTexKey(float scale, int skinColorID, int partColorId, GameTexture.BlendQuality blendQuality) {
		return new EyeTexKey(getPartPath() + getPartName().toLowerCase() + "-closed",
				getPartPalettePath(), getSkinPalettePath(), scale, partColorId, skinColorID, blendQuality);
	}
	
	public EyeTexKey getOpenWigTexKey(float scale, int skinColorID, int textureNum, int partColorId, GameTexture.BlendQuality blendQuality) {
		return new EyeTexKey(getPartPath() + getPartName().toLowerCase() + String.valueOf(textureNum)+"-wig",
				getPartPalettePath(), getSkinPalettePath(), scale, partColorId, skinColorID, blendQuality);
	}
}