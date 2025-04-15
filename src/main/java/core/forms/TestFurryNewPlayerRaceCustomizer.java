package core.forms;

import java.awt.Color;
import java.awt.Point;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import core.gfx.EyeTypeGamePart;
import core.gfx.GameParts;
import core.race.RaceLook;
import core.race.TestFurryRaceLook;
import core.race.factory.RaceDataFactory;
import core.race.parts.BodyPart;
import core.race.parts.TestFurryRaceParts;
import extensions.CustomHumanDrawOptions;
import helpers.DebugHelper;
import helpers.DebugHelper.MESSAGE_TYPE;
import necesse.engine.localization.message.LocalMessage;
import necesse.engine.network.NetworkClient;
import necesse.entity.mobs.MaskShaderOptions;
import necesse.entity.mobs.PlayerMob;
import necesse.gfx.HumanLook;
import necesse.gfx.drawOptions.DrawOptions;
import necesse.gfx.drawOptions.DrawOptionsList;
import necesse.gfx.drawOptions.human.HumanDrawOptions;
import necesse.gfx.forms.components.FormContentVarToggleButton;
import necesse.gfx.forms.components.FormPlayerIcon;
import necesse.gfx.gameTexture.GameTexture;
import necesse.gfx.gameTexture.GameTexture.BlendQuality;
import necesse.inventory.InventoryItem;
import necesse.level.maps.light.GameLight;

public class TestFurryNewPlayerRaceCustomizer extends FormNewPlayerRaceCustomizer {
			
	public TestFurryNewPlayerRaceCustomizer(int x, int y, int width, boolean allowSupernaturalChanges, boolean allowClothesChance) {
		super(new TestFurryRaceLook(true), x, y, width, allowClothesChance, allowClothesChance);		
	}	
	
	@SuppressWarnings("unchecked")
	public TestFurryRaceLook getCustomRaceLook() {	
		if (!this.getRaceLook().getRaceID().equals(TestFurryRaceLook.TEST_FURRY_RACE_ID)) {
			DebugHelper.handleFormattedDebugMessage("Problem converting base RaceLook class with race %s at form %s", 5, MESSAGE_TYPE.ERROR, new Object[] {this.getRaceID(), this.getClass().getName()});
			return new TestFurryRaceLook(true);
		}
		return (TestFurryRaceLook)this.getRaceLook();
	}
	
	@Override
	public RaceLook getRaceLook() {
	    return RaceDataFactory.getRaceLook(this.getPlayerHelper(), new TestFurryRaceLook(true));
	}
	
	@Override
	public void setLook(HumanLook look) {
		this.setRaceLook(RaceLook.fromHumanLook(look, TestFurryRaceLook.class));
		this.updateComponents();
	}
	
	@Override
	protected RaceLook racelookFromBase(HumanLook look) {
		return new TestFurryRaceLook(look);
	}

	public void initializeIcon(int x, int iconY, int width) {

		DebugHelper.handleFormattedDebugMessage("Player Icon form initialized for %s with race %s", 50, MESSAGE_TYPE.DEBUG, new Object[] {this.getPlayerHelper().playerName, this.getRaceID()});
	    FormPlayerIcon formPlayerIcon = new FormPlayerIcon(x, iconY, 128, 128, this.getPlayerHelper()) {
	        @Override
	        public void modifyHumanDrawOptions(HumanDrawOptions drawOptions) {
	            super.modifyHumanDrawOptions(drawOptions);
	            TestFurryNewPlayerRaceCustomizer.this.modifyHumanDrawOptions(this, (CustomHumanDrawOptions) drawOptions);
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
	
	public void modifyHumanDrawOptions(FormPlayerIcon form, CustomHumanDrawOptions drawOptions) {		
		
		this.getCustomRaceLook().modifyHumanDrawOptions(drawOptions, null);		
	}
	public Point getDrawOffset(BodyPart part) {
	    switch (part.getPartName()) {
	        case "HAIR_COLOR": 	return new Point(0, 0);
	        case "TAIL": 		return getTailTypeDrawOffset();
	        case "EARS": 		return getEarsTypeFaceDrawOffset();
	        case "MUZZLE": 		return getMuzzleTypeFaceDrawOffset();
	        case "HEAD": 		return getHeadTypeDrawOffset();
	        case "CUSTOM_EYES": return getEyeTypeFaceDrawOffset();
	        case "CUSTOM_EYES_COLOR": return getEyeColorFaceDrawOffset();
	        default: 			return super.baseGetDrawOffset(part);
	    }
	}
	
	private Point getHeadTypeDrawOffset() 		{	return new Point(0, 0);		}

	public Point getSkinFaceDrawOffset() 		{	return new Point(-3, -4);	}
	
	public Point getEyeTypeFaceDrawOffset() 	{	return new Point(-22, -26);	}
	
	public Point getEyeColorFaceDrawOffset() 	{	return new Point(-22, -26);	}
	
	private Point getTailTypeDrawOffset() 		{	return new Point(0, 0);		}
	
	private Point getMuzzleTypeFaceDrawOffset() {	return new Point(0, -8);		}

	private Point getEarsTypeFaceDrawOffset() 	{	return new Point(0, 8);		}
	
	// Returns the modification cost (stub for customization)
	protected ArrayList<InventoryItem> getPartModificationCost(Color color) {
		return null; // Customize as needed
	}

	// Updates the player look and triggers necessary updates
	protected void updateBodyPartSelection(BodyPart part, Object id, boolean colorCustomization) {
		this.setLookAttribute(this.getRaceLook(), part, id, colorCustomization);		
		this.onChanged();		
	}
	

	@Override
	protected Object getCurrentBodyPartSelection(BodyPart part, boolean colorCustomization) {
	    Object value;  // Store the selected value for logging

	    TestFurryRaceLook ccr = this.getCustomRaceLook();
	    String targetPartName = colorCustomization ? part.getPartColorName() : part.getPartName();
	    switch (targetPartName) {	    
	        // Custom Race Parts
	        case "TAIL": 	            	value = ccr.getTailStyle();	        break;
	        case "TAIL_COLOR": 	            value = ccr.getTailColor();	        break;
	        case "EARS": 	        		value = ccr.getEarsStyle();	        break;
	        case "EARS_COLOR": 	        	value = ccr.getEarsColor();	        break;
	        case "MUZZLE": 	        		value = ccr.getMuzzleStyle();	    break;
	        case "MUZZLE_COLOR": 	        value = ccr.getMuzzleColor();	    break;
	        case "HEAD": 	        		value = ccr.getHeadStyle();	    	break;
	        case "HEAD_COLOR": 	        	value = ccr.getHeadColor();	    	break;
	        case "BODY": 	        		value = ccr.getBodyStyle();	    	break;
	        case "BODY_COLOR": 	        	value = ccr.getBodyColor();	    	break;
	        case "ARMS": 	        		value = ccr.getArmsStyle();	    	break;
	        case "ARMS_COLOR": 	        	value = ccr.getArmsColor();	    	break;	   
	        case "FEET": 	        		value = ccr.getFeetStyle();	    	break;
	        case "FEET_COLOR": 	        	value = ccr.getFeetColor();	    	break;	  
	        case "CUSTOM_EYES": 	        		value = ccr.getCustomEyesStyle();	    	break;
	        case "CUSTOM_EYES_COLOR": 	        	value = ccr.getCustomEyesColor();	    	break;	    
	        default:        value = super.baseGetCurrentBodypartSelection(part);	break;
	    }
	
	    return value;
	}


	@Override
	protected void setLookAttribute(RaceLook look, BodyPart part, Object value, boolean colorCustomization) {
		
		TestFurryRaceLook ccr = (TestFurryRaceLook)look;
	    if (value instanceof Integer) {
	        int intValue = (Integer) value;
	        switch (colorCustomization ? part.getPartColorName() : part.getPartName()) {
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
		        
		        case "MUZZLE": 	    	ccr.setMuzzleStyle(intValue);	        break;
		        case "MUZZLE_COLOR":	ccr.setMuzzleColor(intValue);	        break;
		        
		        case "HEAD": 	    	ccr.setHeadStyle(intValue);	           	break;		  
		        case "HEAD_COLOR": 	    ccr.setHeadColor(intValue);          	break;	
		        
		        case "BODY": 	    	ccr.setBodyStyle(intValue);	           	break;		  
		        case "BODY_COLOR": 	    ccr.setBodyColor(intValue);          	break;	
		        
		        case "ARMS": 	    	ccr.setArmsStyle(intValue);	           	break;		  
		        case "ARMS_COLOR": 	    ccr.setArmsColor(intValue);          	break;	
		        
		        case "FEET": 	    	ccr.setFeetStyle(intValue);	           	break;		  
		        case "FEET_COLOR": 	    ccr.setFeetColor(intValue);          	break;	
		        
		        case "CUSTOM_EYES": 	    	ccr.setCustomEyesStyle(intValue);	           	break;		  
		        case "CUSTOM_EYES_COLOR": 	    ccr.setCustomEyesColor(intValue);          	break;	
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
		CustomHumanDrawOptions options = new CustomHumanDrawOptions(null, look, false);
		Point offset = getDrawOffset(part);
		int drawX = x + _width / 2;
		int drawY = y + _height / 2;
	
		
		if (this.getRaceLookParts().hasCustomPart(part.getPartName())) {
			
			 // Handle Hair style drawing
			
			int styleIndex = look.appearanceByteGet(part.getPartName());
			int colorIndex = look.appearanceByteGet(part.getPartColorName());
			
			//(int side, int textureID, int colorID, int xID)
			GameParts partParts = GameParts.getPart(TestFurryRaceParts.class, part.getPartName());	
			if(part.hasWigTexture()) {
			   // GameTexture styleTexture = partParts.getTexture(2, styleIndex, colorIndex, PREVIEW_TEXTURE_X_POSITION);	
			    GameTexture wigTexture = partParts.getWigTexture(styleIndex, colorIndex);	
			    if(wigTexture == null) {
			    	  DebugHelper.handleFormattedDebugMessage(
			  	            "Part: %s for race %s could not load style index %d.", 5, MESSAGE_TYPE.ERROR, new Object[] {this.getRaceID(), part.getPartName(), styleIndex} 	        );
			    }
			    wigTexture.initDraw()
			              .size(_height)
			              .posMiddle(drawX + offset.x, drawY + offset.y)
			              .draw();
			}
			else {
				
				if(part.getPartName().equals("CUSTOM_EYES") || part.getPartName().equals("CUSTOM_EYES_COLOR")) {
					
					GameTexture.overrideBlendQuality = BlendQuality.NEAREST;
					getFurryFaceDrawOptions(look, options, button.size.height * 2, x+offset.x, y+offset.y, (opt) -> {
							opt.sprite(0, 3).dir(3);
						}).draw();
					 GameTexture.overrideBlendQuality = null;
				}
				
			}
		}  else {
			
			super.baseDrawBodyPartIcon(button, look, part, options, x, y, _width, _height, offset);
		
		}		
	
	}	
	
	// Draws the preview for each body part selection
	protected void drawBodyPartPreview(FormContentVarToggleButton button, BodyPart part, boolean colorCustomization, int id, int x, int y, int _width, int _height) {
		
		TestFurryRaceLook look = new TestFurryRaceLook(this.getRaceLook());
		applyLookModifiers(look, part);
		
		setLookAttribute(look, part, id, colorCustomization);
		setLookAttribute(look, part, this.getCurrentBodyPartSelection(part, !colorCustomization), !colorCustomization);

		CustomHumanDrawOptions options = new CustomHumanDrawOptions(null, look, false);
		
		// Center position for the preview
		int drawX = x + _width / 2;
		int drawY = y + _height / 2;
		Point offset = this.getDrawOffset(part);
		
		
		// Drawing based on body part type
		if (this.getRaceLookParts().hasCustomPart(part.getPartName())) {
			
			int styleIndex = look.appearanceByteGet(part.getPartName());
			int colorIndex = look.appearanceByteGet(part.getPartColorName());
	
			if(part.hasWigTexture()) {
				
				//(int side, int textureID, int colorID, int xID)
				GameParts partParts = GameParts.getPart(TestFurryRaceParts.class, part.getPartName());			
				//GameTexture styleTexture = partParts.getTexture(2, styleIndex, colorIndex, PREVIEW_TEXTURE_X_POSITION);
				
				GameTexture wigTexture = partParts.getWigTexture(styleIndex, colorIndex);	
				if(wigTexture == null) {
					
					  DebugHelper.handleFormattedDebugMessage(
				  	            "Could not load style texture ID %d for part %s with color %d in form %s.",
				  	            5, MESSAGE_TYPE.ERROR, new Object[] {styleIndex, part.getPartName(), colorIndex, this.getClass().getName()} );
	
				
				}
				wigTexture.initDraw().size(_height).posMiddle(drawX+offset.x, drawY+offset.y).draw();	
			}
			else {
				if(part.getPartName().equals("CUSTOM_EYES")|| part.getPartName().equals("CUSTOM_EYES_COLOR")) {
					
				    // Eye preview (using HumanFaceDrawOptions)
					GameTexture.overrideBlendQuality = BlendQuality.NEAREST;
					this.getFurryFaceDrawOptions(look, options, button.size.height, x, y, (opt) -> {
				        opt.sprite(0, 3).dir(3);  // Set specific sprite direction
				    }).draw();
				    GameTexture.overrideBlendQuality = null;
					
				}
			}
		    
		    
		} else {
			super.baseDrawBodyPartPreview(button, look, part, options, id, x, y, _width, _height);
		}
	}

	protected void applyLookModifiers(RaceLook look, BodyPart part) {
		if (part.getPartName() == "SKIN_COLOR" || part.getPartName() == "EYE_TYPE" ||
				part.getPartName() == "EYE_COLOR" || part.getPartName() == "MUZZLE" || part.getPartName() == "EARS" ||
				part.getPartName() == "CUSTOM_EYES_COLOR" || part.getPartName() == "CUSTOM_EYES") {
		    look.setHair(0);
		    look.setFacialFeature(0);
		}
	}

	protected Section createColorCustomSection(BodyPart part, String labelKey, Supplier<Color> colorGetter, 
		Consumer<Color> colorSetFunc, Function<Color, ArrayList<InventoryItem>> costFunc, Predicate<Section> isCurrent, int _width) {
				
			
		return super.createColorCustomSection(part, labelKey, colorGetter, colorSetFunc, costFunc, isCurrent, _width);
	}			
	
	// Generates sections dynamically for each body part
	protected Section createBodyPartSection(BodyPart part, Predicate<Section> isCurrent, int _width) {
			return super.createBodyPartSection(part, isCurrent, _width);
	}
	
	protected Section createBodyPartCustomColorSection(BodyPart part, Predicate<Section> isCurrent, int _width) {

		 return new Section(					 
    		(button, drawX, drawY, width, height) -> drawBodyPartIcon(button, part, drawX, drawY, width, height),
    			new LocalMessage(part.getLabelCategory(), part.getLabelColorKey()),			    			
    			this.getSelectionContent(	core.forms.FormNewPlayerRaceCustomizer.BUTTON_SIZE, _width, part.getTotalColorOptions(),
            (button, id, x, y, w, h, current, hovering) -> drawBodyPartPreview(button, part, true, id, x, y, w, h),
            id -> id == (Integer)getCurrentBodyPartSelection(part, true),
            (id, event) -> updateBodyPartSelection(part, id, true),
            (color)->this.getPartModificationCost(new Color(color))	),
        		isCurrent
    		);
	}

	public void randomize() {		
		this.getCustomRaceLook().randomizeLook();	
		this.updateComponents();
	}
	
	@Override
	public void reset() {
		this.setPlayerHelper(new PlayerMob(0L, (NetworkClient) null));
	}
	
	public void onChanged() {
		super.onChanged();
	}
	

	public static <T> List<T> getClosedCustomEyesDrawOptions(int eyeType, int eyeColor, int skinColor, boolean humanlikeOnly,
			Function<GameTexture, T> mapper) {
		return ((EyeTypeGamePart)GameParts.getPart(TestFurryRaceParts.class,"CUSTOM_EYES"))
				.getClosedColorTextures(eyeType, eyeColor, skinColor, mapper);
	}

	public static <T> List<T> getOpenCustomEyesDrawOptions(int eyeType, int eyeColor, int skinColor, boolean humanlikeOnly,
			Function<GameTexture, T> mapper) {
		return ((EyeTypeGamePart)GameParts.getPart(TestFurryRaceParts.class,"CUSTOM_EYES"))
				.getOpenColorTextures(eyeType, eyeColor, skinColor, mapper);
		
	}

	public static DrawOptions getCustomEyesDrawOptions(int eyeType, int eyeColor, int skinColor, boolean humanlikeOnly,
			boolean closed, int drawX, int drawY, int spriteX, int spriteY, int width, int height, boolean mirrorX,
			boolean mirrorY, float alpha, GameLight light, MaskShaderOptions mask) {
		Function<GameTexture, DrawOptions> mapper = (texture) -> {
			return texture.initDraw().sprite(spriteX, spriteY, 64).light(light).alpha(alpha).size(width, height)
					.mirror(mirrorX, mirrorY).addMaskShader(mask).pos(drawX, drawY);
		};
		return closed
				? new DrawOptionsList(getClosedCustomEyesDrawOptions(eyeType, eyeColor, skinColor, humanlikeOnly, mapper))
				: new DrawOptionsList(getOpenCustomEyesDrawOptions(eyeType, eyeColor, skinColor, humanlikeOnly, mapper));
	}

	public DrawOptions getFurryFaceDrawOptions(RaceLook look, CustomHumanDrawOptions humanDrawOptions, int size, int drawX,
			int drawY) {
		return getFurryFaceDrawOptions(look, humanDrawOptions, size, drawX, drawY, (Consumer<CustomHumanDrawOptions>) null);
	}
	
	public DrawOptions getFurryFaceDrawOptions(RaceLook look, CustomHumanDrawOptions humanDrawOptions, int size, int drawX, int drawY,
			Consumer<CustomHumanDrawOptions> additionalModifiers) {
		
		Point offset = getFaceHairTextureOffset();
		float sizeChange = 32.0F / (float) size;
		int offsetX = (int) ((float) offset.x / sizeChange);
		int offsetY = (int) ((float) offset.y / sizeChange);
		
		humanDrawOptions = humanDrawOptions.sprite(0, 2).dir(2).bodyTexture((GameTexture) null)
				.feetTexture((GameTexture) null).size(size * 2, size * 2).leftArmsTexture((GameTexture) null)
				.rightArmsTexture((GameTexture) null).chestplate((InventoryItem) null).boots((InventoryItem) null)
				.holdItem((InventoryItem) null);
		
		look.modifyHumanDrawOptions(humanDrawOptions, null);
		if (additionalModifiers != null) {
			additionalModifiers.accept(humanDrawOptions);
		}

		return humanDrawOptions.pos(drawX + offsetX, drawY + offsetY);
	}

}