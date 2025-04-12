package core.gfx;

public class TextureReplacer {
    	
    	public boolean targetBaseGamePart;
    	public TARGET targetPart;
    	
    	public enum TARGET{
    		CHEST,
    		HEAD,
    		BACK,
    		SHOES,
    		BODY,
    		ARMS,
    		EYES,
    		HAIR,
    		FACE_HAIR
    	}
    	public TextureReplacer(TARGET s){
    		this.targetPart = s;
    	}
    	
    
}