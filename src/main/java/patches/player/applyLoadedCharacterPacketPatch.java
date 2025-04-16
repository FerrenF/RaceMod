package patches.player;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import core.race.CustomHumanLook;
import core.race.RaceLook;
import core.race.factory.RaceDataFactory;
import core.race.factory.RaceDataFactory.RaceData;
import helpers.DebugHelper;
import helpers.DebugHelper.MESSAGE_TYPE;
import necesse.engine.modLoader.annotations.ModMethodPatch;
import necesse.engine.network.PacketReader;
import necesse.entity.mobs.Mob;
import necesse.entity.mobs.PlayerMob;
import net.bytebuddy.asm.Advice;

@ModMethodPatch(target = PlayerMob.class, name = "applyLoadedCharacterPacket", arguments = {PacketReader.class})
public class applyLoadedCharacterPacketPatch {
	
		@Advice.OnMethodEnter(skipOn = Advice.OnNonDefaultValue.class)
	    public static boolean onEnter(@Advice.This PlayerMob th, @Advice.Argument(0) PacketReader reader) {
	        try {
	            // Use reflection to get and invoke private fields & methods
	           	           
	            Method handleLoadedValues = Mob.class.getDeclaredMethod("handleLoadedValues");

	            Field loadedHealthField = Mob.class.getDeclaredField("loadedHealth");
	            Field loadedResilienceField = Mob.class.getDeclaredField("loadedResilience");
	            Field loadedManaField = Mob.class.getDeclaredField("loadedMana");
	            Field selectedSlotField = PlayerMob.class.getDeclaredField("selectedSlot");
	            Field inventoryExtendedField = PlayerMob.class.getDeclaredField("inventoryExtended");
	            
	           
	            // Set accessible to access private fields
	          
	            handleLoadedValues.setAccessible(true);

	            loadedHealthField.setAccessible(true);
	            loadedResilienceField.setAccessible(true);
	            loadedManaField.setAccessible(true);
	            selectedSlotField.setAccessible(true);
	            inventoryExtendedField.setAccessible(true);

	            // Read data from the packet
	            th.setMaxHealth(reader.getNextInt());
	            loadedHealthField.set(th,  reader.getNextInt());
	            th.setHealthHidden(loadedHealthField.getInt(th));
	            
	            th.setMaxResilience(reader.getNextInt());  
	            loadedResilienceField.set(th, reader.getNextFloat());	            
	            th.setResilienceHidden(loadedResilienceField.getFloat(th));

	            th.setMaxMana(reader.getNextInt());
	            loadedManaField.set(th, reader.getNextFloat());
	            th.setManaHidden(loadedManaField.getFloat(th));

	            th.buffManager.applyContentPacket(reader);	         
	            
	            selectedSlotField.set(th, reader.getNextByteUnsigned());
	            inventoryExtendedField.set(th, reader.getNextBoolean());
	            
	            th.autoOpenDoors = reader.getNextBoolean();
	            th.hotbarLocked = reader.getNextBoolean();
	            
	            /*RaceData rd =  RaceDataFactory.getOrRegisterRaceData(th);
	            RaceLook ra = RaceLook.raceFromContentPacker(reader, 
	            		rd.raceDataInitialized 
	            		? rd.getRaceLook() 
	            		: RaceLook.fromHumanLook(th.look, CustomHumanLook.class));*/
	            RaceLook ra = RaceLook.raceFromContentPacket(reader,
	            		th.look instanceof RaceLook ? (RaceLook)th.look : RaceLook.fromHumanLook(th.look, CustomHumanLook.class));
	            th.look = ra;
	            RaceDataFactory.getOrRegisterRaceData(th, ra);
	          
	            th.getInv().applyContentPacket(reader);
	       
	            th.hungerLevel = reader.getNextFloat();

	            th.equipmentBuffManager.updateAll();

	            th.buffManager.forceUpdateBuffs();	         

	            handleLoadedValues.invoke(th);
	            DebugHelper.handleDebugMessage(String.format(
                        "applyLoadedCharacterPacket for PlayerMob %s with race %s intercepted.",
                        th.playerName, ((RaceLook)th.look).getRaceID()
                ), 25);		
	        } catch (Exception e) {
	            e.printStackTrace();
	        }
	        return true;
	    }
   
}
