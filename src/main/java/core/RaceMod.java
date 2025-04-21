package core;


import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.instrument.Instrumentation;
import java.util.Optional;
import java.util.jar.JarFile;

import core.containers.CustomRaceStylistContainer;
import core.forms.container.CustomRaceStylistContainerForm;
import core.gfx.texture.AsyncTextureLoader;
import core.gfx.texture.TextureManager;
import core.items.EmperorsNewShirt;
import core.items.EmperorsNewShoes;
import core.network.CustomPacketConnectApproved;
import core.network.CustomPacketPlayerAppearance;
import core.race.CustomHumanLook;
import core.race.NekoRaceLook;
import core.race.OrcRaceLook;
import core.race.RaceLook;
import core.race.TestFurryRaceLook;
import core.race.factory.RaceDataFactory;
import core.registries.RaceRegistry;
import helpers.DebugHelper;
import helpers.DebugHelper.MESSAGE_TYPE;
import helpers.SettingsHelper;
import necesse.engine.GameCache;
import necesse.engine.GameLoadingScreen;
import necesse.engine.GlobalData;
import necesse.engine.localization.Localization;
import necesse.engine.modLoader.LoadedMod;
import necesse.engine.modLoader.annotations.ModEntry;
import necesse.engine.network.PacketReader;
import necesse.engine.network.client.loading.ClientLoadingSelectCharacter;
import necesse.engine.network.server.ServerClient;
import necesse.engine.registries.ContainerRegistry;
import necesse.engine.registries.ItemRegistry;
import necesse.engine.registries.PacketRegistry;
import necesse.engine.save.CharacterSave;
import necesse.entity.mobs.friendly.human.humanShop.ShopContainerData;
import necesse.entity.mobs.friendly.human.humanShop.StylistHumanMob;
import necesse.gfx.PlayerSprite;
import necesse.gfx.forms.MainMenuFormManager;
import necesse.gfx.gameTexture.GameTexture;
import necesse.gfx.res.ResourceEncoder;
import necesse.level.maps.layers.settlement.SettlementLevelLayer;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.agent.ByteBuddyAgent;
import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.dynamic.loading.ClassReloadingStrategy;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.matcher.ElementMatchers;
import patches.MainMenuMessagePatch;
import patches.PlayerSpriteHooks;
import patches.CharacterSavesPathPatch;
import patches.GetStylistOpenShopPacketPatch;
import patches.debug.debugGNDItemPatch;
import patches.server.ClientLoadingCharacterSelectSubmitConnectAcceptedPacketPatch;
import patches.server.ClientLoadingSelectCharacterStartPatch;
import patches.server.ServerClientApplyAppearancePacketPatch;
import patches.server.ServerClientApplyLoadedCharacterPacketPatch;
import patches.server.ServerClientLoadClientLookPatch;
import patches.settlement.SettlementLevelLayerMethodPatches;
import versioning.*;
	
@ModEntry
public class RaceMod {
	
	public static JarFile modJar;
	public static final String NECESSE_VERSION_STRING = "0.32.1";
	public static String characterSavePath;
	public static int CUSTOM_STYLIST_CONTAINER;
	public static Instrumentation byteBuddyInst;
	public static SettingsHelper settings = new SettingsHelper();
	public static String VERSION_STRING = "0.1.21 ALPHA";
	public static boolean DUMP_CLASSES = false;
	public static boolean DEBUG_HOOKS = false;
	public static boolean NEEDS_VERSIONING = false;
	public static String OLD_VERSION_STRING;
	
	public static TextureManager raceTextureManager;
	public void preInit() {
		
		byteBuddyInst = ByteBuddyAgent.install();
			  
		SettingsHelper.initialize();
		
		DebugHelper.initialize();
		
		String last_version = SettingsHelper.getSettingsString("DATA", "last_version", "-1", true);
		if(!last_version.equals(VERSION_STRING)) {
			NEEDS_VERSIONING = !last_version.equals("-1"); // dont version if it's 'fresh'?
			OLD_VERSION_STRING = last_version;
			GameLoadingScreen.drawLoadingString(Localization.translate("racemodui", "versionchange"));   
			
			DebugHelper.handleDebugMessage("Last version used missing or changed. Erasing cache.", 5, MESSAGE_TYPE.DEBUG);
			String cacheLocation = GlobalData.appDataPath()+System.getProperty("file.separator")+"cache";			
			
			File c1 = new File(cacheLocation + System.getProperty("file.separator") + "player"+ System.getProperty("file.separator") );
			DebugHelper.handleDebugMessage("Checking for: "+c1.getAbsolutePath(), 5, MESSAGE_TYPE.DEBUG);
	
			if(c1.exists()) {
				deleteDirectory(c1);
				}
			
			File c2 = new File(cacheLocation + System.getProperty("file.separator") + "client"+ System.getProperty("file.separator") );
			DebugHelper.handleDebugMessage("Checking for: "+c2.getAbsolutePath(), 5, MESSAGE_TYPE.DEBUG);

			if(c2.exists()) {
				deleteDirectory(c2);
				}
			
			File c3 = new File(GameCache.cachePath() + "texCache.bin.cache");
			if(c3.exists()) {	c3.delete();	}
			
			File c4 = new File(GameCache.cachePath() + "texCache.idx.cache");
			if(c4.exists()) {	c4.delete();	}
			
			SettingsHelper.setSettingsString("DATA", "last_version", VERSION_STRING);			
		}
		
		
		
		DebugHelper.handleDebugMessage("Race mod beginning pre-initialization. Let's get some hooks into this bad boy.", 5, MESSAGE_TYPE.INFO);
		 
		if(DEBUG_HOOKS) {			  
			 new AgentBuilder.Default()
	            .type(ElementMatchers.named("necesse.engine.registries.GNDRegistry"))
	            .transform((builder, typeDescription, classLoader, module) ->
	                builder.visit(Advice.to(debugGNDItemPatch.class)
	                    .on(ElementMatchers.named("readGNDItem")))
	            )
	            .installOn(byteBuddyInst);
		  }
    	deployPreInitHook();   	

    	RaceDataFactory.initialize(byteBuddyInst);
    	
    	modJar = LoadedMod.getRunningMod().jarFile;    
	}
	
	public static void deleteDirectory(File directory) {
	    if (directory.exists()) {
	        File[] files = directory.listFiles();
	        if (files != null) {
	            for (File file : files) {
	                deleteDirectory(file); // Recursively delete files/folders
	            }
	        }
	        directory.delete(); 
	    }
	}
	
	
	private void deployPreInitHook() {	
		
		  new ByteBuddy()
		      .redefine(overrides.NewCharacterForm.class) 
		      .name("necesse.gfx.forms.presets.NewCharacterForm") 
		      .make()
		      .load(ClassLoader.getSystemClassLoader(), ClassReloadingStrategy.fromInstalledAgent());		  
		  
		  DebugHelper.handleDebugMessage("Successfully replaced NewCharacterForm. Good luck everybody!", 40, MESSAGE_TYPE.DEBUG);
		  		  		 
		  new ByteBuddy()
			  .redefine(ClientLoadingSelectCharacter.class)
	          .method(ElementMatchers.named("submitConnectAccepted"))
	          .intercept(MethodDelegation.to(ClientLoadingCharacterSelectSubmitConnectAcceptedPacketPatch.class)) 
	          .method(ElementMatchers.named("applyLoadedCharacterPacket"))
	          .intercept(MethodDelegation.to(ClientLoadingSelectCharacterStartPatch.class))
	          .make() 
	          .load(ClassLoader.getSystemClassLoader(), ClassReloadingStrategy.fromInstalledAgent()); 
		  
		  DebugHelper.handleDebugMessage("Deployed ClientLoadingCharacterSelectSubmitConnectAcceptedPacketPatch. Good luck everybody!", 40, MESSAGE_TYPE.DEBUG);
		  
		  new ByteBuddy()
			  .redefine(ServerClient.class)
	          .method(ElementMatchers.named("loadClientLook"))
	          .intercept(MethodDelegation.to(ServerClientLoadClientLookPatch.class)) 
	          .method(ElementMatchers.named("applyLoadedCharacterPacket"))
	          .intercept(MethodDelegation.to(ServerClientApplyLoadedCharacterPacketPatch.class))
	          .method(ElementMatchers.named("applyAppearancePacket"))
	          .intercept(MethodDelegation.to(ServerClientApplyAppearancePacketPatch.class))
	          .make() 
	          .load(ClassLoader.getSystemClassLoader(), ClassReloadingStrategy.fromInstalledAgent()); 
	  
		  DebugHelper.handleDebugMessage("Deployed ServerClientLoadClientLookPatch. Good luck everybody!", 40, MESSAGE_TYPE.DEBUG);
			 		  
		  new ByteBuddy()
			  .redefine(StylistHumanMob.class)
	          .method(ElementMatchers.named("getOpenShopPacket"))
	          .intercept(Advice.to(GetStylistOpenShopPacketPatch.class)) 
	          .make() 
	          .load(ClassLoader.getSystemClassLoader(), ClassReloadingStrategy.fromInstalledAgent()); 
		  
		  DebugHelper.handleDebugMessage("Deployed getStylistOpenShopPacketPatch. Good luck everybody!", 40, MESSAGE_TYPE.DEBUG);
		  
		
		  //HumanDrawOptionsArmorDrawOptionsAccessPatch
		  
		  // SettlementLevelLayer
		  
		  new ByteBuddy()
			  .redefine(SettlementLevelLayer.class)
	          .method(ElementMatchers.named("updateOwnerVariables") .and(ElementMatchers.takesArguments(ServerClient.class)))  
	          .intercept(MethodDelegation.to(SettlementLevelLayerMethodPatches.class)) 
	          .method(ElementMatchers.named("updateOwnerVariables") .and(ElementMatchers.takesNoArguments()))  
	          .intercept(MethodDelegation.to(SettlementLevelLayerMethodPatches.class)) 
	          .method(ElementMatchers.named("readBasicSettlementData"))
	          .intercept(MethodDelegation.to(SettlementLevelLayerMethodPatches.class))
	          .method(ElementMatchers.named("writeBasicSettlementData"))
	          .intercept(MethodDelegation.to(SettlementLevelLayerMethodPatches.class))
	          .method(ElementMatchers.named("getLook"))
	          .intercept(MethodDelegation.to(SettlementLevelLayerMethodPatches.class))
	          .make() 
	          .load(ClassLoader.getSystemClassLoader(), ClassReloadingStrategy.fromInstalledAgent()); 
  
	  DebugHelper.handleDebugMessage("Deployed SettlementLevelLayerMethodPatches. Good luck everybody!", 40, MESSAGE_TYPE.DEBUG);
	  
	//writeBasicSettlementData

	}
	public static GameTexture TEX_MASK_LEFT;
	public static GameTexture TEX_MASK_RIGHT;
	public static GameTexture TEX_DISABLE_PART;
	
	public void initResources() {
		TEX_MASK_LEFT = GameTexture.fromFile("player/mask_left");
		TEX_MASK_RIGHT = GameTexture.fromFile("player/mask_right");
		TEX_DISABLE_PART = GameTexture.fromFile("ui/primal/button_x");
	}
	
    public void init() {    	
    	
    	if(!GlobalData.isServer()) {        	
        	characterSavePath = SettingsHelper.getSettingsString("DATA", "save_path");
        	if(characterSavePath == null) characterSavePath = GlobalData.appDataPath()  + "saves"+System.getProperty("file.separator")+"characters"+System.getProperty("file.separator")+"racemod"+System.getProperty("file.separator");
        	interceptCharacterSavePath();
        	addMainMenuMessage();
    	}
    	if(NEEDS_VERSIONING) {
    		GameLoadingScreen.drawLoadingString(Localization.translate("racemodui", "versioning")); 
    		VersionUpgradeRunner.runUpgradeScripts(OLD_VERSION_STRING, VERSION_STRING);
    	}
    	GameLoadingScreen.drawLoadingString(Localization.translate("racemodui", "loading"));   
    	
    	DebugHelper.handleDebugMessage("Deploying hooks...");
		replaceFormNewCharacterForms();
		replacePlayerSprite();   
    	
		
        DebugHelper.handleDebugMessage("Registering containers...");
        registerContainers();
        
    	DebugHelper.handleDebugMessage("Registering races...");
		RaceRegistry.registerRace(CustomHumanLook.HUMAN_RACE_ID, new CustomHumanLook());
		RaceRegistry.registerRace(NekoRaceLook.NEKO_RACE_ID, new NekoRaceLook());
		RaceRegistry.registerRace(TestFurryRaceLook.TEST_FURRY_RACE_ID, new TestFurryRaceLook());
		RaceRegistry.registerRace(OrcRaceLook.ORC_RACE_ID, new OrcRaceLook());
		       
    	DebugHelper.handleDebugMessage("Registering network utilities...");
		PacketRegistry.registerPacket(CustomPacketConnectApproved.class);
		PacketRegistry.registerPacket(CustomPacketPlayerAppearance.class);
		
		DebugHelper.handleDebugMessage("Initializing texture manager.");
		raceTextureManager = new TextureManager("texCache", 500);
		raceTextureManager.init_cache();
		
		
		ItemRegistry.registerItem("emperorsnewshirt", new EmperorsNewShirt(0) , 50, true);
		ItemRegistry.registerItem("emperorsnewshoes", new EmperorsNewShoes(0) , 50, true);
    }
    
	private static void interceptCharacterSavePath() {
		
		File saveDirectory = new File(characterSavePath);
		DebugHelper.handleFormattedDebugMessage("Creating custom character save location at %s.", 40, MESSAGE_TYPE.DEBUG, new Object[] {characterSavePath});    
		saveDirectory.mkdirs();
		
		  try {	
	        	new ByteBuddy()
	            .redefine(CharacterSave.class)
	            .method(ElementMatchers.named("getCharacterSavesPath"))
	            .intercept(MethodDelegation.to(CharacterSavesPathPatch.class)) // Redirect to custom logic
	            .make()
	            .load(PlayerSprite.class.getClassLoader(), ClassReloadingStrategy.fromInstalledAgent());	        
	        	DebugHelper.handleDebugMessage("Successfully patched character save path. Good luck everybody!", 40, MESSAGE_TYPE.DEBUG);    
	            
	        } catch (Exception e) {	
	            e.printStackTrace();         
	        }
	}

	public void postInit() {   		
		
		DebugHelper.handleDebugMessage("Race mod initialized. That was easy!", 5, MESSAGE_TYPE.INFO);
    	Optional<String> rlist = RaceRegistry.getRaces().stream().map(RaceLook::getRaceID).reduce( (r1, r2) -> {return r1+","+r2;});
    	DebugHelper.handleFormattedDebugMessage("%d races loaded: %s", 0, DebugHelper.MESSAGE_TYPE.INFO,
    			new Object[] {RaceRegistry.getTotalRaces(),rlist.get()});

    	if(DUMP_CLASSES) {
    		String filePath = GlobalData.appDataPath()+"classlist.txt";
    		Class<?>[] loadedClasses = byteBuddyInst.getAllLoadedClasses();
    	    
	        try (FileWriter writer = new FileWriter(filePath)) {
	            for (Class<?> clazz : loadedClasses) {
	                writer.write(clazz.getName() + "\n");
	            }
	            System.out.println("Class dump saved to: " + filePath);
	        } catch (IOException e) {
	            e.printStackTrace();
	        }
    	}
	}
	
	public void dispose() {
		
		AsyncTextureLoader.shutdownThreads();
	}
	public static void addMainMenuMessage() {
	
		 new ByteBuddy()
			 .redefine(MainMenuFormManager.class)
	         .visit(Advice.to(MainMenuMessagePatch.class).on(ElementMatchers.named("setup")))
	         .make() 
	         .load(ClassLoader.getSystemClassLoader(), ClassReloadingStrategy.fromInstalledAgent()); 
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
    					(StylistHumanMob) mob, new PacketReader(content), (ShopContainerData) null));
    		}, (client, uniqueSeed, mob, content, serverObject) -> {
    			return new CustomRaceStylistContainer(client, uniqueSeed, (StylistHumanMob) mob, new PacketReader(content), (ShopContainerData) serverObject);
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
