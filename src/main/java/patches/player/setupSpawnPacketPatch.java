package patches.player;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import core.RaceMod;
import core.race.factory.RaceDataFactory;
import core.race.factory.RaceDataFactory.RaceData;
import necesse.engine.modLoader.annotations.ModMethodPatch;
import necesse.engine.network.PacketWriter;
import necesse.entity.mobs.PlayerMob;
import net.bytebuddy.asm.Advice;

@ModMethodPatch(target = PlayerMob.class, name = "setupSpawnPacket", arguments = {PacketWriter.class})
public class setupSpawnPacketPatch {
	
	/*@Advice.OnMethodEnter(skipOn = Advice.OnNonDefaultValue.class)
    static boolean onEnter(@Advice.This PlayerMob th, @Advice.Argument(0) PacketWriter writer) {	      
		

    
		
        return true;
    }*/
	 
    @Advice.OnMethodExit
    static void onExitSetupSpawnPacket(@Advice.This PlayerMob th, @Advice.Argument(0) PacketWriter writer) {
        // Call the original method manually to apply load data
    	
    	if(RaceDataFactory.mobUniqueID(th)!=-1) {    		
    		if(!RaceDataFactory.hasRaceData(th)) {
    			RaceData r = RaceDataFactory.getOrRegisterRaceData(th);	 
    			//r.getRaceLook().setupContentPacket(writer, true);
    		}
    	
    	}
    /*	try {
	        Method superSetupMethod = PlayerMob.class.getSuperclass().getMethod("setupSpawnPacket", PacketWriter.class);
	        superSetupMethod.setAccessible(true); // Ensure it's accessible       
			superSetupMethod.invoke(th, writer);
		} catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
			e.printStackTrace();
		} 
        
		writer.putNextBoolean(th.isInventoryExtended());
		writer.putNextByteUnsigned(th.getSelectedSlot());
		
    	if(RaceDataFactory.mobUniqueID(th)!=-1) {
	    	RaceData r = RaceDataFactory.getOrRegisterRaceData(th);	    	
	    	if(r.raceDataInitialized) {
    			r.getRaceLook().setupContentPacket(writer, true);
    			RaceMod.handleDebugMessage(String.format(
                        "setupSpawnPacket for PlayerMob %s intercepted.",
                        th.playerName
                    ), 25);
    		} 
	    	else {
	    		th.look.setupContentPacket(writer, true);
	    	}
    	}
    	else {
    		th.look.setupContentPacket(writer, true);
    	}    	
		th.getInv().setupContentPacket(writer);
		writer.putNextFloat(th.hungerLevel);   */
    }
}
