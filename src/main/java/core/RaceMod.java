package core;


import java.lang.instrument.Instrumentation;
import java.util.Optional;
import core.race.CustomHumanLook;
import core.race.TestFurryRaceLook;
import core.registries.RaceRegistry;
import extensions.RaceLook;
import factory.RaceDataFactory;
import necesse.engine.GameLoadingScreen;
import necesse.engine.GameLog;
import necesse.engine.localization.Localization;
import necesse.engine.modLoader.LoadedMod;
import necesse.engine.modLoader.annotations.ModEntry;
import necesse.gfx.PlayerSprite;
import necesse.gfx.res.ResourceEncoder;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.agent.ByteBuddyAgent;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.dynamic.loading.ClassReloadingStrategy;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.matcher.ElementMatchers;
import patches.PlayerSpriteHooks;

	
@ModEntry
public class RaceMod {

	public static Instrumentation byteBuddyInst;
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
    	byteBuddyInst = ByteBuddyAgent.install();
    	
    	RaceDataFactory.initialize(byteBuddyInst);
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
		
		replacePlayerSprite();
           
       //replaceHumanDrawOptions();
	 //  interceptDrawOptions();
       replaceFormNewPlayerPreset();
       replaceFormNewCharacterForms();
   
    }
    

	public void postInit() {   	
    	
    	Optional<String> rlist = RaceRegistry.getRaces().stream().map(RaceLook::getRaceID).reduce( (r1, r2) -> {return r1+","+r2;});
    	String dbgRaceLoadedCnt = String.format("%d races loaded: %s",
    			RaceRegistry.getTotalRaces(),
    			rlist.get());
    	handleDebugMessage(dbgRaceLoadedCnt, 40);
    }
    
    private static void rebasePlayerMob() {
  
    	/* new ByteBuddy()
         .rebase(overrides.NetworkClient.class) // Your modified version
         .name("necesse.engine.network.NetworkClient") // Force rename
         .make()
         .load(ClassLoader.getSystemClassLoader(), ClassReloadingStrategy.fromInstalledAgent());*/
    	
        	 
    	

    	
        System.out.println("Successfully replaced PlayerMob instances. I sure hope it works.");

    }
       


    public static void replacePlayerSprite() {
    	
        try {
        	
        	new ByteBuddy()
            .redefine(PlayerSprite.class)
            .method(ElementMatchers.named("getIconDrawOptions"))
            .intercept(MethodDelegation.to(PlayerSpriteHooks.class)) // Redirect to custom logic
            .method(ElementMatchers.named("getDrawOptions"))
            .intercept(MethodDelegation.to(PlayerSpriteHooks.class)) // Redirect to custom logic
            .make()
            .load(PlayerSprite.class.getClassLoader(), ClassReloadingStrategy.fromInstalledAgent());

        System.out.println("Successfully patched PlayerSprite methods!");
            
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
        	/*new ByteBuddy()
            .redefine(Class.forName("necesse.gfx.forms.presets.CharacterSelectForm$1")) // Hook the anonymous inner class
            .method(ElementMatchers.named("onCreatePressed")) // Target the specific method
            .intercept(MethodDelegation.to(NewCharacterFormCreateButtonPressedHook.class)) // Use delegation
            .make()
            .load(ClassLoader.getSystemClassLoader(), ClassReloadingStrategy.fromInstalledAgent());*/

        	
        	 new ByteBuddy()
             .redefine(overrides.FormCharacterSaveComponent.class) // Your modified version
             .name("necesse.gfx.forms.components.FormCharacterSaveComponent") // Force rename
             .make()
             .load(ClassLoader.getSystemClassLoader(), ClassReloadingStrategy.fromInstalledAgent());

         System.out.println("Successfully replaced FormCharacterSaveComponent. I sure hope it works.");
         
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
