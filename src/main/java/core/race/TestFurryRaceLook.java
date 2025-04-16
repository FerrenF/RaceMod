package core.race;

import java.awt.Color;
import core.forms.FormNewPlayerRaceCustomizer;
import core.forms.TestFurryNewPlayerRaceCustomizer;
import core.gfx.EyeTypeGameParts;
import core.gfx.GameParts;
import core.gfx.texture.AsyncTextureLoader.TextureLocation;
import core.race.factory.RaceDataFactory;
import core.race.parts.BodyPart;
import core.race.parts.EyeBodyPart;
import core.race.parts.TestFurryRaceParts;
import extensions.CustomHumanDrawOptions;
import helpers.DebugHelper;
import necesse.engine.network.PacketReader;
import necesse.engine.util.GameRandom;
import necesse.entity.mobs.MaskShaderOptions;
import necesse.entity.mobs.PlayerMob;
import necesse.gfx.HumanGender;
import necesse.gfx.HumanLook;
import necesse.gfx.drawOptions.DrawOptions;
import necesse.gfx.drawOptions.human.HumanDrawOptions;
import necesse.gfx.gameTexture.GameTexture;
import necesse.gfx.gameTexture.GameTexture.BlendQuality;
import necesse.level.maps.light.GameLight;



public class TestFurryRaceLook extends RaceLook {
	
	public static GameTexture TEX_MASK_LEFT;
	public static GameTexture TEX_MASK_RIGHT;
	public static final String TEST_FURRY_RACE_ID = "testfurry";
	public static GameTexture raceCustomizerIcon;
	
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
			Color shoesColor, int tail, int tailColor, int ears, int earsColor, int muzzle, int muzzleColor, int headStyle, 
			int headColor, int armsStyle, int armsColor, int bodyStyle, int bodyColor, int feetStyle, int feetColor, int customEyesStyle, int customEyesColor) {	
		
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
		
		this.setFeetStyle(feetStyle);
		this.setFeetColor(feetColor);
		
		this.setCustomEyesStyle(customEyesStyle);
		this.setCustomEyesColor(customEyesColor);
		
	}
	
	// Note: This does not set valid DEFAULT parts. Just makes sure the keys are there.
	public void initCustomParts() {		
		// not used in CustomHumanLook
	}
	
	@Override
	public String getRaceID() {
		return TestFurryRaceLook.TEST_FURRY_RACE_ID;
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
		
		this.setFeetStyle(0);
		this.setFeetColor(0);
		
		this.setCustomEyesStyle(0);
		this.setCustomEyesColor(0);
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
				copy.getBodyColor(),
				
				copy.getFeetStyle(),
				copy.getFeetColor(),
				
				copy.getCustomEyesStyle(),
				copy.getCustomEyesColor()
				);
		
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
	
	public int getRandomHeadStyle() 	{	return this.getRandomByteFeature("HEAD");	}
	
	public int getRandomHeadColor() 	{	return this.getRandomByteColorFeature("HEAD_COLOR");	}
	
	public int getRandomBodyStyle() 	{	return this.getRandomByteFeature("BODY");	}
	
	public int getRandomBodyColor() 	{	return this.getRandomByteColorFeature("BODY_COLOR");	}
	
	public int getRandomArmsStyle() 	{	return this.getRandomByteFeature("ARMS");	}
	
	public int getRandomArmsColor() 	{	return this.getRandomByteColorFeature("ARMS_COLOR");	}
	
	public int getRandomFeetStyle() 	{	return this.getRandomByteFeature("FEET");	}
	
	public int getRandomFeetColor() 	{	return this.getRandomByteColorFeature("FEET_COLOR");	}
	
	public int getRandomCustomEyesStyle() 	{	return this.getRandomByteFeature("CUSTOM_EYES");	}
	
	public int getRandomCustomEyesColor() 	{	return this.getRandomByteColorFeature("CUSTOM_EYES_COLOR");	}
	
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
	
	public int getFeetStyle() 			{	return this.appearanceByteGet("FEET");	}

	public int getFeetColor() 			{	return this.appearanceByteGet("FEET_COLOR");	}
	
	public int getCustomEyesStyle() 			{	return this.appearanceByteGet("CUSTOM_EYES");	}

	public int getCustomEyesColor() 			{	return this.appearanceByteGet("CUSTOM_EYES_COLOR");	}
	
	
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
	
	public int setFeetStyle(int id) 	{	return this.appearanceByteSet("FEET",(byte)id);	}
	
	public int setFeetColor(int id) 	{	return this.appearanceByteSet("FEET_COLOR",(byte)id);	}
	
	public int setCustomEyesStyle(int id) 	{	return this.appearanceByteSet("CUSTOM_EYES",(byte)id);	}

	public int setCustomEyesColor(int id) 	{	return this.appearanceByteSet("CUSTOM_EYES_COLOR", (byte)id);	}
	
	public void randomizeLook(GameRandom random) 
	{
		this.randomizer = random;	
		this.randomizeLook(random, true, true, true, true, true, true, true, true, true, true);	
	}

	public void randomizeLook() 	
	{	this.randomizeLook(this.getRandomizer());	}
	
	public void randomizeLook(GameRandom random, boolean randomTail, boolean randomTailColor, boolean randomEars, boolean randomEarsColor, boolean randomMuzzle, boolean randomMuzzleColor, 
			boolean randomSkin, boolean changeEyeType, boolean randomEyeColor, boolean randomFacialFeature) {
				
		this.setTailStyle(this.getRandomTailStyle());
		this.setTailColor(this.getRandomTailColor());
		
		this.setMuzzleStyle(this.getRandomMuzzleStyle());
		this.setMuzzleColor(this.getRandomMuzzleColor());
		
		this.setEarsStyle(this.getRandomEarsStyle());
		this.setEarsColor(this.getRandomEarsColor());
		
		this.setHeadStyle(this.getRandomHeadStyle());
		this.setHeadColor(this.getRandomHeadColor());
		
		this.setCustomEyesStyle(this.getRandomCustomEyesStyle());
		this.setCustomEyesColor(this.getRandomCustomEyesColor());
		
		if(randomSkin) {
			this.setBodyStyle(this.getRandomBodyStyle());
			this.setBodyColor(this.getRandomBodyColor());
			
			this.setArmsStyle(this.getRandomArmsStyle());
			this.setArmsColor(this.getRandomArmsColor());
			
			this.setFeetStyle(this.getRandomFeetStyle());
			this.setFeetColor(this.getRandomFeetColor());
		}
		HumanGender gender = (HumanGender) random.getOneOf(new HumanGender[]{HumanGender.MALE, HumanGender.FEMALE, HumanGender.NEUTRAL});
		super.randomizeLook(random, false, gender, false, changeEyeType, randomEyeColor,	randomFacialFeature);
	}
	
	public GameTexture getEarsTexture() {
		return GameParts.getFullTexture(this.getRaceParts().getBodyPart("EARS"), getEarsStyle(), getEarsColor());	
	}
		
	public GameTexture getTailTexture() {
		return GameParts.getFullTexture(this.getRaceParts().getBodyPart("TAIL"), getTailStyle(), getTailColor());	
	}
		
	public GameTexture getMuzzleTexture() {
		return GameParts.getFullTexture(this.getRaceParts().getBodyPart("MUZZLE"), getMuzzleStyle(), getMuzzleColor());	
	}	
	
	public GameTexture getHeadTexture() {
		return GameParts.getFullTexture(this.getRaceParts().getBodyPart("HEAD"), getHeadStyle(), getHeadColor());	
	}
	
	public GameTexture getBodyTexture() {
		return GameParts.getFullTexture(this.getRaceParts().getBodyPart("BODY"), getBodyStyle(), getBodyColor());
	}

	public GameTexture getLeftArmTexture() {
		return GameParts.getFullTexture(this.getRaceParts().getBodyPart("ARMS"), getArmsStyle(), getArmsColor(), 1);		
	}
	
	public GameTexture getRightArmTexture() {
		return GameParts.getFullTexture(this.getRaceParts().getBodyPart("ARMS"), getArmsStyle(), getArmsColor(), 2);
	}
	
	public GameTexture getFeetTexture() {
		return GameParts.getFullTexture(this.getRaceParts().getBodyPart("FEET"), getFeetStyle(), getFeetColor());
	}
	
	public GameTexture getCustomEyesOpenTexture(float scale, BlendQuality blendQuality) {		
		return EyeTypeGameParts.getFullOpenTexture((EyeBodyPart)(this.getRaceParts().getBodyPart("CUSTOM_EYES")),
				getBodyColor(), getCustomEyesStyle(), getCustomEyesColor(), scale, blendQuality, TextureLocation.FROM_JAR);
	}
	
	public GameTexture getCustomEyesClosedTexture(float scale, BlendQuality blendQuality) {
		return EyeTypeGameParts.getFullClosedTexture((EyeBodyPart)(this.getRaceParts().getBodyPart("CUSTOM_EYES")),
				getBodyColor(), getCustomEyesColor(), scale, blendQuality, TextureLocation.FROM_JAR);
	}
	
	public DrawOptions getEyesDrawOptions(boolean humanlikeOnly, boolean closed, int drawX, int drawY, int spriteX,
			int spriteY, int width, int height, boolean mirrorX, boolean mirrorY, float alpha, GameLight light,
			MaskShaderOptions mask) {
		
		return closed
				? getCustomEyesClosedTexture(1.0F, BlendQuality.NEAREST).initDraw().sprite(spriteX, spriteY, 64).light(light).alpha(alpha).size(width, height)
						.mirror(mirrorX, mirrorY).addMaskShader(mask).pos(drawX, drawY)
						
				: getCustomEyesOpenTexture(1.0F, BlendQuality.NEAREST).initDraw().sprite(spriteX, spriteY, 64).light(light).alpha(alpha).size(width, height)
				.mirror(mirrorX, mirrorY).addMaskShader(mask).pos(drawX, drawY);
	}
	
	protected DrawOptions getMuzzleDrawOptions(int skinColor, boolean humanlikeOnly, int drawX, int drawY, int spriteX,
			int spriteY, int width, int height, boolean mirrorX, boolean mirrorY, float alpha, GameLight light,
			MaskShaderOptions mask) {		
		
		return this.getMuzzleTexture()				
				.initDraw()
				.sprite(spriteX, spriteY, 64)
				.light(light)
				.alpha(alpha).size(width, height)
				.mirror(mirrorX, mirrorY)
				.addMaskShader(mask)
				.pos(drawX, drawY);
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
				default:
					break;			
			}			
		}		
				
		drawOptions.eyeDrawOptionsGetter = new CustomHumanDrawOptions.EyesDrawOptionsProvider() {
						
			@Override
			public DrawOptions getEyesDrawOptions(int eyeType, int eyeColor, int skinColor, boolean humanlikeOnly,
					boolean closed, int drawX, int drawY, int spriteX, int spriteY, int width, int height, boolean mirrorX,
					boolean mirrorY, float alpha, GameLight light, MaskShaderOptions mask) {
				return TestFurryRaceLook.this.getEyesDrawOptions(false, closed, drawX, drawY, spriteX, spriteY, width, height, mirrorX, mirrorY, alpha, light, mask);		
			}
		};
		
		drawOptions.onFaceOptionsGetter = new CustomHumanDrawOptions.OnFaceDrawOptionsProvider() {
			
			@Override
			public DrawOptions getOnFaceDrawOptionsProvider(PlayerMob player, int dir, int spriteRes, int drawX, int drawY,
					int spriteX, int spriteY, int width, int height, boolean mirrorX, boolean mirrorY, float alpha,
					GameLight light, MaskShaderOptions mask) {
				
				TestFurryRaceLook rl = (TestFurryRaceLook)RaceDataFactory.getRaceLook(player, TestFurryRaceLook.this);
						
				return rl.getMuzzleTexture().initDraw()
						.sprite(spriteX, spriteY, spriteRes)
						.size(width, height).mirror(mirrorX, mirrorY)					
						.alpha(alpha)
						.light(light).addMaskShader(mask)
						.pos(drawX + ((mask == null)
								? 0 
								: mask.drawXOffset), drawY + ((mask == null)
										? 0 
												: mask.drawYOffset));
			}
		};

		drawOptions.addBehindDraw(new HumanDrawOptions.HumanDrawOptionsGetter() {
					
				@Override
			public DrawOptions getDrawOptions(PlayerMob player, int dir, int spriteX, int spriteY, int spriteRes, int drawX, int drawY,
					int width, int height, boolean mirrorX, boolean mirrorY, GameLight light, float alpha, MaskShaderOptions mask) {
					
					TestFurryRaceLook rl = (TestFurryRaceLook)RaceDataFactory.getRaceLook(player, TestFurryRaceLook.this);
					
					return rl.getTailTexture().initDraw()
							.sprite(spriteX, spriteY, spriteRes)
							.size(width, height).mirror(mirrorX, mirrorY)					
							.alpha((dir == 1 || dir == 3) ? alpha : 0)
							.light(light).addMaskShader(mask)
							.pos(drawX + ((mask == null)
									? 0 
									: mask.drawXOffset), drawY + ((mask == null)
											? 0 
													: mask.drawYOffset));							
			}
		});	
		
		drawOptions.addBehindDraw(new HumanDrawOptions.HumanDrawOptionsGetter() {		
			@Override
			public DrawOptions getDrawOptions(PlayerMob player, int dir, int spriteX, int spriteY, int spriteRes, int drawX, int drawY,
				int width, int height, boolean mirrorX, boolean mirrorY, GameLight light, float alpha, MaskShaderOptions mask) {
					
				TestFurryRaceLook rl = (TestFurryRaceLook)RaceDataFactory.getRaceLook(player, TestFurryRaceLook.this);
				
				return rl.getEarsTexture().initDraw()
						.sprite(spriteX, spriteY, spriteRes)
						.size(width, height).mirror(mirrorX, mirrorY)					
						.alpha((dir == 2 || dir == 3 || dir == 1) ? alpha : 0)
						.light(light).addMaskShader(mask)
						.pos(drawX + ((mask == null)
								? 0 
								: mask.drawXOffset), drawY + ((mask == null)
										? 0 
												: mask.drawYOffset));	
			}
		});	
				
		drawOptions.addTopDraw(new HumanDrawOptions.HumanDrawOptionsGetter() {		
			@Override
			public DrawOptions getDrawOptions(PlayerMob player, int dir, int spriteX, int spriteY, int spriteRes, int drawX, int drawY,
				int width, int height, boolean mirrorX, boolean mirrorY, GameLight light, float alpha, MaskShaderOptions mask) {
 					
 					TestFurryRaceLook rl = (TestFurryRaceLook)RaceDataFactory.getRaceLook(player, TestFurryRaceLook.this);
 					
 					return rl.getTailTexture().initDraw()
 							.sprite(spriteX, spriteY, spriteRes)
 							.size(width, height).mirror(mirrorX, mirrorY)					
 							.alpha(!(dir == 1 || dir == 3) ? alpha : 0)
 							.light(light).addMaskShader(mask)
 							.pos(drawX + ((mask == null)
 									? 0 
 									: mask.drawXOffset), drawY + ((mask == null)
 											? 0 
 													: mask.drawYOffset));	
				}
			});		
	
		drawOptions.addTopDraw(new HumanDrawOptions.HumanDrawOptionsGetter() {		
			@Override
			public DrawOptions getDrawOptions(PlayerMob player, int dir, int spriteX, int spriteY, int spriteRes, int drawX, int drawY,
				int width, int height, boolean mirrorX, boolean mirrorY, GameLight light, float alpha, MaskShaderOptions mask) {
					
					TestFurryRaceLook rl = (TestFurryRaceLook)RaceDataFactory.getRaceLook(player, TestFurryRaceLook.this);
					
				MaskShaderOptions m = mask;
				if(mask == null) {
					if(dir == 3) {
						m = new MaskShaderOptions(TEX_MASK_RIGHT, 0, 0, 0, 0); 
					}
					else if(dir == 1) {
						m = new MaskShaderOptions(TEX_MASK_LEFT, 0, 0, 0, 0); 
					}
				}
				else {
					if(dir == 3) {
						m = mask.copyAndAddMask(TEX_MASK_RIGHT, 0, 0); 
					}
					else if(dir == 1) {
						m = mask.copyAndAddMask(TEX_MASK_LEFT, 0, 0); 
					}
				}
		
				return rl.getEarsTexture().initDraw()
							.sprite(spriteX, spriteY, spriteRes)
							.size(width, height).mirror(mirrorX, mirrorY)					
							.alpha((dir == 0 || dir == 3 || dir == 1) ? alpha : 0)
							.light(light).addMaskShader(mask)
							.pos(drawX + ((m == null)
									? 0 
									: m.drawXOffset), drawY + ((m == null)
											? 0 
													: m.drawYOffset));
			}
		});		
		
		return drawOptions;
	}

	

	@Override
	public Class<? extends FormNewPlayerRaceCustomizer> getAssociatedCustomizerForm() {
		return this.associatedCustomizerForm;
	}



	

	
	
}