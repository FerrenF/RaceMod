package core.gfx;

import java.awt.Color;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.function.Function;

import core.race.parts.EyeBodyPart;
import helpers.DebugHelper;
import helpers.DebugHelper.MESSAGE_TYPE;
import necesse.engine.util.GameRandom;
import necesse.gfx.AbstractGameTextureCache;
import necesse.gfx.GameSkinCache;
import necesse.gfx.GameSkinColors;
import necesse.gfx.gameTexture.GameTexture;

public class EyeTypeGamePart extends GameParts {
	
	protected static GameTexture EYES_CLOSED_TEXTURE;	
	protected static final String EYE_CLOSED_TEXTURE_NAME = "eyes_closed";	
	protected GameTexture defaultClosedTexture;
	protected GameSkinColors skinColors;
	protected GameSkinColors eyeColors;
	protected int _eyeColorCount;
	protected int _skinColorCount;
	public ArrayList<EyeTypeTextureSet> eyeSets;
	
	public class EyeTypeTextureSet{
		public final int eyeIndex;
		public ArrayList<GameTexture> openEyeColorTextures = new ArrayList<GameTexture>();
		public ArrayList<GameTexture> openSkinColorTextures = new ArrayList<GameTexture>();
		public ArrayList<GameTexture> closedEyeColorTextures = new ArrayList<GameTexture>();
		public ArrayList<GameTexture> closedSkinColorTextures = new ArrayList<GameTexture>();
		
		public EyeTypeTextureSet(int _eyeIndex) {
			this.eyeIndex = _eyeIndex;			
		}
	}
	
	protected EyeTypeGamePart(EyeBodyPart assignedPart) {
		super(assignedPart);
	}
	
	public EyeTypeGamePart(GamePartsLoader loader, EyeBodyPart assignedPart) {
		super(loader, assignedPart);		
	}
	
	// This should be the last public method leading into the initialization of the current GameParts instance.
	public void loadPart(GamePartsLoader loader) {		
		
		EyeTypeGamePart newPart = new EyeTypeGamePart((EyeBodyPart)this.assignedPart);
		GameSkinCache cache = new GameSkinCache(this.getPartPath());		
		cache.loadCache();
		
		skinColors = new GameSkinColors();
		eyeColors = new GameSkinColors();
				
		this.loadPartColors();
		this.loadSkinColors();

		defaultClosedTexture = GameTexture.fromFile(this.getPartPath() + this.getPartName() + "_closed", true);
		
		this._textureCount = this.tryFindResources();			
			
		// Other Part
		this.eyeSets = new ArrayList<EyeTypeTextureSet>(this._textureCount);
				
		if(!this.texturesInitialized && this._textureCount > 0)	{						
			this.loadEyeTextures(loader, cache);	
			DebugHelper.handleDebugMessage( String.format("Loading full textures: part %s loaded %d full textures.",
					this.getPartName(),
					this._textureCount),
					70, MESSAGE_TYPE.DEBUG );
						
		} else {
			
			DebugHelper.handleDebugMessage( String.format("Loading textures: part %s has 0 texture count during load.",
					this.getPartName()),
					70, MESSAGE_TYPE.DEBUG  );
			
		}
				
		loader.waitForCurrentTasks();
		cache.saveCache();	
		
		// Finally, set the values on the new part and then add it to the static array.
		newPart.skinColors = this.skinColors;
		newPart.eyeColors = this.eyeColors;
		newPart._textureCount = this._textureCount;
		newPart._colorCount = this._colorCount;
		newPart.colors = this.colors;
		newPart._eyeColorCount = this._eyeColorCount;
		newPart.eyeSets = this.eyeSets;
		newPart.fullTextures = this.fullTextures;		
		parts.add(newPart);
	}
	
	public int getSkinColorCount() 			{	return  _skinColorCount;	}			
	public int getEyeColorCount() 			{	return  _eyeColorCount;	}			
	
	
	private void loadClosedColorTextures(GamePartsLoader loader, AbstractGameTextureCache cache,
			boolean makeFinal, int eyeIndex, GameTexture originalTexture, ArrayList<GameTexture> eyeColorTextures,
			ArrayList<GameTexture> skinColorTextures) {
		
		int i;
		for (i = 0; i < eyeColors.getSize(); ++i) {
			this.loadTexture(	loader,
								cache,
								"closedEye",
								makeFinal,
								eyeIndex,								
								originalTexture,
								eyeColorTextures,								
								i,
								eyeColors,							
								skinColors);
		}

		for (i = 0; i < skinColors.getSize(); ++i) {
			
			this.loadTexture(	loader,
					cache,
					"closedSkin",
					makeFinal,
					eyeIndex,								
					originalTexture,
					skinColorTextures,								
					i,
					skinColors,							
					eyeColors);
			
		}

	}

	private void loadOpenColorTextures(GamePartsLoader loader, AbstractGameTextureCache cache, boolean makeFinal,
			int eyeIndex, GameTexture originalTexture, ArrayList<GameTexture> eyeColorTextures,
			ArrayList<GameTexture> skinColorTextures) {
		
		int i;
		for (i = 0; i < eyeColors.getSize(); ++i) {			
			this.loadTexture(	loader,
					cache,
					"openEye",
					makeFinal,
					eyeIndex,								
					originalTexture,
					eyeColorTextures,								
					i,
					eyeColors,							
					skinColors);
		}

		for (i = 0; i < skinColors.getSize(); ++i) {
			
			this.loadTexture(	loader,
					cache,
					"openSkin",
					makeFinal,
					eyeIndex,								
					originalTexture,
					skinColorTextures,								
					i,												
					skinColors,
					eyeColors);
			
		}

	}
	
	protected void loadEyeTextures(GamePartsLoader loader, GameSkinCache cache) {
		
	    if (this.hasTextures()) {	        	
	        for(int eyeIndex = 0; eyeIndex < this._textureCount;eyeIndex++) {
	        	EyeTypeTextureSet loading = new EyeTypeTextureSet(eyeIndex);    
	        
	        	GameTexture closedTexture;
	        	String fn = this.getPartPath() + this.getPartName().toLowerCase() + String.valueOf(eyeIndex+1);
	        	try {
	    			closedTexture = GameTexture.fromFileRaw(fn, true);
	    			loadOpenColorTextures(loader, cache, true, eyeIndex, closedTexture, loading.openEyeColorTextures,
	    					loading.openSkinColorTextures);
	    			
	    		} catch (FileNotFoundException var10) {
	    			DebugHelper.handleFormattedDebugMessage("Failed to load open %s texture index %d from file %s. %s", 25, MESSAGE_TYPE.ERROR, new Object[] {this.getPartName(), eyeIndex, fn, var10.getMessage()});
	    		}
	        	
	        	fn = this.getPartPath() + this.getPartName().toLowerCase() + "_closed"; 	
	        	try {
	    			closedTexture = GameTexture.fromFileRaw(fn, true);
	    			loadClosedColorTextures(loader, cache, true, eyeIndex, closedTexture, loading.closedEyeColorTextures,
	    					loading.closedSkinColorTextures);
	    		} catch (FileNotFoundException var10) {
	    			DebugHelper.handleFormattedDebugMessage("Failed to load closed %s texture index %d from file %s. %s", 25, MESSAGE_TYPE.ERROR, new Object[] {this.getPartName(), eyeIndex, fn, var10.getMessage()});
	    		}	        	
	        	
	        	this.eyeSets.add(eyeIndex, loading);
	        }
	    }
	}


	
	//colors.removeColors(texture, excludes, removeTones);
	protected void loadSkinColors() {
		String colorPath = this.getPartSkinColorPath();	
        GameTexture colorTexture = GameTexture.fromFile(colorPath, true);
        if(colorTexture == null) {
        	DebugHelper.handleDebugMessage(
			        String.format("Could not find skin colors for eye part %s at %s",this.getPartName(), this.getPartSkinColorPath()), 
			        25, MESSAGE_TYPE.WARNING
			    );
        }
        this.skinColors.addBaseColors(colorTexture, 0, 1, colorTexture.getWidth() - 1);  	     
        this._skinColorCount = this.skinColors.getSize();  
		DebugHelper.handleDebugMessage(
		        String.format("Loaded %d skin colors for part: %s", this._colorCount, this.getPartName()), 
		        70, MESSAGE_TYPE.DEBUG
		    );				
	}
	
	protected void loadPartColors() {
		String colorPath = this.getPartColorPath();				
        GameTexture colorTexture = GameTexture.fromFile(colorPath, true);
        if(colorTexture == null) {
        	DebugHelper.handleDebugMessage(
			        String.format("Could not find skin colors for eye part %s at %s",this.getPartName(), this.getPartColorPath()), 
			        25, MESSAGE_TYPE.WARNING
			    );
        }
        this.eyeColors.addBaseColors(colorTexture, 0, 1, colorTexture.getWidth() - 1);  	     
        this._eyeColorCount = this.eyeColors.getSize();          
		DebugHelper.handleDebugMessage(
		        String.format("Loaded %d colors for part: %s", this._eyeColorCount, this.getPartName()), 
		        70, MESSAGE_TYPE.DEBUG
		    );		
	}
	
	private String getPartSkinColorPath() {
		return ((EyeBodyPart)this.assignedPart).getSkinColorPath();
	}
	
	public <T> List<T> getOpenColorTextures(int eyeIndex, int eyeColor, int skinColor,
			Function<GameTexture, T> mapper) {
		
		EyeTypeTextureSet r = getEyes(eyeIndex);
		
		GameTexture skinColorTexture = (GameTexture) r.openSkinColorTextures
				.get( skinColor %  r.openSkinColorTextures.size());
		GameTexture eyeColorTexture = (GameTexture) r.openEyeColorTextures
				.get( eyeColor % r.openEyeColorTextures.size());
		
		return Arrays.asList(mapper.apply(skinColorTexture), mapper.apply(eyeColorTexture));
	}

	public <T> List<T> getClosedColorTextures(int eyeIndex, int eyeColor, int skinColor, 
			Function<GameTexture, T> mapper) {
		
		EyeTypeTextureSet r = getEyes(eyeIndex);
		
		GameTexture skinColorTexture = (GameTexture) r.closedSkinColorTextures
				.get( skinColor %  r.openSkinColorTextures.size());
		GameTexture eyeColorTexture = (GameTexture) r.closedEyeColorTextures
				.get( eyeColor % r.openEyeColorTextures.size());
		
		return Arrays.asList(mapper.apply(skinColorTexture), mapper.apply(eyeColorTexture));
	}

	public List<GameTexture> getOpenColorTextures(int eyeIndex, int eyeColor, int skinColor) {
		return this.getOpenColorTextures(eyeIndex, eyeColor, skinColor,  (t) -> {
			return t;
		});
	}

	public List<GameTexture> getClosedColorTextures(int eyeIndex, int eyeColor, int skinColor) {
		return this.getClosedColorTextures(eyeIndex, eyeColor, skinColor, (t) -> {
			return t;
		});
	}

	public int getTotalEyeTypes() {
		return this._textureCount;
	}

	public int getTotalColors() {
		return this._eyeColorCount;
	}

	public  EyeTypeTextureSet getEyes(int eyeType) {
		return this.eyeSets.get(eyeType % this.eyeSets.size());
	}

	
	/*private static void loadTexture(GameSkinLoader loader, AbstractGameTextureCache cache, boolean makeFinal,
			int eyeIndex, GameTexture originalTexture, String cachePrefix, ArrayList<GameTexture> list, int index,
			GameSkinColors colors, GameSkinColors... removeTones) {*/
	// Loads the full texture in cid color tone
	public void loadTexture(GamePartsLoader loader,
				AbstractGameTextureCache cache,
				String cachePrefix,				
				boolean makeFinal,
				int eyeTextureID,
				GameTexture originalTexture,
				ArrayList<GameTexture> targetList,
				int targetIndex,
				GameSkinColors colors,
				GameSkinColors... removeTones ) {
						
			if(originalTexture == null) {
				DebugHelper.handleDebugMessage(String.format("Failed to load full size textures for part %s, texture id %d: Original texture is null.", this.getPartName(),eyeTextureID),25, MESSAGE_TYPE.ERROR);
			}
			
			String cacheKey = String.format("%s_%d_%d_%d",
			        cachePrefix.toLowerCase(),           // Cache prefix (category/type)
			        eyeTextureID,                        // Eye texture ID              
			        originalTexture.hashCode(),          // Hash of the texture (for uniqueness)
			        targetIndex                          // Target index, to distinguish different positions or textures
			);	
			int hash = originalTexture.hashCode() + GameRandom.prime(10) * (eyeTextureID + targetIndex) + cacheKey.hashCode();
			
			DebugHelper.handleDebugMessage(String.format("Loading full texture: cache key %s with texture id %d", cacheKey, eyeTextureID),70, MESSAGE_TYPE.DEBUG);
			
			AbstractGameTextureCache.Element element = cache.get(cacheKey);
			if (element != null && element.hash == hash) {
				try {
					GameTexture texture = new GameTexture(String.format("cached %s", cacheKey), element.textureData);
					
					if (originalTexture != null) {
					    originalTexture.delete();
					}
					if (makeFinal) {
						texture.makeFinal();
					}					
					loader.addToList(targetList, targetIndex, texture);
					DebugHelper.handleDebugMessage(String.format("Found full texture in cache: %s", cacheKey),70, MESSAGE_TYPE.DEBUG);
					return;
					
				} catch (Exception var12) {
					DebugHelper.handleDebugMessage(String.format("Could not load cache for %s:" + var12.getMessage(), cacheKey),10, MESSAGE_TYPE.ERROR);
				}
			} else {
				DebugHelper.handleDebugMessage(String.format("Detected invalid: %s", cacheKey),70, MESSAGE_TYPE.DEBUG);
			}

			loader.triggerFirstTimeSetup();
			DebugHelper.handleDebugMessage(String.format("Generating new: %s", cacheKey),70, MESSAGE_TYPE.DEBUG);	
			
			GameTexture _texture = new GameTexture(originalTexture);	
			loader.submitTaskAddToList(targetList, targetIndex,  null,() -> {
				HashSet<Color> excludes = new HashSet<>();
				colors.replaceColors(_texture, targetIndex, excludes);
				colors.removeColors(_texture, excludes, removeTones);							
				_texture.runPreAntialias(false);
				cache.set(cacheKey, hash, _texture);
				return _texture;
			}, makeFinal);		
		}
	
}
