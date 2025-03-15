package patches.server;

import java.lang.reflect.Field;

import core.network.CustomPacketPlayerAppearance;
import necesse.engine.network.packet.PacketSelectedCharacter;
import necesse.engine.network.server.Server;
import necesse.engine.network.server.ServerClient;
import necesse.engine.playerStats.EmptyStats.Mode;
import necesse.engine.playerStats.PlayerStats;
import net.bytebuddy.implementation.bind.annotation.AllArguments;
import net.bytebuddy.implementation.bind.annotation.This;

public class ServerClientApplyLoadedCharacterPacketPatch {

	public static void applyLoadedCharacterPacket(@This ServerClient th, @AllArguments Object[] args) {
		
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
            
        	PacketSelectedCharacter packet = (PacketSelectedCharacter)args[0];
        	characterUniqueIDField.set(th, packet.characterUniqueID);
    	
    		if (packet.networkData != null) {
    			th.newStats = new PlayerStats(false, Mode.WRITE_ONLY);
    			th.newStats.resetCombine();
    			th.newStats.cleanAll();
    			packet.networkData.applyToPlayer(th.playerMob);
    			if (!packet.networkData.applyToStats((PlayerStats) characterStatsField.get(th))) {
    				characterStatsField.set(th,  new PlayerStats(false, Mode.READ_ONLY));
    			}
    		}
    		
    		((Server)serverField.get(th)).usedNames.put(th.authentication, th.getName());
    		((Server)serverField.get(th)).world.savePlayer(th);
    		((Server)serverField.get(th)).network.sendToAllClients(new CustomPacketPlayerAppearance(th));
    		needAppearanceField.set(th, false);
    		submittedCharacterField.set(th,true);
        } catch (NoSuchFieldException | IllegalAccessException | IllegalArgumentException  e) {
            e.printStackTrace();
        }
		
	
	}

	
}
