package patches;

import java.lang.instrument.Instrumentation;

import core.RaceMod;
import extensions.CustomRaceStylistContainer;
import necesse.engine.network.packet.PacketOpenContainer;
import necesse.engine.network.server.Server;
import necesse.engine.network.server.ServerClient;
import necesse.entity.mobs.friendly.human.humanShop.StylistHumanMob;
import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.matcher.ElementMatchers;

//@ModMethodPatch(target = StylistHumanMob.class, name = "getOpenShopPacket", arguments = {Server.class, ServerClient.class})
public class getStylistOpenShopPacketPatch {
	
	@Advice.OnMethodExit
    static void onExit(@Advice.This StylistHumanMob th, 
            @Advice.Argument(0) Server server, 
            @Advice.Argument(1) ServerClient client,  // Fixed argument index
            @Advice.Return(readOnly = false) PacketOpenContainer container) {	      
		
		container = PacketOpenContainer.Mob(RaceMod.CUSTOM_STYLIST_CONTAINER, th,
				CustomRaceStylistContainer.getStylistContainerContent(th, client));
		
    }
}
