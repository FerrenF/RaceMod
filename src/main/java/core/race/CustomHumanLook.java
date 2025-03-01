package core.race;

import java.awt.Color;
import java.util.List;
import java.util.function.Function;

import core.race.parts.HumanRaceParts;
import extensions.HumanNewPlayerRaceCustomizer;
import extensions.RaceLook;
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

public class CustomHumanLook extends RaceLook {
	
	public static final String HUMAN_RACE_ID = "human";
	public CustomHumanLook(GameRandom random, boolean onlyHumanLike) {		
		this(true);
		this.randomizer = random;
		this.randomizeLook(random, onlyHumanLike);
		
	}
	
	public CustomHumanLook() {
		super(HUMAN_RACE_ID);
		this.associatedCustomizerForm = HumanNewPlayerRaceCustomizer.class;
		this.defineCustomRaceBodyParts();		
	}
	
	public CustomHumanLook(boolean init) {
		super(HUMAN_RACE_ID);
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
	
}