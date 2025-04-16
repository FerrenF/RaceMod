package core.gfx.texture;
import java.util.Objects;

import necesse.gfx.gameTexture.GameTexture;

public class TexKey {
	public final String texturePath;
    public final String palettePath;
    public final float scale;
    public final Integer paletteId;
    public final GameTexture.BlendQuality blendQuality;
    
    public TexKey(String texturePath, String palettePath, float scale, int paletteId, GameTexture.BlendQuality blendQuality) {
        this.texturePath = texturePath;
    	this.palettePath = palettePath;
        this.scale = scale;
        this.paletteId = paletteId;
        this.blendQuality = blendQuality;
    }
    
    public TexKey(String texturePath, float scale, GameTexture.BlendQuality blendQuality) {
        this.texturePath = texturePath;
    	this.palettePath = null;
        this.scale = scale;
        this.paletteId = null;
        this.blendQuality = blendQuality;
    }
    
    public boolean hasPalette() {
    	return this.palettePath != null;
    }
       
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TexKey)) return false;
        TexKey key = (TexKey) o;
        return Float.compare(key.scale, scale) == 0 &&
        		Integer.compare(key.paletteId, paletteId) == 0 &&
                Objects.equals(texturePath, key.texturePath) &&
                Objects.equals(palettePath, key.palettePath) &&
                Objects.equals(blendQuality, key.blendQuality);
    }

    @Override
    public int hashCode() {
        return Objects.hash(texturePath, palettePath, scale, paletteId, blendQuality);
    }
}
