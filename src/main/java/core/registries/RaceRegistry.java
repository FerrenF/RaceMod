package core.registries;

import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

import core.race.RaceLook;
import core.registries.RaceRegistry.RaceRegistryElement;
import necesse.engine.GameLoadingScreen;
import necesse.engine.localization.Localization;
import necesse.engine.localization.message.GameMessage;
import necesse.engine.localization.message.LocalMessage;
import necesse.engine.modLoader.LoadedMod;
import necesse.engine.registries.GameRegistry;
import necesse.engine.registries.IDData;
import necesse.engine.registries.IDDataContainer;
import necesse.engine.util.GameRandom;


public class RaceRegistry extends GameRegistry<core.registries.RaceRegistry.RaceRegistryElement<?>> {
	public static final RaceRegistry instance = new RaceRegistry();

	public RaceRegistry() {
		super("Race", 64);
	}

	public void registerCore() {
		GameLoadingScreen.drawLoadingString(Localization.translate("loading", "races"));
	}

	protected void onRegister(RaceRegistryElement<?> object, int id, String stringID, boolean isReplace) {
		
	}
	
	public static RaceLook getRace(String stringID) {
		return getRace(getRaceID(stringID));
	}
	
	public static int getRaceID(String stringID) {
		return instance.getElementID(stringID);
	}
	
	public static int getBiomeIDRaw(String stringID) throws NoSuchElementException {
		return instance.getElementIDRaw(stringID);
	}
	
	public static RaceLook getRace(int id) {
		return id == -1 ? null : ((RaceRegistryElement<?>) instance.getElement(id)).race;
	}
	
	public static List<RaceLook> getRaces() {
		return (List<RaceLook>) instance.streamElements().map((e) -> {
			return e.race;
		}).collect(Collectors.toList());
	}

	public static int getTotalRaces() {
		return instance.size();
	}
	
	protected void onRegistryClose() {
		instance.streamElements().map((e) -> {
			return e.race;
		}).forEach(RaceLook::getRaceID);
		Iterator<RaceRegistryElement<?>> var1 = this.getElements().iterator();

		while (var1.hasNext()) {
			RaceRegistryElement<?> element = (RaceRegistryElement<?>) var1.next();
			element.race.onRaceRegistryClosed();
		}

	}

	public static <T extends RaceLook> T registerRace(String stringID, T race) {
		if (LoadedMod.isRunningModClientSide()) {
			throw new IllegalStateException("Client/server only mods cannot register races");
		} else {
			return (T) ((RaceRegistryElement)instance.registerObj(stringID, new RaceRegistryElement(race))).race;
		}
	}

	protected static class RaceRegistryElement<T extends RaceLook> implements IDDataContainer {
		public final Class<?> raceClass;		
		public GameMessage displayName;
		public RaceLook race;

		public RaceRegistryElement(RaceLook race) {
			this.raceClass = race.getClass();
			this.race = race;
			this.displayName = race.getRaceDisplayName();
		}

		public IDData getIDData() {
			return this.race.idData;
		}
	}


}