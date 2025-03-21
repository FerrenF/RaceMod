package patches.server;

import java.lang.reflect.Field;

import core.network.CustomPacketPlayerAppearance;
import helpers.DebugHelper;
import helpers.DebugHelper.MESSAGE_TYPE;
import necesse.engine.network.packet.PacketSelectedCharacter;
import necesse.engine.network.server.Server;
import necesse.engine.network.server.ServerClient;
import necesse.engine.playerStats.EmptyStats.Mode;
import necesse.engine.playerStats.PlayerStats;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.implementation.bind.annotation.AllArguments;
import net.bytebuddy.implementation.bind.annotation.Argument;
import net.bytebuddy.implementation.bind.annotation.This;


public class ServerClientApplyLoadedCharacterPacketPatch {

	@Advice.OnMethodEnter(skipOn = Advice.OnNonDefaultValue.class)
	public static boolean applyLoadedCharacterPacket(@This ServerClient th, @Argument(0) PacketSelectedCharacter packet) {
		
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
            
        	characterUniqueIDField.set(th, packet.characterUniqueID);
    	
    		if (packet.networkData != null) {
    			th.newStats = new PlayerStats(false, Mode.WRITE_ONLY);
    			th.newStats.resetCombine();
    			th.newStats.cleanAll();
    	
    			packet.networkData.applyToPlayer(th.playerMob);
    			
    			
    			PlayerStats currentStats = (PlayerStats) characterStatsField.get(th);
    			if (!packet.networkData.applyToStats(currentStats)) {
    			    characterStatsField.set(th, new PlayerStats(false, Mode.READ_ONLY));
    			}
    			
    		}
    		
    		Server serverInstance = (Server) serverField.get(th);
    		if (serverInstance != null) {
    		    serverInstance.usedNames.put(th.authentication, th.getName());
    		    serverInstance.world.savePlayer(th);
    		    serverInstance.network.sendToAllClients(new CustomPacketPlayerAppearance(th));
    		}
    		needAppearanceField.set(th, false);
    		submittedCharacterField.set(th,true);
    		    	    		
        } catch (NoSuchFieldException | IllegalAccessException | IllegalArgumentException  e) {
            e.printStackTrace();
        }
		
		return true;
	}

	
}
