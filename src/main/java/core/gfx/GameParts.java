package core.gfx;

import java.awt.Point;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import core.RaceMod;
import core.race.parts.BodyPart;
import core.race.parts.RaceLookParts;
import extensions.RaceLook;
import necesse.engine.modLoader.LoadedMod;
import necesse.engine.util.GameRandom;
import necesse.engine.util.TicketSystemList;
import necesse.gfx.AbstractGameTextureCache;
import necesse.gfx.GameResources;
import necesse.gfx.GameSkinCache;
import necesse.gfx.GameSkinColors;
import necesse.gfx.gameTexture.GameTexture;
import necesse.gfx.res.ResourceEncoder;
import necesse.gfx.res.ResourceFile;

public class GameParts {
	
	private static final ArrayList<GameParts> parts = new ArrayList<GameParts>();
	private static final String PATH_PREFIX = "";
	private static final String TEXTURE_SUFFIX = ".png";
	private static final String WIG_TEXTURE_NAME = "wig";
	private static final Set<String> SIDE_SUFFIXES = Set.of(new String[]{"_L","_R"});
	private static Point wigAccessoryLocation = new Point(0, -1);
	public final BodyPart assignedPart;	
	
	public GameSkinColors colors;	
	private GameTexture originalTexture;
	private GameTexture fullWigTexture;	
	private Map<Integer, ArrayList<GameTexture>> fullTextures;	
	private Map<Integer, ArrayList<GameTexture>> wigTextures;	
		
	private boolean texturesInitialized = false;
		
	private int _textureCount = 0;
	private int _colorCount = 1;
	
	public Class<? extends RaceLookParts> getRacePartsClass() 		{	return this.assignedPart.getRacePartsClass();		}	
	
	public String getPartName() 									{	return this.assignedPart.getPartName();		}
	
	public boolean hasSides()			{	return this.assignedPart.isHasSides();						}
	
	public Point accessoryMapSize()		{	return this.assignedPart.getAccessoryTextureMapSize();		}
	
	public Point textureMapSize()		{	return this.assignedPart.getTextureSpriteMapSize();			}
	
	public boolean hasTextures() 		{	return this.assignedPart.isHasTexture();					}
	
	public boolean hasWig() 			{	return this.assignedPart.isHasWigTexture();					}
	
	public boolean hasColors() 			{	return this.assignedPart.isHasColor();						}
	
	public boolean isBaseGamePart() 	{	return this.assignedPart.isBaseGamePart();					}
	
	public String getPartPath() 		{	return PATH_PREFIX+this.assignedPart.getTexturePath();		}
	
	public String getPartColorPath() 	{	return PATH_PREFIX+this.assignedPart.getColorPath();		}
	
	public String getWigPath() 			{	return PATH_PREFIX+this.assignedPart.getTexturePath()+WIG_TEXTURE_NAME;		}
	
	public int getTextureCount() 		{	return  _textureCount;										}

	public int getColorCount() 			{	return  _colorCount;	}				
	
	public boolean hasAccessoryMap()		{   return this.assignedPart.isHasLastRowAccessoryMap(); }
	
	public boolean hasSeparateWigTexture()	{	return this.assignedPart.isHasSeperateWigTexture(); }
	
	// This constructor is used after the textures have been found and added into a copy of a newly initialized GameParts class. It represents an 'initialized' state.
	private GameParts(BodyPart assignedPart) {
		this.assignedPart = assignedPart;
		this.texturesInitialized = true;
	}
	
	// When this constructor is called, we start loading resources based off of the information in the BodyPart.
	public GameParts(GamePartsLoader loader, BodyPart assignedPart) {
		this.assignedPart = assignedPart;
		this.loadPart(loader);
	}
	
	// This should be the last public method leading into the initialization of the current GameParts instance.
	public void loadPart(GamePartsLoader loader) {		
		
		if(this.isBaseGamePart()) return; // Something else should handle this. Don't waste any more resources.
		
		GameParts newPart = new GameParts(this.assignedPart);
		GameSkinCache cache = new GameSkinCache(this.getPartPath());		
		cache.loadCache();
		this.colors = new GameSkinColors();
		
		if(this.hasColors()) {			
			this.loadPartColors();
			loader.waitForCurrentTasks();
		}

		if(this.hasTextures()) {
			// First, try and find the textures. Count them, and store the number we found.
			this._textureCount = this.tryFindResources();
			
			if(this.hasSides()) {
				this._textureCount/=2;
			}
			// Other Part
			this.wigTextures 	= 	new HashMap<Integer, ArrayList<GameTexture>>();
			this.fullTextures 	= 	new HashMap<Integer, ArrayList<GameTexture>>();
			
			for(int i=0;i<_colorCount;i++){
				fullTextures.put(i, new ArrayList<GameTexture>());		
				wigTextures.put(i, new ArrayList<GameTexture>());		
			}			
		
			int cc =  this.getColorCount();
			if (this._textureCount > 0) {
			    for (int c = 0; c < cc; c++) {
			        for (int i = 0; i < _textureCount * (this.hasSides() ? 2 : 1); i++) {    
			        	this.wigTextures.get(c).add(null);
			        	this.fullTextures.get(c).add(null);
			        }
			    }			
			}
		}
		
		if(!this.texturesInitialized && this._textureCount > 0)	{
						
			this.loadPartFullTextures(loader, cache);
			loader.waitForCurrentTasks();
			RaceMod.handleDebugMessage( String.format("Loading full textures: part %s loaded %d full textures.",
					this.getPartName(),
					this.fullTextures.size()),
					70  );
						
		} else {
			
			RaceMod.handleDebugMessage( String.format("Loading wig textures: part %s has 0 texture count during load.",
					this.getPartName()),
					70  );
			
		}
				
		loader.waitForCurrentTasks();
		
		if(this.hasWig()) {
			
			if(this.assignedPart.isHasSeperateWigTexture()) {
				
				loadPartWigTexture();			
	
				if(this.fullWigTexture != null) {
					
					RaceMod.handleDebugMessage(String.format("Loaded wig texture for part: %s",this.getPartName()), 70);	
					loadPartWigTextures(loader, cache);
					
				}
				else {
					
					RaceMod.handleDebugMessage( String.format("Wig texture is null for part %s.", this.getPartName()), 70  );
					return;
					
				}
			}
			else {
				
				if(this.assignedPart.isHasLastRowAccessoryMap() && this._textureCount > 0) {
					
					this.loadPartIncludedWigTextures(loader, cache);
					
				}
				else {
					
					RaceMod.handleDebugMessage( String.format("Wig texture for part %s is specified as not separate, but the texture does not have an accessory map.", this.getPartName()), 70  );
					return;
					
				}
				
			}
					
		}
		else {			
			RaceMod.handleDebugMessage( String.format("Loading wig textures: part %s does not have a wig texture but specifies that it does.", this.getPartName()), 70  );
		}
		
		cache.saveCache();
		loader.waitForCurrentTasks();
		
		// Finally, set the values on the new part and then add it to the static array.
		newPart._textureCount = this._textureCount;
		newPart._colorCount = this._colorCount;
		newPart.colors = this.colors;
		newPart.originalTexture = this.originalTexture;
		newPart.wigTextures = this.wigTextures;
		newPart.fullWigTexture = this.fullWigTexture;
		newPart.fullTextures = this.fullTextures;		
		parts.add(newPart);
	}
	

	private void loadPartFullTextures(GamePartsLoader loader, GameSkinCache cache) {
		
		if(this.hasTextures()) {			
			int tc = this.getTextureCount();
		
			for(int c=0; c < this.getColorCount(); c++) {	
				
				for(int t=0; t < tc; t++) {						
					if(!this.hasSides()) {
						this.loadCachePartFullTextures(loader, cache, true, t+1, this.hasColors() ? c : -1, this.getPartName(), t);
					} else {
						int multi = 0;
						for(String suffix : SIDE_SUFFIXES) {
							this.loadCachePartFullTextures(loader, cache, true, t+1, this.hasColors() ? c : -1, this.getPartName()+suffix, (tc*multi) + t);
							multi++;
						}
					}
				}
			}
			
		}		
		
	}

	private void loadPartColors() {
		
		String colorPath = this.getPartColorPath();
		if(this.hasColors()) {			
		
  	        GameTexture colorTexture = GameTexture.fromFile(colorPath, true);
  	        if(colorTexture == null) {
  	        	RaceMod.handleDebugMessage(
  				        String.format("Could not find colors for part %s at %s",this.getPartName(), this.getPartColorPath()), 
  				        70
  				    );
  	        }
  	        this.colors.addBaseColors(colorTexture, 0, 1, colorTexture.getWidth() - 1);
  	        this._colorCount = this.colors.getSize();  
			
			RaceMod.handleDebugMessage(
			        String.format("Loaded %d colors for part: %s", this._colorCount, this.getPartName()), 
			        70
			    );
			
		} else {
			RaceMod.handleDebugMessage( String.format("Part: %s does not have colors.", this.getPartName()), 70  );
			this._colorCount = 1;
		}
		
	}
	
	
	private void loadPartIncludedWigTextures(GamePartsLoader loader, GameSkinCache cache) {
			
		for(int t=0; t< this._textureCount; t++) {		
			if(this.hasColors()) {				
				for(int c=0; c < this.colors.getSize(); c++) {					
					this.loadCacheAccessoryMapTextures(loader, cache, true, t, c, wigAccessoryLocation);					
				}
			}
			else {
				this.loadCacheAccessoryMapTextures(loader, cache, true, t, -1, wigAccessoryLocation);	
			}
			
		}
		
	}
	
	private void loadCacheAccessoryMapTextures(GamePartsLoader loader, GameSkinCache cache, boolean makeFinal, int ti, int ci, Point accessorySpriteLocation) {		
		
		final int realColorIndex = (ci==-1) ? 0 : ci;
		GameTexture original = this.fullTextures.get(realColorIndex).get(ti);
		
		if(original == null) {
			RaceMod.handleDebugMessage(String.format("Failed to retrieve: %s accesory texture number %d for color %d)", this.getPartName(), ti, ci),70);
			return;
		}
		
		String cacheKey = String.format("%s_wig_%d_%d", this.getPartName().toLowerCase(), ti, realColorIndex);
		int hash = original.hashCode() + GameRandom.prime(18) * (1+ti) * (this.hasColors() ? colors.getColorHash(ci) : 1);
	
		AbstractGameTextureCache.Element element = cache.get(cacheKey);
		GameTexture texture;
		if (element != null && element.hash == hash) {
			try {
				texture = new GameTexture(String.format("cached%s %s", this.getPartName(), cacheKey), element.textureData);
				if (makeFinal) texture.makeFinal();	
							
				loader.addToList(this.wigTextures.get(realColorIndex),ti, texture);	
				RaceMod.handleDebugMessage(String.format("Found texture in cache: %s", cacheKey),70);
				return;
			} catch (Exception var12) {
				RaceMod.handleDebugMessage(String.format("Could not load %s cache for %s", this.getPartName(), cacheKey),10);
			}
		} else {
			RaceMod.handleDebugMessage(String.format("Detected invalid: %s", cacheKey),70);
		}		
		
		loader.triggerFirstTimeSetup();
		RaceMod.handleDebugMessage(String.format("Generating new: %s", cacheKey),70);			
	
		int wtw = original.getWidth();
		int wth = original.getHeight();
		Point ams = this.accessoryMapSize();	
		
		Point spriteMapSize = new Point(wtw / ams.x, wth / ams.y);
		Point wigSpriteLocation = new Point(
				wigAccessoryLocation.x >= 0 ? ams.x *  accessorySpriteLocation.x : ams.x * (spriteMapSize.x + accessorySpriteLocation.x),
				wigAccessoryLocation.y >= 0 ? ams.y *  accessorySpriteLocation.y : ams.y * (spriteMapSize.y + accessorySpriteLocation.y)		
				);
				
		GameTexture _texture =  new GameTexture(original, wigSpriteLocation.x, wigSpriteLocation.y, ams.x, ams.y);
				loader.submitTaskAddToList(this.wigTextures.get(realColorIndex),ti, (String) null, () -> {
					 if(ci != -1) colors.replaceColors(_texture, realColorIndex);
					 _texture.runPreAntialias(false);
					cache.set(cacheKey, hash, _texture);
					return _texture;
				}, makeFinal);					
	}
	
	
	private void loadPartWigTexture() {
		String wigPath = this.getWigPath();
		if(this.hasWig()) {								
  	        GameTexture wigTexture = GameTexture.fromFile(wigPath, true);
  	        if(wigTexture==null) {
  	        	RaceMod.handleDebugMessage( String.format("Failed to find wig file for %s at %s.", this.getPartName(), wigPath), 70  );
  	        }
  	        this.fullWigTexture = wigTexture;	 
		}
		else {
			this.fullWigTexture = null;
		}		
	}

	// Calls loadCacheWigTextures to pre-load textures for each combination of style and color, and then store these textures in the current GameParts list.
	private void loadPartWigTextures(GamePartsLoader loader, AbstractGameTextureCache cache) {
		for(int t=0; t< this._textureCount; t++) {
			if(this.hasColors()) {				
				for(int c=0; c < this.colors.getSize(); c++) {
					this.loadCacheWigTextures(loader, cache, true, t, c);					
				}
			}
			else {
				this.loadCacheWigTextures(loader, cache, true, t, -1);	
			}
		}
		
	}
	
	// Extracts a subSprite from the wig texture based on xid position, and then stores the texture for cid color id.
	public void loadCacheWigTextures(GamePartsLoader loader, AbstractGameTextureCache cache, boolean makeFinal, int xid, int cid) {
		final int realColorIndex = (cid==-1) ? 0 : cid;
		GameTexture original = this.fullWigTexture;
		String cacheKey = String.format("%s_wig_%d_%d", this.getPartName().toLowerCase(), xid, cid);		
		int hash = original.hashCode() + GameRandom.prime(14) * (1+xid) * (this.hasColors() ? colors.getColorHash(cid) : 1);
				
		AbstractGameTextureCache.Element element = cache.get(cacheKey);
		GameTexture texture;
		if (element != null && element.hash == hash) {
			try {
				texture = new GameTexture(String.format("cached%s %s", this.getPartName(), cacheKey), element.textureData);
				if (makeFinal) {
					texture.makeFinal();
				}
				loader.addToList(this.wigTextures.get(realColorIndex), xid, texture);

				RaceMod.handleDebugMessage(String.format("Found texture in cache: %s", cacheKey),70);
				return;
			} catch (Exception var12) {
				RaceMod.handleDebugMessage(String.format("Could not load %s cache for %s", this.getPartName(), cacheKey),10);
			}
		} else {
			RaceMod.handleDebugMessage(String.format("Detected invalid: %s", cacheKey),70);
		}

		loader.triggerFirstTimeSetup();
		RaceMod.handleDebugMessage(String.format("Generating new: %s", cacheKey),70);	
				
		GameTexture _texture = new GameTexture(original);
		loader.submitTaskAddToList(this.wigTextures.get(realColorIndex), xid, (String) null, () -> {
			 if(cid != -1) colors.replaceColors(_texture, cid);
			_texture.runPreAntialias(false);
			cache.set(cacheKey, hash, _texture);
			return _texture;
		}, makeFinal);
	}
	
		// Loads the full texture in cid color tone
	public void loadCachePartFullTextures(GamePartsLoader loader, AbstractGameTextureCache cache, boolean makeFinal, int tid, int cid, String fileName, int targetIndex) {
		final int realColorIndex = (cid==-1) ? 0 : cid;
		
		String originalTexturePath = this.getPartPath() + fileName.toLowerCase() + tid;
		GameTexture originalTexture = GameTexture.fromFile(originalTexturePath, true);
		
		if(originalTexture == null) {
			RaceMod.handleDebugMessage(String.format("Failed to load full size textures for part %s, texture id %d: Original texture is null.", this.getPartName(),tid),70);
		}
		
		String cacheKey = String.format("%s_full_%d_%d", fileName.toLowerCase(), realColorIndex, tid);		
		int hash = originalTexture.hashCode() + GameRandom.prime(10) * (tid + realColorIndex);
		
		RaceMod.handleDebugMessage(String.format("Loading full texture: cache key %s with texture id %d, color id %d", cacheKey, tid, cid),70);
		
		AbstractGameTextureCache.Element element = cache.get(cacheKey);
		GameTexture texture;
		if (element != null && element.hash == hash) {
			try {
				texture = new GameTexture(String.format("cached%s %s", fileName, cacheKey), element.textureData);
				if (makeFinal) {
					texture.makeFinal();
				}					
				loader.addToList(this.fullTextures.get(realColorIndex), targetIndex, texture);
				RaceMod.handleDebugMessage(String.format("Found full texture in cache: %s", cacheKey),70);
				return;
				
			} catch (Exception var12) {
				RaceMod.handleDebugMessage(String.format("Could not load %s cache for %s", fileName, cacheKey),10);
			}
		} else {
			RaceMod.handleDebugMessage(String.format("Detected invalid: %s", cacheKey),70);
		}

		loader.triggerFirstTimeSetup();
		RaceMod.handleDebugMessage(String.format("Generating new: %s", cacheKey),70);	
		
		GameTexture _texture = new GameTexture(originalTexture);			
		loader.submitTaskAddToList(this.fullTextures.get(realColorIndex), targetIndex, (String)null,() -> {
			if(cid != -1) colors.replaceColors(_texture, cid);
			cache.set(cacheKey, hash, _texture);
			return _texture;
		}, makeFinal);		
	}

	
	private int tryFindResources() {
		
		String searchPath = this.getPartPath();
		String searchPrefix = this.getPartName().toLowerCase();
		String searchRegexValue =  searchPrefix +(this.hasSides() ? "_[lr]":"")+ "\\d+\\.png$";
	    Pattern pattern = Pattern.compile("^.*" + searchPath + searchRegexValue);
	        
		Set<Map.Entry<String, ResourceFile>> allResources = ResourceEncoder.getAllFiles();
		
		long matchCount = allResources.parallelStream().filter((entry)->{			
			return pattern.matcher(entry.getValue().path).matches();
			}).count();
		
		RaceMod.handleDebugMessage("Found " + matchCount + " " + searchPrefix+" textures in JAR path: " + searchPath, 40);
		return (int)matchCount; 
		
	}
	
	public Point getFullTextureSize() {
		if(!this.texturesInitialized || this.hasTextures()) return new Point(0,0);
		return new Point(this.fullTextures.get(0).get(0).getWidth(), this.fullTextures.get(0).get(0).getHeight());
	}
	
	public GameTexture getFullTexture(int textureID, int colorID) {
		return this.fullTextures.get(colorID).get(textureID);
	}
	
	public GameTexture getFullTexture(int textureID, int colorID, int sideNumber) {
		if(sideNumber>1) sideNumber=1;
		return this.fullTextures.get(colorID).get(textureID + (this.getTextureCount()*sideNumber));
	}
	
	public GameTexture getTextureSprite(int textureID, int colorID, int x, int y) {
		if(!this.texturesInitialized) return null;
		
		if(textureID > this.getTextureCount()) {
			RaceMod.handleDebugMessage(String.format("Texture ID %d out of bounds for part %s", textureID, this.getPartName()),30);
			throw new ArrayIndexOutOfBoundsException();
			//return null; when we get done debugging
		}
		if(colorID != -1 && colorID > this.getColorCount()) {
			RaceMod.handleDebugMessage(String.format("No color ID %d found with texture ID %d for part %s", colorID,textureID, this.getPartName()),30);
			throw new ArrayIndexOutOfBoundsException();
		}	
		
		Point os = getFullTextureSize();
		Point tms = this.textureMapSize();
		int wtw = os.x;
		int wth = os.y;
		
		Point spriteMapSize = new Point(wtw / tms.x, wth / tms.y);
		Point spriteLocation = new Point(
				x >= 0 ? tms.x *  x : tms.x * (spriteMapSize.x + x),
				y >= 0 ? tms.y *  y : tms.y * (spriteMapSize.y + y)		
				);
				
		return new GameTexture(this.getFullTexture(textureID, colorID), spriteLocation.x, spriteLocation.y, tms.x, tms.y);
	}
	

	public GameTexture getTextureSprite(int textureID, int colorID, int x, int y, int sideNumber) {
		if(sideNumber>1) sideNumber=1;
		return this.getTextureSprite(textureID + (this.getTextureCount()*sideNumber), colorID, x, y);
	}
	
	
	
	public GameTexture getFullWigTexture(int styleIndex, int colorIndex) {
		return this.wigTextures.get(colorIndex).get(styleIndex);
	}
	
	public GameTexture getWigTexture(int textureID, int colorID) {
		if(!this.texturesInitialized) return null;
		
		if(textureID > this.getTextureCount()) {
			RaceMod.handleDebugMessage(String.format("Texture ID %d out of bounds for part %s", textureID, this.getPartName()),30);
			throw new ArrayIndexOutOfBoundsException();
			//return null; when we get done debugging
		}
		if(colorID != -1 && colorID > this.getColorCount()) {
			RaceMod.handleDebugMessage(String.format("No color ID %d found with texture ID %d for part %s", colorID,textureID, this.getPartName()),30);
			throw new ArrayIndexOutOfBoundsException();}	
			
		return new GameTexture(this.wigTextures.get(colorID).get(textureID));		
	}
	
	private Point getWigTextureSize() {
		return new Point(this.fullWigTexture.getWidth(), this.fullWigTexture.getHeight());
	}

	public static GameParts getPart(Class<? extends RaceLookParts> sourceClass, String name) {
		
		for( GameParts g : parts) {			
			if (g.getRacePartsClass().equals(sourceClass)
					&& g.getPartName().toLowerCase().equals(name.toLowerCase())) return g;
		}
		  RaceMod.handleDebugMessage(
		            String.format("Part: %s does not exist for class %s.", name, sourceClass.getName()), 
		            70
		        );
		return null;
	}
	
	public static int getPartTextureOptionsCount(BodyPart part) {
		for( GameParts g : parts) {
			if(part.getRacePartsClass().equals(g.assignedPart.getRacePartsClass()) && g.getPartName().equals(part.getPartName())) {
				return g.getTextureCount();
			}
		}
		return 0;
	}
	
	public static int getPartColorOptionsCount(BodyPart part) {
		for( GameParts g : parts) {
			if(part.getRacePartsClass().equals(g.assignedPart.getRacePartsClass()) && g.getPartName().equals(part.getPartName())) { return g.getColorCount(); }
		}
		return 0;
	}

	public int getRandomPartColor(GameRandom random) {
		if (this.colors != null && this.getColorCount() > 0) {
			TicketSystemList<Integer> ticketList = new TicketSystemList<Integer>();

			for (int i = 0; i < colors.getSize(); ++i) {
				ticketList.addObject(colors.getWeight(i), i);
			}

			return (Integer) ticketList.getRandomObject(random);
		} else {
			return random.nextInt();
		}
	}


}