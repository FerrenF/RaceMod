package core.race.factory;

import java.lang.instrument.Instrumentation;
import java.lang.ref.SoftReference;
import java.util.Iterator;
import java.util.Map;

import core.race.RaceLook;
import helpers.DebugHelper;
import helpers.DebugHelper.MESSAGE_TYPE;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import necesse.entity.mobs.Mob;
import necesse.entity.mobs.PlayerMob;

public class RaceDataFactory {
	public static RaceDataRegistry registry;
	public static Instrumentation inst;
	public static RaceDataFactory instance;
	
	
	public static void initialize(Instrumentation inst) {
	    if (instance == null) {
	        instance = new RaceDataFactory(inst);
	        registry = new RaceDataRegistry();
	    }
	}
	
	public RaceDataFactory(Instrumentation inst) {		
		RaceDataFactory.inst = inst;
	}	
	
	public static int mobUniqueID(Mob mob) {	
		if(mob == null) return -1;
		if(mob instanceof PlayerMob) {
			
			if(((PlayerMob)mob).playerName == null || ((PlayerMob)mob).playerName.equals("N/A")) {
				return -1;
			}			
			return ((PlayerMob)mob).playerName.hashCode();
		}		
		return mob.getUniqueID();
	}

	public static String mobNameOrPlayerName(Mob mob) {	
		
		if(mob instanceof PlayerMob) {
			return ((PlayerMob)mob).playerName;
		}		
		return mob.getDisplayName();
	}
	
	public static RaceLook getRaceLook(Mob mob, RaceLook defaultValue) {
		if(mobUniqueID(mob)==-1) return defaultValue;
	    RaceData result = getRaceData(mob);
	    if (result == null) {
	        DebugHelper.handleDebugMessage(String.format("getRaceLook for mob %s returned null result.", mobNameOrPlayerName(mob)), 25);
	        return defaultValue; // Return a default RaceLook instead of null.
	    }
	    if (!result.raceDataInitialized) {
	        DebugHelper.handleDebugMessage(String.format("getRaceLook for mob %s did not have an initialized racelook. Defaulting.", mobNameOrPlayerName(mob)), 25);
	    	return defaultValue;
	    }
	    return result.getRaceLook();
	}

	public static boolean hasRaceData(Mob mob) {
		if(mobUniqueID(mob)==-1) {
			return false;
		}
		return registry.containsMob(mob);
	}
	
	 public static void registerRaceData(Mob newMob) {
	        if (!hasRaceData(newMob)) { // Avoid overwriting existing data
	            registry.put(newMob, instance.new RaceData(newMob));
	        }
	 }
	
	 public static RaceData getRaceData(Mob mob) {
	        return hasRaceData(mob) ? registry.get(mob) : null;
	    }
	
	 public static RaceData getOrRegisterRaceData(Mob mob) {        
	        if (registry.containsMob(mob)) {
	            return registry.get(mob);
	        }
	        RaceData toGo = instance.new RaceData(mob);
	        registry.put(mob, toGo);        
	        return toGo;
	    }
	
	public static RaceData getOrRegisterRaceData(Mob mob, RaceLook raceLook) {
        RaceData res;
        if (registry.containsMob(mob)) {
            res = registry.get(mob);
        } else {
            res = instance.new RaceData(mob);
            registry.put(mob, res);
        }
        res.addRaceData(raceLook);
        registry.put(mob, res); // Ensure it's stored
        return res;
    }
	
	public static void setRaceLook(Mob mob, RaceLook raceLook) {
	    RaceData raceData = getOrRegisterRaceData(mob); // Ensures we always get a valid RaceData instance
	    raceData.addRaceData(raceLook);
	    registry.put(mob, raceData); // Ensure the modified RaceData is stored in the registry
	}
	
	public class RaceData {
	    
	    private long lastAccessTime;
	    public Mob src;
	    private RaceLook raceData; 
	    public boolean raceDataInitialized = false;
	    private String race_id;
	    
	    public RaceData(Mob src) {
	        this.src = src;    
	    }
	    
	    public RaceData addRaceData(RaceLook raceData) {
	        this.raceData = raceData;  
	        this.race_id = raceData.getRaceID();
	        this.raceDataInitialized = true;
	        updateLastAccessTime();
	        return this;
	    }
	    
	    public void updateLastAccessTime() {
	        this.lastAccessTime = System.currentTimeMillis();
	    }
	    
	    public RaceLook getRaceLook() {
	        return this.raceDataInitialized ? this.raceData : null;
	    }
	    
	    public long getLastAccessTime() {
	        return lastAccessTime;
	    }
	    
	    public String getRaceID() {
	        return this.raceDataInitialized ? this.race_id : null;
	    }
	}
	public static class RaceDataRegistry extends ConcurrentHashMap<Integer, RaceData> implements Map<Integer, RaceData>
	{
		
		private static final long CLEANUP_THRESHOLD = TimeUnit.MINUTES.toMillis(10); // 10 minute threshold
	    private static final long CLEANUP_INTERVAL = TimeUnit.MINUTES.toMillis(2); // Cleanup every 2 minutes
		
	 	
		private static final long serialVersionUID = 1L;

		public RaceDataRegistry() {
			 startCleanupThread();
		}
		@Override
	    public boolean containsKey(Object key) {
	        return key instanceof Integer && super.containsKey(key);
	    }
		
	    public boolean containsMob(Mob mobkey) {
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
	 		 RaceData data = super.put(key, value);
	         if (data != null) {
	        	 DebugHelper.handleFormattedDebugMessage("PUT requested for mob ID %d with race %s", 60, MESSAGE_TYPE.DEBUG, new Object[] {key, value.getRaceLook()});
	             data.updateLastAccessTime();
	         }
	         return data;
	    }
	 	
	    public RaceData put(Mob mobkey, RaceData value) {
	        return this.put(mobUniqueID(mobkey), value);
	    }

	    @Override
	    public RaceData remove(Object key) {
	        if (key instanceof Integer) {
	            return super.remove(key);
	        }
	        else if(key instanceof Mob) {
	        	return super.remove(mobUniqueID((Mob)key));
	        }
	        return null;
	    }
	    
	    public RaceData remove(Mob mobkey) {
	        return this.remove(mobUniqueID(mobkey));
	    }
	    
	    @Override
	    public RaceData get(Object key) {
	        if (key instanceof Integer) {
	            RaceData data = super.get(key);
	            if (data != null) {
	                data.updateLastAccessTime(); // Update last access time when accessed
	            }
	            return data;
	        }
	        else if (key instanceof Mob){
	        	RaceData data = super.get(mobUniqueID((Mob)key));
	        	if (data != null) {
	                data.updateLastAccessTime(); // Update last access time when accessed
	            }
	        	return data;
	        }
	        return null;
	    }
	    
	    public RaceData get(Mob mobkey) {	       
	        return super.get(mobUniqueID(mobkey));
	    }
	    
	    
	 // Periodically cleanup stale entries
	    private void startCleanupThread() {
	        Thread cleanupThread = new Thread(() -> {
	            while (true) {
	                try {
	                    Thread.sleep(CLEANUP_INTERVAL);
	                    cleanupOldEntries();
	                } catch (InterruptedException e) {
	                    Thread.currentThread().interrupt();
	                    break;
	                }
	            }
	        });
	        cleanupThread.setDaemon(true);  // Make sure the cleanup thread doesn't block app shutdown
	        cleanupThread.start();
	    }

	    // Cleanup method to remove entries older than the threshold
	    private void cleanupOldEntries() {
	        long currentTime = System.currentTimeMillis();
	        Iterator<Entry<Integer, RaceData>> iterator = entrySet().iterator();
	        while (iterator.hasNext()) {
	            Entry<Integer, RaceData> entry = iterator.next();
	            RaceData data = entry.getValue();
	            if (currentTime - data.getLastAccessTime() > CLEANUP_THRESHOLD) {
	                iterator.remove();  // Remove the entry if it's older than the threshold
	            }
	        }
	    }
	}
	
	

	
	

}