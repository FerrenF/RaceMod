package core.race.parts;

import java.awt.Point;
import core.gfx.TextureReplacer;

public class OrcRaceParts extends RaceLookParts{	
	
    public OrcRaceParts() {
    	defineCustomRaceBodyParts();
    }	
    
    public OrcRaceParts(boolean init) {
        super(init);
        defineCustomRaceBodyParts();
     }	
	    
    public void defineCustomRaceBodyParts() {
 
    	
    	  this.addBodyPart("CUSTOM_HAIR",
         		 new BodyPart(OrcRaceParts.class,
         				 "CUSTOM_HAIR",
         				 "racemod.race",
         				 "custom_hair",
         				 8,
		    			 0,
		    			 26,
		    			 true,
         				 "player/race/orc/haircolors",
         				 "player/race/orc/hair/",
         				 new Point(64,64),
         				 new Point(32,32), new TextureReplacer(TextureReplacer.TARGET.HAIR), 5, true, true));
 	   
         this.addBodyPart("FACEHAIR",
        		 new BodyPart(OrcRaceParts.class,
        				 "FACEHAIR",
        				 "racemod.race",
        				 "facehair",
        				 2,
		    			 0,
		    			 26,
		    			 true,
        				 "player/race/orc/haircolors",
        				 "player/race/orc/facehair/",
        				 new Point(64,64),
        				 new Point(32,32), new TextureReplacer(TextureReplacer.TARGET.FACE_HAIR),
        				 5, 
        				 true,
        				 true));
         
         this.addBodyPart("FACIALFEATURES",
        		 new BodyPart(OrcRaceParts.class,
        				 "FACIALFEATURES",
        				 "racemod.race",
        				 "facialfeatures",
        				 3,
		    			 0,
		    			 17,
		    			 true,
        				 "player/race/orc/skincolors",
        				 "player/race/orc/facialfeatures/",
        				 new Point(64,64),
        				 new Point(32, 32), null, 5, true, false));
         
         this.addBodyPart("HEAD",
        		 new BodyPart(OrcRaceParts.class,
        				 "HEAD",
        				 "racemod.race",
        				 "head",
        				 2,
		    			 0,
		    			 17,
		    			 true,
        				 "player/race/orc/skincolors",
        				 "player/race/orc/head/",
        				 new Point(64,64),
        				 new Point(32, 32), new TextureReplacer(TextureReplacer.TARGET.HEAD), 5, true, false	));
         
         this.addBodyPart("BODY",
        		 new BodyPart(OrcRaceParts.class,
        				 "BODY",
        				 "racemod.race",
        				 "body",
        				 1,
		    			 0,
		    			 17,
		    			 true,
        				 "player/race/orc/skincolors",
        				 "player/race/orc/body/",
        				 new Point(64,64),
        				 new Point(32, 32), new TextureReplacer(TextureReplacer.TARGET.BODY), 5, true, false	));
         
         this.addBodyPart("ARMS",
        		 new BodyPart(OrcRaceParts.class,
        				 "ARMS",
        				 "racemod.race",
        				 "arms",
        				 1,
		    			 2,
		    			 17,
		    			 true,
        				 "player/race/orc/skincolors",
        				 "player/race/orc/arms/",
        				 new Point(64,64),
        				 new Point(32, 32), new TextureReplacer(TextureReplacer.TARGET.ARMS), 5, true, false	));
         
         this.addBodyPart("FEET",
        		 new BodyPart(OrcRaceParts.class,
        				 "FEET",
        				 "racemod.race",
        				 "feet",
        				 1,
		    			 0,
		    			 17,
		    			 true,
        				 "player/race/orc/skincolors",
        				 "player/race/orc/feet/",
        				 new Point(64,64),
        				 new Point(32, 32), new TextureReplacer(TextureReplacer.TARGET.SHOES), 5, true, false	));

         this.addBodyPart("CUSTOM_EYES",
        		 new EyeBodyPart(OrcRaceParts.class,
        				 "CUSTOM_EYES",
        				 "racemod.race",
        				 "custom_eyes",
        				 "player/race/orc/skincolors",
        				 "player/race/orc/eyes/eyecolors",
        				 "player/race/orc/eyes/",
        				 12, 16, 32, new Point(64,64),
        				 null, null, 5, true, 1)       		 		
        		 );
          
         this.hidePartCustomizer("BASE_SKIN");
         this.hidePartCustomizer("BASE_SKIN_COLOR");
         this.hidePartCustomizer("BASE_EYE");
         this.hidePartCustomizer("BASE_EYE_COLOR");
         this.hidePartCustomizer("BASE_HAIR");
         this.hidePartCustomizer("BASE_HAIR_COLOR");
         this.hidePartCustomizer("HEAD_COLOR");
         this.hidePartCustomizer("ARMS_COLOR");
         this.hidePartCustomizer("FEET_COLOR");
         this.hidePartCustomizer("FACIALFEATURES_COLOR");
         this.hidePartCustomizer("BASE_FACIAL_HAIR");
    }



}