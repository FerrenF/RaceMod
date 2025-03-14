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
import necesse.gfx.res.ResourceEncoder;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.agent.ByteBuddyAgent;
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
		
		byteBuddyInst = ByteBuddyAgent.install();
		SettingsHelper.initialize();
		DebugHelper.initialize();
		DebugHelper.handleDebugMessage("Race mod beginning pre-initialization. Let's get some hooks into this bad boy.", 5, MESSAGE_TYPE.INFO);
 	
    	deployPreInitHook();
    	
    	RaceDataFactory.initialize(byteBuddyInst);
    	LoadedMod l = LoadedMod.getRunningMod();
    	
    	DebugHelper.handleDebugMessage("RaceMod looking for resources...");
    	ResourceEncoder.addModResources(l);	
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
        
        DebugHelper.handleDebugMessage("Registering containers...");
        registerContainers();
        
    	DebugHelper.handleDebugMessage("Registering races...");
		RaceRegistry.registerRace(CustomHumanLook.HUMAN_RACE_ID, new CustomHumanLook());
		RaceRegistry.registerRace(TestFurryRaceLook.TEST_FURRY_RACE_ID, new TestFurryRaceLook());
		
		DebugHelper.handleDebugMessage("Registering extra textures...");
		TestFurryRaceLook.loadRaceTextures();
		CustomHumanLook.loadRaceTextures();  
       
    	DebugHelper.handleDebugMessage("Registering network utilities...");
		PacketRegistry.registerPacket(CustomPacketConnectApproved.class);
    }
    

	public void postInit() {   		
		
		DebugHelper.handleDebugMessage("Race mod initialized. That was easy!", 5, MESSAGE_TYPE.INFO);
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
    
    public static void registerContainers() {
        try {
        	        	  
        	  CUSTOM_STYLIST_CONTAINER =  ContainerRegistry.registerMobContainer((client, uniqueSeed, mob, content) -> {
      			return new CustomRaceStylistContainerForm<CustomRaceStylistContainer>(client, new CustomRaceStylistContainer(client.getClient(), uniqueSeed,
    					(StylistHumanMob) mob, new PacketReader(content)));
    		}, (client, uniqueSeed, mob, content, serverObject) -> {
    			return new CustomRaceStylistContainer(client, uniqueSeed, (StylistHumanMob) mob, new PacketReader(content));
    		});
        	          	
            
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
