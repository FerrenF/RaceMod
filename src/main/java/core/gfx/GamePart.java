package core.gfx;

import java.awt.Point;

import core.gfx.texture.TexKey;
import core.race.parts.BodyPart;
import core.race.parts.RaceLookParts;
import necesse.gfx.gameTexture.GameTexture;
import necesse.gfx.gameTexture.GameTexture.BlendQuality;

public class GamePart {

	public final BodyPart assignedPart;	
	
	public Class<? extends RaceLookParts> getRacePartsClass() 		{	return this.assignedPart.getRacePartsClass();		}	
		
	public Point accessoryMapSize()		{	return this.assignedPart.getAccessoryTextureMapSize();		}
	
	public Point textureMapSize()		{	return this.assignedPart.getTextureSpriteMapSize();			}
	
	public boolean isBaseGamePart() 	{	return this.assignedPart.isBaseGamePart();					}
	
	public boolean hasWig() 			{	return this.assignedPart.hasWigTexture();					}
	
	public int numTextures() 			{	return this.assignedPart.numTextures();						}
	
	public int numSides()				{	return this.assignedPart.numSides();						}
	
	public int numColors() 				{	return this.assignedPart.numColors();						}
	
	public String getPartName() 		{	return this.assignedPart.getPartName();		}
	
	public String getPartPath() 		{	return this.assignedPart.getTexturePath();		}
	
	public String getPartPalettePath() 	{	return this.assignedPart.getColorPath();		}
	
	public GamePart(BodyPart assignedPart) {
		this.assignedPart = assignedPart;
	}
	
	public TexKey getTexKey(int sideNum, float scale, int textureNum, int colorNum, GameTexture.BlendQuality blendQuality) {
		return new TexKey(getPartPath() + getPartName().toLowerCase() + (sideNum > 0 ? ("-" + String.valueOf(sideNum)) : "")+"-"+String.valueOf(textureNum),
				getPartPalettePath(), scale, colorNum, blendQuality);
	}
	
	public TexKey getWigTexKey(int sideNum, int textureNum, int colorNum) {
		return new TexKey(getPartPath() + getPartName().toLowerCase() + (sideNum > 0 ? ("-" + String.valueOf(sideNum)) : "")+"-"+String.valueOf(textureNum)+"-wig",
				null, 1.0F, 0, BlendQuality.NEAREST);
	}
}