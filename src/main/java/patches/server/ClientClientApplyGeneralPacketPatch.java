package patches.server;

import java.lang.reflect.Field;

import helpers.DebugHelper;
import helpers.DebugHelper.MESSAGE_TYPE;
import necesse.engine.modLoader.annotations.ModMethodPatch;
import necesse.engine.network.NetworkClient;
import necesse.engine.network.PacketReader;
import necesse.engine.network.client.ClientClient;
import necesse.engine.network.packet.PacketPlayerGeneral;
import necesse.entity.mobs.PlayerMob;
import necesse.level.maps.Level;
import net.bytebuddy.asm.Advice;

@ModMethodPatch(arguments = { PacketPlayerGeneral.class }, name = "applyGeneralPacket", target = ClientClient.class)
public class ClientClientApplyGeneralPacketPatch {
	
	 @Advice.OnMethodEnter(skipOn = Advice.OnNonDefaultValue.class)
	 static boolean onEnter(@Advice.This ClientClient th,
	   		@Advice.AllArguments Object[] args) {
	   		DebugHelper.handleDebugMessage("ClientClientApplyGeneralPacketPatch intercepted.", 60, MESSAGE_TYPE.DEBUG);
			return true;
	 }
	 
	 @Advice.OnMethodExit
	 static void onExit(@Advice.This ClientClient th, @Advice.Argument(0) PacketPlayerGeneral packet) {
		try {
			Field hasSpawnedF = NetworkClient.class.getDeclaredField("hasSpawned");
			hasSpawnedF.setAccessible(true);
			
			Field isDeadF = NetworkClient.class.getDeclaredField("isDead");
			isDeadF.setAccessible(true);
			
			th.setLevelIdentifier(packet.levelIdentifier);
			Level level = null;
			if (th.getClient().getLevel() != null && th.isSamePlace(th.getClient().getLevel())) {
				level = th.getClient().getLevel();
			}
	
			if (th.playerMob == null) {
				th.playerMob = new PlayerMob((long) th.slot, th);
				if (th.getClient().getSlot() == th.slot) {
					th.playerMob.staySmoothSnapped = true;
				}
			}
			
			th.setTeamID(packet.team);
			th.playerMob.playerName = packet.name;
			th.playerMob.setUniqueID(th.slot);
			th.playerMob.setLevel(level);
			th.playerMob.setWorldData(th.getClient().worldEntity, th.getClient().worldSettings);
			th.playerMob.applySpawnPacket(new PacketReader(packet.playerSpawnContent));
			th.pvpEnabled = packet.pvpEnabled;
			if (packet.hasSpawned) {
				th.applySpawned(packet.remainingSpawnInvincibilityTime);
			} else {
				hasSpawnedF.set(th, false);
			}
	
		
			if (!isDeadF.getBoolean(th)) {
				th.playerMob.restore();
			}
	
			th.playerMob.init();
			if (packet.isDead) {
				th.die(packet.remainingRespawnTime);
			} else {
			
					isDeadF.set(th,false);
			
			}
	
			th.loadedPlayer = true;
			th.getClient().loading.playersPhase.submitLoadedPlayer(th.slot);
		
		} catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException | SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	 }
	
	/*public static void applyGeneralPacket(@This ClientClient th, @Argument(0) PacketPlayerGeneral packet) {
		try {
			Field hasSpawnedF = NetworkClient.class.getDeclaredField("hasSpawned");
			hasSpawnedF.setAccessible(true);
			
			Field isDeadF = NetworkClient.class.getDeclaredField("isDead");
			isDeadF.setAccessible(true);
			
			th.setLevelIdentifier(packet.levelIdentifier);
			Level level = null;
			if (th.getClient().getLevel() != null && th.isSamePlace(th.getClient().getLevel())) {
				level = th.getClient().getLevel();
			}
	
			if (th.playerMob == null) {
				th.playerMob = new PlayerMob((long) th.slot, th);
				if (th.getClient().getSlot() == th.slot) {
					th.playerMob.staySmoothSnapped = true;
				}
			}
			
			th.setTeamID(packet.team);
			th.playerMob.playerName = packet.name;
			th.playerMob.setUniqueID(th.slot);
			th.playerMob.setLevel(level);
			th.playerMob.setWorldData(th.getClient().worldEntity, th.getClient().worldSettings);
			th.playerMob.applySpawnPacket(new PacketReader(packet.playerSpawnContent));
			th.pvpEnabled = packet.pvpEnabled;
			if (packet.hasSpawned) {
				th.applySpawned(packet.remainingSpawnInvincibilityTime);
			} else {
				hasSpawnedF.set(th, false);
			}
	
		
			if (!isDeadF.getBoolean(th)) {
				th.playerMob.restore();
			}
	
			th.playerMob.init();
			if (packet.isDead) {
				th.die(packet.remainingRespawnTime);
			} else {
			
					isDeadF.set(th,false);
			
			}
	
			th.loadedPlayer = true;
			th.getClient().loading.playersPhase.submitLoadedPlayer(th.slot);
		
		} catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException | SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}*/
}
