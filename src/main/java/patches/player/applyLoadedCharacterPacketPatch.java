package patches.player;
import core.race.CustomHumanLook;
import core.race.RaceLook;
import core.race.factory.RaceDataFactory;
import core.race.factory.RaceDataFactory.RaceData;
import helpers.DebugHelper;
import necesse.engine.modLoader.annotations.ModMethodPatch;
import necesse.engine.network.PacketReader;
import necesse.entity.mobs.PlayerMob;
import net.bytebuddy.asm.Advice;

@ModMethodPatch(target = PlayerMob.class, name = "applyLoadedCharacterPacket", arguments = {PacketReader.class})
public class applyLoadedCharacterPacketPatch {
	
	@Advice.OnMethodEnter()
    static boolean onEnter(@Advice.This PlayerMob th, @Advice.Argument(0) PacketReader reader) {	
		if(RaceDataFactory.mobUniqueID(th)!=-1) {
			
			RaceData r = RaceDataFactory.getOrRegisterRaceData(th);
    		if(r.raceDataInitialized) {	    			
    			th.look = r.getRaceLook();
    			DebugHelper.handleDebugMessage(String.format(
                        "applyLoadedCharacterPacket for PlayerMob %s intercepted.",
                        th.playerName
                    ), 25);
    		}
    		else {
    			RaceLook ra = RaceLook.raceFromContentPacker(reader, new CustomHumanLook(true));
				th.look = ra;
				r.addRaceData(ra);
    		}

		}
        return false;
    }

    @Advice.OnMethodExit
    static void onExitApplyLoadedCharacterPacket(@Advice.This PlayerMob th, @Advice.Argument(0) PacketReader reader) {
        // Call the original method manually to apply load data
      
    
     
    }
}
