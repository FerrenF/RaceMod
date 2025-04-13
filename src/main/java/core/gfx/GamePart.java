package core.gfx;

import java.awt.Point;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;

import core.race.RaceLook;
import core.race.parts.BodyPart;
import core.race.parts.RaceLookParts;
import helpers.DebugHelper;
import helpers.DebugHelper.MESSAGE_TYPE;
import necesse.engine.util.GameRandom;
import necesse.engine.util.TicketSystemList;
import necesse.gfx.AbstractGameTextureCache;
import necesse.gfx.GameSkinCache;
import necesse.gfx.GameSkinColors;
import necesse.gfx.gameTexture.GameTexture;
import necesse.gfx.gameTexture.MergeFunction;
import necesse.gfx.res.ResourceEncoder;
import necesse.gfx.res.ResourceFile;
import org.lwjgl.stb.STBImage;
import org.lwjgl.stb.STBImageResize;

public class GamePart {
	
	/*
	protected static final ArrayList<GamePart> parts = new ArrayList<GamePart>();
	protected static final String PATH_PREFIX = "";
	protected static final String TEXTURE_SUFFIX = ".png";
	protected static final String WIG_TEXTURE_NAME = "wig";
	protected static final Set<String> SIDE_SUFFIXES = Set.of(new String[]{"_L","_R"});
	
	
	protected static Point wigAccessoryLocation = new Point(0, -1);
	public final BodyPart assignedPart;	
	
	public GameSkinColors colors;	
	protected GameTexture originalTexture;
	protected GameTexture fullWigTexture;	
	protected Map<Integer, ArrayList<GameTexture>> fullTextures;	
	protected Map<Integer, ArrayList<GameTexture>> wigTextures;	
	protected Map<String, GameTexture> textureSpriteCache = new HashMap<>();
	protected static Map<TextureMergerInfo, GameTexture> mergedTextureCache = new HashMap<>();

	protected boolean texturesInitialized = false;
		
	protected int _textureCount = 0;
	protected int _colorCount = 1;
	
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
	protected GamePart(BodyPart assignedPart) {
		this.assignedPart = assignedPart;
		this.texturesInitialized = true;
	}
	
	// When this constructor is called, we start loading resources based off of the information in the BodyPart.
	public GamePart(GamePartsLoader loader, BodyPart assignedPart) {
		this.assignedPart = assignedPart;
		this.loadPart(loader);
	}
	
	// This should be the last public method leading into the initialization of the current GameParts instance.
	public void loadPart(GamePartsLoader loader) {		
		
		if(this.isBaseGamePart()) return; // Something else should handle this. Don't waste any more resources.
		
		GamePart newPart = new GamePart(this.assignedPart);
		GameSkinCache cache = new GameSkinCache(this.getPartPath());		
		cache.loadCache();
		this.colors = new GameSkinColors();
		
		if(this.hasColors()) {			
			this.loadPartColors();
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
		
		}
		
		if(!this.texturesInitialized && this._textureCount > 0)	{
						
			this.loadPartFullTextures(loader, cache);	
			DebugHelper.handleDebugMessage( String.format("Loading full textures: part %s loaded %d full textures.",
					this.getPartName(),
					this.fullTextures.size()),
					70, MESSAGE_TYPE.DEBUG );
						
		} else {
			
			DebugHelper.handleDebugMessage( String.format("Loading wig textures: part %s has 0 texture count during load.",
					this.getPartName()),
					70, MESSAGE_TYPE.DEBUG  );
			
		}
				
		loader.waitForCurrentTasks();
		
		if(this.hasWig()) {
			
			if(this.assignedPart.isHasSeperateWigTexture()) {
				
				loadPartWigTexture();			
	
				if(this.fullWigTexture != null) {				
					DebugHelper.handleDebugMessage(String.format("Loaded wig texture for part: %s",this.getPartName()), 25, MESSAGE_TYPE.WARNING);	
					loadPartWigTextures(loader, cache);				
				}
				else {			
					DebugHelper.handleDebugMessage( String.format("Wig texture is null for part %s.", this.getPartName()), 25  ,MESSAGE_TYPE.WARNING);
					return;	
				}
			}
			else {
				if(this.assignedPart.isHasLastRowAccessoryMap() && this._textureCount > 0) {
					this.loadPartIncludedWigTextures(loader, cache);	
				}
				else {
					DebugHelper.handleDebugMessage( String.format("Wig texture for part %s is specified as not separate, but the texture does not have an accessory map.", this.getPartName()), 70 ,MESSAGE_TYPE.WARNING );
					return;	
				}	
			}			
		}
		else {			
			DebugHelper.handleDebugMessage( String.format("Loading wig textures: part %s does not have a wig texture but specifies that it does.", this.getPartName()), 25 , MESSAGE_TYPE.WARNING );
		}
		
		loader.waitForCurrentTasks();
		cache.saveCache();
		
		
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
	

	protected void loadPartFullTextures(GamePartsLoader loader, GameSkinCache cache) {
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

	protected void loadPartColors() {		
		String colorPath = this.getPartColorPath();
		if(this.hasColors()) {			
		
  	        GameTexture colorTexture = GameTexture.fromFile(colorPath, true);
  	        if(colorTexture == null) {
  	        	DebugHelper.handleDebugMessage(
  				        String.format("Could not find colors for part %s at %s",this.getPartName(), this.getPartColorPath()), 
  				        25, MESSAGE_TYPE.WARNING
  				    );
  	        }
  	        this.colors.addBaseColors(colorTexture, 0, 1, colorTexture.getWidth() - 1);
  	        this._colorCount = this.colors.getSize();  
			
			DebugHelper.handleDebugMessage(
			        String.format("Loaded %d colors for part: %s", this._colorCount, this.getPartName()), 
			        70, MESSAGE_TYPE.DEBUG
			    );
			
		} else {
			DebugHelper.handleDebugMessage( String.format("Part: %s does not have colors.", this.getPartName()), 50, MESSAGE_TYPE.WARNING );
			this._colorCount = 1;
		}
		
	}
	
	
	protected void loadPartIncludedWigTextures(GamePartsLoader loader, GameSkinCache cache) {
			
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
			DebugHelper.handleDebugMessage(String.format("Failed to retrieve: %s accesory texture number %d for color %d)", this.getPartName(), ti, ci),25, MESSAGE_TYPE.ERROR);
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
				DebugHelper.handleDebugMessage(String.format("Found texture in cache: %s", cacheKey), 70, MESSAGE_TYPE.DEBUG);
				return;
			} catch (Exception var12) {
				DebugHelper.handleDebugMessage(String.format("Could not load %s cache for %s: "+ var12.getMessage(), this.getPartName(), cacheKey), 25, MESSAGE_TYPE.ERROR);
			}
		} else {
			DebugHelper.handleDebugMessage(String.format("Detected invalid: %s", cacheKey),70, MESSAGE_TYPE.DEBUG);
		}		
		
		loader.triggerFirstTimeSetup();
		DebugHelper.handleDebugMessage(String.format("Generating new: %s", cacheKey),70, MESSAGE_TYPE.DEBUG);			
	
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
	
	
	protected void loadPartWigTexture() {
		String wigPath = this.getWigPath();
		if(this.hasWig()) {								
  	        GameTexture wigTexture = GameTexture.fromFile(wigPath, true);
  	        if(wigTexture==null) {
  	        	DebugHelper.handleDebugMessage( String.format("Failed to find wig file for %s at %s.", this.getPartName(), wigPath), 25, MESSAGE_TYPE.WARNING );
  	        }
  	        this.fullWigTexture = wigTexture;	 
		}
		else {
			this.fullWigTexture = null;
		}		
	}

	// Calls loadCacheWigTextures to pre-load textures for each combination of style and color, and then store these textures in the current GameParts list.
	protected void loadPartWigTextures(GamePartsLoader loader, AbstractGameTextureCache cache) {
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

				DebugHelper.handleDebugMessage(String.format("Found texture in cache: %s", cacheKey),70, MESSAGE_TYPE.DEBUG);
				return;
			} catch (Exception var12) {
				DebugHelper.handleDebugMessage(String.format("Could not load %s cache for %s:" + var12.getMessage(), this.getPartName(), cacheKey), 25, MESSAGE_TYPE.ERROR);
			}
		} else {
			DebugHelper.handleDebugMessage(String.format("Detected invalid: %s", cacheKey),70, MESSAGE_TYPE.DEBUG);
		}

		loader.triggerFirstTimeSetup();
		DebugHelper.handleDebugMessage(String.format("Generating new: %s", cacheKey),70, MESSAGE_TYPE.DEBUG);	
				
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
			DebugHelper.handleDebugMessage(String.format("Failed to load full size textures for part %s, texture id %d: Original texture is null.", this.getPartName(),tid),25, MESSAGE_TYPE.ERROR);
		}
		
		String cacheKey = String.format("%s_full_%d_%d", fileName.toLowerCase(), realColorIndex, tid);		
		int hash = originalTexture.hashCode() + GameRandom.prime(10) * (tid + realColorIndex);
		
		DebugHelper.handleDebugMessage(String.format("Loading full texture: cache key %s with texture id %d, color id %d", cacheKey, tid, cid),70, MESSAGE_TYPE.DEBUG);
		
		AbstractGameTextureCache.Element element = cache.get(cacheKey);
		GameTexture texture;
		if (element != null && element.hash == hash) {
			if (originalTexture != null) {
			    originalTexture.delete();
			}
			try {
				texture = new GameTexture(String.format("cached%s %s", fileName, cacheKey), element.textureData);
				if (makeFinal) {
					texture.makeFinal();
				}					
				loader.addToList(this.fullTextures.get(realColorIndex), targetIndex, texture);
				DebugHelper.handleDebugMessage(String.format("Found full texture in cache: %s", cacheKey),70, MESSAGE_TYPE.DEBUG);
				return;
				
			} catch (Exception var12) {
				DebugHelper.handleDebugMessage(String.format("Could not load %s cache for %s:" + var12.getMessage(), fileName, cacheKey),10, MESSAGE_TYPE.ERROR);
			}
		} else {
			DebugHelper.handleDebugMessage(String.format("Detected invalid: %s", cacheKey),70, MESSAGE_TYPE.DEBUG);
		}

		loader.triggerFirstTimeSetup();
		DebugHelper.handleDebugMessage(String.format("Generating new: %s", cacheKey),70, MESSAGE_TYPE.DEBUG);	
		
		GameTexture _texture = new GameTexture(originalTexture);			
		loader.submitTaskAddToList(this.fullTextures.get(realColorIndex), targetIndex, (String)null,() -> {
			
			if(cid != -1) colors.replaceColors(_texture, cid);
			//_texture.runPreAntialias(false);
			cache.set(cacheKey, hash, _texture);
			return _texture;
		}, makeFinal);		
	}

	
	protected int tryFindResources() {
		
		String searchPath = this.getPartPath();
		String searchPrefix = this.getPartName().toLowerCase();
		String searchRegexValue =  searchPrefix +(this.hasSides() ? "_[lr]":"")+ "\\d+\\.png$";
	    Pattern pattern = Pattern.compile("^.*" + searchPath + searchRegexValue);
	        
		Set<Map.Entry<String, ResourceFile>> allResources = ResourceEncoder.getAllFiles();
		
		long matchCount = allResources.parallelStream().filter((entry)->{			
			return pattern.matcher(entry.getValue().path).matches();
			}).count();
		
		DebugHelper.handleDebugMessage("Found " + matchCount + " " + searchPrefix+" textures in JAR path: " + searchPath, 40, MESSAGE_TYPE.DEBUG);
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
	
	
	public static GameTexture getFullTextureMerger(TextureMergerInfo mergeInfo) {
		if(mergedTextureCache.containsKey(mergeInfo)) {
			return mergedTextureCache.get(mergeInfo);
		}
		GameTexture t1 = new GameTexture(mergeInfo.tex1);
		t1.merge(mergeInfo.tex2, 0, 0, mergeInfo.mergeFunction);
		mergedTextureCache.put(mergeInfo, t1);
		return t1;
	}
	
	private GameTexture getNewTextureSprite(int textureID, int colorID, int x, int y, int sx, int sy) {
		if(!this.texturesInitialized) return null;
		
	    String cacheKey = textureID + "_" + colorID + "_" + x + "_" + y+ "_" + sx + "_" + sy;
	    if (textureSpriteCache.containsKey(cacheKey)) {
	        return textureSpriteCache.get(cacheKey);
	    }

		if(textureID > this.getTextureCount()) {
			DebugHelper.handleDebugMessage(String.format("Texture ID %d out of bounds for part %s", textureID, this.getPartName()),10, MESSAGE_TYPE.ERROR);
			throw new ArrayIndexOutOfBoundsException();
		}
		if(colorID != -1 && colorID > this.getColorCount()) {
			DebugHelper.handleDebugMessage(String.format("No color ID %d found with texture ID %d for part %s", colorID,textureID, this.getPartName()),10, MESSAGE_TYPE.ERROR);
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
				
		GameTexture newTexture = new GameTexture(this.getFullTexture(textureID, colorID), spriteLocation.x, spriteLocation.y, tms.x, tms.y).resize(sx, sy, STBImageResize.STBIR_FILTER_BOX, STBImageResize.STBIR_FILTER_BOX );
		textureSpriteCache.put(cacheKey, newTexture);
		return newTexture;
	}
	
	public GameTexture getTextureSprite(int textureID, int colorID, int x, int y, int sx, int sy) {
		
	    return getNewTextureSprite(textureID, colorID, x, y, sx, sy); 
	}
	
	public GameTexture getTextureSprite(int textureID, int colorID, int x, int y) {    
		Point s = this.getFullTextureSize();
		Point m = this.textureMapSize(); 
	    return getTextureSprite(textureID, colorID, x, y, s.x/m.x, s.y/m.y);
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
			DebugHelper.handleDebugMessage(String.format("Texture ID %d out of bounds for part %s", textureID, this.getPartName()),10, MESSAGE_TYPE.ERROR);
			throw new ArrayIndexOutOfBoundsException();
			//return null; when we get done debugging
		}
		if(colorID != -1 && colorID > this.getColorCount()) {
			DebugHelper.handleDebugMessage(String.format("No color ID %d found with texture ID %d for part %s", colorID,textureID, this.getPartName()),10, MESSAGE_TYPE.ERROR);
			throw new ArrayIndexOutOfBoundsException();}	
			
		return this.wigTextures.get(colorID).get(textureID);		
	}
	
	private Point getWigTextureSize() {
		return new Point(this.fullWigTexture.getWidth(), this.fullWigTexture.getHeight());
	}

	public static GamePart getPart(Class<? extends RaceLookParts> sourceClass, String name) {
		
		for( GamePart g : parts) {			
			if (g.getRacePartsClass().equals(sourceClass)
					&& g.getPartName().toLowerCase().equals(name.toLowerCase())) return g;
		}
		  DebugHelper.handleDebugMessage(
		            String.format("Part: %s does not exist for class %s.", name, sourceClass.getName()), 
		            70
		        );
		return null;
	}
	
	public static int getPartTextureOptionsCount(BodyPart part) {
		for( GamePart g : parts) {
			if(part.getRacePartsClass().equals(g.assignedPart.getRacePartsClass()) && g.getPartName().equals(part.getPartName())) {
				return g.getTextureCount();
			}
		}
		return 0;
	}
	
	public static int getPartColorOptionsCount(BodyPart part) {
		for( GamePart g : parts) {
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

*/
}