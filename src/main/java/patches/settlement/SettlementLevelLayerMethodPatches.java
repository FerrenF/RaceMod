package patches.settlement;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import core.race.CustomHumanLook;
import core.race.RaceLook;
import helpers.DebugHelper;
import helpers.DebugHelper.MESSAGE_TYPE;
import necesse.engine.localization.message.GameMessage;
import necesse.engine.network.PacketReader;
import necesse.engine.network.PacketWriter;
import necesse.engine.network.server.ServerClient;
import necesse.engine.playerStats.EmptyStats.Mode;
import necesse.engine.playerStats.PlayerStats;
import necesse.engine.save.LoadData;
import necesse.gfx.HumanLook;
import necesse.level.maps.layers.settlement.SettlementLevelLayer;
import net.bytebuddy.implementation.bind.annotation.Argument;
import net.bytebuddy.implementation.bind.annotation.This;

public class SettlementLevelLayerMethodPatches {
	
	
	public static void updateOwnerVariables(@This SettlementLevelLayer th, @Argument(0) ServerClient client) {		
		
		try
		{
			Field ownerAuthField = th.getClass().getDeclaredField("ownerAuth");
			Field ownerNameField = th.getClass().getDeclaredField("ownerName");
			Field teamIDField = th.getClass().getDeclaredField("teamID");
			Field lookField = th.getClass().getDeclaredField("look");
			Field statsField = th.getClass().getDeclaredField("stats");
			
			ownerAuthField.setAccessible(true);
			ownerNameField.setAccessible(true);
			teamIDField.setAccessible(true);
			lookField.setAccessible(true);
			statsField.setAccessible(true);
			
			ownerAuthField.set(th, client.authentication);
			ownerNameField.set(th,client.getName());
			teamIDField.set(th,client.getTeamID());
			lookField.set(th, RaceLook.fromHumanLook( client.playerMob.look, CustomHumanLook.class));
			statsField.set(th,client.characterStats());
			
			DebugHelper.handleFormattedDebugMessage("Settlement owner variables: auth %s, name %s, team %s ",
					50, MESSAGE_TYPE.DEBUG, new Object[] {ownerAuthField.get(th).toString(),
							ownerNameField.get(th).toString(), teamIDField.get(th).toString()});
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void readBasicSettlementData(@This SettlementLevelLayer th, @Argument(0) PacketReader reader) {
		
		try
		{
			Field activeField = th.getClass().getDeclaredField("active");
			Field settlementName = th.getClass().getDeclaredField("settlementName");
			Field ownerAuthField = th.getClass().getDeclaredField("ownerAuth");
			Field ownerNameField = th.getClass().getDeclaredField("ownerName");
			Field teamIDField = th.getClass().getDeclaredField("teamID");
			Field lookField = th.getClass().getDeclaredField("look");
			Field isPrivateField = th.getClass().getDeclaredField("isPrivate");
			
			activeField.setAccessible(true);
			settlementName.setAccessible(true);
			ownerAuthField.setAccessible(true);
			ownerNameField.setAccessible(true);
			teamIDField.setAccessible(true);
			lookField.setAccessible(true);
			isPrivateField.setAccessible(true);
			
			activeField.set(th, reader.getNextBoolean());
			settlementName.set(th, GameMessage.fromContentPacket(reader.getNextContentPacket()));
			ownerAuthField.set(th, reader.getNextLong());
			if (reader.getNextBoolean()) {
				ownerNameField.set(th, reader.getNextString());
			} else {
				ownerNameField.set(th, null);
			}
	
			teamIDField.set(th, reader.getNextInt());
			if (reader.getNextBoolean()) {
				
				RaceLook ra = RaceLook.raceFromContentPacker(reader, new CustomHumanLook(true));		
				lookField.set(th, ra);
			} else {
				lookField.set(th, null);
			}
	
			isPrivateField.set(th, reader.getNextBoolean());
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void writeBasicSettlementData(@This SettlementLevelLayer th, @Argument(0) PacketWriter writer) {
		
		try
		{
			Field activeField = th.getClass().getDeclaredField("active");
			Field settlementName = th.getClass().getDeclaredField("settlementName");
			Field ownerAuthField = th.getClass().getDeclaredField("ownerAuth");
			Field ownerNameField = th.getClass().getDeclaredField("ownerName");
			Field teamIDField = th.getClass().getDeclaredField("teamID");
			Field lookField = th.getClass().getDeclaredField("look");
			Field isPrivateField = th.getClass().getDeclaredField("isPrivate");
			
			activeField.setAccessible(true);
			settlementName.setAccessible(true);
			ownerAuthField.setAccessible(true);
			ownerNameField.setAccessible(true);
			teamIDField.setAccessible(true);
			lookField.setAccessible(true);
			isPrivateField.setAccessible(true);
			
			writer.putNextBoolean((boolean) activeField.get(th));
			writer.putNextContentPacket(((GameMessage) settlementName.get(th)).getContentPacket());
			writer.putNextLong(ownerAuthField.getLong(th));
			writer.putNextBoolean(ownerNameField.get(th) != null);
			if (ownerNameField.get(th) != null) {
				writer.putNextString((String)ownerNameField.get(th) );
			}
	
			writer.putNextInt((int) teamIDField.get(th));
			writer.putNextBoolean(lookField.get(th) != null);
			if (lookField.get(th) != null) {
				
				
				if (lookField.get(th) instanceof RaceLook) {
					((RaceLook)lookField.get(th)).setupContentPacket(writer, true);
				}
				else {
					RaceLook.fromHumanLook((HumanLook)lookField.get(th), CustomHumanLook.class).setupContentPacket(writer, true);
				}
				
			}
	
			writer.putNextBoolean(isPrivateField.getBoolean(th));
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	
	public static HumanLook getLook(@This SettlementLevelLayer th) {
		
		try
		{
			Field lookField = th.getClass().getDeclaredField("look");
			Field ownerAuthField = th.getClass().getDeclaredField("ownerAuth");
			
			lookField.setAccessible(true);
			ownerAuthField.setAccessible(true);
			
			if (lookField.get(th) == null && (long)ownerAuthField.get(th) != -1L && th.level.isServer()) {
				th.updateOwnerVariables();
			}
	
			return RaceLook.fromHumanLook((HumanLook) lookField.get(th), CustomHumanLook.class);
		} catch(Exception e) {
			e.printStackTrace();
		}
		
		return new CustomHumanLook(true);
	}
	
	public static void updateOwnerVariables(@This SettlementLevelLayer th) {
		
		try
		{
			Field ownerAuthField = th.getClass().getDeclaredField("ownerAuth");
			Field ownerNameField = th.getClass().getDeclaredField("ownerName");
			Field teamIDField = th.getClass().getDeclaredField("teamID");
			Field lookField = th.getClass().getDeclaredField("look");
			Field statsField = th.getClass().getDeclaredField("stats");
			
		
			ownerAuthField.setAccessible(true);
			ownerNameField.setAccessible(true);
			teamIDField.setAccessible(true);
			lookField.setAccessible(true);
			statsField.setAccessible(true);
			
			if (th.level.isServer()) {
				if (ownerAuthField.getLong(th) != -1L) {
					ServerClient client = th.level.getServer().getClientByAuth(ownerAuthField.getLong(th));
					if (client != null) {
						Method m = SettlementLevelLayer.class.getMethod("updateOwnerVariables", ServerClient.class);
						m.setAccessible(true);
						m.invoke(th, client);
					} else {
						LoadData clientSave = th.level.getServer().world.loadClientScript(ownerAuthField.getLong(th));
						if (clientSave != null) {
							ownerNameField.set(th, ServerClient.loadClientName(clientSave));
							teamIDField.set(th, th.level.getServer().world.getTeams().getPlayerTeamID(ownerAuthField.getLong(th)));
							lookField.set(th, ServerClient.loadClientLook(clientSave));
							statsField.set(th, ServerClient.loadClientStats(clientSave));
						
							DebugHelper.handleFormattedDebugMessage("Settlement owner variables: auth %s, name %s, team %s ",
									50, MESSAGE_TYPE.DEBUG, new Object[] {ownerAuthField.get(th).toString(),
											ownerNameField.get(th).toString(), teamIDField.get(th).toString()});
						} else {
							ownerAuthField.set(th,-1L);
							ownerNameField.set(th,null);
							teamIDField.set(th,-1);
							lookField.set(th,new CustomHumanLook(true));
							statsField.set(th, new PlayerStats(false, Mode.READ_ONLY));
							th.markBasicsDirty();
						}
					}
				} else {
		
					ownerNameField.set(th,null);
					teamIDField.set(th,-1);
					lookField.set(th,null);
					statsField.set(th, new PlayerStats(false, Mode.READ_ONLY));
				}
	
				invokePrivateMethod(th,"updateLevelCache");
			}
			
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		
	}
	
	 // Helper method to invoke private methods using reflection
    public static void invokePrivateMethod(Object instance, String methodName, Object... args) {
        try {
            java.lang.reflect.Method method = instance.getClass().getDeclaredMethod(methodName);
            method.setAccessible(true);
            if(args==null) {
            	method.invoke(instance);
            }else {
            	method.invoke(instance, args);
            }        
            method.setAccessible(false);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
	
}
