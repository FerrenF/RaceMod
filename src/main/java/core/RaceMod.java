package core;


import java.lang.instrument.Instrumentation;
import java.util.Optional;

import core.network.CustomPacketConnectApproved;
import core.race.CustomHumanLook;
import core.race.RaceLook;
import core.race.TestFurryRaceLook;
import core.race.factory.RaceDataFactory;
import core.registries.RaceRegistry;
import extensions.CustomRaceStylistContainer;
import extensions.CustomRaceStylistContainerForm;
import helpers.DebugHelper;
import helpers.DebugHelper.MESSAGE_TYPE;
import helpers.SettingsHelper;
import necesse.engine.GameLoadingScreen;
import necesse.engine.localization.Localization;
import necesse.engine.modLoader.LoadedMod;
import necesse.engine.modLoader.annotations.ModEntry;
import necesse.engine.network.PacketReader;
import necesse.engine.network.client.loading.ClientLoadingSelectCharacter;
import necesse.engine.network.networkInfo.NetworkInfo;
import necesse.engine.network.server.Server;
import necesse.engine.registries.ContainerRegistry;
import necesse.engine.registries.PacketRegistry;
import necesse.entity.mobs.friendly.human.humanShop.StylistHumanMob;
import necesse.gfx.PlayerSprite;
import necesse.gfx.forms.presets.containerComponent.mob.StylistContainerForm;
import necesse.gfx.res.ResourceEncoder;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.agent.ByteBuddyAgent;
import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.dynamic.loading.ClassReloadingStrategy;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.matcher.ElementMatchers;
import patches.ClientLoadingCharacterSelectSubmitConnectAcceptedPacketPatch;
import patches.PlayerSpriteHooks;
import patches.ServerAddClientPatch;
import patches.getStylistOpenShopPacketPatch;

	
@ModEntry
public class RaceMod {
	public static int CUSTOM_STYLIST_CONTAINER;
	public static Instrumentation byteBuddyInst;
	public static SettingsHelper settings = new SettingsHelper();
	public void preInit() {
		SettingsHelper.initialize();
		DebugHelper.initialize();
		DebugHelper.handleDebugMessage("Race mod initialized. Oh boy let's rebuild some classes...");
    	byteBuddyInst = ByteBuddyAgent.install();
    	
    	
    	deployPreInitHook();
    	
    	RaceDataFactory.initialize(byteBuddyInst);
    	LoadedMod l = LoadedMod.getRunningMod();
    	ResourceEncoder.addModResources(l);
    	
		
	
		  DebugHelper.handleDebugMessage("Successfully replaced NewCharacterForm. Good luck everybody!", 40, MESSAGE_TYPE.DEBUG); 
	}
	
	private void deployPreInitHook() {
		
	
		  new ByteBuddy()
		      .redefine(overrides.NewCharacterForm.class) 
		      .name("necesse.gfx.forms.presets.NewCharacterForm") 
		      .make()
		      .load(ClassLoader.getSystemClassLoader(), ClassReloadingStrategy.fromInstalledAgent());		  
		  
		  DebugHelper.handleDebugMessage("Successfully replaced NewCharacterForm. Good luck everybody!", 40, MESSAGE_TYPE.DEBUG);
		  
		  
		  new ByteBuddy()
          .redefine(Server.class)
          .method(ElementMatchers.named("addClient")
          .and(ElementMatchers.takesArguments(NetworkInfo.class, long.class, String.class, boolean.class, boolean.class))
          ).intercept(MethodDelegation.to(ServerAddClientPatch.class)) 
          .make() 
          .load(ClassLoader.getSystemClassLoader(), ClassReloadingStrategy.fromInstalledAgent()); 		  
		  
		  DebugHelper.handleDebugMessage("Deployed ServerAddClientPatch. Good luck everybody!", 40, MESSAGE_TYPE.DEBUG);
		  
		  new ByteBuddy()
		  .redefine(ClientLoadingSelectCharacter.class)
          .method(ElementMatchers.named("submitConnectAccepted"))
          .intercept(MethodDelegation.to(ClientLoadingCharacterSelectSubmitConnectAcceptedPacketPatch.class)) 
          .make() 
          .load(ClassLoader.getSystemClassLoader(), ClassReloadingStrategy.fromInstalledAgent()); 
		  
		  DebugHelper.handleDebugMessage("Deployed ClientLoadingCharacterSelectSubmitConnectAcceptedPacketPatch. Good luck everybody!", 40, MESSAGE_TYPE.DEBUG);
		  
		  
		  new ByteBuddy()
		  .redefine(StylistHumanMob.class)
          .method(ElementMatchers.named("getOpenShopPacket"))
          .intercept(Advice.to(getStylistOpenShopPacketPatch.class)) 
          .make() 
          .load(ClassLoader.getSystemClassLoader(), ClassReloadingStrategy.fromInstalledAgent()); 
		  
		  DebugHelper.handleDebugMessage("Deployed getStylistOpenShopPacketPatch. Good luck everybody!", 40, MESSAGE_TYPE.DEBUG);

	}

	public void initResources() {
		
	}
	
    public void init() {    	
    	
    	GameLoadingScreen.drawLoadingString(Localization.translate("racemodui", "loading"));   
    	
    	DebugHelper.handleDebugMessage("Deploying hooks...");
		replaceFormNewCharacterForms();
		replacePlayerSprite();          
        replaceFormNewPlayerPreset();
        
        
    	DebugHelper.handleDebugMessage("Registering races...");
		RaceRegistry.registerRace(CustomHumanLook.HUMAN_RACE_ID, new CustomHumanLook());
		RaceRegistry.registerRace(TestFurryRaceLook.TEST_FURRY_RACE_ID, new TestFurryRaceLook());
		DebugHelper.handleDebugMessage("Registering textures...");
		TestFurryRaceLook.loadRaceTextures();
		    
        replaceStylistForms();
    	DebugHelper.handleDebugMessage("Registering network utilities...");
		PacketRegistry.registerPacket(CustomPacketConnectApproved.class);
    }
    

	public void postInit() {   	
	
    	Optional<String> rlist = RaceRegistry.getRaces().stream().map(RaceLook::getRaceID).reduce( (r1, r2) -> {return r1+","+r2;});
    	DebugHelper.handleFormattedDebugMessage("%d races loaded: %s", 0, DebugHelper.MESSAGE_TYPE.INFO, new Object[] {RaceRegistry.getTotalRaces(),rlist.get()});
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
        
        	DebugHelper.handleDebugMessage("Successfully patched PlayerSprite methods. Good luck everybody!", 40, MESSAGE_TYPE.DEBUG);    
            
        } catch (Exception e) {
        	
            e.printStackTrace();
            
        }
        
    }
    
    public static void replaceStylistForms() {
        try {
        	/*  new ByteBuddy()
              .redefine(overrides.CustomRaceStyleForm.class) 
              .name("necesse.gfx.forms.presets.containerComponent.mob.PlayerStyleForm") // Force rename
              .make()
              .load(ClassLoader.getSystemClassLoader(), ClassReloadingStrategy.fromInstalledAgent());

        	  DebugHelper.handleDebugMessage("Successfully replaced PlayerStyleForm. Good luck everybody!", 40, MESSAGE_TYPE.DEBUG);     	*/
        	  
        	  CUSTOM_STYLIST_CONTAINER =  ContainerRegistry.registerMobContainer((client, uniqueSeed, mob, content) -> {
      			return new CustomRaceStylistContainerForm<CustomRaceStylistContainer>(client, new CustomRaceStylistContainer(client.getClient(), uniqueSeed,
    					(StylistHumanMob) mob, new PacketReader(content)));
    		}, (client, uniqueSeed, mob, content, serverObject) -> {
    			return new CustomRaceStylistContainer(client, uniqueSeed, (StylistHumanMob) mob, new PacketReader(content));
    		});
        	  
        	  /*new ByteBuddy()
              .redefine(overrides.CustomRaceStylistContainer.class) 
              .name("necesse.inventory.container.mob.StylistContainer") // Force rename
              .make()
              .load(ClassLoader.getSystemClassLoader(), ClassReloadingStrategy.fromInstalledAgent());

        	  DebugHelper.handleDebugMessage("Successfully replaced StylistContainer. Good luck everybody!", 40, MESSAGE_TYPE.DEBUG);     	*/ 
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
	public static void replaceFormNewPlayerPreset() {
        try {
        	 /* new ByteBuddy()
              .redefine(overrides.FormNewPlayerPreset.class)  // Your modified version
              .name("necesse.gfx.forms.components.componentPresets.FormNewPlayerPreset") // Force rename
              .make()
              .load(ClassLoader.getSystemClassLoader(), ClassReloadingStrategy.fromInstalledAgent());

        	  DebugHelper.handleDebugMessage("Successfully replaced FormNewPlayerPreset. Good luck everybody!", 40, MESSAGE_TYPE.DEBUG);     	*/  
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
	
    public static void replaceFormNewCharacterForms() {
        try {        	
        	
        	
          
        	 new ByteBuddy()
             .redefine(overrides.FormCharacterSaveComponent.class) // Your modified version
             .name("necesse.gfx.forms.components.FormCharacterSaveComponent") // Force rename
             .make()
             .load(ClassLoader.getSystemClassLoader(), ClassReloadingStrategy.fromInstalledAgent());

        	 DebugHelper.handleDebugMessage("Successfully replaced FormCharacterSaveComponent. Good luck everybody!", 40, MESSAGE_TYPE.DEBUG);    
      
            new ByteBuddy()
	            .redefine(overrides.DebugPlayerForm.class) // Your modified version
	            .name("necesse.gfx.forms.presets.debug.DebugPlayerForm") // Force rename
	            .make()
	            .load(ClassLoader.getSystemClassLoader(), ClassReloadingStrategy.fromInstalledAgent());

            DebugHelper.handleDebugMessage("Successfully replaced DebugPlayerForm. Good luck everybody!", 40, MESSAGE_TYPE.DEBUG); 
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }    
  
 
}
