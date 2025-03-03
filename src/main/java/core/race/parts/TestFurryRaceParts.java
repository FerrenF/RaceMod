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
		    			 true,
		    			 false,
		    			 true,
		    			 true,
		    			 false,
		    			 "player/race/testfurry/tail/tailcolors",
		    			 "player/race/testfurry/tail/",
		    			 new Point(7,5),
		    			 null,
		    			 null));
    	 
         this.addBodyPart("EARS",
        		 new BodyPart(TestFurryRaceParts.class,
        				 "EARS",
        				 "racemod.race",
        				 "ears",
        				 true,
        				 true,
        				 true,
        				 true,
        				 false,
        				 true,
        				 "player/race/testfurry/skincolors",
        				 "player/race/testfurry/ears/",
        				 new Point(7,5),
        				 new Point(14,2), null));
         
         this.addBodyPart("MUZZLE",
        		 new BodyPart(TestFurryRaceParts.class,
        				 "MUZZLE",
        				 "racemod.race",
        				 "muzzle",
        				 true,
        				 true,
        				 true,
        				 true,
        				 false,
        				 true,
        				 "player/race/testfurry/skincolors",
        				 "player/race/testfurry/muzzle/",
        				 new Point(7,5),
        				 new Point(14, 2), null));
         
         this.addBodyPart("HEAD",
        		 new BodyPart(TestFurryRaceParts.class,
        				 "HEAD",
        				 "racemod.race",
        				 "head",
        				 true,
        				 true,
        				 false,
        				 true,
        				 false,
        				 true,
        				 "player/race/testfurry/skincolors",
        				 "player/race/testfurry/head/",
        				 new Point(7,5),
        				 new Point(14, 2), new TextureReplacer(TextureReplacer.TARGET.HEAD)	));
          
         //  this.addBodyPart("TAIL_COLOR", new BodyPart("TAIL_COLOR", "racemod.race", "tailcolor", true, true,"player/race/testfurry/tail/tailcolors"));
         //  this.addBodyPart("EARS_COLOR", new BodyPart("EARS_COLOR", "racemod.race", "earscolor", true, true,));
    }
    

}