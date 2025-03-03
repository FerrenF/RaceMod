package core.race.parts;

import java.awt.Point;

import core.gfx.GameParts;
import core.gfx.TextureReplacer;

public class BodyPart {
	
	public static String COLOR_LABEL_SUFFIX = "_color";
	public static String PART_COLOR_NAME_SUFFIX = "_COLOR";
	private Class<? extends RaceLookParts> belongsToClass;
	
	private int totalOptions = 0;
	
    private String name;
    private String labelKey;
    private String labelCategory;
    
    private boolean hasTexture;
    private boolean isBaseGamePart;
    private boolean hasColor;
    private boolean hasWigTexture;
    private boolean hasSeparateWigTexture;
    private boolean hasSides;
    private boolean hasLastRowAccessoryMap;
    
	private String colorPath;
    private String texturePath;
    
    private Point textureSpriteMapSize;  
    private Point accessoryTextureMapSize;
    
 
    private TextureReplacer partReplacer;
    
    public boolean isHasSeperateWigTexture() 	{	return hasSeparateWigTexture;	}

	public boolean isHasLastRowAccessoryMap() 	{	return hasLastRowAccessoryMap;	}

	public Point getAccessoryTextureMapSize() 	{	return accessoryTextureMapSize;	}

	public boolean isBaseGamePart() 			{	return isBaseGamePart;	}
    
	public boolean isHasTexture() 				{	return hasTexture;	}
	
	public boolean isReplacerPart()				{	return partReplacer != null; }
	
	public boolean isHasColor() 				{	return hasColor;	}
	
	public String getTexturePath() 				{	return texturePath;	}	

    public boolean isHasWigTexture() 			{	return hasWigTexture;	}
    
	public Point getTextureSpriteMapSize() 		{	return textureSpriteMapSize;	}
	
    public String getPartName() 				{  	return name;   }
    
    public String getPartColorName() 			{ 	return name + PART_COLOR_NAME_SUFFIX;  }
    
    public String getLabelKey() 				{  	return this.labelKey;    }
    
	public String getLabelCategory() 			{	return this.labelCategory;	}
	
	public String getColorPath() 				{	return this.colorPath;	}	
	
	public boolean isHasSides() 				{	return hasSides;	}
    
	public Class<? extends RaceLookParts> getOwnerClass() { return this.belongsToClass;}
	public String getLabelColorKey() 			{		return getLabelKey() + COLOR_LABEL_SUFFIX;	}
	
	public BodyPart(String name,
			String labelKey,
			int totalOptions) {
		
    	// used for base game parts
        this.name = name;
        this.totalOptions = totalOptions;
        this.labelKey = labelKey;
        this.labelCategory = "ui";
        this.isBaseGamePart = true;
        this.hasColor = false;
        this.hasTexture = false;
        this.texturePath = null;
        this.colorPath = null;
        this.belongsToClass = HumanRaceParts.class;
        
    }
    
    public BodyPart(Class<? extends RaceLookParts> belongsToClass,
    		String name,
    		String labelCategory,
    		String labelKey) {
    	
    	this.belongsToClass = belongsToClass;
    	this.labelCategory = labelCategory;
        this.name = name;
        this.labelKey = labelKey;
    }
    
    public BodyPart(Class<? extends RaceLookParts> belongsToClass,
    		String name,
    		String labelCategory,
    		String labelKey,
    		boolean hasTexture,
    		boolean hasSides,
    		boolean hasColor,
    		boolean hasWigTexture,
    		boolean hasSeparateWigTexture,
    		boolean hasLastRowAccessoryMap,
    		String colorPath,
    		String texturePath,    		
    		Point spriteMapSize,
    		Point accessoryTextureMapSize,
    		TextureReplacer replacer) {
    	
    	this(belongsToClass, name, labelCategory, labelKey);
        
    	this.hasSides = hasSides;
    	this.hasWigTexture = hasWigTexture;
        this.hasTexture = hasTexture;
        this.hasColor = hasColor;
        this.colorPath = colorPath;
        this.texturePath = texturePath;
        this.textureSpriteMapSize = spriteMapSize;
        this.hasLastRowAccessoryMap = hasLastRowAccessoryMap;
        this.hasSeparateWigTexture = hasSeparateWigTexture;
        this.accessoryTextureMapSize = accessoryTextureMapSize;
        
    }


    public int getTotalTextureOptions() {  	return this.isBaseGamePart() ? this.totalOptions : GameParts.getPartTextureOptionsCount(this);    }
    public int getTotalColorOptions() 	{  	return this.isBaseGamePart() ? this.totalOptions : GameParts.getPartColorOptionsCount(this);    }
    

	public Class<? extends RaceLookParts> getRacePartsClass() {
		return this.belongsToClass;
	}

	public boolean equals(Object other) {
		return ( ((BodyPart)other).belongsToClass.equals(this.belongsToClass) &&
				((BodyPart)other).getPartName().equals(this.getPartName()));
	}

	public TextureReplacer getReplacer() {
		return this.partReplacer;
	}

}
