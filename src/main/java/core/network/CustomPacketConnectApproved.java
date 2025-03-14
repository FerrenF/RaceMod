package core.network;

import java.lang.reflect.Field;
import java.nio.ByteBuffer;

import core.race.CustomHumanLook;
import core.race.RaceLook;
import core.race.factory.RaceDataFactory;
import necesse.engine.Settings;
import necesse.engine.commands.PermissionLevel;
import necesse.engine.network.NetworkManager;
import necesse.engine.network.NetworkPacket;
import necesse.engine.network.Packet;
import necesse.engine.network.PacketReader;
import necesse.engine.network.PacketWriter;
import necesse.engine.network.client.Client;
import necesse.engine.network.packet.PacketConnectApproved;
import necesse.engine.network.server.Server;
import necesse.engine.network.server.ServerClient;
import necesse.engine.platforms.Platform;
import necesse.engine.playerStats.PlayerStats;
import necesse.engine.seasons.GameSeasons;
import necesse.engine.world.WorldSettings;
import necesse.gfx.HumanLook;

public class CustomPacketConnectApproved extends PacketConnectApproved {
	public final long sessionID;
	public final int slot;
	public final int slots;
	public final long uniqueID;
	public final boolean characterSelect;
	public final int serverCharacterUniqueID;
	public final RaceLook serverCharacterAppearance;
	public final Packet serverCharacterLookContent;
	public final long serverCharacterTimePlayed;
	public final String serverCharacterName;
	public final boolean needAppearance;
	public final PermissionLevel permissionLevel;
	public final boolean hasNewJournalEntry;
	public final boolean allowClientsPower;
	public final Packet activeSeasonsContent;
	public final Packet worldSettingsContent;
	public final NetworkManager.PlatformConnectApprovedData platformConnectApprovedData;

	public byte[] data;
	public CustomPacketConnectApproved(byte[] data) {
		super(data);
		this.data = data;
		System.out.print("packet ping");
		try {
	        Field bufferField = this.getClass().getSuperclass().getSuperclass().getDeclaredField("buffer");
	        bufferField.setAccessible(true);
	        bufferField.set(this, ByteBuffer.wrap(data));
	        
	        Field sizeField = this.getClass().getSuperclass().getSuperclass().getDeclaredField("size");
			sizeField.setAccessible(true);
		    sizeField.set(this, data.length);
		} catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
			e.printStackTrace();
		}       
		
		PacketReader reader = new PacketReader(this);
		this.sessionID = reader.getNextLong();
		this.slot = reader.getNextByteUnsigned();
		this.slots = reader.getNextByteUnsigned();
		this.uniqueID = reader.getNextLong();
		this.characterSelect = reader.getNextBoolean();
		this.needAppearance = reader.getNextBoolean();
		if (this.characterSelect && !this.needAppearance) {
			this.serverCharacterUniqueID = reader.getNextInt();
			//HumanLook holder = new HumanLook(reader);
			
			this.serverCharacterAppearance = RaceLook.raceFromContentPacker(reader, new CustomHumanLook(true));					
			this.serverCharacterLookContent = reader.getNextContentPacket();
			this.serverCharacterTimePlayed = reader.getNextLong();
			this.serverCharacterName = reader.getNextString();
		} else {
			this.serverCharacterUniqueID = 0;
			this.serverCharacterAppearance = null;
			this.serverCharacterLookContent = null;
			this.serverCharacterTimePlayed = 0L;
			this.serverCharacterName = null;
		}
		this.permissionLevel = PermissionLevel.getLevel(reader.getNextByteUnsigned());
		this.hasNewJournalEntry = reader.getNextBoolean();
		this.allowClientsPower = reader.getNextBoolean();
		this.activeSeasonsContent = reader.getNextContentPacket();
		this.worldSettingsContent = reader.getNextContentPacket();
		this.platformConnectApprovedData = Platform.getNetworkManager().createPlatformConnectApprovedData();
		this.platformConnectApprovedData.readPlatformData(reader);
	}

	public CustomPacketConnectApproved(Server server, ServerClient client) {
		super(server, client);
		this.sessionID = client.getSessionID();
		this.slot = client.slot;
		this.slots = server.getSlots();
		this.uniqueID = server.world.getUniqueID();
		this.characterSelect = server.world.settings.allowOutsideCharacters;
		this.needAppearance = client.needAppearance();
		if (this.characterSelect && !this.needAppearance) {
			this.serverCharacterUniqueID = client.getCharacterUniqueID();
			
			this.serverCharacterAppearance = client.playerMob.look instanceof RaceLook ? (RaceLook) client.playerMob.look :
					RaceDataFactory.getOrRegisterRaceData(client.playerMob, new CustomHumanLook(true)).getRaceLook();
			this.serverCharacterLookContent = new Packet();
			client.playerMob.getInv().setupLookContentPacket(new PacketWriter(this.serverCharacterLookContent));
			PlayerStats stats = client.characterStats();
			this.serverCharacterTimePlayed = stats == null ? 0L : (long) stats.time_played.get();
			this.serverCharacterName = client.getName();
		} else {
			this.serverCharacterUniqueID = 0;
			this.serverCharacterAppearance = null;
			this.serverCharacterLookContent = null;
			this.serverCharacterTimePlayed = 0L;
			this.serverCharacterName = null;
		}

		this.permissionLevel = client.getPermissionLevel();
		this.hasNewJournalEntry = client.hasNewJournalEntry;
		this.allowClientsPower = Settings.giveClientsPower;
		this.activeSeasonsContent = new Packet();
		GameSeasons.writeSeasons(new PacketWriter(this.activeSeasonsContent));
		this.worldSettingsContent = new Packet();
		server.world.settings.setupContentPacket(new PacketWriter(this.worldSettingsContent));
		PacketWriter writer = new PacketWriter(this);
		writer.putNextLong(this.sessionID);
		writer.putNextByteUnsigned(this.slot);
		writer.putNextByteUnsigned(this.slots);
		writer.putNextLong(this.uniqueID);
		writer.putNextBoolean(this.characterSelect);
		writer.putNextBoolean(this.needAppearance);
		if (this.characterSelect && !this.needAppearance) {
			writer.putNextInt(this.serverCharacterUniqueID);
			this.serverCharacterAppearance.setupContentPacket(writer, true);
			//RaceDataFactory.getRaceLook(client.playerMob, new CustomHumanLook(true)).setupContentPacket(writer, true);
			//this.serverCharacterAppearance.setupContentPacketWithBase(writer, true);
			writer.putNextContentPacket(this.serverCharacterLookContent);
			writer.putNextLong(this.serverCharacterTimePlayed);
			writer.putNextString(this.serverCharacterName);
		}

		writer.putNextByteUnsigned(this.permissionLevel.getLevel());
		writer.putNextBoolean(this.hasNewJournalEntry);
		writer.putNextBoolean(this.allowClientsPower);
		writer.putNextContentPacket(this.activeSeasonsContent);
		writer.putNextContentPacket(this.worldSettingsContent);
		this.platformConnectApprovedData = Platform.getNetworkManager().createPlatformConnectApprovedData();
		this.platformConnectApprovedData.writePlatformData(writer, server, client);
	}

	@Override
	public WorldSettings getWorldSettings(Client client) {
		return new WorldSettings(client, new PacketReader(this.worldSettingsContent), false);
	}

	@Override
	public void processClient(NetworkPacket packet, Client client) {
		client.submitConnectionPacket(this);
	}
}