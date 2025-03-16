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
import helpers.DebugHelper.MESSAGE_TYPE;
import necesse.engine.modLoader.annotations.ModMethodPatch;
import necesse.engine.network.PacketReader;
import necesse.engine.network.PacketWriter;
import necesse.entity.mobs.Mob;
import necesse.entity.mobs.PlayerMob;
import net.bytebuddy.asm.Advice;

@ModMethodPatch(target = PlayerMob.class, name = "setupLoadedCharacterPacket", arguments = {PacketWriter.class})
public class setupLoadedCharacterPacketPatch {
	
// Although I am keeping this here for now, I did not actually need to patch this method, because by the time this
	// method receives the look, it has already been intercepted and replaced by it's evil twin furry brother.
	
	
	@Advice.OnMethodEnter(skipOn = Advice.OnNonDefaultValue.class)
    static boolean onEnter(@Advice.This PlayerMob th, @Advice.Argument(0) PacketWriter writer) {	
		 try {
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
	            selectedSlotField.setAccessible(true);
	            inventoryExtendedField.setAccessible(true);     
	            
	            writer.putNextInt(th.getMaxHealthFlat());
	            writer.putNextInt(Math.max(th.getHealth(), (int) loadedHealthField.get(th)));
	            writer.putNextInt(th.getMaxResilienceFlat());
	            writer.putNextFloat(Math.max(th.getResilience(), (float) loadedResilienceField.get(th)));
	            writer.putNextInt(th.getMaxManaFlat());
	            writer.putNextFloat(Math.max(th.getMana(), (float) loadedManaField.get(th)));
	            th.buffManager.setupContentPacket(writer);
	            writer.putNextByteUnsigned((byte)selectedSlotField.getInt(th));
	            writer.putNextBoolean(inventoryExtendedField.getBoolean(th));
	            writer.putNextBoolean(th.autoOpenDoors);
	            writer.putNextBoolean(th.hotbarLocked);

	            
	            th.look = (th.look instanceof RaceLook ? (RaceLook)th.look : RaceLook.fromHumanLook(th.look, CustomHumanLook.class));
	            th.look.setupContentPacket(writer, true);
	            RaceDataFactory.getOrRegisterRaceData(th, (RaceLook) th.look);
                   
	            th.getInv().setupContentPacket(writer);
	            writer.putNextFloat(th.hungerLevel);
	            
	            DebugHelper.handleDebugMessage(String.format(
                        "setupLoadedCharacterPacket for PlayerMob %s with race %s intercepted.",
                        th.playerName, ((RaceLook)th.look).getRaceID()
                ), 25);	  

	        } catch (NoSuchFieldException | IllegalAccessException | IllegalArgumentException  e) {
	            e.printStackTrace();
	        }
       return true;
    }

}
