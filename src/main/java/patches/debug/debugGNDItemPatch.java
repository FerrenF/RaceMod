package patches.debug;

import necesse.engine.network.PacketReader;
import necesse.engine.network.gameNetworkData.GNDItem;
import net.bytebuddy.asm.Advice;


// Helps figure out where those nasty packet misalignments are.
public class debugGNDItemPatch {
 
    @Advice.OnMethodExit
    static void onExit(@Advice.Return(readOnly = false) GNDItem result, @Advice.Argument(0) PacketReader reader) {
        if (result == null) {
            System.out.println("readGNDItem returned null, modifying behavior...");
            Thread.dumpStack();
        }
    }
}