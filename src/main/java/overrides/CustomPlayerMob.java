package overrides;

import core.race.CustomHumanLook;
import extensions.RaceLook;

import necesse.engine.GameLog;
import necesse.engine.network.NetworkClient;
import necesse.engine.network.PacketReader;
import necesse.engine.network.PacketWriter;
import necesse.engine.network.packet.PacketPlayerAppearance;
import necesse.engine.save.LoadData;
import necesse.engine.save.SaveData;
import necesse.engine.util.GameRandom;
import necesse.entity.particle.FleshParticle;
import necesse.entity.particle.Particle.GType;
import necesse.gfx.GameSkin;

public class CustomPlayerMob extends necesse.entity.mobs.PlayerMob{
	
	public CustomPlayerMob(long tempNameIdentifier, NetworkClient networkClient) {
		super(tempNameIdentifier, networkClient);	
		this.secondType = "CUSTOM";		
		//this.look = new CustomHumanLook();		
		
		System.out.print("Entered modified PlayerMob class.");
		
	}
	
	@Override
	public void addSaveData(SaveData save) {
	    // Call the superclass method to retain the original functionality
	    super.addSaveData(save);

	    // Add custom logic to save the look field
	    if (this.look != null) {
	        SaveData lookData = new SaveData("LOOK");
	        this.look.addSaveData(lookData);
	        save.addSaveData(lookData);
	    }

	    // Optionally, you can modify other fields here if needed
	}


	@Override
	public void applyLoadData(LoadData save) {
	    // Call the superclass method to retain the original functionality
	    super.applyLoadData(save);

	    // Apply custom logic for the look field
	    LoadData lookData = save.getFirstLoadDataByName("LOOK");
	    if (lookData != null) {
	        this.look.applyLoadData(lookData);
	    } else {
	        GameLog.warn.println("Could not load player look data");
	    }
	}

	
	@Override
	public void addLoadedCharacterSaveData(SaveData save) {
	    // Call the superclass method to retain the original functionality
	    super.addLoadedCharacterSaveData(save);

	    // Add custom logic to save the look field
	    SaveData lookData = new SaveData("LOOK");
	    if (this.look != null) {
	        this.look.addSaveData(lookData);
	    }
	    save.addSaveData(lookData);

	    // Optionally, you can modify other fields here if needed
	}


	@Override
	public void applyLoadedCharacterLoadData(LoadData save) {
	    // Call the superclass method to retain the original functionality
	    super.applyLoadedCharacterLoadData(save);

	    // Apply custom logic for the look field
	    LoadData lookData = save.getFirstLoadDataByName("LOOK");
	    if (lookData != null) {
	        this.look.applyLoadData(lookData);
	    } else {
	        GameLog.warn.println("Could not load player look data");
	    }

	    // Optionally, you can modify other fields here if needed
	}


	@Override
	 public void setupLoadedCharacterPacket(PacketWriter writer) {
        // Call the super method to preserve original functionality
        super.setupLoadedCharacterPacket(writer);

        // Now modify this.look (which is in your subclass)
        if (this.look != null) {
            this.look.setupContentPacket(writer, true);  // Custom modification if needed
        }
	}

	@Override
	public void applyLoadedCharacterPacket(PacketReader reader) {
	    // Call the superclass method to retain the original functionality
	    super.applyLoadedCharacterPacket(reader);

	    // Apply custom logic for the look field (make sure it's applied)
	    this.look.applyContentPacket(reader);

	    // You can also apply any modifications to other fields if needed
	}

	public void applyAppearancePacket(PacketPlayerAppearance packet) {
		super.applyAppearancePacket(packet);
		this.look =  RaceLook.fromHumanLook(packet.look);
	}
	
	@Override
	public void applySpawnPacket(PacketReader reader) {
	    // Call the superclass method to retain the original functionality
	    super.applySpawnPacket(reader);

	    // Apply custom logic for the look field (make sure it's applied)
	    if (this.look != null) {
	        this.look.applyContentPacket(reader);
	    }
	}

	@Override
	public void setupSpawnPacket(PacketWriter writer) {
	    // Call the superclass method to retain the original functionality
	    super.setupSpawnPacket(writer);

	    // Apply custom logic for the look field (make sure it's applied)
	    if (this.look != null) {
	        this.look.setupContentPacket(writer, true);
	    }

	}
	
	@Override
	public void spawnDeathParticles(float knockbackX, float knockbackY) {
		GameSkin gameSkin = this.look.getGameSkin(false);

		for (int i = 0; i < 4; ++i) {
			this.getLevel().entityManager.addParticle(new FleshParticle(this.getLevel(), gameSkin.getBodyTexture(),
					GameRandom.globalRandom.nextInt(5), 8, 32, this.x, this.y, 10.0F, knockbackX, knockbackY),
					GType.IMPORTANT_COSMETIC);
		}

	}

}