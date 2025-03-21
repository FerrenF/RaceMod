package patches.player;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import core.RaceMod;
import core.race.CustomHumanLook;
import core.race.RaceLook;
import core.race.factory.RaceDataFactory;
import core.race.factory.RaceDataFactory.RaceData;
import helpers.DebugHelper;
import necesse.engine.modLoader.annotations.ModMethodPatch;
import necesse.engine.network.PacketWriter;
import necesse.entity.mobs.Mob;
import necesse.entity.mobs.PlayerMob;
import necesse.entity.mobs.networkField.MobNetworkFieldRegistry;
import net.bytebuddy.asm.Advice;

@ModMethodPatch(target = PlayerMob.class, name = "setupSpawnPacket", arguments = {PacketWriter.class})
public class setupSpawnPacketPatch {
	
	@Advice.OnMethodEnter(skipOn = Advice.OnNonDefaultValue.class)
    static boolean onEnter(@Advice.This PlayerMob th, @Advice.Argument(0) PacketWriter writer) {	      
		 try {
			 
			 	Field sendNextHealthPacketF = Mob.class.getDeclaredField("sendNextHealthPacket");
	            Field nextHealthPacketFullF = Mob.class.getDeclaredField("nextHealthPacketFull");         
	            Field sendNextResiliencePacketF = Mob.class.getDeclaredField("sendNextResiliencePacket");
	            Field nextResiliencePacketFullF = Mob.class.getDeclaredField("nextResiliencePacketFull");  
	            Field sendNextMovementPacketF = Mob.class.getDeclaredField("sendNextMovementPacket");
	            Field nextMovementPacketDirectF = Mob.class.getDeclaredField("nextMovementPacketDirect");    
	            
	            Field sendNextManaPacketF = Mob.class.getDeclaredField("sendNextManaPacket");
	            Field nextManaPacketFullF = Mob.class.getDeclaredField("nextManaPacketFull");    
	            
	            Field followingUniqueIDF = Mob.class.getDeclaredField("followingUniqueID");    
	            
	            Field networkFieldsF = Mob.class.getDeclaredField("networkFields");    
	           
	     
	            sendNextHealthPacketF.setAccessible(true);
	            nextHealthPacketFullF.setAccessible(true);
	            sendNextResiliencePacketF.setAccessible(true);
	            nextResiliencePacketFullF.setAccessible(true);
	            sendNextMovementPacketF.setAccessible(true);
	            nextMovementPacketDirectF.setAccessible(true);
	            sendNextManaPacketF.setAccessible(true);
	            nextManaPacketFullF.setAccessible(true);
	            followingUniqueIDF.setAccessible(true);
	            networkFieldsF.setAccessible(true);
	            
				writer.putNextInt(th.getUniqueID());
				th.setupHealthPacket(writer, true);
				sendNextHealthPacketF.set(th,false);
				nextHealthPacketFullF.set(th,false);
				th.setupResiliencePacket(writer, true);
				sendNextResiliencePacketF.set(th, false);
				nextResiliencePacketFullF.set(th, false);
				th.setupMovementPacket(writer);
				sendNextMovementPacketF.set(th, false);
				nextMovementPacketDirectF.set(th, false);
				if (th.usesMana()) {
					writer.putNextBoolean(true);
					th.setupManaPacket(writer, true);
				} else {
					writer.putNextBoolean(false);
				}

				sendNextManaPacketF.set(th, false);
				nextManaPacketFullF.set(th, false);
				if (followingUniqueIDF.getInt(th) != -1) {
					writer.putNextBoolean(true);
					writer.putNextInt((int) followingUniqueIDF.get(th));
				} else {
					writer.putNextBoolean(false);
				}

				writer.putNextBoolean(th.mountSetMounterPos);
				writer.putNextLong(th.lastCombatTime);
				if (th.usesMana()) {
					writer.putNextBoolean(true);
					writer.putNextLong(th.lastManaSpentTime);
					writer.putNextBoolean(th.isManaExhausted);
				} else {
					writer.putNextBoolean(false);
				}

				((MobNetworkFieldRegistry)networkFieldsF.get(th)).writeSpawnPacket(writer);
				th.buffManager.setupContentPacket(writer);
			 
	            // Get private fields via reflection
	            Field inventoryExtendedField = PlayerMob.class.getDeclaredField("inventoryExtended");
	            Field selectedSlotField = PlayerMob.class.getDeclaredField("selectedSlot");         

	            // Set accessible
	            
	            inventoryExtendedField.setAccessible(true);
	            selectedSlotField.setAccessible(true);	    

	            // Write data to the packet
	            writer.putNextBoolean((boolean) inventoryExtendedField.get(th));
	            writer.putNextByteUnsigned((int) selectedSlotField.get(th));

	            // Call look.setupContentPacket(writer, true);
	            RaceLook ra = RaceLook.fromHumanLook(th.look, CustomHumanLook.class);
	            th.look = ra;
	            th.look.setupContentPacket(writer, true);
	            
	            // Call getInv().setupContentPacket(writer);
	            th.getInv().setupContentPacket(writer);
	              
	            // Write hunger level
	            writer.putNextFloat(th.hungerLevel);
	           
	        } catch (Exception e) {
	            e.printStackTrace();
	        }	
			DebugHelper.handleDebugMessage(String.format(
	                "setupSpawnPacket for PlayerMob %s intercepted with race "+ RaceLook.fromHumanLook(th.look, CustomHumanLook.class).getRaceID(),
	                th.playerName
	            ), 25);
			return true;
    }
	 
    
   }

