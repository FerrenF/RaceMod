package patches.packets;

import helpers.DebugHelper;
import helpers.DebugHelper.MESSAGE_TYPE;
import necesse.engine.modLoader.annotations.ModConstructorPatch;
import necesse.engine.network.packet.PacketConnectApproved;
import necesse.engine.network.server.Server;
import necesse.engine.network.server.ServerClient;
import net.bytebuddy.asm.Advice;

@ModConstructorPatch(target = PacketConnectApproved.class, arguments = {Server.class, ServerClient.class})
public class PacketConnectApprovedConstructorPatch2 {
	@Advice.OnMethodEnter(skipOn = Advice.OnNonDefaultValue.class)
    static boolean onEnter() {
		 DebugHelper.handleDebugMessage("Intercepted and bypassed PacketConnectApproved [Server, ServerClient] constructor.", 70, MESSAGE_TYPE.DEBUG);
        return true;  // Skips constructor execution entirely
    }
}
