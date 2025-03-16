package patches.server;

import java.lang.reflect.Field;

import core.network.CustomPacketConnectApproved;
import necesse.engine.network.PacketReader;
import necesse.engine.network.client.Client;
import necesse.engine.network.client.ClientClient;
import necesse.engine.network.client.ClientIslandNotes;
import necesse.engine.network.client.ClientTutorial;
import necesse.engine.network.packet.PacketConnectApproved;
import necesse.engine.seasons.GameSeasons;
import necesse.gfx.forms.presets.sidebar.TrackedSidebarForm;
import net.bytebuddy.implementation.bind.annotation.Argument;
import net.bytebuddy.implementation.bind.annotation.This;

public class ClientSubmitConnectAcceptedPatch {

	
	public static void submitConnectionPacket(@This Client th, @Argument(0) PacketConnectApproved _p) {
		try {
	    Field playersField 	= Client.class.getDeclaredField("players");
        Field slotsField 	= Client.class.getDeclaredField("slots");
        Field slotField 	= Client.class.getDeclaredField("slot");
        Field worldUniqueIDField = Client.class.getDeclaredField("worldUniqueID");
        Field allowClientsPowerField = Client.class.getDeclaredField("allowClientsPower");
        Field permissionLevelField = Client.class.getDeclaredField("permissionLevel");
        
        playersField.setAccessible(true);
        slotsField.setAccessible(true);
        slotField.setAccessible(true);
        worldUniqueIDField.setAccessible(true);
        allowClientsPowerField.setAccessible(true);
        permissionLevelField.setAccessible(true);
        
		CustomPacketConnectApproved p = (CustomPacketConnectApproved) _p;
		th.sessionID = p.sessionID;
		playersField.set(th, new ClientClient[p.slots]);
		slotsField.set(th, p.slots);
		slotField.set(th, p.slot);
		
		worldUniqueIDField.set(th, p.uniqueID);
		
		th.worldSettings = p.getWorldSettings(th);
		allowClientsPowerField.set(th, p.allowClientsPower);
		permissionLevelField.set(th, p.permissionLevel);
		th.hasNewJournalEntry = p.hasNewJournalEntry;
		th.loading.submitApprovedPacket(p);
		th.tutorial = new ClientTutorial(th, worldUniqueIDField.getLong(th));
		th.islandNotes = new ClientIslandNotes(worldUniqueIDField.getLong(th));
		TrackedSidebarForm.loadTrackedQuests(th);
		GameSeasons.readSeasons(new PacketReader(p.activeSeasonsContent));
		
		} catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException | SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
}
