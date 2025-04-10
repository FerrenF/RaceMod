package core.gfx.sprite;

import java.nio.file.Paths;
import java.util.Objects;

public class SpriteKey {
	public final String path;
    public final String textureId;
    public final float scale;

    public SpriteKey(String path, String textureId, float scale) {
        this.path = path;
    	this.textureId = textureId;
        this.scale = scale;
    }
    
    public SpriteKey(String path, float scale) {
        this.path = path;
    	this.textureId = Paths.get(path).getFileName().toString();
        this.scale = scale;
    }
    
    public String path() {
    	return this.path;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SpriteKey)) return false;
        SpriteKey key = (SpriteKey) o;
        return Float.compare(key.scale, scale) == 0 &&
                Objects.equals(textureId, key.textureId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(textureId, scale);
    }
}
