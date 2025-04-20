package core.gfx.texture;
import java.util.List;
import java.util.Objects;

import necesse.gfx.gameTexture.GameTexture;

public class TexKey extends BaseTexKey {
	public final String textureID;
    public final String palettePath;
    public final Integer paletteId;
    
    public TexKey(String textureID, String texturePath, String palettePath, float scale, int paletteId, GameTexture.BlendQuality blendQuality) {
    	super(texturePath, scale, blendQuality);
    	this.textureID = textureID;
    	this.palettePath = palettePath;
        this.paletteId = paletteId;
    }
    
    public TexKey(String textureID, String texturePath, float scale, GameTexture.BlendQuality blendQuality) {
    	super(texturePath, scale, blendQuality);
    	this.textureID = textureID;
    	this.palettePath = null;
        this.paletteId = null;
    }
    
    public boolean hasPalette() {
    	return this.palettePath != null;
    }
    
    List<Integer> palleteIdList(){
    	return List.of(paletteId);
    }
    
    List<String> requiredPathsToBuild(){
    	return palettePath != null ? List.of(texturePath, palettePath) : List.of(texturePath);
    }
    
    @Override
    protected String computeKeyHash() {
        String data = textureID+ "|"+palettePath + "|" + texturePath + "|" + scale +"|" + paletteId+ "|" + blendQuality;
        return sha1(data);
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TexKey)) return false;
        if (!super.equals(o)) return false;
        TexKey that = (TexKey) o;
        return Objects.equals(textureID, that.textureID) &&
        	   Objects.equals(texturePath, that.texturePath) &&
        	   Objects.equals(scale, that.scale) &&
               Objects.equals(palettePath, that.palettePath) &&
               Objects.equals(blendQuality, that.blendQuality) &&
               Objects.equals(paletteId, that.paletteId);
    }
}
