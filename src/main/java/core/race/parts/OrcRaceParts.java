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
         				 true,
         				 false,
         				 true,
         				 true,
         				 false,
         				 true,
         				 "player/race/orc/haircolors",
         				 "player/race/orc/hair/",
         				 new Point(64,64),
         				 new Point(32,32), new TextureReplacer(TextureReplacer.TARGET.HAIR), 5, true));
 	   
         this.addBodyPart("FACEHAIR",
        		 new BodyPart(OrcRaceParts.class,
        				 "FACEHAIR",
        				 "racemod.race",
        				 "facehair",
        				 true,
        				 false,
        				 true,
        				 true,
        				 false,
        				 true,
        				 "player/race/orc/haircolors",
        				 "player/race/orc/facehair/",
        				 new Point(64,64),
        				 new Point(32,32), new TextureReplacer(TextureReplacer.TARGET.FACE_HAIR), 5, true));
         
         this.addBodyPart("FACIALFEATURES",
        		 new BodyPart(OrcRaceParts.class,
        				 "FACIALFEATURES",
        				 "racemod.race",
        				 "facialfeatures",
        				 true,
        				 false,
        				 true,
        				 true,
        				 false,
        				 true,
        				 "player/race/orc/skincolors",
        				 "player/race/orc/facialfeatures/",
        				 new Point(64,64),
        				 new Point(32, 32), null, 5, true));
         
         this.addBodyPart("HEAD",
        		 new BodyPart(OrcRaceParts.class,
        				 "HEAD",
        				 "racemod.race",
        				 "head",
        				 true,
        				 false,
        				 true,
        				 true,
        				 false,
        				 true,
        				 "player/race/orc/skincolors",
        				 "player/race/orc/head/",
        				 new Point(64,64),
        				 new Point(32, 32), new TextureReplacer(TextureReplacer.TARGET.HEAD), 5, true	));
         
         this.addBodyPart("BODY",
        		 new BodyPart(OrcRaceParts.class,
        				 "BODY",
        				 "racemod.race",
        				 "body",
        				 true,
        				 false,
        				 true,
        				 true,
        				 false,
        				 true,
        				 "player/race/orc/skincolors",
        				 "player/race/orc/body/",
        				 new Point(64,64),
        				 new Point(32, 32), new TextureReplacer(TextureReplacer.TARGET.BODY), 5, true	));
         
         this.addBodyPart("ARMS",
        		 new BodyPart(OrcRaceParts.class,
        				 "ARMS",
        				 "racemod.race",
        				 "arms",
        				 true,
        				 true,
        				 true,
        				 true,
        				 false,
        				 true,
        				 "player/race/orc/skincolors",
        				 "player/race/orc/arms/",
        				 new Point(64,64),
        				 new Point(32, 32), new TextureReplacer(TextureReplacer.TARGET.ARMS), 5, true	));
         
         this.addBodyPart("FEET",
        		 new BodyPart(OrcRaceParts.class,
        				 "FEET",
        				 "racemod.race",
        				 "feet",
        				 true,
        				 false,
        				 true,
        				 true,
        				 false,
        				 true,
        				 "player/race/orc/skincolors",
        				 "player/race/orc/feet/",
        				 new Point(64,64),
        				 new Point(32, 32), new TextureReplacer(TextureReplacer.TARGET.SHOES), 5, true	));

         this.addBodyPart("CUSTOM_EYES",
        		 new EyeBodyPart(OrcRaceParts.class,
        				 "CUSTOM_EYES",
        				 "racemod.race",
        				 "custom_eyes",
        				 "player/race/orc/skincolors",
        				 "player/race/orc/eyes/eyecolors",
        				 "player/race/orc/eyes/",
        				 new Point(64,64),
        				 null, null, 5, true)       		 		
        		 );
          
         this.hidePartCustomizer("SKIN_COLOR");
         this.hidePartCustomizer("EYE_COLOR");
         this.hidePartCustomizer("EYE_TYPE");
         this.hidePartCustomizer("HAIR_STYLE");
         this.hidePartCustomizer("HAIR_COLOR");
         this.hidePartCustomizer("HEAD_COLOR");
         this.hidePartCustomizer("ARMS_COLOR");
         this.hidePartCustomizer("FEET_COLOR");
         this.hidePartCustomizer("FACIALFEATURES_COLOR");
         this.hidePartCustomizer("FACIAL_HAIR");
    }



}