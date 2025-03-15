package patches.debug;

import necesse.engine.modLoader.annotations.ModMethodPatch;
import necesse.engine.network.PacketReader;
import necesse.engine.network.gameNetworkData.GNDItem;
import necesse.engine.network.packet.PacketPlayerAppearance;
import necesse.engine.registries.GNDRegistry;
import necesse.entity.mobs.PlayerMob;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.asm.Advice;

public class debugGNDItemPatch {
 
    @Advice.OnMethodExit
    static void onExit(@Advice.Return(readOnly = false) GNDItem result, @Advice.Argument(0) PacketReader reader) {
        if (result == null) {
            System.out.println("readGNDItem returned null, modifying behavior...");
            Thread.dumpStack();
        }
    }
}