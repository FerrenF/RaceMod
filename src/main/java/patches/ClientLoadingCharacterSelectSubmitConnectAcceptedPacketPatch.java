package patches;

import java.lang.reflect.Field;

import necesse.engine.network.packet.PacketConnectApproved;
import necesse.entity.mobs.PlayerMob;
import net.bytebuddy.implementation.bind.annotation.AllArguments;
import net.bytebuddy.implementation.bind.annotation.This;
import necesse.engine.network.NetworkClient;
import necesse.engine.network.PacketReader;
import necesse.engine.network.client.loading.ClientLoadingSelectCharacter;
import core.network.CustomPacketConnectApproved;


public class ClientLoadingCharacterSelectSubmitConnectAcceptedPacketPatch {
	

    public static void submitConnectAccepted(@This ClientLoadingSelectCharacter th, @AllArguments Object[] args) {
    		
    		PacketConnectApproved packet = (PacketConnectApproved) args[0];
        try {
        	
        	CustomPacketConnectApproved cp = (CustomPacketConnectApproved)packet;
            // Access private fields
            Field needAppearanceField = ClientLoadingSelectCharacter.class.getDeclaredField("needAppearance");
            Field allowCharacterSelectField = ClientLoadingSelectCharacter.class.getDeclaredField("allowCharacterSelect");
            Field serverCharacterUniqueIDField = ClientLoadingSelectCharacter.class.getDeclaredField("serverCharacterUniqueID");
            Field serverCharacterPlayerField = ClientLoadingSelectCharacter.class.getDeclaredField("serverCharacterPlayer");
            Field serverCharacterTimePlayedField = ClientLoadingSelectCharacter.class.getDeclaredField("serverCharacterTimePlayed");

            // Make them accessible
            needAppearanceField.setAccessible(true);
            allowCharacterSelectField.setAccessible(true);
            serverCharacterUniqueIDField.setAccessible(true);
            serverCharacterPlayerField.setAccessible(true);
            serverCharacterTimePlayedField.setAccessible(true);

            // Modify the fields
            needAppearanceField.setBoolean(th, cp.needAppearance);
            allowCharacterSelectField.setBoolean(th, cp.characterSelect);
            serverCharacterUniqueIDField.setInt(th, cp.serverCharacterUniqueID);
            serverCharacterTimePlayedField.setLong(th, cp.serverCharacterTimePlayed);

            // Handle serverCharacterPlayer initialization if needed
            if (cp.serverCharacterAppearance != null) {
                PlayerMob serverCharacterPlayer = new PlayerMob((long) cp.serverCharacterUniqueID, (NetworkClient) null);
                serverCharacterPlayer.look = cp.serverCharacterAppearance;
                serverCharacterPlayer.getInv().applyLookContentPacket(new PacketReader(cp.serverCharacterLookContent));
                serverCharacterPlayer.playerName = cp.serverCharacterName;

                // Set serverCharacterPlayer using reflection
                serverCharacterPlayerField.set(th, serverCharacterPlayer);
            }

            // Restore access (optional but recommended)
            needAppearanceField.setAccessible(false);
            allowCharacterSelectField.setAccessible(false);
            serverCharacterUniqueIDField.setAccessible(false);
            serverCharacterPlayerField.setAccessible(false);
            serverCharacterTimePlayedField.setAccessible(false);

        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace(); // Handle potential exceptions gracefully
        }

        // Call markDone() if needed
        if (getPrivateBoolean(th, "needAppearance") == false && getPrivateBoolean(th, "allowCharacterSelect") == false) {
            invokePrivateMethod(th, "markDone");
        }
    }

    // Helper method to get private boolean fields
    private static boolean getPrivateBoolean(Object instance, String fieldName) {
        try {
            Field field = instance.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            boolean value = field.getBoolean(instance);
            field.setAccessible(false);
            return value;
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Helper method to invoke private methods using reflection
    private static void invokePrivateMethod(Object instance, String methodName) {
        try {
            java.lang.reflect.Method method = instance.getClass().getDeclaredMethod(methodName);
            method.setAccessible(true);
            method.invoke(instance);
            method.setAccessible(false);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

/*


Target:


public void submitConnectAccepted(PacketConnectApproved packet) {
		this.needAppearance = packet.needAppearance;
		this.allowCharacterSelect = packet.characterSelect;
		this.serverCharacterUniqueID = packet.serverCharacterUniqueID;
		if (packet.serverCharacterAppearance != null) {
			this.serverCharacterPlayer = new PlayerMob((long) this.serverCharacterUniqueID, (NetworkClient) null);
			this.serverCharacterPlayer.look = packet.serverCharacterAppearance;
			this.serverCharacterPlayer.getInv()
					.applyLookContentPacket(new PacketReader(packet.serverCharacterLookContent));
			this.serverCharacterPlayer.playerName = packet.serverCharacterName;
		}

		this.serverCharacterTimePlayed = packet.serverCharacterTimePlayed;
		if (!this.needAppearance && !this.allowCharacterSelect) {
			this.markDone();
		}

	}



*/