package patches.player;
import core.race.CustomHumanLook;
import core.race.RaceLook;
import core.race.factory.RaceDataFactory;
import core.race.factory.RaceDataFactory.RaceData;
import helpers.DebugHelper;
import helpers.DebugHelper.MESSAGE_TYPE;
import necesse.engine.modLoader.annotations.ModMethodPatch;
import necesse.engine.save.LoadData;
import necesse.entity.mobs.PlayerMob;
import net.bytebuddy.asm.Advice;

@ModMethodPatch(target = PlayerMob.class, name = "applyLoadedCharacterLoadData", arguments = {LoadData.class})
public class applyLoadedCharacterLoadDataPatch {

	@Advice.OnMethodEnter()
    static boolean onEnter(@Advice.This PlayerMob th, @Advice.Argument(0) LoadData save) {	
		
		if(RaceDataFactory.mobUniqueID(th)!=-1) {
    /*		if(r.raceDataInitialized) {
    			th.look = r.getRaceLook();
    			DebugHelper.handleDebugMessage(String.format(
                        "applyLoadedCharacterLoadData for PlayerMob %s intercepted with race %s.",
                        th.playerName, r.getRaceID()
                    ), 50, MESSAGE_TYPE.DEBUG);
    		}
    		else
    		{    */			
    			RaceLook ra = RaceLook.raceFromLoadData(save, new CustomHumanLook(true));		    
	    		th.look = ra;
	    		RaceDataFactory.getOrRegisterRaceData(th, ra);
				DebugHelper.handleDebugMessage(String.format(
		                "applyLoadedCharacterLoadData for PlayerMob %s intercepted and interpreted with race %s.",
		                th.playerName, ra.getRaceID()
		            ), 50, MESSAGE_TYPE.DEBUG);
	    		}	    	 	
    		//}	    
		
        return false;
    }

    @Advice.OnMethodExit
    static void onExitApplyLoadedCharacterLoadData(@Advice.This PlayerMob th, @Advice.Argument(0) LoadData save) {
       
    
    	
    }
}
