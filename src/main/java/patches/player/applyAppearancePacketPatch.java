package patches.player;

import net.bytebuddy.asm.Advice.Argument;
import net.bytebuddy.asm.Advice.This;
import core.RaceMod;
import factory.RaceDataFactory;
import factory.RaceDataFactory.RaceData;
import necesse.engine.modLoader.annotations.ModMethodPatch;
import necesse.engine.network.PacketReader;
import necesse.engine.network.packet.PacketPlayerAppearance;
import necesse.engine.save.LoadData;
import necesse.entity.mobs.PlayerMob;
import net.bytebuddy.asm.Advice;

@ModMethodPatch(target = PlayerMob.class, name = "applyAppearancePacket", arguments = {PacketPlayerAppearance.class})
public class applyAppearancePacketPatch {

    @Advice.OnMethodExit
    static void onExitApplyAppearancePacketPatch(@Advice.This PlayerMob th, @Advice.Argument(0) PacketPlayerAppearance packet) {
        // Call the original method manually to apply load data
    	if(RaceDataFactory.hasRaceData(th)) {
    		RaceData r = RaceDataFactory.getRaceData(th);
    		if(r.raceDataInitialized) {
    			
    			PacketReader reader = new PacketReader(packet);
    			r.getRaceLook().applyContentPacket(reader);
    			RaceMod.handleDebugMessage(String.format(
                        "applyAppearancePacket for PlayerMob %s intercepted.",
                        th.playerName
                    ), 25);
    		}
    	}
     
    }
}
