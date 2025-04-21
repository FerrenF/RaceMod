package core.race.parts;

import java.awt.Point;

public class NekoRaceParts extends RaceLookParts{	
		
	public NekoRaceParts() {
		super();
		defineCustomRaceBodyParts();
	}
	
    public NekoRaceParts(boolean init) {
       super(init);
       defineCustomRaceBodyParts();
    }
    
    public void defineCustomRaceBodyParts() {    	
    	 this.addBodyPart("TAIL",
    			 new BodyPart(NekoRaceParts.class,
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
		    			 true,
		    			 true));
    	 
         this.addBodyPart("EARS",
        		 new BodyPart(NekoRaceParts.class,
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
        				 new Point(32,32),
        				 null,
        				 5,
        				 true,
        				 true));
    }
   
}