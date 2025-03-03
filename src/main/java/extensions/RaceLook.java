package extensions;

import java.awt.Color;
import java.util.List;
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
import necesse.gfx.GameEyes;
import necesse.gfx.GameHair;
import necesse.gfx.GameSkin;
import necesse.gfx.HumanGender;
import necesse.gfx.HumanLook;
import necesse.gfx.gameTexture.GameTexture;
import overrides.CustomPlayerMob;
import core.gfx.GameParts;
import core.gfx.GamePartsLoader;
import core.race.CustomHumanLook;
import core.race.parts.BodyPart;
import core.race.parts.RaceLookParts;

public class RaceLook extends HumanLook {	
	
	public static Color DEFAULT_UNKNOWN_COLOR = new Color(64,64,64); // a lovely gray
	public static Function<Color, Color> DEFAULT_COLOR_LIMITER = (i) -> { return new Color(GameMath.limit(i.getRed(), 25, 225), GameMath.limit(i.getGreen(), 25, 225),
			GameMath.limit(i.getBlue(), 25, 225));};
			
	public Class<? extends FormNewPlayerRaceCustomizer> associatedCustomizerForm;
	public IDData idData = new IDData();
	public RaceLookParts partsList;
	protected GameRandom randomizer;
	// We are replacing hard coded definitions for part IDs with mutable, ordered maps.
	protected SortedMap<String, Byte> appearanceByteMap = new TreeMap<>();
	protected SortedMap<String, Color> appearanceColorMap = new TreeMap<>();
	
	public Function<Color, Color> colorLimiterFunction = DEFAULT_COLOR_LIMITER;
	
	protected String race_id;
	
	public static RaceLook getRaceLook(CustomPlayerMob _player) {	
		
	    if ("CUSTOM".equals(_player.secondType)) {  // Safe string comparison
	        if (_player.look instanceof RaceLook) {
	            return (RaceLook) _player.look;
	        } else {
	            System.err.println("Error: newPlayer.look is not an instance of RaceLook!");
	            return null;  // Handle invalid state gracefully
	        }
	    }
	    
	    RaceLook converted = racelookFromBase(_player.look);
	    if (converted == null) {
	        System.err.println("Error: Failed to convert HumanLook to RaceLook!");
	    }
	    return converted;
	}
	public static RaceLook racelookFromBase(HumanLook look) {	return RaceLook.fromHumanLook(look);}
	
	public RaceLook(String _race_id) {
		super();						
		this.race_id = _race_id;
	}

	public RaceLookParts getRaceParts() {
		return this.partsList;
	}
	public String getRaceID() {
		return this.race_id;
	}
	
	public static RaceLook fromHumanLook(HumanLook look) {
		return new RaceLook(CustomHumanLook.HUMAN_RACE_ID);
	}
	
	public RaceLook(RaceLook copy) {
		this.copy(copy);
	}

	public RaceLook(PacketReader pr) {	
		this.applyContentPacket(pr);
	}

	public RaceLook(RaceLook raceLook, HumanLook look) {
		this(raceLook);
		this.copyBase(look);
	}

	public RaceLook() {
		super();
	}

	public void copyBase(HumanLook look) {
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
		if (look.race_id != this.race_id) {
			throw new IllegalArgumentException(String.format("Error: When copying a race into an already existing instance, both ID's must match. Source race_id: %s, Target race id: %s", this.race_id, look.race_id));
		}
		this.appearanceByteMap.clear();
		this.appearanceColorMap.clear();
		this.appearanceByteMap.putAll(look.appearanceByteMap);
		this.appearanceColorMap.putAll(look.appearanceColorMap);
		this.race_id = look.race_id;
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
		this.setShirtColor(this.partsList.defaultColors()[0]);
		this.setShoesColor(this.partsList.defaultColors()[0]);		
	}

	public void randomizeLook() {
		this.randomizeLook(	this.appearanceByteMap.keySet().stream().map((k)->{return true;}).toList(),
							this.appearanceColorMap.keySet().stream().map((k)->{return true;}).toList());
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

	public int getRandomHairStyleBasedOnGender(GameRandom random, HumanGender gender) { 
		return getRandomByteFeature("HAIR_STYLE");			
	}

	public void setupContentPacket(PacketWriter writer, boolean includeColor) {
		super.setupContentPacket(writer, includeColor);
		
		writer.putNextString(race_id);
	    // Write a boolean flag indicating whether colors should be included in the packet
	    writer.putNextBoolean(includeColor);

	    // Write each entry in the appearanceByteMap (ordered by TreeMap)
	    // This ensures all byte-based appearance attributes (e.g., hair type, eye type) are sent in a consistent order
	    this.appearanceByteMap.forEach((key, value) -> {
	        writer.putNextByte(value);
	    });

	    // If includeColor is true, write each entry in the appearanceColorMap
	    // Each color is written as three unsigned bytes (Red, Green, Blue)
	    if (includeColor) {
	        this.appearanceColorMap.forEach((key, value) -> {
	            writer.putNextByteUnsigned(value.getRed());
	            writer.putNextByteUnsigned(value.getGreen());
	            writer.putNextByteUnsigned(value.getBlue());
	        });
	    }
	}

	public RaceLook applyContentPacket(PacketReader reader) {
		super.applyContentPacket(reader);
		this.race_id = reader.getNextString();		
	    boolean includesClothesColor = reader.getNextBoolean();

	    // Read appearance byte values into the map
	    this.appearanceByteMap.forEach((key, value) -> {
	        this.appearanceByteMap.put(key, reader.getNextByte());
	    });

	    // Read color values if included
	    if (includesClothesColor) {
	        this.appearanceColorMap.forEach((key, value) -> {
	            int red = reader.getNextByteUnsigned();
	            int green = reader.getNextByteUnsigned();
	            int blue = reader.getNextByteUnsigned();
	            this.appearanceColorMap.put(key, new Color(red, green, blue));
	        });
	    }

	    return this;
	}

	public void addSaveData(SaveData save) {		
		super.addSaveData(save);
		save.addUnsafeString("race_id", race_id);
	    // Save all byte-based appearance attributes
	    this.appearanceByteMap.forEach((key, value) -> {
	        save.addInt(key, value);
	    });

	    // Save all color-based appearance attributes
	    this.appearanceColorMap.forEach((key, value) -> {
	        save.addColor(key, value);
	    });
	    
	}

	public void applyLoadData(LoadData save) {
		super.applyLoadData(save);
		this.race_id = save.getUnsafeString("race_id");
	    // Load all byte-based appearance attributes
	    this.appearanceByteMap.forEach((key, value) -> {
	        this.appearanceByteMap.put(key, save.getByte(key, value));
	    });

	    // Load all color-based appearance attributes
	    this.appearanceColorMap.forEach((key, value) -> {
	        this.appearanceColorMap.put(key, save.getColor(key, value));
	    });	    
	}


	protected int appearanceByteGet(String f) {
		return this.appearanceByteMap.getOrDefault(f,(byte) 0);
	}
	
	protected int appearanceByteSet(String f, byte b) {
		if(this.appearanceByteMap.containsKey(f)) return this.appearanceByteMap.put(f, b);
		this.appearanceByteMap.put(f, b);
		return b;
	}
	
	protected Color appearanceColorGet(String f) {
		return this.appearanceColorMap.getOrDefault(f,DEFAULT_UNKNOWN_COLOR);
	}
	
	protected Color appearanceColorSet(String f, Color c) {
		return (Color)this.appearanceColorMap.put(f, c);
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
	
	public GameTexture getHairTexture() {	return GameHair.getHair(this.getHair()).getHairTexture(this.getHairColor());	}

	public GameTexture getBackHairTexture() {	return GameHair.getHair(this.getHair()).getBackHairTexture(this.getHairColor());	}

	public GameTexture getWigTexture() {	return GameHair.getHair(this.getHair()).getWigTexture(this.getHairColor());	}

	public GameTexture getFacialFeatureTexture() {	return GameHair.getFacialFeature(this.getFacialFeature()).getHairTexture(this.getHairColor());	}

	public GameTexture getBackFacialFeatureTexture() {	return GameHair.getFacialFeature(this.getFacialFeature()).getBackHairTexture(this.getHairColor());	}
	
	public GameSkin getGameSkin(boolean onlyHumanlike) {	return GameSkin.getSkin(this.getSkin(), onlyHumanlike);	}	
	
	/*public GameSkin getGameSkin(boolean onlyHumanlike) {	return super.getGameSkin(onlyHumanlike);	}	
	public GameTexture getHairTexture() 				{	return super.getHairTexture();				}
	public GameTexture getBackHairTexture() 			{	return super.getBackHairTexture();			}
	public GameTexture getWigTexture() 					{	return super.getWigTexture();				}
	public GameTexture getFacialFeatureTexture() 		{	return super.getFacialFeatureTexture();		}
	public GameTexture getBackFacialFeatureTexture() 	{	return super.getBackFacialFeatureTexture();	}*/
	public GameEyes getEyes() 							{	return super.getEyes();						}
	
	public static Color limitClothesColor(Color color) {
		return new Color(GameMath.limit(color.getRed(), 25, 225), GameMath.limit(color.getGreen(), 25, 225),
				GameMath.limit(color.getBlue(), 25, 225));
	}

	public void initParts() {	
		
	}

	public void onRaceRegistryClosed() {
		// TODO Auto-generated method stub
		
	}

	public GameMessage getRaceDisplayName() {
		return new LocalMessage("racemod.race", this.getRaceID());
	}

	


}