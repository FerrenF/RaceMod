package core.network;

import java.util.Iterator;
import java.util.Map;

import core.race.CustomHumanLook;
import core.race.RaceLook;
import core.race.factory.RaceDataFactory;
import necesse.engine.GlobalData;
import necesse.engine.Settings;
import necesse.engine.commands.PermissionLevel;
import necesse.engine.localization.message.GameMessage;
import necesse.engine.localization.message.LocalMessage;
import necesse.engine.network.NetworkPacket;
import necesse.engine.network.Packet;
import necesse.engine.network.PacketReader;
import necesse.engine.network.PacketWriter;
import necesse.engine.network.client.Client;
import necesse.engine.network.client.ClientClient;
import necesse.engine.network.packet.PacketCharacterSelectError;
import necesse.engine.network.packet.PacketPlayerAppearance;
import necesse.engine.network.packet.PacketRequestPlayerData;
import necesse.engine.network.server.Server;
import necesse.engine.network.server.ServerClient;
import necesse.engine.state.MainGame;
import necesse.engine.util.GameUtils;
import necesse.entity.mobs.PlayerMob;
import necesse.gfx.HumanLook;

public class CustomPacketPlayerAppearance extends PacketPlayerAppearance {
	public final int slot;
	public final int characterUniqueID;
	public final RaceLook look;
	public final String name;

	public CustomPacketPlayerAppearance(byte[] data) {
		super(data);
		PacketReader reader = new PacketReader(this);
		this.slot = reader.getNextByteUnsigned();
		this.characterUniqueID = reader.getNextInt();
		RaceLook ra = RaceLook.raceFromContentPacker(reader, new CustomHumanLook(true));	
		
		Server s = GlobalData.getCurrentState() instanceof MainGame 
				?((MainGame)GlobalData.getCurrentState()).getClient().getLocalServer()
				:((necesse.engine.state.MainMenu)GlobalData.getCurrentState()).getClient().getLocalServer();
		if(s != null) {
			PlayerMob targetPlayer = s.getPlayer(slot);
			RaceDataFactory.getOrRegisterRaceData(targetPlayer, ra);
		}
		this.look = ra;
		this.name = reader.getNextString();
	}

	public CustomPacketPlayerAppearance(ServerClient client) {
		super(client);
		this.slot = client.slot;
		this.characterUniqueID = client.getCharacterUniqueID();
		this.look = RaceLook.fromHumanLook(client.playerMob.look, CustomHumanLook.class);			
		this.name = client.getName();
		this.putData();
	}

	public CustomPacketPlayerAppearance(int slot, int characterUniqueID, PlayerMob player) {
		super(slot, characterUniqueID, player);
		this.slot = slot;
		this.characterUniqueID = characterUniqueID;
		this.look = RaceLook.fromHumanLook(player.look, CustomHumanLook.class);
		this.name = player.getDisplayName();
		this.putData();
	}

	private void putData() {
		PacketWriter writer = new PacketWriter(this);
		writer.putNextByteUnsigned(this.slot);
		writer.putNextInt(this.characterUniqueID);
		this.look.setupContentPacket(writer, true);
		writer.putNextString(this.name);
	}

	public void processServer(NetworkPacket packet, Server server, ServerClient client) {
		if (this.slot != client.slot) {
			System.out.print("Player " + client.authentication + " (\"" + client.getName() + "\", slot " + client.slot
					+ ") tried to change wrong client slot appearance: " + this.slot);
		} else {
			if (client.needAppearance()) {
				GameMessage validName = GameUtils.isValidPlayerName(this.name);
				if (validName != null) {
					client.sendPacket(new PacketCharacterSelectError(this.look, validName));
				} else {
					Iterator var5 = server.usedNames.entrySet().iterator();

					while (var5.hasNext()) {
						Map.Entry<Long, String> entry = (Map.Entry) var5.next();
						Long auth = (Long) entry.getKey();
						String usedName = (String) entry.getValue();
						if (auth != client.authentication && usedName.equalsIgnoreCase(this.name)) {
							client.sendPacket(new PacketCharacterSelectError(new HumanLook(),
									new LocalMessage("ui", "characternameinuse", "name", this.name)));
							return;
						}
					}

					client.applyAppearancePacket(this);
					if (client.getName().equalsIgnoreCase(Settings.serverOwnerName)) {
						client.setPermissionLevel(PermissionLevel.OWNER, false);
					}

					client.sendConnectingMessage();
				}
			} else if (client.getPermissionLevel().getLevel() < PermissionLevel.ADMIN.getLevel()) {
				System.out.println(client.getName() + " tried to change appearance, but isn't admin");
				server.network.sendPacket(new CustomPacketPlayerAppearance(client), client);
			} else if (!client.getName().equalsIgnoreCase(this.name)) {
				System.out.println(client.getName() + " tried to change appearance with wrong name");
				server.network.sendPacket(new CustomPacketPlayerAppearance(client), client);
			} else if (!server.world.settings.cheatsAllowedOrHidden()) {
				System.out.println(client.getName() + " tried to change appearance, but cheats aren't allowed");
				server.network.sendPacket(new CustomPacketPlayerAppearance(client), client);
			} else {
				client.applyAppearancePacket(this);
			}

		}
	}

	public void processClient(NetworkPacket packet, Client client) {
		ClientClient target = client.getClient(this.slot);
		if (target == null) {
			client.network.sendPacket(new PacketRequestPlayerData(this.slot));
		} else {			
			target.applyAppearancePacket(this);
		}		
		client.loading.createCharPhase.submitPlayerAppearancePacket(this);
	}
}