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

@ModMethodPatch(target = PlayerMob.class, name = "applyLoadedCharacterLoadData", arguments = {LoadData.class})
public class applyLoadedCharacterLoadDataPatch {

	@Advice.OnMethodEnter()
    static boolean onEnter(@Advice.This PlayerMob th, @Advice.Argument(0) LoadData save) {	
		
		if(RaceDataFactory.mobUniqueID(th)!=-1) {
    		RaceData r = RaceDataFactory.getOrRegisterRaceData(th);
    		if(r.raceDataInitialized) {
    			th.look = r.getRaceLook();
    		}
    		else
    		{    			
    			RaceLook ra = RaceLook.raceFromLoadData(save, new CustomHumanLook(true));
		    	r.addRaceData(ra);
	    		th.look = ra;
				DebugHelper.handleDebugMessage(String.format(
		                "applyLoadedCharacterLoadData for PlayerMob %s intercepted with race %s.",
		                th.playerName, r.getRaceID()
		            ), 25);
	    		}	    	 	
    		}	    
		
        return false;
    }

    @Advice.OnMethodExit
    static void onExitApplyLoadedCharacterLoadData(@Advice.This PlayerMob th, @Advice.Argument(0) LoadData save) {
       
    
    	
    }
}
