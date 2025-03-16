package core.race;

import java.awt.Color;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.function.Function;

import necesse.engine.localization.message.GameMessage;
import necesse.engine.localization.message.LocalMessage;
import necesse.engine.network.PacketReader;
import necesse.engine.network.PacketWriter;
import necesse.engine.registries.IDData;
import necesse.engine.save.LoadData;
import necesse.engine.save.SaveData;
import necesse.engine.util.GameMath;
import necesse.engine.util.GameRandom;
import necesse.entity.mobs.MaskShaderOptions;
import necesse.entity.mobs.Mob;
import necesse.gfx.GameEyes;
import necesse.gfx.GameHair;
import necesse.gfx.GameSkin;
import necesse.gfx.HumanGender;
import necesse.gfx.HumanLook;
import necesse.gfx.drawOptions.DrawOptions;
import necesse.gfx.drawOptions.human.HumanDrawOptions;
import necesse.gfx.gameTexture.GameTexture;
import necesse.gfx.res.ResourceEncoder;
import necesse.gfx.res.ResourceFile;
import net.bytebuddy.implementation.bind.annotation.Super;
import core.forms.FormNewPlayerRaceCustomizer;
import core.race.factory.RaceDataFactory;
import core.race.factory.RaceDataFactory.RaceData;
import core.race.parts.BodyPart;
import core.race.parts.HumanRaceParts;
import core.race.parts.RaceLookParts;
import core.registries.RaceRegistry;
import helpers.DebugHelper;
import helpers.DebugHelper.MESSAGE_TYPE;

public abstract class RaceLook extends HumanLook {	
	
	public static final String iconTexturePath = "race/";
	public static Color DEFAULT_UNKNOWN_COLOR = new Color(64,64,64); // a lovely gray
	
	protected GameRandom randomizer;
	public IDData idData = new IDData();
	public Function<Color, Color> colorLimiterFunction = DEFAULT_COLOR_LIMITER;
	
	public static Function<Color, Color> DEFAULT_COLOR_LIMITER = 
			(i) -> { return new Color(GameMath.limit(i.getRed(), 25, 225), GameMath.limit(i.getGreen(), 25, 225),GameMath.limit(i.getBlue(), 25, 225));};
			
	public Class<? extends FormNewPlayerRaceCustomizer> associatedCustomizerForm;
	
	protected SortedMap<String, Byte> appearanceByteMap = new TreeMap<>();
	protected SortedMap<String, Color> appearanceColorMap = new TreeMap<>();
	
	protected RaceLookParts partsList;
	
	public RaceLook(String _race_id) {
		super();			
	}
	
	public RaceLook(RaceLook copy) {
		this.copy(copy);
	}
	
	public RaceLook(PacketReader pr) {	
		this.applyContentPacket(pr);
	}
	
	public RaceLookParts getRaceParts() {
		return this.partsList;
	}
	
	public RaceLook(RaceLook raceLook, HumanLook look) {
		this(raceLook);
		this.copyBase(look);
		
	}

	public RaceLook() {
		super();
	}
	
	public static RaceLook fromHumanLook(HumanLook look, Class<? extends RaceLook> fallbackClass) {
		try {
			RaceLook ra = look instanceof RaceLook ? (RaceLook)look : fallbackClass.getConstructor(boolean.class).newInstance(true);
			ra.copyBase(look);
			return ra;
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
				| NoSuchMethodException | SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return new CustomHumanLook(true);
		}
	}
	
	public static RaceLook fromRaceLook(RaceLook startingRace) {
		try {
			RaceLook ra = startingRace.getClass().getConstructor(boolean.class).newInstance(true);
			ra.copy(startingRace);
			return ra;
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
				| NoSuchMethodException | SecurityException e) {
			e.printStackTrace();		
				return null;
			
		}
	}
	
	public abstract String getRaceID();
	

	public void copyBase(HumanLook look) {
		if(look == null) return;
		this.setEyeColor(look.getEyeColor());
		this.setEyeType(look.getEyeType());
		this.setHair(look.getHair());
		this.setHairColor(look.getHairColor());
		this.setFacialFeature(look.getFacialFeature());
		this.setSkin(look.getSkin());
		this.setShirtColor(look.getShirtColor());
		this.setShoesColor(look.getShoesColor());		
	}

	public GameRandom getRandomizer() {
		if(this.randomizer == null) {
			this.randomizer = GameRandom.globalRandom;
		}
		return this.randomizer;
	}
	
	public void copy(RaceLook look) {
		if (look.getRaceID()!= this.getRaceID()) {
			throw new IllegalArgumentException(String.format("Error: When copying a race into an already existing instance, both race ID's must match. Source race_id: %s, Target race id: %s", this.getRaceID(), look.getRaceID()));
		}
		this.appearanceByteMap.clear();
		this.appearanceColorMap.clear();
		this.appearanceByteMap.putAll(look.appearanceByteMap);
		this.appearanceColorMap.putAll(look.appearanceColorMap);
	}
	
	public byte getRandomByteFeature(String key) {		
		   if(!this.getRaceParts().hasPart(key)) return 0;		   
		   int totalOptions = this.getRaceParts().getBodyPart(key).getTotalTextureOptions(); 
		   if(totalOptions == 0) return 0;
		   return (byte) GameRandom.globalRandom.getIntBetween(0, totalOptions-1); 	    
	}	
	
	public byte getRandomByteColorFeature(String key) {		
		   if(!this.getRaceParts().hasPart(key)) return 0;
		   BodyPart part =  this.getRaceParts().getBodyPart(key);
		   if(!part.isHasColor()) return 0; // has no hardcoded color
		   int totalOptions = part.getTotalColorOptions(); 
		   if (totalOptions == 0) return 0;
		   return (byte) GameRandom.globalRandom.getIntBetween(0, totalOptions-1); 	    
	}	
	
	//int totalOptions = this.getRaceParts().getBodyPart(key).getTotalColorOptions(); 		
	public Color getRandomColor() {
		GameRandom rand = this.getRandomizer();
		return colorLimiterFunction.apply(new Color(rand.getIntBetween(50, 200), rand.getIntBetween(50, 200),	rand.getIntBetween(50, 200)));
	}
	
	public Color getRandomColor(String key) {
		GameRandom rand = this.getRandomizer();
		return colorLimiterFunction.apply(new Color(rand.getIntBetween(50, 200), rand.getIntBetween(50, 200),	rand.getIntBetween(50, 200)));
	}

	public byte getRandomSkinColor() 		{	return getRandomByteFeature("SKIN_COLOR");	}

	public byte getRandomHairColor() 		{	return getRandomByteFeature("HAIR_COLOR");		}

	public byte getRandomEyeColor() 		{	return getRandomByteFeature("EYE_COLOR");		}

	public byte getRandomFacialFeature() 	{	return getRandomByteFeature("FACIAL_HAIR");		}

	public byte getRandomEyeType() 			{	return getRandomByteFeature("EYE_TYPE");		}

	public byte getRandomHairStyle() 		{	return getRandomByteFeature("HAIR_STYLE");		}
	
	public Color getRandomShoeColor() {
		int opts = this.getRaceParts().defaultColors().length;		
		return this.getRaceParts().defaultColors()[this.getRandomizer().getIntBetween(0, opts-1)];
	}

	public Color getRandomShirtColor() 		{	return getRandomShoeColor();	}
	
	public void resetCustomDefault() {
		// override me!
	}
	
	public void resetBaseDefault() {
		this.setEyeColor(0);
		this.setEyeType(0);
		this.setHair(0);
		this.setHairColor(0);
		this.setFacialFeature(0);
		this.setSkin(0);
		this.setShirtColor(this.getRaceParts().defaultColors()[0]);
		this.setShoesColor(this.getRaceParts().defaultColors()[0]);		
	}
	
	public void randomizeLook(GameRandom random, boolean onlyHumanLike) {
		HumanGender gender = (HumanGender) random.getOneOf(new HumanGender[]{HumanGender.MALE, HumanGender.FEMALE, HumanGender.NEUTRAL});
		this.randomizeLook(random, onlyHumanLike, gender, true, true, true, true);
	}	
	
	public void randomizeLook(boolean onlyHumanLike, boolean randomFacialFeature, boolean randomSkin,
			boolean changeEyeType, boolean randomEyeColor) {
		HumanGender gender = (HumanGender) GameRandom.globalRandom
				.getOneOf(new HumanGender[]{HumanGender.MALE, HumanGender.FEMALE, HumanGender.NEUTRAL});
		
		this.randomizeLook(GameRandom.globalRandom, onlyHumanLike, gender, randomSkin, changeEyeType, randomEyeColor,
				randomFacialFeature);
	}
	
	public void randomizeLook(GameRandom random, boolean onlyHumanLike, HumanGender gender, boolean randomSkin,
			boolean changeEyeType, boolean randomEyeColor, boolean randomFacialFeature) {
		
		if(!random.equals(this.randomizer)) {this.randomizer = random;}
		if (changeEyeType) {
			this.setEyeType(this.getRandomEyeType());
		}

		this.setHair(this.getRandomHairStyle());
		if (gender == HumanGender.MALE) {
			if (randomFacialFeature) {
				this.setFacialFeature(this.getRandomFacialFeature());
			}
		} else {
			this.setFacialFeature(0);
		}

		this.setHairColor(this.getRandomHairColor());
		if (randomSkin) {
			this.setSkin(this.getRandomSkinColor());
		}

		if (randomEyeColor) {
			this.setEyeColor(this.getRandomEyeColor());
		}

		this.setShirtColor(this.getRandomShirtColor());
		this.setShoesColor(this.getRandomShoeColor());
	}
	
	public static <T> void applyRandomFeatures(SortedMap<String,T> src, Function<String, T> getter, List<Boolean> flags) {
		Set<String> keys = src.keySet();
		for(int i = 0; i < keys.size(); i++) {
			String k = (keys.toArray(new String[0]))[i];
			try {
				boolean flag = flags.get(i);
				if(flag) src.put(k, getter.apply(k));
			} catch (Exception e) {
				continue;
			}			
		}
	}
	
	public void randomizeLook(List<Boolean> byteFlags, List<Boolean> colorFlags) {
		applyRandomFeatures(this.appearanceByteMap, this::getRandomByteFeature, byteFlags);
		applyRandomFeatures(this.appearanceColorMap, this::getRandomColor, colorFlags);
	}

	@Override
	public int getRandomHairStyleBasedOnGender(GameRandom random, HumanGender gender) { 
		return getRandomByteFeature("HAIR_STYLE");			
	}

	@Override
	public void setupContentPacket(PacketWriter writer, boolean includeColor) {
		
		DebugHelper.handleDebugMessage("setupContentPacket called.", 50, MESSAGE_TYPE.DEBUG);
		writer.putNextString(this.getRaceID());
		writer.putNextBoolean(includeColor);
		writer.putNextByte((byte)super.getHair());
		writer.putNextByte((byte)super.getFacialFeature());
		writer.putNextByte((byte)super.getHairColor());
		writer.putNextByte((byte)super.getSkin());
		writer.putNextByte((byte)super.getEyeType());
		writer.putNextByte((byte)super.getEyeColor());
		
		
		if (includeColor) {
			writer.putNextByteUnsigned(super.getShirtColor().getRed());
			writer.putNextByteUnsigned(super.getShirtColor().getGreen());
			writer.putNextByteUnsigned(super.getShirtColor().getBlue());
			writer.putNextByteUnsigned(super.getShoesColor().getRed());
			writer.putNextByteUnsigned(super.getShoesColor().getGreen());
			writer.putNextByteUnsigned(super.getShoesColor().getBlue());
		}
		
		writer.putNextInt(appearanceByteMap.size()); // Store the number of entries
		this.appearanceByteMap.forEach((key, value) -> {
		    writer.putNextString(key); // Write key
		    writer.putNextByte(value); // Write value
		});
	    
		if (includeColor) {
		    writer.putNextInt(appearanceColorMap.size()); // Store size
		    this.appearanceColorMap.forEach((key, value) -> {
		        writer.putNextString(key);
		        writer.putNextByteUnsigned(value.getRed());
		        writer.putNextByteUnsigned(value.getGreen());
		        writer.putNextByteUnsigned(value.getBlue());
		    });
		}

	}
	
	

	@Override
	public RaceLook applyContentPacket(PacketReader reader) {
		
		// Read race ID
		
	    String race_id = reader.getNextString();

	    if(!race_id.equals(this.getRaceID())) {
	    	DebugHelper.handleFormattedDebugMessage("Received bad content packet. Race %s does not equal the race applied to, %s.", 0, MESSAGE_TYPE.ERROR, new Object[] {race_id, this.getRaceID()});
	    	Thread.dumpStack();
	    }
	    boolean includesClothesColor = reader.getNextBoolean(); // Read first

	    // Read base appearance attributes
	    super.setHair(reader.getNextByte());
	    super.setFacialFeature(reader.getNextByte());
	    super.setHairColor(reader.getNextByte());
	    super.setSkin(reader.getNextByte());
	    super.setEyeType(reader.getNextByte());
	    super.setEyeColor(reader.getNextByte());

	    // Read clothing colors if present
	    if (includesClothesColor) {
	        int shirtRed = reader.getNextByteUnsigned();
	        int shirtGreen = reader.getNextByteUnsigned();
	        int shirtBlue = reader.getNextByteUnsigned();
	        super.setShirtColor(new Color(shirtRed, shirtGreen, shirtBlue));
	        

	        int shoesRed = reader.getNextByteUnsigned();
	        int shoesGreen = reader.getNextByteUnsigned();
	        int shoesBlue = reader.getNextByteUnsigned();
	        super.setShoesColor(new Color(shoesRed, shoesGreen, shoesBlue));
	    }  

	    // Read appearance bytes
	    appearanceByteMap.clear(); // Ensure no old data remains
	    int byteMapSize = reader.getNextInt(); // Read the actual number of entries
	    for (int i = 0; i < byteMapSize; i++) {
	        String key = reader.getNextString();
	        byte value = reader.getNextByte();
	        appearanceByteMap.put(key, value);
	    }

	    
	    if (includesClothesColor) {
	        appearanceColorMap.clear();
	        int colorMapSize = reader.getNextInt(); // Read the actual number of entries
	        for (int i = 0; i < colorMapSize; i++) {
	            String key = reader.getNextString();
	            int red = reader.getNextByteUnsigned();
	            int green = reader.getNextByteUnsigned();
	            int blue = reader.getNextByteUnsigned();
	            appearanceColorMap.put(key, new Color(red, green, blue));
	        }
	    }


	    return this;
	}


	@Override
	public void addSaveData(SaveData save) {			
		super.addSaveData(save);
		
			
		save.addSafeString("race_id", this.getRaceID());
	    // Save all byte-based appearance attributes
	    this.appearanceByteMap.forEach((key, value) -> {
	    	save.addByte(key, value);
	    });

	    // Save all color-based appearance attributes
	    this.appearanceColorMap.forEach((key, value) -> {
	    	save.addColor(key, value);
	    });	    
	 
	   // save.addSaveData(lkd);
	}

	public void applyLoadData(LoadData save) {
		
		super.applyLoadData(save);
		
		LoadData lkd = save;//.getFirstLoadDataByName("LOOK");	
		String race_id = lkd.getSafeString("race_id", null);
		
		if(race_id == null) {
			DebugHelper.handleDebugMessage("Error reading race of load data at applyLoadData", 25);
			return;
		}
		
		 if(!race_id.equals(this.getRaceID())) {
		    DebugHelper.handleFormattedDebugMessage("Load data mismatched race. Race %s does not equal the race applied to, %s. Transforming.",
		    		0, MESSAGE_TYPE.WARNING, new Object[] {race_id, this.getRaceID()});
		}		 
		
	    // Load all byte-based appearance attributes
	    this.appearanceByteMap.forEach((key, value) -> {
	        this.appearanceByteMap.put(key, save.getByte(key, value));
	    });

	    // Load all color-based appearance attributes
	    this.appearanceColorMap.forEach((key, value) -> {
	        this.appearanceColorMap.put(key, save.getColor(key, value));
	    });	    
	}
	
	public static RaceLook raceFromContentPacker(PacketReader reader, RaceLook fallback) {	
		
		PacketReader cpy = new PacketReader(reader);		
		String raceString = cpy.getNextString();
		
		if(raceString == null) {
			DebugHelper.handleDebugMessage("Error reading race of content packet at raceFromContentPacker", 25, MESSAGE_TYPE.ERROR);
			return fallback;
		}	
	
		DebugHelper.handleFormattedDebugMessage("Interpreting race from packet at raceFromContentPacker. Found: %s", 25, MESSAGE_TYPE.DEBUG, new Object[] {raceString});
		
		if(RaceRegistry.getRaceID(raceString) != -1) {
			RaceLook r;
			try {
				r = RaceRegistry.getRace(raceString).getClass().getConstructor(boolean.class).newInstance(true);
			} catch (InstantiationException | IllegalAccessException | IllegalArgumentException
					| InvocationTargetException | NoSuchMethodException | SecurityException e) {
				DebugHelper.handleDebugMessage(String.format("Error generating new instance of race %s in raceFromContentPacker", raceString), 25, MESSAGE_TYPE.ERROR);
				e.printStackTrace();
				return fallback.applyContentPacket(reader);
			}
			r.applyContentPacket(reader);
		    return r;
		}
		Thread.dumpStack();
		DebugHelper.handleDebugMessage(String.format("Failed to load race %s in raceFromContentPacker. returning fallback racelook.", raceString), 25, MESSAGE_TYPE.WARNING);
		return fallback.applyContentPacket(reader);
	}
	
	public static RaceLook raceFromLoadData(LoadData save, RaceLook fallback) {	
		
		LoadData lkd = save.getFirstLoadDataByName("LOOK");		
		
		if(lkd == null) {
			DebugHelper.handleDebugMessage("Error loading racein raceFromLoadData: null value from save section LOOK", 25, MESSAGE_TYPE.ERROR);
			return fallback;
		}
		String _race_id = lkd.getSafeString("race_id", null);		
		DebugHelper.handleFormattedDebugMessage("Interpreting race from LoadData at raceFromLoadData. Found: %s", 25, MESSAGE_TYPE.DEBUG, new Object[] {_race_id});

		if(_race_id != null && RaceRegistry.getRaceID(_race_id) != -1) {
			RaceLook r;
			
			try {
				r = RaceRegistry.getRace(_race_id).getClass().getConstructor(boolean.class).newInstance(true);
			} catch (InstantiationException | IllegalAccessException | IllegalArgumentException
					| InvocationTargetException | NoSuchMethodException | SecurityException e) {
				DebugHelper.handleDebugMessage(String.format("Error generating new instance of race %s in raceFromLoadData", _race_id), 25, MESSAGE_TYPE.ERROR);
				e.printStackTrace();
				return fallback;
			}
		    r.appearanceByteMap.forEach((key, value) -> {
		        r.appearanceByteMap.put(key, lkd.getByte(key, value));
		    });
	
		    // Load all color-based appearance attributes
		    r.appearanceColorMap.forEach((key, value) -> {
		        r.appearanceColorMap.put(key, lkd.getColor(key, value));
		    });	    
		    return r;
		}
		Thread.dumpStack();
		DebugHelper.handleDebugMessage(String.format("Failed to load race %s in raceFromLoadData. returning fallback racelook.", _race_id), 25, MESSAGE_TYPE.WARNING);
		return fallback;
	}
	
	public int appearanceByteGet(String f) {
		return this.appearanceByteMap.getOrDefault(f,(byte) 0);
	}
	
	public int appearanceByteSet(String f, byte b) {
		if(this.appearanceByteMap.containsKey(f)) return this.appearanceByteMap.put(f, b);
		this.appearanceByteMap.put(f, b);
		
		return b;
	}
	
	protected Color appearanceColorGet(String f) {
		return this.appearanceColorMap.getOrDefault(f,DEFAULT_UNKNOWN_COLOR);
	}
	
	protected Color appearanceColorSet(String f, Color c) {
		Color res = (Color)this.appearanceColorMap.put(f, c);
	
		return res;
	}
	
	public void setHair(int value) {
		super.setHair(value);
		this.appearanceByteSet("HAIR_STYLE", (byte) value);
	}

	public void setFacialFeature(int hair) {
		super.setFacialFeature(hair);
		this.appearanceByteSet("FACIAL_HAIR", (byte) hair);
	}

	public void setHairColor(int hairColor) {
		super.setHairColor(hairColor);
		this.appearanceByteSet("HAIR_COLOR", (byte) hairColor);
	}

	public void setSkin(int skin) {
		super.setSkin(skin);
		this.appearanceByteSet("SKIN_COLOR", (byte) skin);
	}

	public void setEyeType(int eyeType) {
		super.setEyeType(eyeType);
		this.appearanceByteSet("EYE_TYPE", (byte) eyeType);
	}

	public void setEyeColor(int eyeColor) {
		super.setEyeColor(eyeColor);
		this.appearanceByteSet("EYE_COLOR", (byte) eyeColor);
	}

	public void setShirtColor(Color shirtColor) {
		super.setShirtColor(shirtColor);
		this.appearanceColorMap.put("SHIRT_COLOR", shirtColor);
	}

	public void setShoesColor(Color shoesColor) {
		super.setShoesColor(shoesColor);
		this.appearanceColorMap.put("SHOE_COLOR", shoesColor);
	}
	
	
	public int getHair() 			{	return appearanceByteGet("HAIR_STYLE");		}

	public int getFacialFeature() 	{	return appearanceByteGet("FACIAL_HAIR");	}

	public int getHairColor()		{	return appearanceByteGet("HAIR_COLOR");		}

	public int getSkin() 			{	return appearanceByteGet("SKIN_COLOR");		}

	public int getEyeType() 		{	return appearanceByteGet("EYE_TYPE");		}

	public int getEyeColor()		{	return appearanceByteGet("EYE_COLOR");		}

	public Color getShirtColor() 	{	return appearanceColorGet("SHIRT_COLOR");	}

	public Color getShoesColor() 	{	return appearanceColorGet("SHOE_COLOR");	}
	
	public GameTexture getHairTexture() 				{	return GameHair.getHair(this.getHair()).getHairTexture(this.getHairColor());	}

	public GameTexture getBackHairTexture() 			{	return GameHair.getHair(this.getHair()).getBackHairTexture(this.getHairColor());	}

	public GameTexture getWigTexture() 					{	return GameHair.getHair(this.getHair()).getWigTexture(this.getHairColor());	}

	public GameTexture getFacialFeatureTexture() 		{	return GameHair.getFacialFeature(this.getFacialFeature()).getHairTexture(this.getHairColor());	}

	public GameTexture getBackFacialFeatureTexture() 	{	return GameHair.getFacialFeature(this.getFacialFeature()).getBackHairTexture(this.getHairColor());	}
	
	public GameSkin getGameSkin(boolean onlyHumanlike) 	{	return GameSkin.getSkin(this.getSkin(), onlyHumanlike);	}	

	public String getCustomizerIconPath() {		
		String ra_id = this.getRaceID();
		if(ra_id == null) return null;
		String target_texture = iconTexturePath+"icon_"+ra_id.toLowerCase();
		return target_texture;		
	}
		
	public static Color limitClothesColor(Color color) {
		return new Color(GameMath.limit(color.getRed(), 25, 225), GameMath.limit(color.getGreen(), 25, 225),
				GameMath.limit(color.getBlue(), 25, 225));
	}

	public void initParts() {	
		
	}

	public void onRaceRegistryClosed() {
		
	}

	public GameMessage getRaceDisplayName() {
		return new LocalMessage("racemod.race", this.getRaceID());
	}

	public abstract HumanDrawOptions modifyHumanDrawOptions(HumanDrawOptions drawOptions, MaskShaderOptions mask);

	public abstract Class<? extends FormNewPlayerRaceCustomizer> getAssociatedCustomizerForm();



	

	

}