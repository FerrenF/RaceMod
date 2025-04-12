package core.race;

import java.awt.Color;
import java.awt.Point;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import core.forms.FormNewPlayerRaceCustomizer;
import core.forms.OrcNewPlayerRaceCustomizer;
import core.gfx.EyeTypeGamePart;
import core.gfx.GameParts;
import core.gfx.GameParts.TextureMergerInfo;
import core.gfx.GamePartsLoader;
import core.gfx.OrcDrawOptions;
import core.gfx.TestFurryDrawOptions;
import core.race.factory.RaceDataFactory;
import core.race.parts.BodyPart;
import core.race.parts.EyeBodyPart;
import core.race.parts.OrcRaceParts;
import extensions.CustomHumanDrawOptions;
import helpers.DebugHelper;
import helpers.DebugHelper.MESSAGE_TYPE;
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
import necesse.gfx.gameTexture.GameTexture.BlendQuality;
import necesse.gfx.gameTexture.MergeFunction;
import necesse.level.maps.light.GameLight;



public class OrcRaceLook extends RaceLook {
	
	public static final String ORC_RACE_ID = "orc";
	public static GameTexture raceCustomizerIcon;
	public static OrcRaceLook getCustomRaceLook(RaceLook _look) {	
		if (!_look.getRaceID().equals(OrcRaceLook.ORC_RACE_ID)) {
			DebugHelper.handleDebugMessage(String.format("Draw options for raceID %s requested for non-raceID %s from %s. Using defaults.", OrcRaceLook.ORC_RACE_ID, _look.getRaceID(), _look.getClass().getName()), 25);
			return new OrcRaceLook(true);
		}
		return (OrcRaceLook)_look;
	}

	public OrcRaceLook(GameRandom random, boolean onlyHumanLike) {		
		this(true);
		this.randomizer = random;
		this.randomizeLook(random, onlyHumanLike);
	}
	
	public OrcRaceLook() {
		super(ORC_RACE_ID);
		this.associatedCustomizerForm = OrcNewPlayerRaceCustomizer.class;
	}
	
	public OrcRaceLook(boolean init) {
		this();
		if(init) {
			this.partsList = new OrcRaceParts(init);
			this.resetCustomDefault();		
		}
	}
			
	public OrcRaceLook(int hair, int facialFeature, int hairColor, int skin, int eyeColor, int eyeType, Color shirtColor,
			Color shoesColor, int faceHairStyle, int faceHairColor, int facialFeatureStyle, int customHairStlye, int customHairColor,
			int headStyle, int armsStyle, int bodyStyle, int bodyColor, int feetStyle,  
			int customEyesStyle, int customEyesColor) {	
		
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
		
		this.setFaceHairStyle(faceHairStyle);
		this.setFaceHairColor(faceHairColor);
		
		this.setFacialFeaturesStyle(facialFeatureStyle);
		
		this.setCustomHairStyle(customHairStlye);
		this.setCustomHairColor(customHairColor);
		
		this.setHeadStyle(headStyle);
		
		this.setArmsStyle(armsStyle);
		
		this.setBodyStyle(bodyStyle);
		this.setBodyColor(bodyColor);
		
		this.setFeetStyle(feetStyle);
		
		this.setCustomEyesStyle(customEyesStyle);
		this.setCustomEyesColor(customEyesColor);
		
	}
	
	
	// Note: This does not set valid DEFAULT parts. Just makes sure the keys are there.
	public void initCustomParts() {		
		// not used in CustomHumanLook
	}
	
	@Override
	public String getRaceID() {
		return OrcRaceLook.ORC_RACE_ID;
	}
	
	public OrcRaceLook(HumanLook copy) {
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
		
		this.setFaceHairStyle(0);
		this.setFaceHairColor(0);
		
		this.setFacialFeaturesStyle(0);
		
		this.setCustomHairStyle(0);
		this.setCustomHairColor(0);
		
		this.setHeadStyle(0);
		
		this.setArmsStyle(0);
		
		this.setBodyStyle(0);
		this.setBodyColor(0);
		
		this.setFeetStyle(0);
		
		this.setCustomEyesStyle(0);
		this.setCustomEyesColor(0);
	}
	
	public OrcRaceLook(OrcRaceLook copy) {
		this(	copy.getHair(),
				copy.getFacialFeature(),
				copy.getHairColor(),
				copy.getSkin(),				
				copy.getEyeColor(),
				copy.getEyeType(),

				copy.getShirtColor(),
				copy.getShoesColor(),	
				
				copy.getFaceHairStyle(),
				copy.getFaceHairColor(),
				copy.getFacialFeaturesStyle(),
				copy.getCustomHairStyle(),
				copy.getCustomHairColor(),	
					
				copy.getHeadStyle(),
				copy.getArmsStyle(),
				copy.getBodyStyle(),
				copy.getBodyColor(),
				copy.getFeetStyle(),
				
				copy.getCustomEyesStyle(),
				copy.getCustomEyesColor()
				);
		
	}

	public OrcRaceLook(PacketReader pr) {
		super(pr);
		
	}	
	
	// Randomization
	
	
	public int getRandomHeadStyle() 	{	return this.getRandomByteFeature("HEAD");	}
		
	public int getRandomBodyStyle() 	{	return this.getRandomByteFeature("BODY");	}
	
	public int getRandomBodyColor() 	{	return this.getRandomByteColorFeature("BODY_COLOR");	}
	
	public int getRandomArmsStyle() 	{	return this.getRandomByteFeature("ARMS");	}
		
	public int getRandomFeetStyle() 	{	return this.getRandomByteFeature("FEET");	}
		
	public int getRandomCustomEyesStyle() 	{	return this.getRandomByteFeature("CUSTOM_EYES");	}
	
	public int getRandomCustomEyesColor() 	{	return this.getRandomByteColorFeature("CUSTOM_EYES_COLOR");	}
	
	public int getRandomCustomHairStyle()	{	return this.getRandomByteColorFeature("CUSTOM_HAIR");	}
	
	public int getRandomCustomHairColor()	{	return this.getRandomByteColorFeature("CUSTOM_HAIR_COLOR");	}
	
	public int getRandomFaceHairStyle()		{	return this.getRandomByteColorFeature("CUSTOM_EYES_COLOR");	}
	
	public int getRandomFacialFeatures()	{	return this.getRandomByteColorFeature("FACIALFEATURES");	}
	
	// Getters
		
	public int getHeadStyle() 			{	return this.appearanceByteGet("HEAD");	}
		
	public int getBodyStyle() 			{	return this.appearanceByteGet("BODY");	}

	public int getBodyColor() 			{	return this.appearanceByteGet("BODY_COLOR");	}
	
	public int getArmsStyle() 			{	return this.appearanceByteGet("ARMS");	}
	
	public int getFeetStyle() 			{	return this.appearanceByteGet("FEET");	}
	
	public int getCustomEyesStyle() 			{	return this.appearanceByteGet("CUSTOM_EYES");	}

	public int getCustomEyesColor() 			{	return this.appearanceByteGet("CUSTOM_EYES_COLOR");	}
	
	public int getCustomHairStyle()				{	return this.appearanceByteGet("CUSTOM_HAIR");	}
	
	public int getCustomHairColor()				{	return this.appearanceByteGet("CUSTOM_HAIR_COLOR");	}
	
	public int getFacialFeaturesStyle()			{	return this.appearanceByteGet("FACIALFEATURES");	}
	
	public int getFacialFeaturesColor()			{	return this.getBodyColor();	}
	
	public int getFeetColor()					{	return this.getBodyColor();	}
	
	public int getHeadColor()					{	return this.getBodyColor();	}
	
	public int getArmsColor()					{	return this.getBodyColor();	}
	
	public int getFaceHairStyle()				{	return this.appearanceByteGet("FACEHAIR");	}
	
	public int getFaceHairColor()				{	return this.appearanceByteGet("FACEHAIR_COLOR");	}
	
	// Setters
		
	public int setHeadStyle(int id) 	{	return this.appearanceByteSet("HEAD",(byte)id);	}
	
	public int setBodyStyle(int id) 	{	return this.appearanceByteSet("BODY",(byte)id);	}
	
	public int setBodyColor(int id) 	{	return this.appearanceByteSet("BODY_COLOR",(byte)id);	}
	
	public int setArmsStyle(int id) 	{	return this.appearanceByteSet("ARMS",(byte)id);	}
		
	public int setFeetStyle(int id) 	{	return this.appearanceByteSet("FEET",(byte)id);	}
		
	public int setCustomEyesStyle(int id) 	{	return this.appearanceByteSet("CUSTOM_EYES",(byte)id);	}

	public int setCustomEyesColor(int id) 	{	return this.appearanceByteSet("CUSTOM_EYES_COLOR", (byte)id);	}
	
	public int setCustomHairStyle(int id)				{	return this.appearanceByteSet("CUSTOM_HAIR", (byte)id);	}
	
	public int setCustomHairColor(int id)				{	return this.appearanceByteSet("CUSTOM_HAIR_COLOR", (byte)id);	}
	
	public int setFacialFeaturesStyle(int id)			{	return this.appearanceByteSet("FACIALFEATURES", (byte)id);	}
	
	public int setFaceHairStyle(int id)				{	return this.appearanceByteSet("FACEHAIR", (byte)id);	}
	
	public int setFaceHairColor(int id)				{	return this.appearanceByteSet("FACEHAIR_COLOR", (byte)id);	}
	
	
	public static void loadRaceTextures() {	
		DebugHelper.handleDebugMessage("Loading race textures for race " + OrcRaceLook.ORC_RACE_ID, 50, MESSAGE_TYPE.DEBUG);
		
		for(BodyPart bp : new OrcRaceParts().getCustomBodyParts()) {			
			GamePartsLoader loader = new GamePartsLoader();
			loader.startLoaderThreads();
			if(bp instanceof EyeBodyPart) 
			{
				new EyeTypeGamePart(loader, (EyeBodyPart) bp);
			}
			else
			{
				new GameParts(loader, bp);
			}
		}
		
	}

	public void randomizeLook(GameRandom random) 
	{
		this.randomizer = random;	
		this.randomizeLook(random, true, true, true, true, true, true, true, true);	
	}

	public void randomizeLook() 	
	{	this.randomizeLook(this.getRandomizer());	}
	
	public void randomizeLook(GameRandom random, boolean randomSkin, 
			boolean changeEyeType, boolean randomEyeColor, boolean randomFacialFeature,			
			boolean randomCustomHair, boolean randomFaceHair, boolean randomCustomHairColor, boolean randomHeadStyle) {
				

		if(randomHeadStyle) 		this.setHeadStyle(this.getRandomHeadStyle());
		
		if(changeEyeType) 			this.setCustomEyesStyle(this.getRandomCustomEyesStyle());
		if(randomEyeColor) 			this.setCustomEyesColor(this.getRandomCustomEyesColor());
		
		if(randomFaceHair)			this.setFaceHairStyle(this.getRandomFaceHairStyle());
		
		if(randomCustomHair)		this.setCustomHairStyle(this.getRandomCustomHairStyle());
		if(randomCustomHairColor) 	this.setCustomHairColor(this.getRandomCustomHairColor());
		
		if(randomFacialFeature)		this.setFacialFeaturesStyle(this.getRandomFacialFeatures());
		
		if(randomSkin) {
			
			int color = this.getRandomBodyColor();
			this.setHeadStyle(this.getRandomHeadStyle());
			this.setBodyStyle(this.getRandomBodyStyle());
			this.setBodyColor(color);			
			this.setArmsStyle(this.getRandomArmsStyle());			
			this.setFeetStyle(this.getRandomFeetStyle());
		}
		HumanGender gender = (HumanGender) random.getOneOf(new HumanGender[]{HumanGender.MALE, HumanGender.FEMALE, HumanGender.NEUTRAL});
		super.randomizeLook(random, false, gender, false, changeEyeType, randomEyeColor, false);
	}

	

	public static <T> List<T> getClosedEyesDrawOptions(int eyeType, int eyeColor, int skinColor, 
			Function<GameTexture, T> mapper) {
		return ((EyeTypeGamePart)GameParts.getPart(OrcRaceParts.class, "CUSTOM_EYES"))
				.getClosedColorTextures(eyeType, eyeColor, skinColor, mapper);
	}

	public static <T> List<T> getOpenEyesDrawOptions(int eyeType, int eyeColor, int skinColor,
			Function<GameTexture, T> mapper) {
		return ((EyeTypeGamePart)GameParts.getPart(OrcRaceParts.class, "CUSTOM_EYES"))
				.getOpenColorTextures(eyeType, eyeColor, skinColor, mapper);
	}

	public static DrawOptions getEyesDrawOptions(int eyeType, int eyeColor, int skinColor, boolean humanlikeOnly,
			boolean closed, int drawX, int drawY, int spriteX, int spriteY, int width, int height, boolean mirrorX,
			boolean mirrorY, float alpha, GameLight light, MaskShaderOptions mask) {
		Function<GameTexture, DrawOptions> mapper = (texture) -> {
			return texture.initDraw().sprite(spriteX, spriteY, 64).light(light).alpha(alpha).size(width, height)
					.mirror(mirrorX, mirrorY).addMaskShader(mask).pos(drawX, drawY);
		};
		return closed
				? new DrawOptionsList(getClosedEyesDrawOptions(eyeType, eyeColor, skinColor, mapper))
				: new DrawOptionsList(getOpenEyesDrawOptions(eyeType, eyeColor, skinColor, mapper));
	}
	
	public DrawOptions getEyesDrawOptions(boolean humanlikeOnly, boolean closed, int drawX, int drawY, int spriteX,
			int spriteY, int width, int height, boolean mirrorX, boolean mirrorY, float alpha, GameLight light,
			MaskShaderOptions mask) {
		return getEyesDrawOptions(this.getCustomEyesStyle(), this.getCustomEyesColor(), getBodyColor(), humanlikeOnly, closed, drawX,
				drawY, spriteX, spriteY, width, height, mirrorX, mirrorY, alpha, light, mask);
	}

	public GameTexture getHeadTexture(int spriteX, int spriteY) {
		return GameParts.getPart(OrcRaceParts.class, "HEAD").getTextureSprite(getHeadStyle(), getBodyColor(), spriteX, spriteY);
	}
	
	public GameTexture getHeadTexture(int spriteX, int spriteY, int resizeX, int resizeY) {
		return GameParts.getPart(OrcRaceParts.class, "HEAD").getTextureSprite(getHeadStyle(), getBodyColor(), spriteX, spriteY, resizeX, resizeY);
	}
	
	public GameTexture getHeadTexture() {
		return GameParts.getPart(OrcRaceParts.class, "HEAD").getFullTexture(getHeadStyle(), getBodyColor());
	}
	
	public GameTexture getBodyTexture() {
		return GameParts.getPart(OrcRaceParts.class, "BODY").getFullTexture(getBodyStyle(), getBodyColor());
	}

	public GameTexture getLeftArmTexture() {
		return GameParts.getPart(OrcRaceParts.class, "ARMS").getFullTexture(getArmsStyle(), getArmsColor(), 0);
	}
	
	public GameTexture getRightArmTexture() {
		return GameParts.getPart(OrcRaceParts.class, "ARMS").getFullTexture(getArmsStyle(), getArmsColor(), 1);
	}
	
	public GameTexture getFeetTexture() {
		return GameParts.getPart(OrcRaceParts.class, "FEET").getFullTexture(getFeetStyle(), getFeetColor());
	}

	public GameTexture getHairTexture()	{
		return GameParts.getPart(OrcRaceParts.class, "CUSTOM_HAIR").getFullTexture(getCustomHairStyle(), getCustomHairColor());
	}
	
	public GameTexture getFaceHairTexture()	{
		return GameParts.getPart(OrcRaceParts.class, "FACEHAIR").getFullTexture(getFaceHairStyle(), getFaceHairColor());
	}
	
	public GameTexture getFacialFeaturesTexture()	{
		return GameParts.getPart(OrcRaceParts.class, "FACIALFEATURES").getFullTexture(getFacialFeaturesStyle(), getFaceHairColor());
	}
	
	public GameTexture getFacialFeaturesTexture(int spriteX, int spriteY) {
		return GameParts.getPart(OrcRaceParts.class, "FACIALFEATURES").getTextureSprite(getFacialFeaturesStyle(), getFaceHairColor(), spriteX, spriteY);
	}
	
	public GameTexture getFacialFeaturesTexture(int spriteX, int spriteY, int width, int height) {
		return GameParts.getPart(OrcRaceParts.class, "FACIALFEATURES").getTextureSprite(getFacialFeaturesStyle(), getFaceHairColor(), spriteX, spriteY, width, height);
	}
	
	public List<GameTexture> getCustomEyesOpenTextures() {
		return ((EyeTypeGamePart)GameParts.getPart(OrcRaceParts.class, "CUSTOM_EYES"))
				.getOpenColorTextures(getCustomEyesStyle(), getCustomEyesColor(), getHeadColor() );
	}
	
	public List<GameTexture> getCustomEyesClosedTexture() {
		return ((EyeTypeGamePart)GameParts.getPart(OrcRaceParts.class, "CUSTOM_EYES"))
				.getClosedColorTextures(getCustomEyesStyle(), getCustomEyesColor(), getHeadColor());
	}
	
	@Override
	public CustomHumanDrawOptions modifyHumanDrawOptions(CustomHumanDrawOptions drawOptions, MaskShaderOptions mask) {
		
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
					drawOptions.feetTexture(getFeetTexture());
					break;
				case EYES:
					break;
				case HAIR:
					drawOptions.hairTexture(getHairTexture());
				case FACE_HAIR:
					drawOptions.facialFeatureTexture(getFaceHairTexture());
				default:
					break;			
			}			
		}		
				
		drawOptions.eyeDrawOptionsGetter = new CustomHumanDrawOptions.EyesDrawOptionsProvider() {
						
			@Override
			public DrawOptions getEyesDrawOptions(int eyeType, int eyeColor, int skinColor, boolean humanlikeOnly,
					boolean closed, int drawX, int drawY, int spriteX, int spriteY, int width, int height, boolean mirrorX,
					boolean mirrorY, float alpha, GameLight light, MaskShaderOptions mask) {
				return OrcRaceLook.this.getEyesDrawOptions(false, closed, drawX, drawY, spriteX, spriteY, width, height, mirrorX, mirrorY, alpha, light, mask);		
			}
		};
		
		drawOptions.onFaceOptionsGetter = new CustomHumanDrawOptions.OnFaceDrawOptionsProvider() {
			
			@Override
			public DrawOptions getOnFaceDrawOptionsProvider(PlayerMob player, int dir, int spriteRes, int drawX, int drawY,
					int spriteX, int spriteY, int width, int height, boolean mirrorX, boolean mirrorY, float alpha,
					GameLight light, MaskShaderOptions mask) {
				
				OrcRaceLook rl = (OrcRaceLook)RaceDataFactory.getRaceLook(player, OrcRaceLook.this);
				return new OrcDrawOptions(
						player != null ? player.getLevel() : null, rl)
						.spriteRes(spriteRes)
						.size(new Point(width, height))						
						.facialFeaturesTexture(rl, spriteX, spriteY, true)
						.dir(dir)
						.mirrorX(mirrorX)
						.mirrorY(mirrorY)
						.allAlpha(alpha)
						.light(light)
						.drawOffset(
								mask == null 
								? 0 
								: mask.drawXOffset,
								mask == null 
								? 0 
								: mask.drawYOffset)
						.pos(drawX, drawY)
						.mask(mask)
						.drawFacialFeatures(true);
			}
		};	

		return drawOptions;
	}

	

	@Override
	public Class<? extends FormNewPlayerRaceCustomizer> getAssociatedCustomizerForm() {
		return this.associatedCustomizerForm;
	}

	



	

	
	
}