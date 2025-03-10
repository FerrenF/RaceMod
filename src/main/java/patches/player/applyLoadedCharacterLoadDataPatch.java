package patches.player;
import net.bytebuddy.asm.Advice.Argument;
import net.bytebuddy.asm.Advice.This;
import core.RaceMod;
import core.race.CustomHumanLook;
import extensions.RaceLook;
import factory.RaceDataFactory;
import factory.RaceDataFactory.RaceData;
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
				RaceMod.handleDebugMessage(String.format(
		                "applyLoadedCharacterLoadData for PlayerMob %s intercepted with race %s.",
		                th.playerName, r.race_id
		            ), 25);
	    		}	    	 	
    		}
	    	else {
	    		
	    	}
		
        return false;
    }

    @Advice.OnMethodExit
    static void onExitApplyLoadedCharacterLoadData(@Advice.This PlayerMob th, @Advice.Argument(0) LoadData save) {
       
    
    	
    }
}
