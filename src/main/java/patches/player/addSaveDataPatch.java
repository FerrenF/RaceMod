package patches.player;

import factory.RaceDataFactory;
import factory.RaceDataFactory.RaceData;
import net.bytebuddy.asm.Advice.Argument;
import net.bytebuddy.asm.Advice.This;
import core.RaceMod;
import extensions.RaceLook;
import factory.RaceDataFactory;
import necesse.engine.modLoader.annotations.ModMethodPatch;
import necesse.engine.save.LoadData;
import necesse.engine.save.SaveData;
import necesse.entity.mobs.PlayerMob;
import net.bytebuddy.asm.Advice;


@ModMethodPatch(target = PlayerMob.class, name = "addSaveData", arguments = {SaveData.class})
public class addSaveDataPatch {


    @Advice.OnMethodExit
    static void onExitAddSaveData(@Advice.This PlayerMob th, @Advice.Argument(0) SaveData save) {
    	
    	if(RaceDataFactory.hasRaceData(th)) {    		
    	RaceData r = RaceDataFactory.getRaceData(th);
    		if(r.raceDataInitialized) {   		
    			//	r.getRaceLook().addSaveData(save);    			
    			
    			RaceMod.handleDebugMessage(String.format(
                        "addSaveData for PlayerMob %s intercepted.",
                       th.playerName
                    ), 25);
    		}	    
    	}
    }
}