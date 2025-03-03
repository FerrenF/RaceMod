package core.gfx;

import necesse.gfx.drawOptions.human.HumanDrawOptions;
import necesse.gfx.gameTexture.GameTexture;

public class TextureReplacer {
    	
    	public boolean targetBaseGamePart;
    	public TARGET targetPart;
    	
    	public enum TARGET{
    		CHEST,
    		HEAD,
    		BACK,
    		RIGHT_ARM,
    		LEFT_ARM,
    		SHOES
    	}
    	public TextureReplacer(TARGET s){
    		this.targetPart = s;
    	}
    	
    	public HumanDrawOptions modifyHumanDrawOptions(HumanDrawOptions drawOptions, GameTexture textureReplacement) {
    		
    		return drawOptions;
    	}
}