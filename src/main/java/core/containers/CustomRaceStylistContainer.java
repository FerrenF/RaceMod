package core.containers;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.function.Supplier;

import core.network.CustomPacketPlayerAppearance;
import core.race.CustomHumanLook;
import core.race.RaceLook;
import core.race.factory.RaceDataFactory;
import core.race.parts.BodyPart;
import core.race.parts.RaceLookParts;
import helpers.DebugHelper;
import helpers.DebugHelper.MESSAGE_TYPE;
import necesse.engine.network.NetworkClient;
import necesse.engine.network.Packet;
import necesse.engine.network.PacketReader;
import necesse.engine.network.PacketWriter;
import necesse.engine.network.packet.PacketSpawnMob;
import necesse.engine.network.server.ServerClient;
import necesse.engine.sound.SoundEffect;
import necesse.engine.sound.SoundManager;
import necesse.engine.util.GameRandom;
import necesse.entity.mobs.Mob;
import necesse.entity.mobs.friendly.human.HumanMob;
import necesse.entity.mobs.friendly.human.humanShop.HumanShop;
import necesse.entity.mobs.friendly.human.humanShop.ShopContainerData;
import necesse.entity.mobs.friendly.human.humanShop.StylistHumanMob;
import necesse.gfx.GameResources;
import necesse.gfx.HumanLook;
import necesse.inventory.InventoryItem;
import necesse.inventory.InventorySlot;
import necesse.inventory.PlayerInventory;
import necesse.inventory.container.customAction.ContentCustomAction;
import necesse.inventory.container.customAction.EmptyCustomAction;
import necesse.inventory.container.events.StylistSettlersUpdateContainerEvent;
import necesse.inventory.container.mob.ShopContainer;
import necesse.inventory.container.mob.StylistContainer;
import necesse.inventory.item.armorItem.cosmetics.misc.ShirtArmorItem;
import necesse.inventory.item.armorItem.cosmetics.misc.ShoesArmorItem;
import necesse.inventory.item.armorItem.cosmetics.misc.WigArmorItem;
import necesse.level.maps.Level;
import necesse.level.maps.levelData.settlementData.LevelSettler;
import necesse.level.maps.levelData.settlementData.SettlementLevelData;
import necesse.level.maps.levelData.settlementData.settler.SettlerMob;

public class CustomRaceStylistContainer extends ShopContainer {
	private static final int HAIR_COST = 200;
	private static final int FACIAL_FEATURE_COST = 200;
	private static final int HAIR_COLOR_COST = 100;
	private static final int SHIRT_COLOR_COST = 200;
	private static final int SHOES_COLOR_COST = 100;
	public final ContentCustomAction playerStyleButton;
	public final ContentCustomAction settlerStyleButton;
	public final EmptyCustomAction styleButtonResponse;
	public StylistHumanMob stylistMob;
	public final long styleCostSeed;
	public ArrayList<HumanMob> availableSettlers;

	public CustomRaceStylistContainer(final NetworkClient client, int uniqueSeed, StylistHumanMob mob,
			PacketReader contentReader, ShopContainerData containerData) {
		super(client, uniqueSeed, mob, contentReader.getNextContentPacket(), containerData);

		this.stylistMob = mob;
		this.styleCostSeed = this.priceSeed * (long) GameRandom.prime(42);
		this.availableSettlers = (new StylistSettlersUpdateContainerEvent(contentReader)).getHumanMobs(mob.getLevel());
		this.subscribeEvent(StylistSettlersUpdateContainerEvent.class, (e) -> {
			return true;
		}, () -> {
			return true;
		});
		this.onEvent(StylistSettlersUpdateContainerEvent.class, (e) -> {
			this.availableSettlers = e.getHumanMobs(mob.getLevel());
		});
		this.playerStyleButton = (ContentCustomAction) this.registerAction(new ContentCustomAction() {
			protected void run(Packet content) {
				if (client.isServer()) {
					
					RaceLook oldLook = RaceDataFactory.getRaceLook(client.playerMob, RaceLook.fromHumanLook(client.playerMob.look, CustomHumanLook.class));
					RaceLook newLook = RaceLook.raceFromContentPacket(new PacketReader(content), RaceLook.fromHumanLook(client.playerMob.look, CustomHumanLook.class));
					ArrayList<InventoryItem> cost = CustomRaceStylistContainer.this.getTotalStyleCost(oldLook, newLook);
					
					if (CustomRaceStylistContainer.this.canStyle(cost)) {
						ServerClient serverClient = client.getServerClient();
						Iterator<InventoryItem> var5 = cost.iterator();

						while (var5.hasNext()) {
							InventoryItem item = (InventoryItem) var5.next();
							client.playerMob.getInv().main.removeItems(client.playerMob.getLevel(), client.playerMob,
									item.item, item.getAmount(), "buy");
							if (item.item.getStringID().equals("coin")) {
								serverClient.newStats.money_spent.increment(item.getAmount());
							}
						}

						client.playerMob.look = newLook;
						RaceDataFactory.setRaceLook(mob, newLook);
						serverClient.getServer().network.sendToAllClients(new CustomPacketPlayerAppearance(serverClient));
						CustomRaceStylistContainer.this.styleButtonResponse.runAndSend();
						if (serverClient.achievementsLoaded()) {
							serverClient.achievements().FEELING_STYLISH.markCompleted(serverClient);
						}
					}
					
				}

			}
		});
		this.settlerStyleButton = (ContentCustomAction) this.registerAction(new ContentCustomAction() {
			protected void run(Packet content) {
				if (client.isServer()) {
					PacketReader reader = new PacketReader(content);
					int mobUniqueID = reader.getNextInt();
					RaceLook newLook = RaceLook.raceFromContentPacket(contentReader, new CustomHumanLook(true));
					Level level = client.playerMob.getLevel();
					ServerClient serverClient = client.getServerClient();
					if (!level.settlementLayer.doesClientHaveAccess(serverClient)) {
						(new StylistSettlersUpdateContainerEvent(CustomRaceStylistContainer.this.stylistMob, serverClient))
								.applyAndSendToClient(serverClient);
						return;
					}

					SettlementLevelData settlementData = SettlementLevelData.getSettlementData(level);
					if (settlementData == null) {
						(new StylistSettlersUpdateContainerEvent(CustomRaceStylistContainer.this.stylistMob, serverClient))
								.applyAndSendToClient(serverClient);
						return;
					}

					LevelSettler settler = settlementData.getSettler(mobUniqueID);
					if (settler == null) {
						(new StylistSettlersUpdateContainerEvent(CustomRaceStylistContainer.this.stylistMob, serverClient))
								.applyAndSendToClient(serverClient);
						return;
					}

					SettlerMob mob = settler.getMob();
					if (!(mob instanceof HumanMob)) {
						(new StylistSettlersUpdateContainerEvent(CustomRaceStylistContainer.this.stylistMob, serverClient))
								.applyAndSendToClient(serverClient);
						return;
					}

					HumanMob humanMob = (HumanMob) mob;
					
					RaceLook hum = RaceDataFactory.hasRaceData((Mob) mob) 
							? RaceDataFactory.getRaceLook((Mob)mob, new CustomHumanLook(true))
							: RaceLook.fromHumanLook(humanMob.look, CustomHumanLook.class);
					
					ArrayList<InventoryItem> cost = CustomRaceStylistContainer.this.getTotalStyleCost(hum, newLook);
					if (CustomRaceStylistContainer.this.canStyle(cost)) {
						Iterator<InventoryItem> var12 = cost.iterator();

						while (var12.hasNext()) {
							InventoryItem item = (InventoryItem) var12.next();
							client.playerMob.getInv().main.removeItems(client.playerMob.getLevel(), client.playerMob,
									item.item, item.getAmount(), "buy");
							if (item.item.getStringID().equals("coin")) {
								serverClient.newStats.money_spent.increment(item.getAmount());
							}
						}

						humanMob.customLook = true;
						humanMob.look = new HumanLook(newLook);
						serverClient.getServer().network.sendToClientsWithEntity(new PacketSpawnMob(humanMob),
								humanMob);
						CustomRaceStylistContainer.this.styleButtonResponse.runAndSend();
					}

					(new StylistSettlersUpdateContainerEvent(CustomRaceStylistContainer.this.stylistMob, serverClient))
							.applyAndSendToClient(serverClient);
				}

			}
		});
		this.styleButtonResponse = (EmptyCustomAction) this.registerAction(new EmptyCustomAction() {
			protected void run() {
				if (client.isClient()) {
					SoundManager.playSound(GameResources.coins, SoundEffect.effect(client.playerMob));
				}

			}
		});
	}

	private void updatePlayerInventory(PlayerInventory inventory, HumanLook oldLook) {
		for (int i = 0; i < inventory.getSize(); ++i) {
			this.updatePlayerSlot(new InventorySlot(inventory, i), oldLook, inventory.player.look);
		}

	}

	private void updatePlayerSlot(InventorySlot slot, HumanLook oldLook, HumanLook newLook) {
		if (!slot.isSlotClear()) {
			InventoryItem item = slot.getItem();
			if (item.item.getStringID().equals("wig")) {
				if (WigArmorItem.getHair(item.getGndData()) == oldLook.getHair()
						&& WigArmorItem.getHairCol(item.getGndData()) == oldLook.getHairColor()) {
					WigArmorItem.addWigData(item, newLook);
					slot.markDirty();
				}
			} else if (item.item.getStringID().equals("shirt")) {
				if (ShirtArmorItem.getColor(item.getGndData()).equals(oldLook.getShirtColor())) {
					ShirtArmorItem.addColorData(item, newLook.getShirtColor());
					slot.markDirty();
				}
			} else if (item.item.getStringID().equals("shoes")
					&& ShoesArmorItem.getColor(item.getGndData()).equals(oldLook.getShoesColor())) {
				ShoesArmorItem.addColorData(item, newLook.getShoesColor());
				slot.markDirty();
			}

		}
	}

	public ArrayList<InventoryItem> getTotalStyleCost(RaceLook previousLook, RaceLook newLook) {
		ArrayList<InventoryItem> items = new ArrayList<InventoryItem>();
		
		boolean changed = false;
		for(BodyPart p : previousLook.getRaceParts().getBodyParts()) {	
			boolean changedPart = this.addToList(items,
			this.getBodyPartCost(p, previousLook.appearanceByteGet(p.getPartName()), newLook.appearanceByteGet(p.getPartName())));
			boolean changedPartColor = this.addToList(items,
					this.getBodyPartCost(p, previousLook.appearanceByteGet(p.getPartColorName()), newLook.appearanceByteGet(p.getPartColorName())));
			changed = changedPart || changedPartColor || changed;
		}	
		return !changed ? null : items;
	}
	
	public ArrayList<InventoryItem> getTotalStyleCost(HumanLook previousLook, HumanLook newLook) {
		ArrayList<InventoryItem> items = new ArrayList();
		boolean changed = this.addToList(items, this.getSkinColorCost(previousLook.getSkin(), newLook.getSkin()));
		changed = this.addToList(items, this.getEyeTypeCost(previousLook.getEyeType(), newLook.getEyeType()))
				|| changed;
		changed = this.addToList(items, this.getEyeColorCost(previousLook.getEyeColor(), newLook.getEyeColor()))
				|| changed;
		changed = this.addToList(items, this.getHairStyleCost(previousLook.getHair(), newLook.getHair())) || changed;
		changed = this.addToList(items,
				this.getFacialFeatureCost(previousLook.getFacialFeature(), newLook.getFacialFeature())) || changed;
		changed = this.addToList(items, this.getHairColorCost(previousLook.getHairColor(), newLook.getHairColor()))
				|| changed;
		changed = this.addToList(items, this.getShirtColorCost(previousLook.getShirtColor(), newLook.getShirtColor()))
				|| changed;
		changed = this.addToList(items, this.getShoesColorCost(previousLook.getShoesColor(), newLook.getShoesColor()))
				|| changed;
		return !changed ? null : items;
	}
	
	public boolean addToList(ArrayList<InventoryItem> items, ArrayList<InventoryItem> append) {
		if (append == null) {
			return false;
		} else {
			Iterator<InventoryItem> var3 = append.iterator();

			while (var3.hasNext()) {
				InventoryItem item = (InventoryItem) var3.next();
				item.combineOrAddToList(this.client.playerMob.getLevel(), this.client.playerMob, items, "add");
			}

			return true;
		}
	}

	public ArrayList<InventoryItem> getBodyPartCost(BodyPart b, Object oldID, Object newID){
		
		Supplier<ArrayList<InventoryItem>> getter = ()->{
			if(b.isBaseGamePart()) {
				if(oldID instanceof Integer) {
					int oldVal = (int)oldID;
					int newVal = (int)newID;
					switch(b.getPartName()) {
					    case "BASE_SKIN": 	            return this.getSkinColorCost(oldVal, newVal);
				        case "BASE_EYE": 	            return this.getEyeTypeCost(oldVal, newVal);
				        case "BASE_EYE_COLOR": 	            return this.getEyeColorCost(oldVal, newVal);
				        case "BASE_HAIR": 	            return this.getHairStyleCost(oldVal, newVal);
				        case "BASE_FACIAL_HAIR": 	        return this.getFacialFeatureCost(oldVal, newVal);
				        case "BASE_HAIR_COLOR": 	            return this.getHairColorCost(oldVal, newVal);
				        default: 
				        	return new ArrayList<InventoryItem>(Collections.singletonList(new InventoryItem("coin", this.getRandomPrice(
									this.styleCostSeed * (long) GameRandom.prime(24) + (long) newID * (long) GameRandom.prime(82),
									BodyPart.STYLIST_COST_DEFAULT))));
					}
				}
				else if(oldID instanceof Color) {				
					Color oldVal = (Color)oldID;
					Color newVal = (Color)newID;
					switch(b.getPartName()) {
					 	case "BASE_SHIRT": 	        return this.getShirtColorCost(oldVal,newVal);
				        case "BASE_SHOES": 	        return this.getShoesColorCost(oldVal, newVal);					
					}
				}
			
			}
			
			else if(b.stylistCostIsShards()) {
				return new ArrayList<InventoryItem>(Collections.singletonList(new InventoryItem("voidshard", b.stylistCost())));
			}
			System.out.println(b.getPartName()+ ", " + (b.stylistCostIsShards() ? " shards: " : " coins:") + String.valueOf(b.stylistCost()));
			
			int amt = this.getRandomPrice(
					this.styleCostSeed * (long) GameRandom.prime(24) + (int)newID * (long) GameRandom.prime(82),
					b.stylistCost());
			
			return new ArrayList<InventoryItem>(Collections.singletonList(new InventoryItem("coin", amt)));		
		};
		return oldID.equals(newID) ? null : getter.get();
		
		
	}
	
	public ArrayList<InventoryItem> getSkinColorCost(int oldID, int newID) {
		return oldID == newID ? null : new ArrayList<InventoryItem>(Collections.singletonList(new InventoryItem("voidshard", 10)));
	}

	public ArrayList<InventoryItem> getEyeTypeCost(int oldID, int newID) {
		return oldID == newID ? null : new ArrayList<InventoryItem>(Collections.singletonList(new InventoryItem("voidshard", 5)));
	}

	public ArrayList<InventoryItem> getEyeColorCost(int oldID, int newID) {
		return oldID == newID ? null : new ArrayList<InventoryItem>(Collections.singletonList(new InventoryItem("voidshard", 3)));
	}

	public ArrayList<InventoryItem> getHairStyleCost(int oldID, int newID) {
		return oldID == newID
				? null
				: new ArrayList<InventoryItem>(Collections.singletonList(new InventoryItem("coin", this.getRandomPrice(
						this.styleCostSeed * (long) GameRandom.prime(24) + (long) newID * (long) GameRandom.prime(82),
						200))));
	}

	public ArrayList<InventoryItem> getFacialFeatureCost(int oldID, int newID) {
		return oldID == newID
				? null
				: new ArrayList<InventoryItem>(Collections.singletonList(new InventoryItem("coin", this.getRandomPrice(
						this.styleCostSeed * (long) GameRandom.prime(29) + (long) newID * (long) GameRandom.prime(32),
						200))));
	}

	public ArrayList<InventoryItem> getHairColorCost(int oldID, int newID) {
		return oldID == newID
				? null
				: new ArrayList<InventoryItem>(Collections.singletonList(new InventoryItem("coin", this.getRandomPrice(
						this.styleCostSeed * (long) GameRandom.prime(67) + (long) newID * (long) GameRandom.prime(817),
						100))));
	}

	public ArrayList<InventoryItem> getShirtColorCost(Color oldColor, Color newColor) {
		return newColor != null && oldColor.getRGB() == newColor.getRGB()
				? null
				: new ArrayList<InventoryItem>(Collections.singletonList(new InventoryItem("coin",
						this.getRandomPrice(this.styleCostSeed * (long) GameRandom.prime(466), 200))));
	}

	public ArrayList<InventoryItem> getShoesColorCost(Color oldColor, Color newColor) {
		return newColor != null && oldColor.getRGB() == newColor.getRGB()
				? null
				: new ArrayList<InventoryItem>(Collections.singletonList(new InventoryItem("coin",
						this.getRandomPrice(this.styleCostSeed * (long) GameRandom.prime(576), 100))));
	}

	private int getRandomPrice(long seed, int middlePrice) {
		return HumanShop.getRandomHappinessMiddlePrice(new GameRandom(seed), this.settlerHappiness, middlePrice, 2, 4);
	}

	public boolean canStyle(ArrayList<InventoryItem> cost) {
		if (cost == null) {
			return false;
		} else {
			Iterator<InventoryItem> var2 = cost.iterator();

			InventoryItem item;
			do {
				if (!var2.hasNext()) {
					return true;
				}

				item = (InventoryItem) var2.next();
			} while (this.client.playerMob.getInv().main.getAmount(this.client.playerMob.getLevel(),
					this.client.playerMob, item.item, "buy") >= item.getAmount());

			return false;
		}
	}

	/*
	 * ShopContainerData baseData = mob.getShopContainerData(client);
		Packet packet = new Packet();
		PacketWriter writer = new PacketWriter(packet);
		writer.putNextContentPacket(baseData.content);
		(new StylistSettlersUpdateContainerEvent(mob, client)).write(writer);
		return new ShopContainerData(packet, baseData.shopManager);
	 */
		
	public static ShopContainerData getStylistContainerContent(StylistHumanMob mob, ServerClient client) {
		ShopContainerData baseData = mob.getShopContainerData(client);
		Packet packet = new Packet();
		PacketWriter writer = new PacketWriter(packet);	
		writer.putNextContentPacket(baseData.content);
		(new StylistSettlersUpdateContainerEvent(mob, client)).write(writer);
		return new ShopContainerData(packet, baseData.shopManager);
	}
}