package patches.player;

import core.race.factory.RaceDataFactory;
import helpers.DebugHelper;
import helpers.DebugHelper.MESSAGE_TYPE;
import necesse.engine.modLoader.annotations.ModMethodPatch;
import necesse.engine.network.packet.PacketPlayerAppearance;
import necesse.entity.mobs.PlayerMob;
import net.bytebuddy.asm.Advice;
import core.network.CustomPacketPlayerAppearance;
@ModMethodPatch(target = PlayerMob.class, name = "applyAppearancePacket", arguments = {PacketPlayerAppearance.class})
public class applyAppearancePacketPatch {
	
	@Advice.OnMethodEnter(skipOn = Advice.OnNonDefaultValue.class)
    static boolean onEnter(@Advice.This PlayerMob th, @Advice.Argument(0) PacketPlayerAppearance _packet) {	 
		
		CustomPacketPlayerAppearance packet = (CustomPacketPlayerAppearance)_packet;			
      	th.refreshClientUpdateTime();
    	th.look = packet.look;
    	th.getInv().giveLookArmor(false);
    	th.playerName = packet.name;
    	RaceDataFactory.getOrRegisterRaceData(th, packet.look);
    	DebugHelper.handleDebugMessage(String.format(
                "applyAppearancePacket for PlayerMob %s intercepted with race "+packet.look.getRaceID(),
                th.playerName
            ), 50, MESSAGE_TYPE.DEBUG);
    	
        return true;
    }
	
}
/*
this.refreshClientUpdateTime();
this.look = new HumanLook(packet.look);
this.inv.giveLookArmor(false);
this.playerName = packet.name;
*/