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
    	      
    	
    //   public BodyPart(String name, String labelCategory, String labelKey, boolean hasTexture, boolean hasColor, String colorPath, String texturePath, int totalOptions) {

    	 this.addBodyPart("TAIL",
    			 new BodyPart(TestFurryRaceParts.class,
		    			 "TAIL",
		    			 "racemod.race",
		    			 "tail",
		    			 true,
		    			 false,
		    			 false,
		    			 true,
		    			 true,
		    			 false,
		    			 "player/race/testfurry/tail/tailcolors",
		    			 "player/race/testfurry/tail/",
		    			 new Point(64,64),
		    			 null,
		    			 null));
    	 
         this.addBodyPart("EARS",
        		 new BodyPart(TestFurryRaceParts.class,
        				 "EARS",
        				 "racemod.race",
        				 "ears",
        				 true,
        				 false,
        				 true,
        				 true,
        				 false,
        				 true,
        				 "player/race/testfurry/skincolors",
        				 "player/race/testfurry/ears/",
        				 new Point(64,64),
        				 new Point(32,32), null));
         
         this.addBodyPart("MUZZLE",
        		 new BodyPart(TestFurryRaceParts.class,
        				 "MUZZLE",
        				 "racemod.race",
        				 "muzzle",
        				 true,
        				 false,
        				 true,
        				 true,
        				 false,
        				 true,
        				 "player/race/testfurry/skincolors",
        				 "player/race/testfurry/muzzle/",
        				 new Point(64,64),
        				 new Point(32, 32), null));
         
         this.addBodyPart("HEAD",
        		 new BodyPart(TestFurryRaceParts.class,
        				 "HEAD",
        				 "racemod.race",
        				 "head",
        				 true,
        				 false,
        				 true,
        				 true,
        				 false,
        				 true,
        				 "player/race/testfurry/skincolors",
        				 "player/race/testfurry/head/",
        				 new Point(64,64),
        				 new Point(32, 32), new TextureReplacer(TextureReplacer.TARGET.HEAD)	));
         
         this.addBodyPart("BODY",
        		 new BodyPart(TestFurryRaceParts.class,
        				 "BODY",
        				 "racemod.race",
        				 "body",
        				 true,
        				 false,
        				 true,
        				 true,
        				 false,
        				 true,
        				 "player/race/testfurry/body/skincolors",
        				 "player/race/testfurry/body/",
        				 new Point(64,64),
        				 new Point(32, 32), new TextureReplacer(TextureReplacer.TARGET.BODY)	));
         
         this.addBodyPart("ARMS",
        		 new BodyPart(TestFurryRaceParts.class,
        				 "ARMS",
        				 "racemod.race",
        				 "arms",
        				 true,
        				 true,
        				 true,
        				 true,
        				 false,
        				 true,
        				 "player/race/testfurry/arms/skincolors",
        				 "player/race/testfurry/arms/",
        				 new Point(64,64),
        				 new Point(32, 32), new TextureReplacer(TextureReplacer.TARGET.ARMS)	));
          
         this.hidePartCustomizer("SKIN_COLOR");
        // this.hidePartCustomizer("EYE_COLOR");
        // this.hidePartCustomizer("EYE_TYPE");
         
    }
    

}