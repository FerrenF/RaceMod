package patches.player;
import core.race.CustomHumanLook;
import core.race.RaceLook;
import core.race.factory.RaceDataFactory;
import core.race.factory.RaceDataFactory.RaceData;
import helpers.DebugHelper;
import helpers.DebugHelper.MESSAGE_TYPE;
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
                    ), 50, MESSAGE_TYPE.DEBUG);
    		}
    		else {
    			RaceLook ra = RaceLook.raceFromContentPacker(reader, new CustomHumanLook(true));
				th.look = ra;
				r.addRaceData(ra);
				
				DebugHelper.handleDebugMessage(String.format(
                        "applyLoadedCharacterPacket for PlayerMob %s with race %s intercepted and interpreted from packet.",
                        th.playerName, r.getRaceID()
                    ), 50, MESSAGE_TYPE.DEBUG);
    		}

		}
        return false;
    }

    @Advice.OnMethodExit
    static void onExitApplyLoadedCharacterPacket(@Advice.This PlayerMob th, @Advice.Argument(0) PacketReader reader) {
        // Call the original method manually to apply load data
      
    
     
    }
}
