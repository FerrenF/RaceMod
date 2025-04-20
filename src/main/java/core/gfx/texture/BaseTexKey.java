package core.gfx.texture;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Objects;

import necesse.gfx.gameTexture.GameTexture;

public class BaseTexKey {
	public final String texturePath;
    public final float scale;
    public final GameTexture.BlendQuality blendQuality;
    
    public static String sha1(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            byte[] result = md.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : result) {
                sb.append(String.format("%02x", b)); // hex
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-1 not available", e);
        }
    }
    
    public BaseTexKey(String texturePath, float scale, GameTexture.BlendQuality blendQuality) {
        this.texturePath = texturePath;
        this.scale = scale;
        this.blendQuality = blendQuality;
    }    

    List<Integer> palleteIdList(){
    	return List.of();
    }
    
    List<String> requiredPathsToBuild(){
    	return List.of(texturePath);
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BaseTexKey)) return false;
        BaseTexKey that = (BaseTexKey) o;
        return Float.compare(that.scale, scale) == 0 &&
               Objects.equals(texturePath, that.texturePath) &&
               blendQuality == that.blendQuality;
    }


    protected String computeKeyHash() {
        String data = texturePath + "|" + scale + "|" + blendQuality;
        return sha1(data);
    }
    
    @Override
    public int hashCode() {
        return computeKeyHash().hashCode();
    }
}
