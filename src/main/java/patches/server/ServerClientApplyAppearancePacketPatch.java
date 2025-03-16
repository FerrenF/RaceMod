package patches.server;

import java.lang.reflect.Field;

import core.network.CustomPacketPlayerAppearance;
import necesse.engine.network.packet.PacketSelectedCharacter;
import necesse.engine.network.server.Server;
import necesse.engine.network.server.ServerClient;
import necesse.engine.playerStats.EmptyStats.Mode;
import necesse.engine.playerStats.PlayerStats;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.implementation.bind.annotation.AllArguments;
import net.bytebuddy.implementation.bind.annotation.This;

public class ServerClientApplyAppearancePacketPatch {

	@Advice.OnMethodEnter(skipOn = Advice.OnNonDefaultValue.class)
	public static boolean applyAppearancePacket(@This ServerClient th, @AllArguments Object[] args) {
		
		CustomPacketPlayerAppearance packet = (CustomPacketPlayerAppearance)args[0];
		try {
            // Make the fields accessible via reflection
            Field characterUniqueIDField = ServerClient.class.getDeclaredField("characterUniqueID");
            Field characterStatsField = ServerClient.class.getDeclaredField("characterStats");
            Field serverField = ServerClient.class.getDeclaredField("server");
            Field needAppearanceField = ServerClient.class.getDeclaredField("needAppearance");
            Field submittedCharacterField = ServerClient.class.getDeclaredField("submittedCharacter");

            characterUniqueIDField.setAccessible(true);
            characterStatsField.setAccessible(true);
            serverField.setAccessible(true);
            needAppearanceField.setAccessible(true);
            submittedCharacterField.setAccessible(true);
            
            characterUniqueIDField.set(th,packet.characterUniqueID);
        	th.playerMob.applyAppearancePacket(packet);
        	((Server)serverField.get(th)).usedNames.put(th.authentication, th.getName());
        	((Server)serverField.get(th)).world.savePlayer(th);
        	((Server)serverField.get(th)).network.sendToAllClients(packet);
        	needAppearanceField.set(th,false);
        	submittedCharacterField.set(th,true);
        	
        } catch (NoSuchFieldException | IllegalAccessException | IllegalArgumentException  e) {
            e.printStackTrace();
        }
		
		return true;
	}

	
}
