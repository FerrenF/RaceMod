package patches.packets;

import helpers.DebugHelper;
import helpers.DebugHelper.MESSAGE_TYPE;
import necesse.engine.modLoader.annotations.ModConstructorPatch;
import necesse.engine.network.packet.PacketPlayerAppearance;
import necesse.engine.network.server.ServerClient;
import necesse.entity.mobs.PlayerMob;
import net.bytebuddy.asm.Advice;

@ModConstructorPatch(target = PacketPlayerAppearance.class, arguments = {int.class, int.class, PlayerMob.class})
public class PacketPlayerAppearanceConstructorPatch3 {
	  @Advice.OnMethodEnter()
	  static boolean onEnter() {
		  DebugHelper.handleDebugMessage("Intercepted and bypassed PacketPlayerAppearance constructor.", 50, MESSAGE_TYPE.DEBUG);
        return true;  // Skips constructor execution entirely
	  }
}