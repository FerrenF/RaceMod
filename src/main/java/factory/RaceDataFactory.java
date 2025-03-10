package factory;

import java.lang.instrument.Instrumentation;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;
import core.RaceMod;
import core.race.CustomHumanLook;

import java.util.Set;
import java.util.WeakHashMap;

import extensions.RaceLook;
import necesse.entity.mobs.PlayerMob;

public class RaceDataFactory {
	public static RaceDataRegistry registry;
	public static Instrumentation inst;
	public static RaceDataFactory instance;
	
	
	public static void initialize(Instrumentation inst) {
	    if (instance == null) {
	        instance = new RaceDataFactory(inst);
	    }
	}
	
	public RaceDataFactory(Instrumentation inst) {		
		RaceDataFactory.inst = inst;
		RaceDataFactory.registry = new RaceDataRegistry();
	}	
	
	public static int mobUniqueID(PlayerMob mob) {	
		if(mob.playerName == null || mob.playerName.equals("N/A")) {
			return -1;
		}
		
		return mob.playerName.hashCode();
	}

	public static RaceLook getRaceLook(PlayerMob mob, RaceLook defaultValue) {
		if(mobUniqueID(mob)==-1) return defaultValue;
	    RaceData result = getRaceData(mob);
	    if (result == null) {
	        RaceMod.handleDebugMessage(String.format("getRaceLook for mob %s returned null result.", mob.playerName), 25);
	        return defaultValue; // Return a default RaceLook instead of null.
	    }
	    return  result.raceDataInitialized ? result.getRaceLook() : defaultValue;
	}

	public static boolean hasRaceData(PlayerMob mob) {
		if(mobUniqueID(mob)==-1) {
			return false;
		}
		return registry.containsMob(mob);
	}
	
	public static void registerRaceData(PlayerMob newPlayerMob) {
		registry.put(newPlayerMob, instance.new RaceData(newPlayerMob));
	}
	
	public static RaceData getRaceData(PlayerMob mob) {
		if(!(hasRaceData(mob))) return null;
		return registry.get(mob);
	}
	
	public static RaceData getOrRegisterRaceData(PlayerMob mob) {		
		if(registry.containsMob(mob)) {
			return registry.get(mob);
		}
		RaceData toGo = instance.new RaceData(mob);
		registry.put(mob, toGo);		
		return toGo;
	}
	
	public static RaceData getOrRegisterRaceData(PlayerMob mob, RaceLook raceLook) {
	    RaceData res = registry.containsMob(mob) ? registry.get(mob) : instance.new RaceData(mob);
	    res.addRaceData(raceLook);
	    registry.put(mob, res); // <-- Ensure it is stored
	    return res;
	}
	
	public static void setRaceLook(PlayerMob mob, RaceLook raceLook) {
	    RaceData raceData = getOrRegisterRaceData(mob); // Ensures we always get a valid RaceData instance
	    raceData.addRaceData(raceLook);
	    registry.put(mob, raceData); // Ensure the modified RaceData is stored in the registry
	}
	
	public class RaceData {
		
		public PlayerMob src;
		public RaceLook raceData;
		public boolean raceDataInitialized = false;
		public String race_id;
		
		public RaceData(PlayerMob src) {
			this.src = src;	
		}
		
		public RaceData addRaceData(RaceLook raceData) {
			this.raceData = raceData;
			this.race_id = raceData.getRaceID();
			this.raceDataInitialized = true;
			return this;
		}
		
		public RaceLook getRaceLook() {
			return this.raceDataInitialized ? this.raceData : (RaceLook)null;
		}
		
		public String getRaceID() {
			return this.raceDataInitialized ? this.race_id : null;
		}
		
	}
	public static class RaceDataRegistry extends HashMap<Integer, RaceData> implements Map<Integer, RaceData>
	{
		
	
		
		//private final Map<Integer, RaceData> backingMap =  new HashMap<>();
	 	
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		@Override
	    public boolean containsKey(Object key) {
	        return key instanceof Integer && super.containsKey(key);
	    }
		
	    public boolean containsMob(PlayerMob mobkey) {
	        return super.containsKey(mobUniqueID(mobkey));
	    }

	 	@Override
	    public boolean containsValue(Object value) {
	        return super.containsValue(value);
	    }
	 	
	 	@Override
	    public void clear() {
	 		super.clear();
	    }
	 	
	 	@Override
	    public Set<Map.Entry<Integer, RaceData>> entrySet() {
	        return super.entrySet();
	    }
	 	
	 	@Override
	    public RaceData put(Integer key, RaceData value) {
	        return super.put(key, value);
	    }
	 	
	    public RaceData put(PlayerMob mobkey, RaceData value) {
	        return super.put(mobUniqueID(mobkey), value);
	    }

	    @Override
	    public RaceData remove(Object key) {
	        if (key instanceof Integer) {
	            return super.remove(key);
	        }
	        return null;
	    }
	    
	    public RaceData remove(PlayerMob mobkey) {
	        return this.remove(mobUniqueID(mobkey));
	    }
	    
	    @Override
	    public RaceData get(Object key) {
	        if (key instanceof Integer)  return super.get(key);	        
	        if (key instanceof PlayerMob) return this.get(mobUniqueID((PlayerMob)key));
	        return null;
	    }
	    
	    public RaceData get(PlayerMob mobkey) {	       
	        return super.get(mobUniqueID(mobkey));
	    }
	}
	
	

	
	

}