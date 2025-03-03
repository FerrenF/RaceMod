package core;



import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.Optional;

import core.race.CustomHumanLook;
import core.race.TestFurryRaceLook;
import core.registries.RaceRegistry;
import extensions.RaceLook;
import necesse.engine.GameLoadingScreen;
import necesse.engine.GameLog;
import necesse.engine.localization.Localization;
import necesse.engine.modLoader.LoadedMod;
import necesse.engine.modLoader.annotations.ModEntry;
import necesse.engine.network.NetworkClient;
import necesse.engine.network.server.Server;
import necesse.entity.mobs.PlayerMob;
import necesse.gfx.drawOptions.human.HumanDrawOptions;
import necesse.gfx.forms.components.componentPresets.FormNewPlayerPreset;
import necesse.gfx.res.ResourceEncoder;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.agent.ByteBuddyAgent;
import net.bytebuddy.asm.Advice.AllArguments;
import net.bytebuddy.asm.Advice.Origin;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.asm.AsmVisitorWrapper;
import net.bytebuddy.dynamic.loading.ClassReloadingStrategy;
//import overrides.FormNewPlayerPreset.CustomFormPlayerIcon;
import net.bytebuddy.implementation.FieldAccessor;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.implementation.bind.annotation.RuntimeType;
import net.bytebuddy.matcher.ElementMatchers;
import overrides.CustomPlayerMob;
import patches.GetArmorDrawOptionsInterceptor;

	
@ModEntry
public class RaceMod {

	public static int RACE_MOD_DEBUG_LEVEL = 75;
	public static int getDebugLevel() {
		return RACE_MOD_DEBUG_LEVEL;
	}
	
	public static void handleDebugMessage(String msg, int messageLevel) {
		if(messageLevel <= RACE_MOD_DEBUG_LEVEL) GameLog.out.println(msg);
	}
	
	public static void oops(String how) {
		GameLog.out.println(how);
	}
		
	public void preInit() {
		GameLog.out.println("Race mod initialized. Oh boy let's rebuild some classes...");
    	ByteBuddyAgent.install();
    	
    	LoadedMod l = LoadedMod.getRunningMod();
    	ResourceEncoder.addModResources(l);
		
	}
	
	public void initResources() {
		
	}
    public void init() {    	
    	
    	GameLoadingScreen.drawLoadingString(Localization.translate("racemodui", "loading"));
    	    	
    	handleDebugMessage("Registering races...", 0);
		RaceRegistry.registerRace(CustomHumanLook.HUMAN_RACE_ID, new CustomHumanLook());
		RaceRegistry.registerRace(TestFurryRaceLook.TEST_FURRY_RACE_ID, new TestFurryRaceLook());
		handleDebugMessage("Registering races textures...", 40);
		TestFurryRaceLook.loadRaceTextures();
       //replacePlayerSprite();
       //rebasePlayerMob();
      
       //replaceHumanDrawOptions();
	   interceptDrawOptions();
       replaceFormNewPlayerPreset();
       replaceFormNewCharacterForms();
      
    }
    
    private void interceptDrawOptions() {
    	  new ByteBuddy()
          .redefine(necesse.gfx.drawOptions.human.HumanDrawOptions.class)
          .visit(Advice.to(GetArmorDrawOptionsInterceptor.class)
              .on(ElementMatchers.named("getArmorDrawOptions")))
          .make()
          .load(ClassLoader.getSystemClassLoader(), ClassReloadingStrategy.fromInstalledAgent());
	}

	public void postInit() {   	
    	
    	Optional<String> rlist = RaceRegistry.getRaces().stream().map(RaceLook::getRaceID).reduce( (r1, r2) -> {return r1+","+r2;});
    	String dbgRaceLoadedCnt = String.format("%d races loaded: %s",
    			RaceRegistry.getTotalRaces(),
    			rlist.get());
    	handleDebugMessage(dbgRaceLoadedCnt, 40);
    }
    
    private static void rebasePlayerMob() {
    	
    	new ByteBuddy()
        .redefine(PlayerMob.class)
        .method(ElementMatchers.isConstructor())
        .intercept(MethodDelegation.to(ConstructorInterceptor.class))
        .make()
        .load(PlayerMob.class.getClassLoader(), ClassReloadingStrategy.fromInstalledAgent());

        System.out.println("Successfully subclassed PlayerMob. I sure hope it works.");

    }
    
    public static class ConstructorInterceptor {
        @RuntimeType
        public static Object intercept(@Origin Constructor<?> constructor, @AllArguments Object[] args) {
        	
        	if (args[0] instanceof Long && args[1] instanceof NetworkClient) {
                return new CustomPlayerMob((Long) args[0], (NetworkClient) args[1]);
            }
            throw new UnsupportedOperationException("Unknown PlayerMob constructor called");
            
        }
    }
    
    public static void replacePlayerSprite() {
    	
        try {
        	
        	  new ByteBuddy()
              .redefine(overrides.PlayerSprite.class)  // Your modified version
              .name("necesse.gfx.PlayerSprite") // Force rename
              .make()
              .load(ClassLoader.getSystemClassLoader(), ClassReloadingStrategy.fromInstalledAgent());

            System.out.println("Successfully replaced PlayerSprite. I sure hope it works.");
            
        } catch (Exception e) {
        	
            e.printStackTrace();
            
        }
        
    }
    
    
	public static void replaceFormNewPlayerPreset() {
        try {
        	  new ByteBuddy()
              .redefine(overrides.FormNewPlayerPreset.class)  // Your modified version
              .name("necesse.gfx.forms.components.componentPresets.FormNewPlayerPreset") // Force rename
              .make()
              .load(ClassLoader.getSystemClassLoader(), ClassReloadingStrategy.fromInstalledAgent());

            System.out.println("Successfully replaced FormNewPlayerPreset. I sure hope it works.");
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
	
    public static void replaceFormNewCharacterForms() {
        try {        	
        	
         
            new ByteBuddy()
                .redefine(overrides.NewCharacterForm.class) // Your modified version
                .name("necesse.gfx.forms.presets.NewCharacterForm") // Force rename
                .make()
                .load(ClassLoader.getSystemClassLoader(), ClassReloadingStrategy.fromInstalledAgent());

            System.out.println("Successfully replaced NewCharacterForm. I sure hope it works.");
            
            new ByteBuddy()
	            .redefine(overrides.DebugPlayerForm.class) // Your modified version
	            .name("necesse.gfx.forms.presets.debug.DebugPlayerForm") // Force rename
	            .make()
	            .load(ClassLoader.getSystemClassLoader(), ClassReloadingStrategy.fromInstalledAgent());

            System.out.println("Successfully replaced DebugPlayerForm. I sure hope it works.");
        
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public static void replaceHumanDrawOptions() {
    	
        try {               
        	new ByteBuddy()
                .redefine(overrides.HumanDrawOptions.class) // Your modified version
                .name("necesse.gfx.drawOptions.human.HumanDrawOptions") // Force rename
                .make()
                .load(ClassLoader.getSystemClassLoader(), ClassReloadingStrategy.fromInstalledAgent());

            System.out.println("Successfully replaced HumanDrawOptions. I sure hope it works.");            
              
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
 
}
