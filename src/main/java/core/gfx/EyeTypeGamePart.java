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
		String texID = getPartName().toLowerCase() + "-" + String.valueOf(textureNum);
		return new EyeTexKey(texID, getPartPath() + texID,
				getPartPalettePath(), getSkinPalettePath(), scale, partColorId, skinColorID, blendQuality);
	}
	
	public EyeTexKey getClosedTexKey(float scale, int skinColorID, int partColorId, GameTexture.BlendQuality blendQuality) {
		String texID = getPartName().toLowerCase() + "-closed";
		return new EyeTexKey(texID, getPartPath() + texID,
				getPartPalettePath(), getSkinPalettePath(), scale, partColorId, skinColorID, blendQuality);
	}
	
	public EyeTexKey getOpenWigTexKey(float scale, int skinColorID, int textureNum, int partColorId, GameTexture.BlendQuality blendQuality) {
		String texID = getPartName().toLowerCase() + "-" + String.valueOf(textureNum);
		return new EyeTexKey(texID +"-wig", getPartPath() + texID,
				getPartPalettePath(), getSkinPalettePath(), scale, partColorId, skinColorID, blendQuality);
	}
}