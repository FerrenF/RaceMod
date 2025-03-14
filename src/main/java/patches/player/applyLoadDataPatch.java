package patches.player;

import net.bytebuddy.asm.Advice.Argument;
import net.bytebuddy.asm.Advice.This;
import core.race.CustomHumanLook;
import core.race.RaceLook;
import core.race.factory.RaceDataFactory;
import core.race.factory.RaceDataFactory.RaceData;
import helpers.DebugHelper;
import necesse.engine.modLoader.annotations.ModMethodPatch;
import necesse.engine.network.PacketReader;
import necesse.engine.save.LoadData;
import necesse.entity.mobs.PlayerMob;
import net.bytebuddy.asm.Advice;

@ModMethodPatch(target = PlayerMob.class, name = "applyLoadData", arguments = {LoadData.class})
public class applyLoadDataPatch {
	
	
	@Advice.OnMethodEnter()
    static boolean onEnter(@Advice.This PlayerMob th, @Advice.Argument(0) LoadData save) {
				
		if(RaceDataFactory.mobUniqueID(th)!=-1) {
	    	RaceData r = RaceDataFactory.getOrRegisterRaceData(th);
    		if(r.raceDataInitialized) {	    			
    			th.look = r.getRaceLook();
    			DebugHelper.handleDebugMessage(String.format(
                        "applyLoadData for PlayerMob %s intercepted.",
                        th.playerName
                    ), 25);
    		}
    		else {
    			RaceLook toApply = RaceLook.raceFromLoadData(save, new CustomHumanLook(true));
    			th.look = toApply;
    			r.addRaceData(toApply);
    			DebugHelper.handleDebugMessage(String.format(
                        "applyLoadData for PlayerMob %s with race %s intercepted and interpreted from save.",
                        th.playerName, toApply.getRaceID()
                    ), 25);
    		}	
    	}
        return false;
    }
	
    @Advice.OnMethodExit
    static void onExitApplyLoadData(@Advice.This PlayerMob th, @Advice.Argument(0) LoadData save) {
    	
    /*	
		if(!RaceDataFactory.hasRaceData(th)) {
    		RaceLook ra = RaceLook.raceFromLoadData(save, new CustomHumanLook(true));
	    	RaceData r = RaceDataFactory.getOrRegisterRaceData(th, ra);	    	
	    	
			RaceMod.handleDebugMessage(String.format(
	                "applyLoadData for PlayerMob %s intercepted with race %s.",
	                th.playerName, r.race_id
	            ), 25);
	    	 	
    	}  	*/
    	
    }
}
