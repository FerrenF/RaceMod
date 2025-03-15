package patches.player;

import net.bytebuddy.asm.Advice.Argument;
import net.bytebuddy.asm.Advice.This;
import core.race.CustomHumanLook;
import core.race.RaceLook;
import core.race.factory.RaceDataFactory;
import core.race.factory.RaceDataFactory.RaceData;
import helpers.DebugHelper;
import helpers.DebugHelper.MESSAGE_TYPE;
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
    
			RaceLook toApply = RaceLook.raceFromLoadData(save, new CustomHumanLook(true));
			th.look = toApply;
			r.addRaceData(toApply);
			DebugHelper.handleDebugMessage(String.format(
                    "applyLoadData for PlayerMob %s with race %s intercepted and interpreted from save.",
                    th.playerName, toApply.getRaceID()
                ), 50, MESSAGE_TYPE.DEBUG);
    	}
        return false;
    }
	
}
