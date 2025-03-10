package patches.player;
import core.RaceMod;
import core.race.CustomHumanLook;
import extensions.RaceLook;
import factory.RaceDataFactory;
import factory.RaceDataFactory.RaceData;
import necesse.engine.modLoader.annotations.ModMethodPatch;
import necesse.engine.network.PacketReader;
import necesse.entity.mobs.PlayerMob;
import net.bytebuddy.asm.Advice;

@ModMethodPatch(target = PlayerMob.class, name = "applyLoadedCharacterPacket", arguments = {PacketReader.class})
public class applyLoadedCharacterPacketPatch {
	
	@Advice.OnMethodEnter()
    static boolean onEnter(@Advice.This PlayerMob th, @Advice.Argument(0) PacketReader reader) {	
		if(RaceDataFactory.mobUniqueID(th)!=-1) {
			RaceLook ra;
			RaceData r;
			if(!RaceDataFactory.hasRaceData(th)) {
				ra = RaceLook.raceFromContentPacker(reader, new CustomHumanLook(true));
				ra.applyContentPacket(reader);
				th.look = ra;
				r = RaceDataFactory.getOrRegisterRaceData(th, ra);  
			}
			
		}
        return false;
    }

    @Advice.OnMethodExit
    static void onExitApplyLoadedCharacterPacket(@Advice.This PlayerMob th, @Advice.Argument(0) PacketReader reader) {
        // Call the original method manually to apply load data
      
    	if(RaceDataFactory.mobUniqueID(th)!=-1) {
    		
    	
    		
	    	/*RaceData r = RaceDataFactory.getOrRegisterRaceData(th);
	    	if(r!=null) {
	    		if(r.raceDataInitialized) {
	    			//r.getRaceLook().applyContentPacket(reader);
	    			RaceMod.handleDebugMessage(String.format(
	                        "applyLoadedCharacterPacket for PlayerMob %s intercepted.",
	                        th.playerName
	                    ), 25);
	    		}
	    	}*/
    	
    	}
     
    }
}
