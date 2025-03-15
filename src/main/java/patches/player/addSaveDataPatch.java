package patches.player;

import core.race.factory.RaceDataFactory;
import core.race.factory.RaceDataFactory.RaceData;
import helpers.DebugHelper;
import helpers.DebugHelper.MESSAGE_TYPE;
import necesse.engine.save.SaveData;
import necesse.entity.mobs.PlayerMob;
import net.bytebuddy.asm.Advice;


// We didn't need this one because by the time the game is calling addSaveData on the look, it has already been replaced by it's evil twin furry brother.
//@ModMethodPatch(target = PlayerMob.class, name = "addSaveData", arguments = {SaveData.class})
public class addSaveDataPatch {


    @Advice.OnMethodExit
    static void onExitAddSaveData(@Advice.This PlayerMob th, @Advice.Argument(0) SaveData save) {
    	
    	if(RaceDataFactory.hasRaceData(th)) {    		
    	RaceData r = RaceDataFactory.getRaceData(th);
    		if(r.raceDataInitialized) {   		
    			//	r.getRaceLook().addSaveData(save);    			
    			
    			DebugHelper.handleDebugMessage(String.format(
                        "addSaveData for PlayerMob %s intercepted.",
                       th.playerName
                    ), 50, MESSAGE_TYPE.DEBUG);
    		}	    
    	}
    }
}