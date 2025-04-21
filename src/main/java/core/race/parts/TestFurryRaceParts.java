package core.race.parts;

import java.awt.Point;
import core.gfx.TextureReplacer;

public class TestFurryRaceParts extends RaceLookParts{	
	
    public TestFurryRaceParts() {
    	defineCustomRaceBodyParts();
    }	
    
    public TestFurryRaceParts(boolean init) {
        super(init);
        defineCustomRaceBodyParts();
     }	
	    
    public void defineCustomRaceBodyParts() {
 
 	  /*
 	   * BodyPart(Class<? extends RaceLookParts> belongsToClass,
    		String name,
    		String labelCategory,
    		String labelKey,
    		int numTextures,
    		int numSides,
    		int numColors,
    		boolean hasWigTexture,
    		String colorPath,
    		String texturePath,    		
    		Point spriteMapSize,
    		Point accessoryTextureMapSize,
    		TextureReplacer replacer,
    		int stylistCost,
    		boolean stylistCostIsShards)
 	   */
    	 this.addBodyPart("TAIL",
    			 new BodyPart(TestFurryRaceParts.class,
		    			 "TAIL",
		    			 "racemod.race",
		    			 "tail",
		    			 7,
		    			 0,
		    			 31,
		    			 true,
		    			 "player/race/testfurry/skincolors",
		    			 "player/race/testfurry/tail/",
		    			 new Point(64,64),
		    			 new Point(32,32),
		    			 null,
		    			 5,
		    			 true, false));
    	 
         this.addBodyPart("EARS",
        		 new BodyPart(TestFurryRaceParts.class,
        				 "EARS",
        				 "racemod.race",
        				 "ears",
        				 9,
		    			 0,
		    			 31,
		    			 true,
        				 "player/race/testfurry/skincolors",
        				 "player/race/testfurry/ears/",
        				 new Point(64,64),
        				 new Point(32,32), null, 5, true, false));
         
         this.addBodyPart("MUZZLE",
        		 new BodyPart(TestFurryRaceParts.class,
        				 "MUZZLE",
        				 "racemod.race",
        				 "muzzle",
        				 7,
		    			 0,
		    			 31,
		    			 true,
        				 "player/race/testfurry/skincolors",
        				 "player/race/testfurry/muzzle/",
        				 new Point(64,64),
        				 new Point(32, 32), null, 5, true, false));
         
         this.addBodyPart("HEAD",
        		 new BodyPart(TestFurryRaceParts.class,
        				 "HEAD",
        				 "racemod.race",
        				 "head",
        				 6,
		    			 0,
		    			 31,
		    			 true,
        				 "player/race/testfurry/skincolors",
        				 "player/race/testfurry/head/",
        				 new Point(64,64),
        				 new Point(32, 32), new TextureReplacer(TextureReplacer.TARGET.HEAD), 5, true, false	));
         
         this.addBodyPart("BODY",
        		 new BodyPart(TestFurryRaceParts.class,
        				 "BODY",
        				 "racemod.race",
        				 "body",
        				 2,
		    			 0,
		    			 31,
		    			 true,
        				 "player/race/testfurry/body/skincolors",
        				 "player/race/testfurry/body/",
        				 new Point(64,64),
        				 new Point(32, 32), new TextureReplacer(TextureReplacer.TARGET.BODY), 5, true, false	));
         
         this.addBodyPart("ARMS",
        		 new BodyPart(TestFurryRaceParts.class,
        				 "ARMS",
        				 "racemod.race",
        				 "arms",
        				 1,
		    			 2,
		    			 31,
		    			 true,
        				 "player/race/testfurry/arms/skincolors",
        				 "player/race/testfurry/arms/",
        				 new Point(64,64),
        				 new Point(32, 32), new TextureReplacer(TextureReplacer.TARGET.ARMS), 5, true, false	));
         
         this.addBodyPart("FEET",
        		 new BodyPart(TestFurryRaceParts.class,
        				 "FEET",
        				 "racemod.race",
        				 "feet",
        				 2,
		    			 0,
		    			 31,
		    			 true,
        				 "player/race/testfurry/feet/skincolors",
        				 "player/race/testfurry/feet/",
        				 new Point(64,64),
        				 new Point(32, 32), new TextureReplacer(TextureReplacer.TARGET.SHOES), 5, true, false	));
   
         this.addBodyPart("CUSTOM_EYES",
        		 new EyeBodyPart(TestFurryRaceParts.class,
        				 "CUSTOM_EYES",
        				 "racemod.race",
        				 "custom_eyes",
        				 "player/race/testfurry/skincolors",
        				 "player/race/testfurry/eyes/eyecolors",
        				 "player/race/testfurry/eyes/",
        				 12, 16, 31, new Point(64,64),
        				 new Point(32, 32),
        				 null, 5, true, 1)       		 		
        		 );
         
         this.hidePartCustomizer("BASE_SKIN");
         this.hidePartCustomizer("BASE_SKIN_COLOR");
         this.hidePartCustomizer("BASE_EYE");     
         this.hidePartCustomizer("BASE_EYE_COLOR");  
    }



}