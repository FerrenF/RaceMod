package patches.player;
import net.bytebuddy.asm.Advice.Argument;
import net.bytebuddy.asm.Advice.This;
import core.race.RaceLook;
import core.race.factory.RaceDataFactory;
import core.race.factory.RaceDataFactory.RaceData;
import helpers.DebugHelper;
import necesse.engine.modLoader.annotations.ModMethodPatch;
import necesse.engine.save.LoadData;
import necesse.engine.save.SaveData;
import necesse.entity.mobs.PlayerMob;
import net.bytebuddy.asm.Advice;

// Same deal as addSaveData, look has already been intercepted and replaced, so the correct superclass method will trigger and we do not need to force the game to do it.
@ModMethodPatch(target = PlayerMob.class, name = "addLoadedCharacterSaveData", arguments = {SaveData.class})
public class addLoadedCharacterSaveDataPatch {


    @Advice.OnMethodExit
    static void onExitAddLoadedCharacterSaveDataPatch(@Advice.This PlayerMob th, @Advice.Argument(0) SaveData save) {
        
    	if(RaceDataFactory.hasRaceData(th)) {    		
        	RaceData r = RaceDataFactory.getRaceData(th);
        		if(r.raceDataInitialized) {   
        			th.look=r.getRaceLook();
        			//r.getRaceLook().addSaveData(save);    			
        			DebugHelper.handleDebugMessage(String.format(
                            "addLoadedCharacterSaveData for PlayerMob %s intercepted with race "+r.getRaceID(),
                           th.playerName
                        ), 25);
        		}	    
        	}
      
    }
}
