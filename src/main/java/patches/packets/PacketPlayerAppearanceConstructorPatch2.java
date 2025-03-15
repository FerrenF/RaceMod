package patches.packets;

import helpers.DebugHelper;
import helpers.DebugHelper.MESSAGE_TYPE;
import necesse.engine.modLoader.annotations.ModConstructorPatch;
import necesse.engine.network.packet.PacketPlayerAppearance;
import necesse.engine.network.server.ServerClient;
import net.bytebuddy.asm.Advice;

@ModConstructorPatch(target = PacketPlayerAppearance.class, arguments = {ServerClient.class})
public class PacketPlayerAppearanceConstructorPatch2 {
	  @Advice.OnMethodEnter()
	  static boolean onEnter() {
		DebugHelper.handleDebugMessage("Intercepted and bypassed PacketPlayerAppearance [ServerClient] constructor.", 50, MESSAGE_TYPE.DEBUG);
        return true;  // Skips constructor execution entirely
	  }
}