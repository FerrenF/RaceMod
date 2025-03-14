package core.race;

import java.awt.Color;

import core.forms.FormNewPlayerRaceCustomizer;
import core.forms.HumanNewPlayerRaceCustomizer;
import core.race.parts.HumanRaceParts;
import core.race.parts.RaceLookParts;
import core.race.parts.TestFurryRaceParts;
import helpers.DebugHelper;
import helpers.DebugHelper.MESSAGE_TYPE;
import necesse.engine.network.PacketReader;
import necesse.engine.util.GameRandom;
import necesse.entity.mobs.MaskShaderOptions;
import necesse.gfx.HumanLook;
import necesse.gfx.drawOptions.human.HumanDrawOptions;
import necesse.gfx.gameTexture.GameTexture;

public class CustomHumanLook extends RaceLook {
	
	public static GameTexture raceCustomizerIcon;
	public static final String HUMAN_RACE_ID = "human";
	public CustomHumanLook(GameRandom random, boolean onlyHumanLike) {		
		this(true);
		this.randomizer = random;
		this.randomizeLook(random, onlyHumanLike);	
	}
	
	public static CustomHumanLook getCustomRaceLook(RaceLook _look) {	
		if (!_look.getRaceID().equals(CustomHumanLook.HUMAN_RACE_ID)) {
			DebugHelper.handleDebugMessage(String.format("Draw options for raceID %s requested for non-raceID %s from %s. Using defaults.", TestFurryRaceLook.TEST_FURRY_RACE_ID, _look.getRaceID(), _look.getClass().getName()), 25);
			return new CustomHumanLook(true);
		}
		return (CustomHumanLook)_look;
	}
	
	public CustomHumanLook() {
		super(HUMAN_RACE_ID);
		this.associatedCustomizerForm = HumanNewPlayerRaceCustomizer.class;
		this.defineCustomRaceBodyParts();		
	}
	
	public CustomHumanLook(boolean init) {
		super();
		this.associatedCustomizerForm = HumanNewPlayerRaceCustomizer.class;
		if(init) {
			this.partsList = new HumanRaceParts(init);
			this.resetCustomDefault();	
			this.defineCustomRaceBodyParts();			
		}
	}
	
	public CustomHumanLook(int hair, int facialFeature, int hairColor, int skin, int eyeColor, int eyeType, Color shirtColor,
			Color shoesColor) {
		
		this(true);
		this.setHair(hair);
		this.setFacialFeature(facialFeature);
		this.setHairColor(hairColor);
		this.setSkin(skin);
		this.setEyeColor(eyeColor);
		this.setShirtColor(shirtColor);
		this.setShoesColor(shoesColor);
		this.setEyeType(eyeType);
	}
	

	
	// Note: This does not set valid DEFAULT parts. Just makes sure the keys are there.
	public void defineCustomRaceBodyParts() {		
		// not used in CustomHumanLook
	}
	
	public CustomHumanLook(HumanLook copy) {
		this(	copy.getHair(),
				copy.getFacialFeature(),
				copy.getHairColor(),
				copy.getSkin(),
				copy.getEyeColor(),
				copy.getEyeType(),
				copy.getShirtColor(),
				copy.getShoesColor()	);	
	}	

	public void resetCustomDefault() {
		super.resetBaseDefault();	
	}
	
	public CustomHumanLook(CustomHumanLook copy) {
		super(copy);	
	}

	public CustomHumanLook(PacketReader pr) {
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

	@Override
	public HumanDrawOptions modifyHumanDrawOptions(HumanDrawOptions drawOptions, MaskShaderOptions mask) {
		
		return drawOptions;
	}
	
	@Override
	public Class<? extends FormNewPlayerRaceCustomizer> getAssociatedCustomizerForm() {
		return this.associatedCustomizerForm;
	}
	
	public static void loadRaceTextures() {
		DebugHelper.handleDebugMessage("Loading race textures for race " + CustomHumanLook.HUMAN_RACE_ID, 50, MESSAGE_TYPE.DEBUG);
	}

	@Override
	public String getRaceID() {
		return CustomHumanLook.HUMAN_RACE_ID;
	}

}