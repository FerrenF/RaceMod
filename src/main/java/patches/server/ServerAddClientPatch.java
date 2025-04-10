package patches.server;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.Objects;

import core.RaceMod;
import core.network.CustomPacketConnectApproved;
import necesse.engine.Settings;
import necesse.engine.network.NetworkPacket;
import necesse.engine.network.networkInfo.NetworkInfo;
import necesse.engine.network.server.Server;
import necesse.engine.network.server.ServerClient;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.implementation.bind.annotation.AllArguments;
import net.bytebuddy.implementation.bind.annotation.This;
import necesse.engine.GameAuth;
import necesse.engine.GameEvents;
import necesse.engine.events.ServerClientConnectedEvent;
import necesse.engine.modLoader.annotations.ModMethodPatch;
import necesse.engine.network.packet.PacketDisconnect;
import necesse.engine.network.packet.PacketDisconnect.Code;
import necesse.engine.util.GameRandom;

@ModMethodPatch(arguments = { NetworkInfo.class, long.class, String.class, boolean.class, boolean.class }, name = "addClient", target = Server.class)
public class ServerAddClientPatch {
	
		 
	@Advice.OnMethodEnter(skipOn = Advice.OnNonDefaultValue.class)
    static boolean onEnter(@Advice.This Server th,
    		@Advice.AllArguments Object[] args) {
    		
		return true;
    }
	@Advice.OnMethodExit
    static void onExit(@Advice.This Server th,
    		@Advice.AllArguments Object[] args, @Advice.Return(readOnly=false) boolean result) {
    		
		NetworkInfo networkInfo = (NetworkInfo) args[0];
		long authentication = (long) args[1];
		String version = (String) args[2];
		boolean craftingUsesNearbyInventories = (boolean) args[3];
		boolean trackNewQuests = (boolean) args[4];
		if (!th.isSingleplayer()) {
			System.out.println("Client \"" + authentication + "\" with address "
					+ (networkInfo == null ? "LOCAL" : networkInfo.getDisplayName()) + " is connecting with version "
					+ version + ".");
		}

		boolean hasClient = th.world.hasClient(authentication);
		String clientName = (String) th.usedNames.getOrDefault(authentication, "N/A");
		String consoleName;
		if (Settings.isBanned(authentication) || hasClient && Settings.isBanned(clientName)) {
			if (!th.isSingleplayer() && !th.isHosted() || authentication != GameAuth.getAuthentication()) {
				th.network.sendPacket(new NetworkPacket(new PacketDisconnect(-1, Code.BANNED_CLIENT), networkInfo));
				consoleName = hasClient ? " (" + clientName + ")." : ".";
				System.out.println("Client " + authentication + " is banned" + consoleName);
				result = false;
				return;
			}

			System.out.println("The singleplayer/host client was banned, unbanning now.");
			Settings.removeBanned(String.valueOf(authentication));
			if (hasClient) {
				Settings.removeBanned(clientName);
			}
		}

		consoleName = hasClient ? "\"" + clientName + "\"" : "\"" + authentication + "\"";
		if (!version.equals(RaceMod.NECESSE_VERSION_STRING)) {
			System.out.println("Client " + consoleName + " had wrong version (" + version + ").");
			th.network.sendPacket(new NetworkPacket(new PacketDisconnect(-1, Code.WRONG_VERSION), networkInfo));
			result = false;
			return;
			//return false;
		} else {
			for (int i = 0; i < th.getSlots(); ++i) {
				ServerClient client = th.getClient(i);
				if (client != null && client.authentication == authentication) {
					th.packetManager.startVerboseLogging(networkInfo, 10000);
					if (Objects.equals(client.networkInfo, networkInfo)) {
						System.out.println(
								"Client " + consoleName + " is already connected. Sending another approved packet...");
						client.sendPacket(new CustomPacketConnectApproved(th, client));
						result = false;
						return;
						
					}

					System.out.println("Client " + consoleName + " was already playing.");
					th.network
							.sendPacket(new NetworkPacket(new PacketDisconnect(-1, Code.ALREADY_PLAYING), networkInfo));
					result = false;
					return;
				}
			}

			int slotOffset = 0;		

			try {
			    // Accessing `clients` array
			    Field clientsField = th.getClass().getDeclaredField("clients");
			    clientsField.setAccessible(true);
			    ServerClient[] clients = (ServerClient[]) clientsField.get(th);
			    
			    // Accessing `authClientMap`
			    Field authClientMapField = th.getClass().getDeclaredField("authClientMap");
			    authClientMapField.setAccessible(true);
			    Map<Long, ServerClient> authClientMap = (Map<Long, ServerClient>) authClientMapField.get(th);

			    for (int i = 0; i < th.getSlots(); ++i) {
			        int actualSlot = (i + slotOffset) % th.getSlots();
			        if (clients[actualSlot] == null) {
			            System.out.println("Client " + consoleName + " connected on slot " + (actualSlot + 1) + "/"
			                    + th.getSlots() + ".");
			            long sessionID = GameRandom.globalRandom.nextLong();
			            if (!hasClient) {
			                System.out.println("Creating new player: " + authentication);
			                clients[actualSlot] = ServerClient.getNewPlayerClient(th, sessionID, networkInfo,
			                        actualSlot, authentication);
			                clients[actualSlot].saveClient();
			            } else {
			                clients[actualSlot] = th.world.loadClient(sessionID, authentication, networkInfo,
			                        actualSlot);
			                System.out.println("Loaded player: " + authentication);
			                if (!clients[actualSlot].needAppearance()) {
			                    clients[actualSlot].sendConnectingMessage();
			                }
			            }

			            authClientMap.put(authentication, clients[actualSlot]);
			            clients[actualSlot].sendPacket(new CustomPacketConnectApproved(th, clients[actualSlot]));
			            clients[actualSlot].craftingUsesNearbyInventories = craftingUsesNearbyInventories;
			            clients[actualSlot].trackNewQuests = trackNewQuests;
			            GameEvents.triggerEvent(new ServerClientConnectedEvent(clients[actualSlot]));
			            result = true;
			            return;
			        }
			    }

			    // Restore field access to its original state (optional but good practice)
			    clientsField.setAccessible(false);
			    authClientMapField.setAccessible(false);

			} catch (NoSuchFieldException | IllegalAccessException e) {
			    e.printStackTrace(); // Log the error so you can debug any issues
			}

			System.out.println("Could not find a slot for client \"" + authentication + "\".");
			th.network.sendPacket(new NetworkPacket(new PacketDisconnect(-1, Code.SERVER_FULL), networkInfo));
			result = false;
			return;
		}
		
		
	
    }
	

/*	public static boolean addClient(@This Server th, @AllArguments Object[] args) {
		
		
		NetworkInfo networkInfo = (NetworkInfo) args[0];
		long authentication = (long) args[1];
		String version = (String) args[2];
		boolean craftingUsesNearbyInventories = (boolean) args[3];
		boolean trackNewQuests = (boolean) args[4];
		if (!th.isSingleplayer()) {
			System.out.println("Client \"" + authentication + "\" with address "
					+ (networkInfo == null ? "LOCAL" : networkInfo.getDisplayName()) + " is connecting with version "
					+ version + ".");
		}

		boolean hasClient = th.world.hasClient(authentication);
		String clientName = (String) th.usedNames.getOrDefault(authentication, "N/A");
		String consoleName;
		if (Settings.isBanned(authentication) || hasClient && Settings.isBanned(clientName)) {
			if (!th.isSingleplayer() && !th.isHosted() || authentication != GameAuth.getAuthentication()) {
				th.network.sendPacket(new NetworkPacket(new PacketDisconnect(-1, Code.BANNED_CLIENT), networkInfo));
				consoleName = hasClient ? " (" + clientName + ")." : ".";
				System.out.println("Client " + authentication + " is banned" + consoleName);
				return false;
			}

			System.out.println("The singleplayer/host client was banned, unbanning now.");
			Settings.removeBanned(String.valueOf(authentication));
			if (hasClient) {
				Settings.removeBanned(clientName);
			}
		}

		consoleName = hasClient ? "\"" + clientName + "\"" : "\"" + authentication + "\"";
		if (!version.equals("0.31.1")) {
			System.out.println("Client " + consoleName + " had wrong version (" + version + ").");
			th.network.sendPacket(new NetworkPacket(new PacketDisconnect(-1, Code.WRONG_VERSION), networkInfo));
			return false;
		} else {
			for (int i = 0; i < th.getSlots(); ++i) {
				ServerClient client = th.getClient(i);
				if (client != null && client.authentication == authentication) {
					th.packetManager.startVerboseLogging(networkInfo, 10000);
					if (Objects.equals(client.networkInfo, networkInfo)) {
						System.out.println(
								"Client " + consoleName + " is already connected. Sending another approved packet...");
						client.sendPacket(new CustomPacketConnectApproved(th, client));
						return false;
					}

					System.out.println("Client " + consoleName + " was already playing.");
					th.network
							.sendPacket(new NetworkPacket(new PacketDisconnect(-1, Code.ALREADY_PLAYING), networkInfo));
					return false;
				}
			}

			int slotOffset = 0;		

			try {
			    // Accessing `clients` array
			    Field clientsField = th.getClass().getDeclaredField("clients");
			    clientsField.setAccessible(true);
			    ServerClient[] clients = (ServerClient[]) clientsField.get(th);
			    
			    // Accessing `authClientMap`
			    Field authClientMapField = th.getClass().getDeclaredField("authClientMap");
			    authClientMapField.setAccessible(true);
			    Map<Long, ServerClient> authClientMap = (Map<Long, ServerClient>) authClientMapField.get(th);

			    for (int i = 0; i < th.getSlots(); ++i) {
			        int actualSlot = (i + slotOffset) % th.getSlots();
			        if (clients[actualSlot] == null) {
			            System.out.println("Client " + consoleName + " connected on slot " + (actualSlot + 1) + "/"
			                    + th.getSlots() + ".");
			            long sessionID = GameRandom.globalRandom.nextLong();
			            if (!hasClient) {
			                System.out.println("Creating new player: " + authentication);
			                clients[actualSlot] = ServerClient.getNewPlayerClient(th, sessionID, networkInfo,
			                        actualSlot, authentication);
			                clients[actualSlot].saveClient();
			            } else {
			                clients[actualSlot] = th.world.loadClient(sessionID, authentication, networkInfo,
			                        actualSlot);
			                System.out.println("Loaded player: " + authentication);
			                if (!clients[actualSlot].needAppearance()) {
			                    clients[actualSlot].sendConnectingMessage();
			                }
			            }

			            authClientMap.put(authentication, clients[actualSlot]);
			            clients[actualSlot].sendPacket(new CustomPacketConnectApproved(th, clients[actualSlot]));
			            clients[actualSlot].craftingUsesNearbyInventories = craftingUsesNearbyInventories;
			            clients[actualSlot].trackNewQuests = trackNewQuests;
			            GameEvents.triggerEvent(new ServerClientConnectedEvent(clients[actualSlot]));
			            return true;
			        }
			    }

			    // Restore field access to its original state (optional but good practice)
			    clientsField.setAccessible(false);
			    authClientMapField.setAccessible(false);

			} catch (NoSuchFieldException | IllegalAccessException e) {
			    e.printStackTrace(); // Log the error so you can debug any issues
			}

			System.out.println("Could not find a slot for client \"" + authentication + "\".");
			th.network.sendPacket(new NetworkPacket(new PacketDisconnect(-1, Code.SERVER_FULL), networkInfo));
			return false;
		}
	}*/
	
	
}
