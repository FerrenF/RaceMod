package patches.player;
import java.lang.reflect.Field;
import core.race.CustomHumanLook;
import core.race.RaceLook;
import core.race.factory.RaceDataFactory;
import helpers.DebugHelper;
import necesse.engine.modLoader.annotations.ModMethodPatch;
import necesse.engine.network.PacketReader;
import necesse.entity.mobs.Mob;
import necesse.entity.mobs.PlayerMob;
import necesse.entity.mobs.networkField.MobNetworkFieldRegistry;
import net.bytebuddy.asm.Advice;

@ModMethodPatch(target = PlayerMob.class, name = "applySpawnPacket", arguments = {PacketReader.class})
public class applySpawnPacketPatch {
	
	 @Advice.OnMethodEnter(skipOn = Advice.OnNonDefaultValue.class)
	    public static boolean onEnter(@Advice.This PlayerMob th, @Advice.Argument(0) PacketReader reader) {
	        try {
	        	
	            // Make the fields accessible via reflection
	            Field loadedHealthField = Mob.class.getDeclaredField("loadedHealth");
	            Field loadedResilienceField = Mob.class.getDeclaredField("loadedResilience");
	            Field loadedManaField = Mob.class.getDeclaredField("loadedMana");
	            Field selectedSlotField = PlayerMob.class.getDeclaredField("selectedSlot");
	            Field inventoryExtendedField = PlayerMob.class.getDeclaredField("inventoryExtended");
	           	            
	       
	            Field followingUniqueIDF = Mob.class.getDeclaredField("followingUniqueID");    
	            
	            Field networkFieldsF = Mob.class.getDeclaredField("networkFields");    
	            
	            // Set the fields accessible
	            loadedHealthField.setAccessible(true);
	            loadedResilienceField.setAccessible(true);
	            loadedManaField.setAccessible(true);
	            selectedSlotField.setAccessible(true);
	            inventoryExtendedField.setAccessible(true);   
	            networkFieldsF.setAccessible(true);
	            followingUniqueIDF.setAccessible(true);
	            
	        	th.refreshClientUpdateTime();
	        	th.setUniqueID(reader.getNextInt());
	    		th.applyHealthPacket(reader, true);
	    		if (th.getHealth() != loadedHealthField.getInt(th)) {
	    			th.setHealthHidden(loadedHealthField.getInt(th));
	    		}

	    		th.applyResiliencePacket(reader, true);
	    		if (th.getResilience() != loadedResilienceField.getFloat(th)) {
	    			th.setResilienceHidden(loadedResilienceField.getFloat(th));
	    		}

	    		th.applyMovementPacket(reader, true);
	    		if (reader.getNextBoolean()) {
	    			th.applyManaPacket(reader, true);
	    			if (th.getMana() != loadedManaField.getFloat(th)) {
	    				th.setManaHidden(loadedManaField.getFloat(th));
	    			}
	    		}

	    		if (reader.getNextBoolean()) {
	    			followingUniqueIDF.set(th, reader.getNextInt());
	    		} else {
	    			followingUniqueIDF.set(th, -1);
	    		}

	    		th.mountSetMounterPos = reader.getNextBoolean();
	    		th.lastCombatTime = reader.getNextLong();
	    		if (reader.getNextBoolean()) {
	    			th.lastManaSpentTime = reader.getNextLong();
	    			th.isManaExhausted = reader.getNextBoolean();
	    		}

	    		((MobNetworkFieldRegistry)networkFieldsF.get(th)).readSpawnPacket(reader);
	    		th.buffManager.applyContentPacket(reader);
	        	
	    		inventoryExtendedField.set(th, reader.getNextBoolean());
	    		selectedSlotField.set(th, reader.getNextByteUnsigned());
	
	    		RaceLook ra = RaceLook.raceFromContentPacket(reader, RaceDataFactory.getRaceLook(th, new CustomHumanLook(true)));
		        th.look = ra;
		        RaceDataFactory.getOrRegisterRaceData(th, ra);
	    		th.getInv().applyContentPacket(reader);
	    		th.hungerLevel = reader.getNextFloat();
	    		 
	            DebugHelper.handleDebugMessage(String.format(
                        "applySpawnPacketPatch for PlayerMob %s with race %s intercepted.",
                        th.playerName, ra.getRaceID()
                ), 25);		            

	        } catch (Exception e) {
	            e.printStackTrace();
	        }
	        return true;
	    }
	 
	 
    @Advice.OnMethodExit
    static void onExitApplySpawnPacketPatch(@Advice.This PlayerMob th, @Advice.Argument(0) PacketReader reader) {

    
    }
}
