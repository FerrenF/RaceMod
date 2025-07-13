package core.race;

import java.awt.Color;

import core.RaceMod;
import core.forms.FormNewPlayerRaceCustomizer;
import core.forms.NekoNewPlayerRaceCustomizer;
import core.gfx.GameParts;
import core.race.factory.RaceDataFactory;
import core.race.parts.NekoRaceParts;
import extensions.CustomHumanDrawOptions;
import helpers.DebugHelper;
import helpers.DebugHelper.MESSAGE_TYPE;
import necesse.engine.network.PacketReader;
import necesse.engine.util.GameRandom;
import necesse.entity.mobs.MaskShaderOptions;
import necesse.entity.mobs.PlayerMob;
import necesse.gfx.HumanLook;
import necesse.gfx.drawOptions.DrawOptions;
import necesse.gfx.drawOptions.human.HumanDrawOptions;
import necesse.gfx.gameTexture.GameTexture;
import necesse.level.maps.light.GameLight;

public class NekoRaceLook extends RaceLook {
	
	public static GameTexture raceCustomizerIcon;
	public static final String NEKO_RACE_ID = "neko";
	
	@Override
	public String getRaceID() {
		return NekoRaceLook.NEKO_RACE_ID;
	}

	public NekoRaceLook(GameRandom random) {		
		this(true);
		this.randomizer = random;
		this.randomizeLook(random);	
	}
	
	public static NekoRaceLook getCustomRaceLook(RaceLook _look) {	
		if (!_look.getRaceID().equals(NekoRaceLook.NEKO_RACE_ID)) {
			DebugHelper.handleDebugMessage(String.format("Draw options for raceID %s requested for non-raceID %s from %s. Using defaults.", NekoRaceLook.NEKO_RACE_ID, _look.getRaceID(), _look.getClass().getName()), 25);
			return new NekoRaceLook(true);
		}
		return (NekoRaceLook)_look;
	}
	
	public NekoRaceLook() {
		super(NEKO_RACE_ID);
		this.associatedCustomizerForm = NekoNewPlayerRaceCustomizer.class;
		this.defineCustomRaceBodyParts();		
	}
	
	public NekoRaceLook(boolean init) {
		super();
		this.associatedCustomizerForm = NekoNewPlayerRaceCustomizer.class;
		if(init) {
			this.partsList = new NekoRaceParts(init);
			this.resetCustomDefault();	
			this.defineCustomRaceBodyParts();			
		}
	}
	
	public NekoRaceLook(int hair, int facialFeature, int hairColor, int skin, int eyeColor, int eyeType, Color shirtColor,
			Color shoesColor, int earsType, int earsColor, int tailStyle, int tailColor) {
		
		this(true);
		this.setHair(hair);
		this.setFacialFeature(facialFeature);
		this.setHairColor(hairColor);
		this.setSkin(skin);
		this.setEyeColor(eyeColor);
		this.setShirtColor(shirtColor);
		this.setShoesColor(shoesColor);
		this.setEyeType(eyeType);
		this.setEarsStyle(earsType);
		this.setEarsColor(earsColor);
		this.setTailStyle(tailStyle);
		this.setTailColor(tailColor);
	}
	

	
	// Note: This does not set valid DEFAULT parts. Just makes sure the keys are there.
	public void defineCustomRaceBodyParts() {		
		// not used in CustomHumanLook
	}
	
	public NekoRaceLook(HumanLook copy) {
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
				0);	
	}	

	public void resetCustomDefault() {
		super.resetBaseDefault();	
	
		this.setTailColor(0);
		this.setTailStyle(0);
		
		this.setEarsColor(0);
		this.setEarsStyle(0);
	}
	
	public NekoRaceLook(NekoRaceLook copy) {
		
		this(	copy.getHair(),
				copy.getFacialFeature(),
				copy.getHairColor(),
				copy.getSkin(),				
				copy.getEyeColor(),
				copy.getEyeType(),
				
				copy.getShirtColor(),
				copy.getShoesColor(),
				copy.getEarsStyle(),
				copy.getEarsColor(),	
				copy.getTailStyle(),
				copy.getTailColor()
						
				);
	}

	public NekoRaceLook(PacketReader pr) {
		super(pr);
	}	
	
	public void randomizeLook(GameRandom random) {
		this.randomizer = random;
		this.randomizeLook();
	}
	
	public void randomizeLook() {
		this.randomizeLook(this.getRandomizer(), false);
	}
	
	public void randomizeLook(boolean onlyHumanLike) {
		this.randomizeLook(this.getRandomizer(), onlyHumanLike);
	}
	
	public int setTailStyle(int id) 	{	return this.appearanceByteSet("TAIL",(byte)id);	}
	
	public int setTailColor(int id) 	{	return this.appearanceByteSet("TAIL_COLOR",(byte)id);	}
	
	public int setEarsStyle(int id) 	{	return this.appearanceByteSet("EARS",(byte)id);	}
	
	public int setEarsColor(int id) 	{	return this.appearanceByteSet("EARS_COLOR",(byte)id);	}
	
	public int getTailStyle() 			{	return this.appearanceByteGet("TAIL");	}
	
	public int getTailColor() 			{	return this.appearanceByteGet("TAIL_COLOR");	}
	
	public int getEarsStyle() 			{	return this.appearanceByteGet("EARS");	}
	
	public int getEarsColor() 			{	return this.appearanceByteGet("EARS_COLOR");	}
	
	public int getRandomTailStyle() 	{	return this.getRandomByteFeature("TAIL");	}
	
	public int getRandomTailColor() 	{	return this.getRandomByteColorFeature("TAIL_COLOR");	}
	
	public int getRandomEarsStyle() 	{	return this.getRandomByteFeature("EARS");	}
	
	public int getRandomEarsColor() 	{	return this.getRandomByteColorFeature("EARS_COLOR");	}
	
	
	public GameTexture getEarsTexture() {
		return GameParts.getFullTexture(this.getRaceParts().getBodyPart("EARS"), getEarsStyle(), getEarsColor());	
	}
		
	public GameTexture getTailTexture() {
		return GameParts.getFullTexture(this.getRaceParts().getBodyPart("TAIL"), getTailStyle(), getTailColor());	
	}
		
	
	@Override
	public CustomHumanDrawOptions modifyHumanDrawOptions(CustomHumanDrawOptions drawOptions, MaskShaderOptions mask) {

		if(this.getEarsStyle() != -1) {
			drawOptions.addBehindDraw(new HumanDrawOptions.HumanDrawOptionsGetter() {		
				@Override
				public DrawOptions getDrawOptions(PlayerMob player, int dir, int spriteX, int spriteY, int spriteRes, int drawX, int drawY,
					int width, int height, boolean mirrorX, boolean mirrorY, GameLight light, float alpha, MaskShaderOptions mask) {
						
					NekoRaceLook rl = (NekoRaceLook)RaceDataFactory.getRaceLook(player, NekoRaceLook.this);
					
					return rl.getEarsTexture().initDraw()
							.sprite(spriteX, spriteY, spriteRes)
							.size(width, height)
							.mirror(mirrorX, mirrorY)					
							.alpha((dir == 2 || dir == 3 || dir == 1) ? alpha : 0)
							.light(light)
							.addMaskShader(mask)
							.pos(drawX, drawY);
				}
			});	
		}
		if(this.getTailStyle() != -1) {
			
			drawOptions.addBehindDraw(new HumanDrawOptions.HumanDrawOptionsGetter() {
				
				@Override
			public DrawOptions getDrawOptions(PlayerMob player, int dir, int spriteX, int spriteY, int spriteRes, int drawX, int drawY,
					int width, int height, boolean mirrorX, boolean mirrorY, GameLight light, float alpha, MaskShaderOptions mask) {
					
					NekoRaceLook rl = (NekoRaceLook)RaceDataFactory.getRaceLook(player, NekoRaceLook.this);
					
					return rl.getTailTexture().initDraw()
							.sprite(spriteX, spriteY, spriteRes)
							.size(width, height)
							.mirror(mirrorX, mirrorY)					
							.alpha((dir == 1 || dir == 3) ? alpha : 0)
							.light(light)
							.addMaskShader(mask)
							.pos(drawX, drawY);						
			}
			});	

			drawOptions.addTopDraw(new HumanDrawOptions.HumanDrawOptionsGetter() {		
				@Override
				public DrawOptions getDrawOptions(PlayerMob player, int dir, int spriteX, int spriteY, int spriteRes, int drawX, int drawY,
					int width, int height, boolean mirrorX, boolean mirrorY, GameLight light, float alpha, MaskShaderOptions mask) {
							
					NekoRaceLook rl = (NekoRaceLook)RaceDataFactory.getRaceLook(player, NekoRaceLook.this);
							
							return rl.getTailTexture().initDraw()
									.sprite(spriteX, spriteY, spriteRes)
									.size(width, height)
									.mirror(mirrorX, mirrorY)					
									.alpha(!(dir == 1 || dir == 3) ? alpha : 0)
									.light(light)
									.addMaskShader(mask)
									.pos(drawX, drawY);
					}
				});		
		}
		if(this.getEarsStyle() != -1) {
			drawOptions.addTopDraw(new HumanDrawOptions.HumanDrawOptionsGetter() {		
				@Override
				public DrawOptions getDrawOptions(PlayerMob player, int dir, int spriteX, int spriteY, int spriteRes, int drawX, int drawY,
					int width, int height, boolean mirrorX, boolean mirrorY, GameLight light, float alpha, MaskShaderOptions mask) {
						
					NekoRaceLook rl = (NekoRaceLook)RaceDataFactory.getRaceLook(player, NekoRaceLook.this);
						
					MaskShaderOptions m = mask;
					if(mask == null) {
						if(dir == 3) {
							m = new MaskShaderOptions(RaceMod.TEX_MASK_RIGHT, 0, 0, 0, 0); 
						}
						else if(dir == 1) {
							m = new MaskShaderOptions(RaceMod.TEX_MASK_LEFT, 0, 0, 0, 0); 
						}
					}
					else {
						if(dir == 3) {
							m = mask.copyAndAddMask(RaceMod.TEX_MASK_RIGHT, 0, 0); 
						}
						else if(dir == 1) {
							m = mask.copyAndAddMask(RaceMod.TEX_MASK_LEFT, 0, 0); 
						}
					}
			
					return rl.getEarsTexture().initDraw()
								.sprite(spriteX, spriteY, spriteRes)
								.size(width, height)
								.mirror(mirrorX, mirrorY)					
								.alpha((dir == 0 || dir == 3 || dir == 1) ? alpha : 0)
								.light(light)
								.addMaskShader(m)
								.pos(drawX, drawY);
				}
			});
		}
		return drawOptions;
	}
	
	@Override
	public Class<? extends FormNewPlayerRaceCustomizer> getAssociatedCustomizerForm() {
		return this.associatedCustomizerForm;
	}
	
	public static void loadRaceTextures() {
		DebugHelper.handleDebugMessage("Loading race textures for race " + NekoRaceLook.NEKO_RACE_ID, 50, MESSAGE_TYPE.DEBUG);
	}


}