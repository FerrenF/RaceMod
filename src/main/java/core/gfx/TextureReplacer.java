package core.gfx;

import core.race.parts.BodyPart;
import necesse.gfx.drawOptions.human.HumanDrawOptions;
import necesse.gfx.gameTexture.GameTexture;

public class TextureReplacer {
    	
    	public boolean targetBaseGamePart;
    	public TARGET targetPart;
    	
    	public enum TARGET{
    		CHEST,
    		HEAD,
    		BACK,
    		SHOES,
    		BODY,
    		ARMS
    	}
    	public TextureReplacer(TARGET s){
    		this.targetPart = s;
    	}
    	
    
}