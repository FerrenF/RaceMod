package patches.packets;

import helpers.DebugHelper;
import helpers.DebugHelper.MESSAGE_TYPE;
import necesse.engine.modLoader.annotations.ModConstructorPatch;
import necesse.engine.network.packet.PacketConnectApproved;
import net.bytebuddy.asm.Advice;

@ModConstructorPatch(target = PacketConnectApproved.class, arguments = {byte[].class})
public class PacketConnectApprovedConstructorPatch1 {
	  @Advice.OnMethodEnter()
	  static boolean onEnter() {
		  DebugHelper.handleDebugMessage("Intercepted and bypassed PacketConnectApproved constructor.", 50, MESSAGE_TYPE.DEBUG);
        return true;  // Skips constructor execution entirely
	  }
}
