package core.gfx.texture;

import java.util.List;
import java.util.Objects;

import necesse.gfx.gameTexture.GameTexture;

public class EyeTexKey extends TexKey {
	public final String skinPalettePath;
	public final Integer skinPaletteId;
 	    
    public EyeTexKey(String textureID, String texturePath, String palettePath, String skinPalettePath, float scale, int paletteId, int skinPaletteId, GameTexture.BlendQuality blendQuality) {
    	super(textureID, texturePath, palettePath, scale, paletteId, blendQuality);
    	this.skinPalettePath = skinPalettePath;
    	this.skinPaletteId = skinPaletteId;
    }
           
    @Override
    List<Integer> palleteIdList(){
    	return List.of(paletteId, skinPaletteId);
    }
    
    @Override
    List<String> requiredPathsToBuild(){
    	return List.of(texturePath, palettePath, skinPalettePath);
    }
    
    @Override
    protected String computeKeyHash() {
        String data = textureID + "|" + palettePath + "|" + skinPalettePath + "|" + texturePath + "|" + scale +"|" + paletteId + "|" + skinPaletteId + "|" + blendQuality;
        return sha1(data);
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof EyeTexKey)) return false;
        if (!super.equals(o)) return false;
        EyeTexKey that = (EyeTexKey) o;
        return Objects.equals(textureID, that.textureID) &&
        		Objects.equals(texturePath, that.texturePath) &&
        		Objects.equals(skinPalettePath, that.skinPalettePath) &&
        		Objects.equals(skinPaletteId, that.skinPaletteId) &
                Objects.equals(palettePath, that.palettePath) &&
                Objects.equals(paletteId, that.paletteId);
    }
}