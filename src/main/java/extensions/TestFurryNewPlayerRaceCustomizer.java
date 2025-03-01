package extensions;

import java.awt.Color;
import java.awt.Point;
import java.util.ArrayList;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import core.RaceMod;
import core.gfx.GameParts;
import core.gfx.TestFurryDrawOptions;
import core.race.TestFurryRaceLook;
import core.race.parts.BodyPart;
import core.race.parts.TestFurryRaceParts;
import necesse.engine.Settings;
import necesse.engine.localization.message.LocalMessage;
import necesse.engine.network.NetworkClient;
import necesse.entity.mobs.MaskShaderOptions;
import necesse.entity.mobs.PlayerMob;
import necesse.gfx.GameHair;
import necesse.gfx.HumanLook;
import necesse.gfx.drawOptions.DrawOptions;
import overrides.CustomPlayerMob;
import necesse.gfx.drawOptions.human.HumanDrawOptions;
import necesse.gfx.drawOptions.human.HumanDrawOptions.HumanDrawOptionsGetter;
import necesse.gfx.forms.components.FormContentVarToggleButton;
import necesse.gfx.forms.components.FormPlayerIcon;
import necesse.gfx.gameTexture.GameSprite;
import necesse.gfx.gameTexture.GameTexture;
import necesse.gfx.gameTexture.GameTexture.BlendQuality;
import necesse.inventory.InventoryItem;
import necesse.inventory.item.armorItem.cosmetics.misc.ShirtArmorItem;
import necesse.inventory.item.armorItem.cosmetics.misc.ShoesArmorItem;
import necesse.level.maps.light.GameLight;

public class TestFurryNewPlayerRaceCustomizer extends FormNewPlayerRaceCustomizer {
			
	public TestFurryNewPlayerRaceCustomizer(int x, int y, int width, boolean allowSupernaturalChanges, boolean allowClothesChance) {
		super(new TestFurryRaceLook(true), x, y, width, allowClothesChance, allowClothesChance);		
	}	
	
	@SuppressWarnings("unchecked")
	public TestFurryRaceLook getCustomRaceLook() {	
		if (this.getRaceID() != TestFurryRaceLook.TEST_FURRY_RACE_ID) {
			RaceMod.handleDebugMessage("Problem converting base RaceLook class to super "+this.getClass().getName(), 25);
			return null;
		}
		return (TestFurryRaceLook)this.getRaceLook();
	}
	
	@Override
	protected RaceLook racelookFromBase(HumanLook look) {
		// TODO Auto-generated method stub
		return new TestFurryRaceLook(look);
	}

	public void initializeIcon(int x, int iconY, int width) {
		// Create the FormPlayerIcon and cast it to FormComponent if necessary
	    FormPlayerIcon formPlayerIcon = new FormPlayerIcon(x, iconY, 128, 128, this.newPlayer) {
	        @Override
	        public void modifyHumanDrawOptions(HumanDrawOptions drawOptions) {
	            //super.modifyHumanDrawOptions(drawOptions);
	            TestFurryNewPlayerRaceCustomizer.this.modifyHumanDrawOptions(drawOptions);
	        }
	    };

	    // Add the FormPlayerIcon to the component list
	    this.icon = (FormPlayerIcon) this.addComponent(formPlayerIcon);

	    this.icon.acceptRightClicks = true;

	    this.icon.onClicked((e) -> {
	        if (e.event.getID() == -99) {
	            this.icon.setRotation(this.icon.getRotation() - 1);
	        } else {
	            this.icon.setRotation(this.icon.getRotation() + 1);
	        }
	    });
	}	
	public void modifyHumanDrawOptions(HumanDrawOptions drawOptions) {
		
		//(this.player, this.dir, this.spriteX, this.spriteY, this.spriteRes,
		//drawX, drawY, this.width, this.height, this.mirrorX, this.mirrorY, this.light, this.alpha,
		//this.mask)
		drawOptions.addTopDraw(new TestFurryDrawOptions.FurryDrawOptionsGetter() {
			
				@Override
				public DrawOptions getDrawOptions(PlayerMob player, int dir, int spriteX, int spriteY, int spriteRes, int drawX, int drawY,
						int width, int height, boolean mirrorX, boolean mirrorY, GameLight light, float alpha, MaskShaderOptions mask) {
					
					TestFurryRaceLook rl = TestFurryDrawOptions.getCustomRaceLook(TestFurryDrawOptions.getRaceLook((CustomPlayerMob)player));
					return new TestFurryDrawOptions(player.getLevel(), player)
							.earsTexture(rl.getEarsTexture(spriteX, spriteY).resize(width, height))
							.muzzleTexture(rl.getMuzzleTexture(spriteX, spriteY).resize(width, height))
							.tailTexture(rl.getTailTexture(spriteX, spriteY).resize(width, height))
							.dir(dir)
							.drawOffset(0, 0).pos(drawX, drawY)
							.drawEars(true).drawMuzzle(true).drawTail(false);

				}
			});
		
		drawOptions.addOnBodyDraw(new TestFurryDrawOptions.FurryDrawOptionsGetter() {
			
			@Override
			public DrawOptions getDrawOptions(PlayerMob player, int dir, int spriteX, int spriteY, int spriteRes, int drawX, int drawY,
					int width, int height, boolean mirrorX, boolean mirrorY, GameLight light, float alpha, MaskShaderOptions mask) {
				
				TestFurryRaceLook rl = TestFurryDrawOptions.getCustomRaceLook(TestFurryDrawOptions.getRaceLook((CustomPlayerMob)player));
				return new TestFurryDrawOptions(player.getLevel(), player)
						.tailTexture(rl.getTailTexture(spriteX, spriteY).resize(width, height))
						.dir(dir)
						.drawOffset(0, 0).pos(drawX, drawY)
						.drawEars(false).drawMuzzle(false).drawTail(true);

			}
		});
		
	}
	public Point getDrawOffset(BodyPart part) {
	    switch (part.getPartName()) {
	        case "SKIN_COLOR": 	return getSkinFaceDrawOffset();
	        case "EYE_TYPE":	return getEyeTypeFaceDrawOffset();
	        case "EYE_COLOR":  	return getEyeColorFaceDrawOffset();
	        case "HAIR_STYLE": 
	        case "FACIAL_HAIR":
	        case "HAIR_COLOR": 	return new Point(0, 0);
	        case "TAIL": 		return getTailTypeDrawOffset();
	        case "EARS": 		return getEarsTypeFaceDrawOffset();
	        case "MUZZLE": 		return getMuzzleTypeFaceDrawOffset();
	        default: 			return new Point(0, 0);
	    }
	}
	
	public Point getSkinFaceDrawOffset() 		{	return new Point(-3, -4);	}
	
	public Point getEyeTypeFaceDrawOffset() 	{	return new Point(-22, -26);	}
	
	public Point getEyeColorFaceDrawOffset() 	{	return new Point(-22, -26);	}
	
	private Point getTailTypeDrawOffset() 	{	return new Point(0, -26);		}
	
	private Point getMuzzleTypeFaceDrawOffset() {	return new Point(0, 0);		}

	private Point getEarsTypeFaceDrawOffset() 	{	return new Point(0, 0);		}
	
	// Returns the modification cost (stub for customization)
	protected ArrayList<InventoryItem> getPartModificationCost(Color color) {
		return null; // Customize as needed
	}

	// Updates the player look and triggers necessary updates
	protected void updateBodyPartSelection(BodyPart part, Object id) {
		this.setLookAttribute(this.getRaceLook(), part, id);		
		this.onChanged();		
	}

	@Override
	protected Object getCurrentBodyPartSelection(BodyPart part) {
		int DEBUG_VALUE = 80;
	    Object value;  // Store the selected value for logging

	    TestFurryRaceLook ccr = (TestFurryRaceLook)this.getRaceLook();
	    switch (part.getPartName()) {
	    	// Base Game Parts
	        case "SKIN_COLOR": 	            value = ccr.getSkin();	            	break;
	        case "EYE_TYPE": 	            value = ccr.getEyeType();	            break;
	        case "EYE_COLOR": 	            value = ccr.getEyeColor();	            break;
	        case "HAIR_STYLE": 	            value = ccr.getHair();	            	break;
	        case "FACIAL_HAIR": 	        value = ccr.getFacialFeature();	        break;
	        case "HAIR_COLOR": 	            value = ccr.getHairColor();	            break;
	        case "SHIRT_COLOR": 	        value = ccr.getShirtColor();	            break;
	        case "SHOES_COLOR": 	        value = ccr.getShoesColor();	            break;
	        
	        // Custom Race Parts
	        case "TAIL": 	            	value = ccr.getTailStyle();	            break;
	        case "TAIL_COLOR": 	            value = ccr.getTailColor();	            break;
	        case "EARS": 	        		value = ccr.getEarsStyle();	            break;
	        case "EARS_COLOR": 	        	value = ccr.getEarsColor();	            break;
	        case "MUZZLE": 	        		value = ccr.getMuzzleStyle();	            break;
	        case "MUZZLE_COLOR": 	        value = ccr.getMuzzleColor();	            break;
	        default: 
	            value = -1; // Fallback value for unknown parts
	            break;
	    }
	
	    RaceMod.handleDebugMessage("getCurrentBodyPartSelection(" + part.getPartName() + ") = " + value 
		        + " [Type: " + (value != null ? value.getClass().getSimpleName() : "null") + "]", DEBUG_VALUE);

	    return value;
	}


	@Override
	protected void setLookAttribute(RaceLook look, BodyPart part, Object value) {
		
		TestFurryRaceLook ccr = (TestFurryRaceLook)look;
	    if (value instanceof Integer) {
	        int intValue = (Integer) value;
	        switch (part.getPartName()) {
	            case "SKIN_COLOR": 		ccr.setSkin(intValue); 					break;
	            case "EYE_TYPE": 		ccr.setEyeType(intValue); 				break;
	            case "HAIR_STYLE": 		ccr.setHair(intValue); 					break;
	            case "FACIAL_HAIR": 	ccr.setFacialFeature(intValue); 		break;
	            case "EYE_COLOR": 		ccr.setEyeColor(intValue); 				break;
	            case "HAIR_COLOR": 		ccr.setHairColor(intValue); 			break;
	            
	            case "TAIL": 	    	ccr.setTailStyle(intValue);	            break;
		        case "TAIL_COLOR": 		ccr.setTailColor(intValue);	            break;
		        case "EARS": 	    	ccr.setEarsStyle(intValue);	            break;
		        case "EARS_COLOR": 		ccr.setEarsColor(intValue);	            break;
		        case "MUZZLE": 	    	ccr.setMuzzleStyle(intValue);	           break;
		        case "MUZZLE_COLOR":	ccr.setMuzzleColor(intValue);	           break;
	        }
	    } else if (value instanceof Color) {
	    	
	        Color colorValue = (Color) value;
	        switch (part.getPartName()) {	            
	            case "SHIRT_COLOR": ccr.setShirtColor(colorValue); break;
	            case "SHOES_COLOR": ccr.setShoesColor(colorValue); break;
	        }
	        
	    } else {
	        throw new IllegalArgumentException("Unsupported type for body part: " + part.getPartName());
	    }
	    this.updateLook();
		this.updateComponents();
	}

	// Draws the icon for each body part section
	protected void drawBodyPartIcon(FormContentVarToggleButton button, BodyPart part, int x, int y, int _width, int _height) {
		TestFurryRaceLook look = new TestFurryRaceLook(this.getCustomRaceLook());
		applyLookModifiers(look, part);
		HumanDrawOptions options = new HumanDrawOptions(null, look, false);
		Point offset = getDrawOffset(part);
		int drawX = x + _width / 2;
		int drawY = y + _height / 2;
	
		if (part.getPartName() == "TAIL" || part.getPartName() == "EARS" || part.getPartName() == "MUZZLE") {
			
			 // Handle Hair style drawing
			
			int styleIndex = (part.getPartName() == "TAIL") ? look.getTailStyle() :
				(part.getPartName() == "MUZZLE") ? look.getMuzzleStyle() :
					look.getEarsStyle();
			int colorIndex = (part.getPartName() == "TAIL") ? look.getTailColor() :
				(part.getPartName() == "MUZZLE") ? look.getMuzzleColor() :
					look.getEarsColor();
			
			//(int side, int textureID, int colorID, int xID)
			GameParts partParts = GameParts.getPart(TestFurryRaceParts.class, part.getPartName());	
		    GameTexture styleTexture = partParts.getTexture(2, styleIndex, colorIndex, PREVIEW_TEXTURE_X_POSITION);	
		    if(styleTexture == null) {
		    	  RaceMod.handleDebugMessage(
		  	            String.format("Part: %s for race %s could not load style index %d.", this.getRaceID(), part.getPartName(), styleIndex), 
		  	            70
		  	        );
		    }
		    styleTexture.initDraw()
		              .size(_height)
		              .posMiddle(drawX, drawY)
		              .draw();
		}  else {
			super.baseDrawBodyPartIcon(button, look, part, options, x, y, _width, _height, offset);
		}		
	
	}	
	
	// Draws the preview for each body part selection
	protected void drawBodyPartPreview(FormContentVarToggleButton button, BodyPart part, int id, int x, int y, int _width, int _height) {
		
		TestFurryRaceLook look = new TestFurryRaceLook(this.getRaceLook());
		applyLookModifiers(look, part);
		setLookAttribute(look, part, id);
		HumanDrawOptions options = new HumanDrawOptions(null, look, false);
		
		// Center position for the preview
		int drawX = x + _width / 2;
		int drawY = y + _height / 2;
		
		// Drawing based on body part type
		if (part.getPartName() == "TAIL" || part.getPartName() == "EARS" || part.getPartName() == "MUZZLE") {
			
		    // Hair Style preview
			int styleIndex = (part.getPartName() == "TAIL") ? look.getTailStyle() :
				(part.getPartName() == "MUZZLE") ? look.getMuzzleStyle() :
					look.getEarsStyle();
			int colorIndex = (part.getPartName() == "TAIL") ? look.getTailColor() :
				(part.getPartName() == "MUZZLE") ? look.getMuzzleColor() :
					look.getEarsColor();
			
			//(int side, int textureID, int colorID, int xID)
			GameParts partParts = GameParts.getPart(TestFurryRaceParts.class, part.getPartName());			
			GameTexture styleTexture = partParts.getTexture(2, styleIndex, colorIndex, PREVIEW_TEXTURE_X_POSITION);
			
			if(styleTexture == null) {
				RaceMod.handleDebugMessage( String.format("Could not load style texture ID %d for part %s in form %s.", styleIndex, part.getPartName(), this.getClass().getName()), 70  );
			}
		    styleTexture.initDraw().size(_height).posMiddle(drawX, drawY).draw();		    
		    
		    
		} else {
			super.baseDrawBodyPartPreview(button, look, part, options, id, x, y, _width, _height);
		}
	}

	protected void applyLookModifiers(RaceLook look, BodyPart part) {
		if (part.getPartName() == "SKIN_COLOR" || part.getPartName() == "EYE_TYPE" || part.getPartName() == "EYE_COLOR" || part.getPartName() == "MUZZLE" || part.getPartName() == "EARS") {
		    look.setHair(0);
		    look.setFacialFeature(0);
		}
	}

	protected Section createColorCustomSection(BodyPart part, String labelKey, Supplier<Color> colorGetter, 
		Consumer<Color> colorSetFunc, Function<Color, ArrayList<InventoryItem>> costFunc, Predicate<Section> isCurrent, int _width) {
				
		
		Color[] defaultColors = this.getRaceLookParts().defaultColors();
		String itemnm = part.getPartName().equals("SHIRT_COLOR") ? "shirt" : 
            part.getPartName().equals("SHOES_COLOR") ? "shoes" : "";
		
		Function<Color, InventoryItem> itemSetter = itemnm.equals("shirt") ?
			    (cc) -> ShirtArmorItem.addColorData(new InventoryItem(itemnm), cc) :
			    (cc) -> ShoesArmorItem.addColorData(new InventoryItem(itemnm), cc);
			    
		return new Section(
		(button, drawX, drawY, width, height) -> {
			
		    InventoryItem item = itemSetter.apply(colorGetter.get());
		    int size = Math.min(width, height);
		    item.drawIcon((PlayerMob) null, drawX + width / 2 - size / 2, drawY + height / 2 - size / 2, size, null);
		    
		},
		new LocalMessage(part.getLabelCategory(), part.getLabelKey()), 
		this.getSelectionColorOrCustom(extensions.FormNewPlayerRaceCustomizer.BUTTON_SIZE, _width, 
		    (button, id, drawX, drawY, width, height, current, hovering) -> {
		    	
		        Color color = defaultColors[id];
		        InventoryItem item = itemSetter.apply(color);
		        int size = Math.min(width, height);
		        item.drawIcon((PlayerMob) null, drawX + width / 2 - size / 2, drawY + height / 2 - size / 2, size, null);
		        
		    }, 
		    ()->{return colorGetter.get();}, 
		    (color) -> {		    	
		    	this.setLookAttribute(this.getRaceLook(), part, color);
		       // this.updateLook();
		    },
		    (color) -> {
		    	this.updateBodyPartSelection(part, color);              
		      //  this.updateLook();
		       // this.updateComponents();          
		    },
		    (color)->{return costFunc.apply(color);}
		),
		isCurrent
		);
	}			
	
	// Generates sections dynamically for each body part
	protected Section createBodyPartSection(BodyPart part, Predicate<Section> isCurrent, int _width) {

		if (part.getPartName().equals("SHIRT_COLOR") || part.getPartName().equals("SHOES_COLOR")) {
			return createColorCustomSection(
					part,
					part.getLabelKey(),					 		            
		            ()->(Color)(this.getCurrentBodyPartSelection(part)),            
		            (color) -> {this.setLookAttribute(this.getRaceLook(), part, (Color)color);},		            
		            (color)->this.getPartModificationCost(color),
		            isCurrent,
		            _width
		        );
		}
		else {
			
			 return new Section(
			    		(button, drawX, drawY, width, height) -> drawBodyPartIcon(button, part, drawX, drawY, width, height),
			    			new LocalMessage(part.getLabelCategory(), part.getLabelKey()),	    			
			        this.getSelectionContent(
			            extensions.FormNewPlayerRaceCustomizer.BUTTON_SIZE, _width, part.getTotalTextureOptions(),
			            (button, id, x, y, w, h, current, hovering) -> drawBodyPartPreview(button, part, id, x, y, w, h),
			            id -> id == (Integer)getCurrentBodyPartSelection(part),
			            (id, event) -> updateBodyPartSelection(part, id),
			            (color)->this.getPartModificationCost(new Color(color))
			        		),
			        		isCurrent
			    		);
			 
		}	 
	
	}



	public void reset() {
		this.setPlayer(new CustomPlayerMob(0L, (NetworkClient) null));
	}

	public void randomize() {		
		this.getCustomRaceLook().randomizeLook();	
		this.updateComponents();
	}
		
	public void onChanged() {	}

	protected void updateLook() {
		this.newPlayer.getInv().giveLookArmor();
	}

	public PlayerMob getNewPlayer() {
		this.newPlayer.getInv().giveStarterItems();
		return this.newPlayer;
	}

	

	public ArrayList<InventoryItem> getSkinColorCost(int id) 		{		return null;	}
	
	public ArrayList<InventoryItem> getEyeTypeCost(int id) 			{		return null;	}
	
	public ArrayList<InventoryItem> getEyeColorCost(int id) 		{		return null;	}
	
	public ArrayList<InventoryItem> getHairStyleCost(int id) 		{		return null;	}
	
	public ArrayList<InventoryItem> getFacialFeatureCost(int id) 	{		return null;	}
	
	public ArrayList<InventoryItem> getHairColorCost(int id) 		{		return null;	}
	
	public ArrayList<InventoryItem> getShirtColorCost(Color color) 	{		return null;	}
	
	public ArrayList<InventoryItem> getShoesColorCost(Color color) 	{		return null;	}

	


	

}