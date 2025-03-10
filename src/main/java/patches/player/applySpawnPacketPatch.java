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

@ModMethodPatch(target = PlayerMob.class, name = "applySpawnPacket", arguments = {PacketReader.class})
public class applySpawnPacketPatch {

	@Advice.OnMethodEnter(skipOn = Advice.OnNonDefaultValue.class)
    static boolean onEnter(@Advice.This PlayerMob th, @Advice.Argument(0) PacketReader reader) {	      
		

		if(RaceDataFactory.mobUniqueID(th)!=-1) {
	    	RaceData r = RaceDataFactory.getOrRegisterRaceData(th);
    		if(r.raceDataInitialized) {	    			
    			th.look = r.getRaceLook();
    			RaceMod.handleDebugMessage(String.format(
                        "applySpawnPacket for PlayerMob %s intercepted.",
                        th.playerName
                    ), 25);
    		}
    		else {
    			RaceLook toApply = RaceLook.raceFromContentPacker(reader, new CustomHumanLook(true));
    			th.look = toApply;
    			r.addRaceData(toApply);
    			RaceMod.handleDebugMessage(String.format(
                        "applySpawnPacket for PlayerMob %s intercepted and interpreted from packet.",
                        th.playerName
                    ), 25);
    		}	
    	}
		
        return false;
    }
    @Advice.OnMethodExit
    static void onExitApplySpawnPacketPatch(@Advice.This PlayerMob th, @Advice.Argument(0) PacketReader reader) {

    
    }
}
