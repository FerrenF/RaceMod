package core.race.parts;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

import necesse.engine.GlobalData;
import necesse.gfx.GameEyes;
import necesse.gfx.GameHair;
import necesse.gfx.GameSkin;

public abstract class RaceLookParts{	
	
	private Map<String, BodyPart> bodyParts;
	private Set<String> customizerHiders;
	
	public static Color[] DEFAULT_COLORS = new Color[]{new Color(1644825), new Color(4934475),
			new Color(9868950), new Color(14803425), new Color(14249068), new Color(14221312), new Color(8388608),
			new Color(14262124), new Color(14247168), new Color(8403968), new Color(14276460), new Color(14275840),
			new Color(8420608), new Color(8313196), new Color(2414848), new Color(1409024), new Color(7133621),
			new Color(55696), new Color(32853), new Color(7127001), new Color(42713), new Color(25216),
			new Color(7107289), new Color(3033), new Color(1664), new Color(10775769), new Color(7340249),
			new Color(4325504), new Color(14249157), new Color(14221489), new Color(8388712)};
	
	public RaceLookParts() {
		  this.bodyParts = new HashMap<>();
		  this.customizerHiders = new HashSet<>();
		}
	
	public RaceLookParts(boolean init) {
      this();
      if(init) this.defineRaceBodyParts();
    }

	
    public void addBodyPart(String partName, BodyPart part) {
	    bodyParts.put(partName, part);
	}
	    

    
    // Retrieve body part by name
    // If an underscore is present in the partName, it will attempt to find a part name matching the characters up to the first underscore.
    // This is done to support the base game's separation of parts and colors with different identifiers.
    // New races have the colors built in to the BodyPart class.
    
    public BodyPart getBodyPart(String partName) {
    	if(partName==null) return null;
    	// test reduction to base name
    	if(partName.contains("_")) {
	    	BodyPart try1 = (BodyPart)(bodyParts.getOrDefault(partName.subSequence(0, partName.lastIndexOf('_')), null));    	
	    	if(try1 != null) return try1;    	
    	}
        return (BodyPart)(bodyParts.get(partName));
    }
        

    public List<BodyPart> getBodyParts() {
    	if (bodyParts == null) return List.of();
    	return new ArrayList<>(bodyParts.values());
    }
    
    public List<BodyPart> getCustomBodyParts() {
    	if (bodyParts == null) return List.of();
    	return new ArrayList<>(bodyParts.values()).stream().filter((part)->!part.isBaseGamePart()).toList();
    }
    
    public List<BodyPart> getReplacerParts() {
    	if (bodyParts == null) return new ArrayList<>();
    	return new ArrayList<>(bodyParts.values()).stream().filter((part)->part.isReplacerPart()).toList();
    }
    
    public void defineRaceBodyParts() {
	  this.addBodyPart("SKIN_COLOR", new BodyPart("SKIN_COLOR", "skincolor", tryGetBaseGameGfx(()->GameSkin.getTotalSkins())));
      this.addBodyPart("EYE_TYPE", new BodyPart("EYE_TYPE", "eyetype", tryGetBaseGameGfx(()->GameEyes.getTotalEyeTypes())));
      this.addBodyPart("EYE_COLOR", new BodyPart("EYE_COLOR", "eyecolor", tryGetBaseGameGfx(()->GameEyes.getTotalColors())));
      this.addBodyPart("HAIR_STYLE", new BodyPart("HAIR_STYLE", "hairstyle", tryGetBaseGameGfx(()->GameHair.getTotalHair())));
      this.addBodyPart("FACIAL_HAIR", new BodyPart("FACIAL_HAIR", "facialhair", tryGetBaseGameGfx(()->GameHair.getTotalFacialFeatures())));
      this.addBodyPart("HAIR_COLOR", new BodyPart("HAIR_COLOR", "haircolor", tryGetBaseGameGfx(()->GameHair.getTotalHairColors())));      
      this.addBodyPart("SHIRT_COLOR", new BodyPart("SHIRT_COLOR", "shirtcolor", DEFAULT_COLORS.length));
      this.addBodyPart("SHOES_COLOR", new BodyPart("SHOES_COLOR", "shoescolor", DEFAULT_COLORS.length));   
    }

    public static int tryGetBaseGameGfx(Supplier<Integer> getter) {
    	if(GlobalData.isServer()) {
    		return 0;
    	}
    	return getter.get();
    }
	public Color defaultColors(int id) {
		return DEFAULT_COLORS[id];
	}
	
	public Color[] defaultColors() {
		return DEFAULT_COLORS;
	}
	
	public boolean isHiddenPart(String key) {
		return this.getHiddenParts().contains(key);
	}
	
	public Set<String> getHiddenParts()	{
	   return customizerHiders;
	}
	
	public boolean hasCustomPart(String key) {
		return hasPart(key) && !this.bodyParts.get(key).isBaseGamePart();
	}
	
	public boolean hasPart(String key) {
		return this.bodyParts.containsKey(key) || this.bodyParts.containsKey(key.replace(BodyPart.PART_COLOR_NAME_SUFFIX, ""));
	}
	
	public boolean hidePartCustomizer(String key) {
		if(this.hasPart(key)) {
			this.getHiddenParts().add(key);
			return true;
		}
		return false;
	}
}