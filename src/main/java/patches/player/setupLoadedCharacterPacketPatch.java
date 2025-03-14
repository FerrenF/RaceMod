package patches.player;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import core.RaceMod;
import core.race.factory.RaceDataFactory;
import core.race.factory.RaceDataFactory.RaceData;
import necesse.engine.modLoader.annotations.ModMethodPatch;
import necesse.engine.network.PacketWriter;
import necesse.entity.mobs.Mob;
import necesse.entity.mobs.PlayerMob;
import net.bytebuddy.asm.Advice;

@ModMethodPatch(target = PlayerMob.class, name = "setupLoadedCharacterPacket", arguments = {PacketWriter.class})
public class setupLoadedCharacterPacketPatch {
	
/*	@Advice.OnMethodEnter(skipOn = Advice.OnNonDefaultValue.class)
    static boolean onEnter(@Advice.This PlayerMob th, @Advice.Argument(0) PacketWriter writer) {	
		
		
		
        return true;
    }*/
	 
	 @Advice.OnMethodExit
	    static void onExitSetupLoadedCharacterPacket(@Advice.This PlayerMob th, @Advice.Argument(0) PacketWriter writer) {
		/* try {
	            // Make the fields accessible via reflection
	            Field loadedHealthField = Mob.class.getDeclaredField("loadedHealth");
	            Field loadedResilienceField = Mob.class.getDeclaredField("loadedResilience");
	            Field loadedManaField = Mob.class.getDeclaredField("loadedMana");
	            Field selectedSlotField = PlayerMob.class.getDeclaredField("selectedSlot");
	            Field inventoryExtendedField = PlayerMob.class.getDeclaredField("inventoryExtended");

	            // Set the fields accessible
	            loadedHealthField.setAccessible(true);
	            loadedResilienceField.setAccessible(true);
	            loadedManaField.setAccessible(true);

	             	            
	            writer.putNextInt(th.getMaxHealthFlat());
	            writer.putNextInt(Math.max(th.getHealth(), (int) loadedHealthField.get(th)));
	            writer.putNextInt(th.getMaxResilienceFlat());
	            writer.putNextFloat(Math.max(th.getResilience(), (float) loadedResilienceField.get(th)));
	            writer.putNextInt(th.getMaxManaFlat());
	            writer.putNextFloat(Math.max(th.getMana(), (float) loadedManaField.get(th)));
	            th.buffManager.setupContentPacket(writer);
	            writer.putNextByteUnsigned((int)selectedSlotField.get(th));
	            writer.putNextBoolean((boolean)inventoryExtendedField.getBoolean(th));
	            writer.putNextBoolean(th.autoOpenDoors);
	            writer.putNextBoolean(th.hotbarLocked);

	            if (RaceDataFactory.mobUniqueID(th) != -1) {
	                RaceData r = RaceDataFactory.getOrRegisterRaceData(th);
	                if (r.raceDataInitialized) {
	                    r.getRaceLook().setupContentPacket(writer, true);
	                    RaceMod.handleDebugMessage(String.format(
	                            "setupLoadedCharacterPacket for PlayerMob %s intercepted.",
	                            th.playerName
	                    ), 25);
	                }
	                else {
	                	th.look.setupContentPacket(writer, true);
	                }
	            } else {
	                th.look.setupContentPacket(writer, true);
	            }
	            
	            th.getInv().setupContentPacket(writer);
	            writer.putNextFloat(th.hungerLevel);

	        } catch (NoSuchFieldException | IllegalAccessException | IllegalArgumentException  e) {
	            e.printStackTrace();
	        }*/
	    }
}
