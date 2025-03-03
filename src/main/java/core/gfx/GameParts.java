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
	
	private static Point wigAccessoryLocation = new Point(0, -1);
	public final BodyPart assignedPart;	
	
	public GameSkinColors colors;	
	private GameTexture fullWigTexture;	
	private ArrayList<GameTexture> fullTextures;	
	
	private Map<Integer, ArrayList<GameTexture>> wigTextures;		
	private Map<Integer, ArrayList<ArrayList<GameTexture>>> frontTextures;
	private Map<Integer, ArrayList<ArrayList<GameTexture>>> backTextures;
	private Map<Integer, ArrayList<ArrayList<GameTexture>>> leftTextures;
	private Map<Integer, ArrayList<ArrayList<GameTexture>>> rightTextures;
	
	private boolean texturesInitialized = false;
		
	private int _textureCount = 0;
	private int _colorCount = 1;
	
	public Class<? extends RaceLookParts> getRacePartsClass() 		{	return this.assignedPart.getRacePartsClass();		}	
	
	public String getPartName() 		{	return this.assignedPart.getPartName();		}
	
	public boolean hasSides()			{	return this.assignedPart.isHasSides();		}
	
	public Point textureMapSize()		{	return this.assignedPart.getTextureSpriteMapSize();		}
	
	public boolean hasTextures() 		{	return this.assignedPart.isHasTexture();	}
	
	public boolean hasWig() 			{	return this.assignedPart.isHasWigTexture();		}
	
	public boolean hasColors() 			{	return this.assignedPart.isHasColor();		}
	
	public boolean isBaseGamePart() 	{	return this.assignedPart.isBaseGamePart();	}
	
	public String getPartPath() 		{	return PATH_PREFIX+this.assignedPart.getTexturePath();	}
	
	public String getPartColorPath() 	{	return PATH_PREFIX+this.assignedPart.getColorPath();	}
	
	public String getWigPath() 			{	return PATH_PREFIX+this.assignedPart.getTexturePath()+WIG_TEXTURE_NAME;		}
	
	public int getTextureCount() 		{	return  _textureCount;	}

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
		if(this.hasTextures()) {
			
			// First, try and find the textures. Count them, and store the number we found.
			this._textureCount = this.tryFindResources();
			
			// Other Part
			this.wigTextures 	= 	new HashMap<Integer, ArrayList<GameTexture>>();
			this.fullTextures 	= 	new ArrayList<GameTexture>(this.getTextureCount());		
			for(int i=0;i<_textureCount;i++) {
				fullTextures.add(null);
			}
			this.backTextures 	=  	new HashMap<Integer, ArrayList< ArrayList<GameTexture>>>();
			this.frontTextures 	=  	new HashMap<Integer, ArrayList< ArrayList<GameTexture>>>();
			this.leftTextures 	=  	new HashMap<Integer, ArrayList< ArrayList<GameTexture>>>();
			this.rightTextures 	=  	new HashMap<Integer, ArrayList< ArrayList<GameTexture>>>();			
		}

		if(this.hasColors()) {			
			this.loadPartColors();
			loader.waitForCurrentTasks();
		}
		
		if (this._textureCount > 0) {
			
			// Map and array initialization
			int arrayInitSize = this._textureCount;			
			// Step 1: Initialize the first level of maps
		    for (int c = 0; c < this.getColorCount(); c++) {  
		    	this.wigTextures.put(c, new ArrayList<GameTexture>());
		        this.backTextures.put(c, new ArrayList<>(arrayInitSize));
		        this.frontTextures.put(c, new ArrayList<>(arrayInitSize));
		        this.leftTextures.put(c, new ArrayList<>(arrayInitSize));
		        this.rightTextures.put(c, new ArrayList<>(arrayInitSize));

		        // Step 2: Populate each list with empty lists
		        for (int i = 0; i < arrayInitSize; i++) {        
		            this.backTextures.get(c).add(new ArrayList<>());
		            this.frontTextures.get(c).add(new ArrayList<>());
		            this.leftTextures.get(c).add(new ArrayList<>());
		            this.rightTextures.get(c).add(new ArrayList<>());
		        }
		    }
			
			int subArrayInitSize = this.textureMapSize().x;
			// Step 3: Populate second-level lists
		    for (int c = 0; c < this.getColorCount(); c++) {
		        for (int i = 0; i < _textureCount; i++) {    
		        	this.wigTextures.get(c).add(null);
		            for (int x = 0; x < subArrayInitSize; x++) {
		                this.backTextures.get(c).get(i).add(null);
		                this.frontTextures.get(c).get(i).add(null);
		                this.leftTextures.get(c).get(i).add(null);
		                this.rightTextures.get(c).get(i).add(null);
		            }
		        }
		    }			
			
		}
		
		
		
		if(!this.texturesInitialized){
			
			if(this.hasTextures()) {
				
				if(this._textureCount > 0) {					
					this.loadPartFullTextures(loader, cache);
					loader.waitForCurrentTasks();
					RaceMod.handleDebugMessage( String.format("Loading full textures: part %s loaded %d full textures.", this.getPartName(), this.fullTextures.size()), 70  );
				
					
					if(this.hasSides()) {
						if(this._textureCount > 0) {
							this.loadPartFrontTextures(loader, cache);
							this.loadPartBackTextures(loader, cache);
							this.loadPartLeftTextures(loader, cache);
							this.loadPartRightTextures(loader, cache);
						}
						else {
							RaceMod.handleDebugMessage( String.format("Loading side textures: part %s has 0 texture count during load.", this.getPartName()), 70  );
						}
					}
					else {
						this.loadPartBackTextures(loader, cache);
					}
						
				}
				else {
					RaceMod.handleDebugMessage( String.format("Loading wig textures: part %s has 0 texture count during load.", this.getPartName()), 70  );
				}			
				
			}
			
		}
				
		loader.waitForCurrentTasks();
		
		if(this.hasWig()) {
			
			if(this.assignedPart.isHasSeperateWigTexture()) {
				loadPartWigTexture();			
	
				if(this.fullWigTexture != null) {
					RaceMod.handleDebugMessage(
					        String.format("Loaded wig texture for part: %s",this.getPartName()), 70);		
					
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
		newPart.wigTextures = this.wigTextures;
		newPart.fullWigTexture = this.fullWigTexture;
		newPart.fullTextures = this.fullTextures;
		newPart.frontTextures = this.frontTextures;
		newPart.backTextures = this.backTextures;
		newPart.leftTextures = this.leftTextures;
		newPart.rightTextures = this.rightTextures;
		
		parts.add(newPart);
	}
	
	

	private static int LEFT_ROW = 3;
	private static int RIGHT_ROW = 1;
	private static int FRONT_ROW = 2;
	private static int BACK_ROW = 0;
	
	private void loadPartFrontTextures(GamePartsLoader loader, GameSkinCache cache) {
		loadSideTextures(loader, cache, FRONT_ROW);
	}
	
	private void loadPartRightTextures(GamePartsLoader loader, GameSkinCache cache) {
		loadSideTextures(loader, cache, RIGHT_ROW);			
	}

	private void loadPartLeftTextures(GamePartsLoader loader, GameSkinCache cache) {
		loadSideTextures(loader, cache, LEFT_ROW);		
	}

	private void loadPartBackTextures(GamePartsLoader loader, GameSkinCache cache) {
		loadSideTextures(loader, cache, BACK_ROW);
	}
	
	private void loadSideTextures(GamePartsLoader loader, GameSkinCache cache, int side) {		
		
		Point mapSize = this.textureMapSize();
		
		for(int t=0; t< this._textureCount; t++) {
			for(int x=0; x<mapSize.x; x++) {
				if(this.hasColors()) {				
					for(int c=0; c < this.colors.getSize(); c++) {					
						this.loadCacheMapTextures(loader, cache, true, this.textureMapSize(), t, x, c, side, this.getPartName());					
					}
				}
				else {
					this.loadCacheMapTextures(loader, cache, true,this.textureMapSize(), t, x, -1, side, this.getPartName());	
				}
			}
		}
	}

	private void loadCacheMapTextures(GamePartsLoader loader, GameSkinCache cache, boolean makeFinal, Point textureMapSize,
			int ti, int xi, int ci, int yi, String partName) {
		
		GameTexture original = this.fullTextures.get(ti);
		final int realColorIndex = (ci==-1) ? 0 : ci;
		if(original == null) {
			RaceMod.handleDebugMessage(String.format("Failed to retrieve: %s texture number %d for color %d at sprite pos (%d, %d)", this.getPartName(), ti, ci, xi, yi),70);
			return;
		}
		
		String cacheKey = String.format("%s_%d_%d_%d_%d", this.getPartName().toLowerCase(), ti, xi, yi, ci == -1 ? 0 : ci);
		int hash = original.hashCode() + GameRandom.prime(18) * (1+ti) * (1+xi) * (1+yi) * (this.hasColors() ? colors.getColorHash(ci) : 1);
	
		AbstractGameTextureCache.Element element = cache.get(cacheKey);
		GameTexture texture;
		if (element != null && element.hash == hash) {
			try {
				texture = new GameTexture(String.format("cached%s %s", this.getPartName(), cacheKey), element.textureData);
				if (makeFinal) {
					texture.makeFinal();	
					}
				
				switch(yi) {
					case 0: this.backTextures.get(realColorIndex).get(ti).add(xi, texture); break;
					case 1: this.rightTextures.get(realColorIndex).get(ti).add(xi, texture); break;
					case 2: this.frontTextures.get(realColorIndex).get(ti).add(xi, texture); break;
					case 3: this.leftTextures.get(realColorIndex).get(ti).add(xi, texture); break;
				}	
				
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
	
		Point mapSize = this.textureMapSize();
		int wtw = original.getWidth();
		int wth = original.getHeight();
		
		Point spriteSize = new Point(wtw/mapSize.x, wth/mapSize.y);		
		Point beginPoint = new Point(spriteSize.x*xi, spriteSize.y*yi);
		
		if((beginPoint.x + spriteSize.x > wtw) || (beginPoint.y+spriteSize.y > wth)) {
			RaceMod.handleDebugMessage( String.format("Failed to create new cache key %s: Calculation texture coordinates fell outside the bounds of the original texture.", cacheKey), 70  );
			return;
		}		
		texture = new GameTexture(original, beginPoint.x, beginPoint.y, spriteSize.x, spriteSize.y);
		
		
		GameTexture _texture = new GameTexture(texture);
		switch(yi) {
			case 0: {
				loader.submitTaskAddToList(this.backTextures.get(realColorIndex).get(ti), xi, (String) null, () -> {
					 if(ci != -1) colors.replaceColors(_texture, realColorIndex);
					 _texture.runPreAntialias(false);
					cache.set(cacheKey, hash, _texture);
					return _texture;
				}, makeFinal);				
				break;				
				}
			case 3: {
				loader.submitTaskAddToList(this.leftTextures.get(realColorIndex).get(ti), xi, (String) null, () -> {
					 if(ci != -1) colors.replaceColors(_texture, realColorIndex);
					 _texture.runPreAntialias(false);
					cache.set(cacheKey, hash, _texture);
					return _texture;
				}, makeFinal);	
				break;
				}
			case 2: {
				loader.submitTaskAddToList(this.frontTextures.get(realColorIndex).get(ti), xi, (String) null, () -> {
					 if(ci != -1) colors.replaceColors(_texture, realColorIndex);
					 _texture.runPreAntialias(false);
					cache.set(cacheKey, hash, _texture);
					return _texture;
				}, makeFinal);	
				break;
				}
			case 1: {
				loader.submitTaskAddToList(this.rightTextures.get(realColorIndex).get(ti), xi, (String) null, () -> {
					 if(ci != -1) colors.replaceColors(_texture, realColorIndex);
					 _texture.runPreAntialias(false);
					cache.set(cacheKey, hash, _texture);
					return _texture;
				}, makeFinal);	
				break;
				}
			default: break;
		}	
	}

	
	private void loadPartFullTextures(GamePartsLoader loader, GameSkinCache cache) {
		if(this.hasTextures()) {				
			for(int t=0; t < this.getTextureCount(); t++) {	
				this.loadCachePartFullTextures(loader, cache, true, t+1, this.getPartName().toLowerCase());					
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
					this.loadCacheAccessoryMapTextures(loader, cache, true, this.textureMapSize(), this.accessoryMapSize(), t, c);					
				}
			}
			else {
				this.loadCacheAccessoryMapTextures(loader, cache, true, this.textureMapSize(), this.accessoryMapSize(), t, -1);	
			}
			
		}
		
	}
	private Point accessoryMapSize() {
		return this.assignedPart.getAccessoryTextureMapSize();
	}

	private void loadCacheAccessoryMapTextures(GamePartsLoader loader, GameSkinCache cache, boolean makeFinal, Point textureMapSize, Point accessoryMapSize,
			int ti, int ci) {		
		
		GameTexture original = this.fullTextures.get(ti);
		final int realColorIndex = (ci==-1) ? 0 : ci;
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
				if (makeFinal) {
					texture.makeFinal();	
					}				
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
		
		Point m_spriteSize = new Point(wtw/textureMapSize.x, wth/textureMapSize.y);		
		
		int accessorySpriteStartY = (textureMapSize.y - 1) * m_spriteSize.y;
		Point accessorySpriteAreaBegin = new Point(0, accessorySpriteStartY);		
		Point accessoryMapAreaSize = new Point(wtw, m_spriteSize.y);
		
		Point accessoryMapSpriteSize = new Point(accessoryMapAreaSize.x/accessoryMapSize.x, 
												 accessoryMapAreaSize.y/accessoryMapSize.y);
		
		Point accessorySpriteWigTextureLocationOffset = new Point(0, accessoryMapSpriteSize.y); 
				
		GameTexture _texture =  new GameTexture(original,
				accessorySpriteAreaBegin.x + accessorySpriteWigTextureLocationOffset.x,
				accessorySpriteAreaBegin.y + accessorySpriteWigTextureLocationOffset.y,
				accessoryMapSpriteSize.x, accessoryMapSpriteSize.y);
		
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
					this.loadCacheWigTextures(loader, cache, true, t, c, this.getPartName());					
				}
			}
			else {
				this.loadCacheWigTextures(loader, cache, true, t, -1, this.getPartName());	
			}
		}
		
	}
	
	// Extracts a subSprite from the wig texture based on xid position, and then stores the texture for cid color id.
	public void loadCacheWigTextures(GamePartsLoader loader, AbstractGameTextureCache cache, boolean makeFinal, int xid, int cid,
			String fileName) {
		
		GameTexture original = this.fullWigTexture;
		String cacheKey = String.format("%s_wig_%d_%d", fileName.toLowerCase(), xid, cid);		
		int hash = original.hashCode() + GameRandom.prime(14) * (1+xid) * (this.hasColors() ? colors.getColorHash(cid) : 1);
		
		
		AbstractGameTextureCache.Element element = cache.get(cacheKey);
		GameTexture texture;
		if (element != null && element.hash == hash) {
			try {
				texture = new GameTexture(String.format("cached%s %s", this.getPartName(), cacheKey), element.textureData);
				if (makeFinal) {
					texture.makeFinal();
				}

				loader.addToList(this.wigTextures.get(cid == -1 ? 0 : cid), xid, texture);
				
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
		
		boolean x_dir = true;
		int numWigTextures = 0;
		if(wtw > wth) { numWigTextures = wtw / wth;}
		else if(wth > wtw) {
			numWigTextures = wth / wtw;
			x_dir = false;
		}
		
		if(numWigTextures == 0) {
			RaceMod.handleDebugMessage( String.format("Failed to create new cache key %s: Calculation of wig texture size resulted in zero wig textures for part %s.", cacheKey, this.getPartName()), 70  );
			return;
		}
		
		int wigTextureSize = x_dir ? (wtw / numWigTextures) : (wth / numWigTextures);
		
		int beginX = x_dir ? wigTextureSize * xid : 0;
		int beginY = x_dir ? 0 : wigTextureSize * xid;
		int sizeX = x_dir ? wigTextureSize : wtw;
		int sizeY = x_dir ? wth : wigTextureSize; 		
			
		GameTexture _texture = new GameTexture(original, beginX, beginY, sizeX, sizeY);
		loader.submitTaskAddToList(this.wigTextures.get(cid == -1 ? 0 : cid), xid, (String) null, () -> {
			 if(cid != -1) colors.replaceColors(_texture, cid);
			_texture.runPreAntialias(false);
			cache.set(cacheKey, hash, _texture);
			return _texture;
		}, makeFinal);
	}
	
		// Loads the full texture in cid color tone
		public void loadCachePartFullTextures(GamePartsLoader loader, AbstractGameTextureCache cache, boolean makeFinal, int tid,
				String fileName) {
			
			String originalTexturePath = this.getPartPath() + fileName + tid;
			GameTexture originalTexture = GameTexture.fromFile(originalTexturePath, true);
			
			if(originalTexture == null) {
				RaceMod.handleDebugMessage(String.format("Failed to load full size texture for part: %s texture id %d", originalTexturePath,tid),70);
			}
			String cacheKey = String.format("%s_%d", this.getPartPath() + fileName.toLowerCase(), tid);		
			int hash = originalTexture.hashCode() + GameRandom.prime(10) * tid;
			
			RaceMod.handleDebugMessage(String.format("Loading full texture: %s with cache key %s", originalTexturePath,cacheKey),70);
			
			AbstractGameTextureCache.Element element = cache.get(cacheKey);
			GameTexture texture;
			if (element != null && element.hash == hash) {
				try {
					texture = new GameTexture(String.format("cached%s %s", this.getPartName(), cacheKey), element.textureData);
					if (makeFinal) {
						texture.makeFinal();
					}					
					loader.addToList(this.fullTextures, tid-1, texture);
					RaceMod.handleDebugMessage(String.format("Found full texture in cache: %s", cacheKey),70);
					return;
					
				} catch (Exception var12) {
					RaceMod.handleDebugMessage(String.format("Could not load %s cache for %s", this.getPartName(), cacheKey),10);
				}
			} else {
				RaceMod.handleDebugMessage(String.format("Detected invalid: %s", cacheKey),70);
			}

			loader.triggerFirstTimeSetup();
			RaceMod.handleDebugMessage(String.format("Generating new: %s", cacheKey),70);	
			
			GameTexture _texture = new GameTexture(originalTexture);			
			loader.submitTaskAddToList(this.fullTextures, tid-1, (String)null,() -> {
				cache.set(cacheKey, hash, _texture);
				return _texture;
			}, makeFinal);		
		}

	
	private int tryFindResources() {
		
		String searchPath = this.getPartPath();
		String searchPrefix = this.getPartName().toLowerCase();
		String searchRegexValue =  searchPrefix + "\\d+\\.png$";
	    Pattern pattern = Pattern.compile("^.*" + searchPath + searchRegexValue);
	        
		Set<Map.Entry<String, ResourceFile>> allResources = ResourceEncoder.getAllFiles();
		
		long matchCount = allResources.parallelStream().filter((entry)->{			
			return pattern.matcher(entry.getValue().path).matches();
			}).count();
		
		RaceMod.handleDebugMessage("Found " + matchCount + " " + searchPrefix+" textures in JAR path: " + searchPath, 40);
		return (int)matchCount; 
		
	}

	@Deprecated
	private int tryFindTextures() {
		int foundTextures = 0;
		String searchPath = this.getPartPath();
		String searchPrefix = this.getPartName().toLowerCase();
		
	    try {
	    	
	        // Get the currently running mod's JAR file
	        LoadedMod runningMod = LoadedMod.getRunningMod();
	        JarFile modJar = runningMod.jarFile;
	        
	        if (!searchPath.endsWith("/")) {  searchPath += "/";    }
	        
	        // Define regex pattern to match texture files
	        String searchRegexValue =  searchPrefix + "\\d+\\.png$";
	        Pattern pattern = Pattern.compile("^resources/" + searchPath + searchRegexValue);

	        // List matching texture files
	        Enumeration<JarEntry> entries = modJar.entries();
	        while (entries.hasMoreElements()) {
	            JarEntry entry = entries.nextElement();
	            String entryName = entry.getName();
	            // Check if the entry matches our expected texture pattern
	            Matcher matcher = pattern.matcher(entryName);
	            if (matcher.matches()) {
	               foundTextures+=1;
	            }
	        }
	        
	    } catch (Exception e) {
	        e.printStackTrace();
	        RaceMod.handleDebugMessage("Error while searching for " + searchPrefix + " textures in JAR at: " + searchPath,5);
	    }
	    RaceMod.handleDebugMessage("Found " + foundTextures + " " + searchPrefix+" textures in JAR path: " + searchPath, 40);
	    return foundTextures;
	}
	
	
	public GameTexture getTexture(int side, int textureID, int colorID, int xID) {
		if(!this.texturesInitialized) return null;
		
		if(textureID > this.getTextureCount()) {
			RaceMod.handleDebugMessage(String.format("Texture ID %d for side %d not found for part %s", textureID, side, this.getPartName()),30);
			throw new ArrayIndexOutOfBoundsException();
			//return null; when we get done debugging
		}
		if(colorID != -1 && colorID > this.getColorCount()) {
			RaceMod.handleDebugMessage(String.format("No color with index %d found for texture index %d side %d for part %s", colorID,textureID, side, this.getPartName()),30);
			throw new ArrayIndexOutOfBoundsException();
		}
		if(side > 3) {
			String m = String.format("Side ID %d outside the range of valid values for part %s", side, this.getPartName());
			RaceMod.handleDebugMessage(m,30);
			throw new IllegalArgumentException(m);
		}
		else if(side > 0 && !this.hasSides()) {
			String m = String.format("Side ID %d outside the range of valid values for part %s: part has no side textures.", side, this.getPartName());
			RaceMod.handleDebugMessage(m,30);
			throw new IllegalArgumentException(m);
		}
		
		switch(side) {
			case 0: return this.backTextures.get(colorID).get(textureID).get(xID);
			case 1: return this.rightTextures.get(colorID).get(textureID).get(xID);
			case 2: return this.frontTextures.get(colorID).get(textureID).get(xID);
			case 3: return this.leftTextures.get(colorID).get(textureID).get(xID);
			default: return null;
		}
		
	}
	public GameTexture getWigTexture(int styleIndex, int colorIndex) {
		return this.wigTextures.get(colorIndex).get(styleIndex);
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
			if(part.getRacePartsClass().equals(g.assignedPart.getRacePartsClass()) && g.getPartName().equals(part.getPartName())) { return g.getTextureCount();}
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

	public GameTexture getFullTexture(int textureIndex) {
		return this.fullTextures.get(textureIndex);
	}



}