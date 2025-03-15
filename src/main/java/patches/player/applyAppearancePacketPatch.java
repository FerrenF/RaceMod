package patches.player;

import core.race.RaceLook;
import core.race.factory.RaceDataFactory;
import core.race.factory.RaceDataFactory.RaceData;
import helpers.DebugHelper;
import necesse.engine.modLoader.annotations.ModMethodPatch;
import necesse.engine.network.packet.PacketPlayerAppearance;
import necesse.entity.mobs.PlayerMob;
import net.bytebuddy.asm.Advice;
import core.network.CustomPacketPlayerAppearance;
import core.race.CustomHumanLook;
@ModMethodPatch(target = PlayerMob.class, name = "applyAppearancePacket", arguments = {PacketPlayerAppearance.class})
public class applyAppearancePacketPatch {
	
	@Advice.OnMethodEnter(skipOn = Advice.OnNonDefaultValue.class)
    static boolean onEnter(@Advice.This PlayerMob th, @Advice.Argument(0) PacketPlayerAppearance _packet) {	 
		
		if(!(_packet instanceof CustomPacketPlayerAppearance)) return false;
		CustomPacketPlayerAppearance packet = (CustomPacketPlayerAppearance)_packet;
		if(RaceDataFactory.mobUniqueID(th)!=-1) {
			
	    	RaceData r = RaceDataFactory.getOrRegisterRaceData(th, packet.look);	
	      	th.refreshClientUpdateTime();
	      	if(!r.raceDataInitialized) return false;
	    	th.look = packet.look;
	    	th.getInv().giveLookArmor(false);
	    	th.playerName = packet.name;
	    	DebugHelper.handleDebugMessage(String.format(
	                "applyAppearancePacket for PlayerMob %s intercepted.",
	                th.playerName
	            ), 25);
	    	
	        return true;
			}
		return false;
    }
	
    @Advice.OnMethodExit
    static void onExitApplyAppearancePacketPatch(@Advice.This PlayerMob th, @Advice.Argument(0) PacketPlayerAppearance packet) {
    	
    	
     
    }
}
/*
this.refreshClientUpdateTime();
this.look = new HumanLook(packet.look);
this.inv.giveLookArmor(false);
this.playerName = packet.name;
*/