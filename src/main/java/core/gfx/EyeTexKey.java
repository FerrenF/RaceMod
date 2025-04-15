package core.gfx;

import java.util.Objects;

import necesse.gfx.gameTexture.GameTexture;

public class EyeTexKey extends TexKey {
	public final String skinPalettePath;
	public final Integer skinPaletteId;
 	    
    public EyeTexKey(String texturePath, String palettePath, String skinPalettePath, float scale, int paletteId, int skinPaletteId, GameTexture.BlendQuality blendQuality) {
    	super(texturePath, palettePath, scale, paletteId, blendQuality);
    	this.skinPalettePath = skinPalettePath;
    	this.skinPaletteId = skinPaletteId;
    }
       
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof EyeTexKey)) return false;
        EyeTexKey key = (EyeTexKey) o;
        return Float.compare(key.scale, scale) == 0 &&
        		Integer.compare(key.paletteId, paletteId) == 0 &&
                Objects.equals(texturePath, key.texturePath) &&
                Objects.equals(palettePath, key.palettePath) &&
                Objects.equals(skinPaletteId, key.skinPaletteId) &&
                Objects.equals(skinPalettePath, key.skinPalettePath) &&
                Objects.equals(blendQuality, key.blendQuality);
    }

    @Override
    public int hashCode() {
        return Objects.hash(texturePath, palettePath, skinPalettePath, scale, paletteId, skinPaletteId, blendQuality);
    }
}