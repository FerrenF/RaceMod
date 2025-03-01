package core.race;

import java.awt.Color;
import java.util.List;
import java.util.function.Function;

import core.gfx.GameParts;
import core.gfx.GamePartsLoader;
import core.race.parts.BodyPart;
import core.race.parts.TestFurryRaceParts;
import extensions.RaceLook;
import extensions.TestFurryNewPlayerRaceCustomizer;
import necesse.engine.network.PacketReader;
import necesse.engine.util.GameMath;
import necesse.engine.util.GameRandom;
import necesse.entity.mobs.MaskShaderOptions;
import necesse.gfx.GameEyes;
import necesse.gfx.GameHair;
import necesse.gfx.GameSkin;
import necesse.gfx.HumanGender;
import necesse.gfx.HumanLook;
import necesse.gfx.drawOptions.DrawOptions;
import necesse.gfx.drawOptions.DrawOptionsList;
import necesse.gfx.gameTexture.GameTexture;
import necesse.level.maps.light.GameLight;

public class TestFurryRaceLook extends RaceLook {
	
	public static final String TEST_FURRY_RACE_ID = "testfurry";
	
	// the onlyHumanLike prameter is just a proxy.
	public TestFurryRaceLook(GameRandom random, boolean onlyHumanLike) {		
		this(true);
		this.randomizer = random;
		this.randomizeLook(random, onlyHumanLike);
	}
	
	public TestFurryRaceLook() {
		super(TEST_FURRY_RACE_ID);
		this.associatedCustomizerForm = TestFurryNewPlayerRaceCustomizer.class;
	}
	
	public TestFurryRaceLook(boolean init) {
		this();
		if(init) {
			this.partsList = new TestFurryRaceParts(init);
			this.resetCustomDefault();	
			this.initCustomParts();			
		}
	}
	
	public TestFurryRaceLook(int hair, int facialFeature, int hairColor, int skin, int eyeColor, int eyeType, Color shirtColor,
			Color shoesColor, int tail, int tailColor, int ears, int earsColor, int muzzle, int muzzleColor) {	
		
		this(true);
		// Base
		this.setHair(hair);
		this.setFacialFeature(facialFeature);
		this.setHairColor(hairColor);
		this.setSkin(skin);
		this.setEyeColor(eyeColor);
		this.setShirtColor(shirtColor);
		this.setShoesColor(shoesColor);
		this.setEyeType(eyeType);
		// Custom
		this.setTailStyle(tail);
		this.setTailColor(tailColor);
		this.setEarsStyle(ears);
		this.setEarsColor(earsColor);
		this.setMuzzleColor(muzzleColor);
		this.setMuzzleStyle(muzzle);
	}
	
	
	
	// Note: This does not set valid DEFAULT parts. Just makes sure the keys are there.
	public void initCustomParts() {		
		// not used in CustomHumanLook
	}
	
	public TestFurryRaceLook(HumanLook copy) {
		this(	copy.getHair(),
				copy.getFacialFeature(),
				copy.getHairColor(),
				copy.getSkin(),
				copy.getEyeColor(),
				copy.getEyeType(),
				copy.getShirtColor(),
				copy.getShoesColor(),
				0,
				0,
				0,
				0,
				0,
				0);	
	}	

	public void resetCustomDefault() {
		super.resetBaseDefault();
		this.setTailColor(0);
		this.setTailStyle(0);
		this.setEarsColor(0);
		this.setEarsStyle(0);
		this.setMuzzleColor(0);
		this.setMuzzleStyle(0);		
	}
	
	public TestFurryRaceLook(TestFurryRaceLook copy) {
		this(copy.getHair(), copy.getFacialFeature(), copy.getHairColor(), copy.getSkin(), copy.getEyeColor(), copy.getEyeType(),
				copy.getShirtColor(), copy.getShoesColor(), copy.getTailStyle(), copy.getTailColor(), copy.getEarsStyle(), copy.getEarsColor(), copy.getMuzzleStyle(), copy.getMuzzleColor());
		
	}

	public TestFurryRaceLook(PacketReader pr) {
		super(pr);
	}	
	
	// Randomization
	
	public int getRandomTailStyle() 	{	return this.getRandomByteFeature("TAIL");	}
	
	public int getRandomTailColor() 	{	return this.getRandomByteColorFeature("TAIL_COLOR");	}
	
	public int getRandomEarsStyle() 	{	return this.getRandomByteFeature("EARS");	}
	
	public int getRandomEarsColor() 	{	return this.getRandomByteColorFeature("EARS_COLOR");	}
	
	public int getRandomMuzzleStyle() 	{	return this.getRandomByteFeature("MUZZLE");	}
	
	public int getRandomMuzzleColor() 	{	return this.getRandomByteColorFeature("MUZZLE_COLOR");	}
	
	// Getters
	
	public int getTailStyle() 			{	return this.appearanceByteGet("TAIL");	}
	
	public int getTailColor() 			{	return this.appearanceByteGet("TAIL_COLOR");	}
	
	public int getEarsStyle() 			{	return this.appearanceByteGet("EARS");	}
	
	public int getEarsColor() 			{	return this.appearanceByteGet("EARS_COLOR");	}
	
	public int getMuzzleStyle() 		{	return this.appearanceByteGet("MUZZLE");	}
	
	public int getMuzzleColor() 		{	return this.appearanceByteGet("MUZZLE_COLOR");	}
	
	// Setters
	
	public int setTailStyle(int id) 	{	return this.appearanceByteSet("TAIL",(byte)id);	}
	
	public int setTailColor(int id) 	{	return this.appearanceByteSet("TAIL_COLOR",(byte)id);	}
	
	public int setEarsStyle(int id) 	{	return this.appearanceByteSet("EARS",(byte)id);	}
	
	public int setEarsColor(int id) 	{	return this.appearanceByteSet("EARS_COLOR",(byte)id);	}
	
	public int setMuzzleStyle(int id) 	{	return this.appearanceByteSet("MUZZLE",(byte)id);	}
	
	public int setMuzzleColor(int id) 	{	return this.appearanceByteSet("MUZZLE_COLOR",(byte)id);	}
	
	public static void loadRaceTextures() {	for(BodyPart bp : new TestFurryRaceParts().getBodyParts()) {
			if(bp.isBaseGamePart()) {
				continue;
			}
			
			GamePartsLoader loader = new GamePartsLoader();
			loader.startLoaderThreads();
			new GameParts(loader, bp);
		}
	}

	public void randomizeLook(GameRandom random) 	{	this.randomizer = random;	this.randomizeLook(random, true, true, true, true, true, true, true, true, true, true);	}

	public void randomizeLook() 					{	this.randomizeLook(this.getRandomizer());	}
	
	public void randomizeLook(GameRandom random, boolean randomTail, boolean randomTailColor, boolean randomEars, boolean randomEarsColor, boolean randomMuzzle, boolean randomMuzzleColor, 
			boolean randomSkin, boolean changeEyeType, boolean randomEyeColor, boolean randomFacialFeature) {
		
		// FUTURE GENDER SURGERY POINT HERE
		HumanGender gender = (HumanGender) random.getOneOf(new HumanGender[]{HumanGender.MALE, HumanGender.FEMALE, HumanGender.NEUTRAL});
		
		this.randomizeLook(random, false, gender, randomSkin, changeEyeType, randomEyeColor,	randomFacialFeature);
	}

	

	public static <T> List<T> getClosedEyesDrawOptions(int eyeType, int eyeColor, int skinColor, boolean humanlikeOnly,
			Function<GameTexture, T> mapper) {
		return GameEyes.getEyes(eyeType).getClosedColorTextures(eyeColor, skinColor, humanlikeOnly, mapper);
	}

	public static <T> List<T> getOpenEyesDrawOptions(int eyeType, int eyeColor, int skinColor, boolean humanlikeOnly,
			Function<GameTexture, T> mapper) {
		return GameEyes.getEyes(eyeType).getOpenColorTextures(eyeColor, skinColor, humanlikeOnly, mapper);
	}

	public static DrawOptions getEyesDrawOptions(int eyeType, int eyeColor, int skinColor, boolean humanlikeOnly,
			boolean closed, int drawX, int drawY, int spriteX, int spriteY, int width, int height, boolean mirrorX,
			boolean mirrorY, float alpha, GameLight light, MaskShaderOptions mask) {
		Function<GameTexture, DrawOptions> mapper = (texture) -> {
			return texture.initDraw().sprite(spriteX, spriteY, 64).light(light).alpha(alpha).size(width, height)
					.mirror(mirrorX, mirrorY).addMaskShader(mask).pos(drawX, drawY);
		};
		return closed
				? new DrawOptionsList(getClosedEyesDrawOptions(eyeType, eyeColor, skinColor, humanlikeOnly, mapper))
				: new DrawOptionsList(getOpenEyesDrawOptions(eyeType, eyeColor, skinColor, humanlikeOnly, mapper));
	}

	public DrawOptions getEyesDrawOptions(boolean humanlikeOnly, boolean closed, int drawX, int drawY, int spriteX,
			int spriteY, int width, int height, boolean mirrorX, boolean mirrorY, float alpha, GameLight light,
			MaskShaderOptions mask) {
		return getEyesDrawOptions(this.getEyeType(), this.getEyeColor(), this.getSkin(), humanlikeOnly, closed, drawX,
				drawY, spriteX, spriteY, width, height, mirrorX, mirrorY, alpha, light, mask);
	}

	public GameTexture getEarsTexture(int spriteX, int spriteY) {
		return GameParts.getPart(TestFurryRaceParts.class, "EARS").getTexture(spriteY, getEarsStyle(), 0, spriteX);
	}

	public GameTexture getTailTexture(int spriteX, int spriteY) {
		return GameParts.getPart(TestFurryRaceParts.class, "TAIL").getTexture(spriteY, getEarsStyle(), 0, spriteX);
	}

	public GameTexture getMuzzleTexture(int spriteX, int spriteY) {
		return GameParts.getPart(TestFurryRaceParts.class, "MUZZLE").getTexture(spriteY, getEarsStyle(), 0, spriteX);
	}	


	
	
}