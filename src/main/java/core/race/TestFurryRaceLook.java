package core.race;

import java.awt.Color;
import java.awt.Point;
import java.util.List;
import java.util.function.Function;

import core.gfx.GameParts;
import core.gfx.GamePartsLoader;
import core.gfx.TestFurryDrawOptions;
import core.race.factory.RaceDataFactory;
import core.race.parts.BodyPart;
import core.race.parts.HumanRaceParts;
import core.race.parts.RaceLookParts;
import core.race.parts.TestFurryRaceParts;
import extensions.FormNewPlayerRaceCustomizer;
import extensions.TestFurryNewPlayerRaceCustomizer;
import helpers.DebugHelper;
import necesse.engine.network.PacketReader;
import necesse.engine.util.GameRandom;
import necesse.entity.mobs.MaskShaderOptions;
import necesse.entity.mobs.PlayerMob;
import necesse.gfx.GameEyes;
import necesse.gfx.HumanGender;
import necesse.gfx.HumanLook;
import necesse.gfx.drawOptions.DrawOptions;
import necesse.gfx.drawOptions.DrawOptionsList;
import necesse.gfx.drawOptions.human.HumanDrawOptions;
import necesse.gfx.gameTexture.GameTexture;
import necesse.level.maps.light.GameLight;


public class TestFurryRaceLook extends RaceLook {
	
	public static final String TEST_FURRY_RACE_ID = "testfurry";
	
	public static TestFurryRaceLook getCustomRaceLook(RaceLook _look) {	
		if (!_look.getRaceID().equals(TestFurryRaceLook.TEST_FURRY_RACE_ID)) {
			DebugHelper.handleDebugMessage(String.format("Draw options for raceID %s requested for non-raceID %s from %s. Using defaults.", TestFurryRaceLook.TEST_FURRY_RACE_ID, _look.getRaceID(), _look.getClass().getName()), 25);
			return new TestFurryRaceLook(true);
		}
		return (TestFurryRaceLook)_look;
	}

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
		}
	}
			
	public TestFurryRaceLook(int hair, int facialFeature, int hairColor, int skin, int eyeColor, int eyeType, Color shirtColor,
			Color shoesColor, int tail, int tailColor, int ears, int earsColor, int muzzle, int muzzleColor, int headStyle, int headColor, int armsStyle, int armsColor, int bodyStyle, int bodyColor) {	
		
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
		
		this.setHeadStyle(headStyle);
		this.setHeadColor(headColor);
		
		this.setArmsStyle(armsStyle);
		this.setArmsColor(armsColor);
		
		this.setBodyStyle(bodyStyle);
		this.setBodyColor(bodyColor);
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
				0,
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
		
		this.setHeadStyle(0);
		this.setHeadColor(0);
		
		this.setArmsColor(0);
		this.setArmsStyle(0);
		
		this.setBodyColor(0);
		this.setBodyStyle(0);
	}
	
	public TestFurryRaceLook(TestFurryRaceLook copy) {
		this(	copy.getHair(),
				copy.getFacialFeature(),
				copy.getHairColor(),
				copy.getSkin(),				
				copy.getEyeColor(),
				copy.getEyeType(),
				
				copy.getShirtColor(),
				copy.getShoesColor(),
				
				copy.getTailStyle(),
				copy.getTailColor(),
				
				copy.getEarsStyle(),
				copy.getEarsColor(),
				
				copy.getMuzzleStyle(),
				copy.getMuzzleColor(),
				
				copy.getHeadStyle(),
				copy.getHeadColor(),
				
				copy.getArmsStyle(),
				copy.getArmsColor(),
				
				copy.getBodyStyle(),
				copy.getBodyColor());
		
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
	
	public int getHeadStyle() 			{	return this.appearanceByteGet("HEAD");	}
	
	public int getHeadColor() 			{	return this.appearanceByteGet("HEAD_COLOR");	}
	
	public int getBodyStyle() 			{	return this.appearanceByteGet("BODY");	}

	public int getBodyColor() 			{	return this.appearanceByteGet("BODY_COLOR");	}
	
	public int getArmsStyle() 			{	return this.appearanceByteGet("ARMS");	}

	public int getArmsColor() 			{	return this.appearanceByteGet("ARMS_COLOR");	}
	
	
	// Setters
	
	public int setTailStyle(int id) 	{	return this.appearanceByteSet("TAIL",(byte)id);	}
	
	public int setTailColor(int id) 	{	return this.appearanceByteSet("TAIL_COLOR",(byte)id);	}
	
	public int setEarsStyle(int id) 	{	return this.appearanceByteSet("EARS",(byte)id);	}
	
	public int setEarsColor(int id) 	{	return this.appearanceByteSet("EARS_COLOR",(byte)id);	}
	
	public int setMuzzleStyle(int id) 	{	return this.appearanceByteSet("MUZZLE",(byte)id);	}
	
	public int setMuzzleColor(int id) 	{	return this.appearanceByteSet("MUZZLE_COLOR",(byte)id);	}
	
	public int setHeadStyle(int id) 	{	return this.appearanceByteSet("HEAD",(byte)id);	}

	public int setHeadColor(int id) 	{	return this.appearanceByteSet("HEAD_COLOR",(byte)id);	}
	
	public int setBodyStyle(int id) 	{	return this.appearanceByteSet("BODY",(byte)id);	}
	
	public int setBodyColor(int id) 	{	return this.appearanceByteSet("BODY_COLOR",(byte)id);	}
	
	public int setArmsStyle(int id) 	{	return this.appearanceByteSet("ARMS",(byte)id);	}
	
	public int setArmsColor(int id) 	{	return this.appearanceByteSet("ARMS_COLOR",(byte)id);	}
	
	public static void loadRaceTextures() {	
		
		for(BodyPart bp : new TestFurryRaceParts().getCustomBodyParts()) {			
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
		return GameParts.getPart(TestFurryRaceParts.class, "EARS").getTextureSprite(getEarsStyle(), getEarsColor(), spriteX, spriteY);
	}
	
	public GameTexture getEarsTexture(int spriteX, int spriteY, int resizeX, int resizeY) {
		return GameParts.getPart(TestFurryRaceParts.class, "EARS").getTextureSprite(getEarsStyle(), getEarsColor(), spriteX, spriteY, resizeX, resizeY);
	}
	
	public GameTexture getTailTexture(int spriteX, int spriteY) {
		return GameParts.getPart(TestFurryRaceParts.class, "TAIL").getTextureSprite(getTailStyle(), getTailColor(), spriteX, spriteY);
	}
	
	public GameTexture getTailTexture(int spriteX, int spriteY, int resizeX, int resizeY) {
		return GameParts.getPart(TestFurryRaceParts.class, "TAIL").getTextureSprite(getTailStyle(), getTailColor(), spriteX, spriteY, resizeX, resizeY);
	}
	
	public GameTexture getMuzzleTexture(int spriteX, int spriteY) {
		return GameParts.getPart(TestFurryRaceParts.class, "MUZZLE").getTextureSprite(getMuzzleStyle(), getMuzzleColor(), spriteX, spriteY);
	}
	
	public GameTexture getMuzzleTexture(int spriteX, int spriteY, int resizeX, int resizeY) {
		return GameParts.getPart(TestFurryRaceParts.class, "MUZZLE").getTextureSprite(getMuzzleStyle(), getMuzzleColor(), spriteX, spriteY, resizeX, resizeY);
	}

	public GameTexture getHeadTexture(int spriteX, int spriteY) {
		return GameParts.getPart(TestFurryRaceParts.class, "HEAD").getTextureSprite(getHeadStyle(), getHeadColor(), spriteX, spriteY);
	}
	
	public GameTexture getHeadTexture(int spriteX, int spriteY, int resizeX, int resizeY) {
		return GameParts.getPart(TestFurryRaceParts.class, "HEAD").getTextureSprite(getHeadStyle(), getHeadColor(), spriteX, spriteY, resizeX, resizeY);
	}
	
	public GameTexture getHeadTexture() {
		return GameParts.getPart(TestFurryRaceParts.class, "HEAD").getFullTexture(getHeadStyle(), getHeadColor());
	}

	public GameTexture getBodyTexture() {
		return GameParts.getPart(TestFurryRaceParts.class, "BODY").getFullTexture(getBodyStyle(), getBodyColor());
	}

	public GameTexture getLeftArmTexture() {
		return GameParts.getPart(TestFurryRaceParts.class, "ARMS").getFullTexture(getArmsStyle(), getArmsColor(), 0);
	}
	
	public GameTexture getRightArmTexture() {
		return GameParts.getPart(TestFurryRaceParts.class, "ARMS").getFullTexture(getArmsStyle(), getArmsColor(), 1);
	}

	@Override
	public HumanDrawOptions modifyHumanDrawOptions(HumanDrawOptions drawOptions, MaskShaderOptions mask) {
		
		for( BodyPart part : this.getRaceParts().getReplacerParts()) {
			switch(part.getReplacer().targetPart) {
				case BACK:
					break;
				case BODY:
					drawOptions.bodyTexture(getBodyTexture());
					break;
				case CHEST:
					break;
				case ARMS:
					drawOptions.leftArmsTexture(getLeftArmTexture());
					drawOptions.rightArmsTexture(getRightArmTexture());
					break;
				case HEAD:
					drawOptions.headTexture(getHeadTexture());
					break;
				case SHOES:
					break;
				default:
					break;			
			}			
		}
		
		drawOptions.addTopDraw(new TestFurryDrawOptions.FurryDrawOptionsGetter() {
						
 				@Override
				public DrawOptions getDrawOptions(PlayerMob player, int dir, int spriteX, int spriteY, int spriteRes, int drawX, int drawY,
						int width, int height, boolean mirrorX, boolean mirrorY, GameLight light, float alpha, MaskShaderOptions mask) {
 					
 					TestFurryRaceLook rl = (TestFurryRaceLook)RaceDataFactory.getRaceLook(player, TestFurryRaceLook.this);
					return new TestFurryDrawOptions(player.getLevel(), rl)
							.spriteRes(spriteRes)
							.size(new Point(width, height))
							.earsTexture(rl, spriteX, spriteY, true)
							.muzzleTexture(rl, spriteX, spriteY, true)
							.tailTexture(rl, spriteX, spriteY, true)
							.dir(dir).mirrorX(mirrorX).mirrorY(mirrorY).allAlpha(alpha).light(light)
							.drawOffset(mask == null ? 0 : mask.drawXOffset, mask == null ? 0 : mask.drawYOffset).pos(drawX, drawY).mask(mask)
							.drawEars(true).drawMuzzle(true).drawTail(true);

				}
			});		
		return drawOptions;
	}

	@Override
	public Class<? extends FormNewPlayerRaceCustomizer> getAssociatedCustomizerForm() {
		return this.associatedCustomizerForm;
	}


	

	
	
}