package core.race.parts;
import java.awt.Point;
import core.gfx.TextureReplacer;

public class BodyPart {
	
	public static int STYLIST_COST_DEFAULT = 100;
	public static String COLOR_LABEL_SUFFIX = "_color";
	public static String PART_COLOR_NAME_SUFFIX = "_COLOR";
	
	private Class<? extends RaceLookParts> belongsToClass;
	
	protected String name;
    protected String labelKey;
    protected String labelCategory;
    
    protected int _numTextures;
    protected int _numColors;
    protected int _numSides;
    protected int _stylistCost = STYLIST_COST_DEFAULT;
    protected boolean _hasWigTexture;
    protected boolean _isBaseGamePart;
    protected boolean _stylistCostIsShards = false;
    
	protected String palettePath;
	protected String texturePath;
    
	protected Point textureSpriteMapSize;  
	protected Point accessoryTextureMapSize;
    
	protected TextureReplacer partReplacer;
    
	public Class<? extends RaceLookParts> getOwnerClass() { return this.belongsToClass;}
	
	public int numTextures() 					{	return _numTextures;					}
	public int numColors() 						{	return _numColors;						}
	public int numSides() 						{	return _numSides;						}
	
	public boolean isReplacerPart()				{	return partReplacer != null; 			}
    public boolean hasWigTexture() 				{	return _hasWigTexture;					}	
    public boolean isBaseGamePart() 			{	return _isBaseGamePart;					}
    
	public String getTexturePath() 				{	return texturePath;						}	
	public String getColorPath() 				{	return this.palettePath;				}	
	
	public Point getTextureSpriteMapSize() 		{	return textureSpriteMapSize;			}
	public Point getAccessoryTextureMapSize() 	{	return accessoryTextureMapSize;			}
	
    public String getPartName() 				{  	return name;   							}   
    public String getPartColorName() 			{ 	return name + PART_COLOR_NAME_SUFFIX;  	}
    
    public String getLabelKey() 				{  	return this.labelKey;    				}
    public String getLabelColorKey() 			{	return getLabelKey() + COLOR_LABEL_SUFFIX;	}
	public String getLabelCategory() 			{	return this.labelCategory;				}
	
	public int stylistCost() 					{	return _stylistCost;					}
	public boolean stylistCostIsShards() 		{	return _stylistCostIsShards;			}
    

	
	
	// Constructor for base game parts
	public BodyPart(String name,
			String labelKey,
			int numTextureOptions,
			int numColorOptions) {
			
        this.name = name;
        this.labelKey = labelKey;
        this.labelCategory = "ui";
        this._isBaseGamePart = true;
        this._numColors = numColorOptions;
        this._numTextures = numTextureOptions;
        this.texturePath = null;
        this.palettePath = null;
        this.belongsToClass = HumanRaceParts.class;
        this.partReplacer=null;
    }
        
    public BodyPart(Class<? extends RaceLookParts> belongsToClass,
    		String name,
    		String labelCategory,
    		String labelKey,
    		int numTextures,
    		int numSides,
    		int numColors,
    		boolean hasWigTexture,
    		String colorPath,
    		String texturePath,    		
    		Point spriteMapSize,
    		Point accessoryTextureMapSize,
    		TextureReplacer replacer,
    		int stylistCost,
    		boolean stylistCostIsShards) {
    	
    	this.belongsToClass = belongsToClass;
    	this.labelCategory = labelCategory;
        this.name = name;
        this.labelKey = labelKey;       
    	this._numSides = numSides;
    	this._hasWigTexture = hasWigTexture;
        this._numColors = numColors;
        this.palettePath = colorPath;
        this.texturePath = texturePath;
        this.textureSpriteMapSize = spriteMapSize;
        this.accessoryTextureMapSize = accessoryTextureMapSize;
        this.partReplacer = replacer;
        this._stylistCost = stylistCost;
        this._stylistCostIsShards = stylistCostIsShards;
    }

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
