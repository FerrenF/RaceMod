package patches.packets;

import necesse.engine.modLoader.annotations.ModConstructorPatch;
import necesse.engine.network.packet.PacketConnectApproved;
import necesse.engine.network.server.Server;
import necesse.engine.network.server.ServerClient;
import net.bytebuddy.asm.Advice;

@ModConstructorPatch(target = PacketConnectApproved.class, arguments = {Server.class, ServerClient.class})
public class PacketConnectApprovedConstructorPatch2 {
	@Advice.OnMethodEnter(skipOn = Advice.OnNonDefaultValue.class)
    static boolean onEnter() {
        return true;  // Skips constructor execution entirely
    }
}
