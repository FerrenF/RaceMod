package core.forms;

import java.awt.Color;
import java.awt.Point;
import java.util.ArrayList;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import core.gfx.GameParts;
import core.race.OrcRaceLook;
import core.race.RaceLook;
import core.race.factory.RaceDataFactory;
import core.race.parts.BodyPart;
import extensions.CustomHumanDrawOptions;
import helpers.DebugHelper;
import helpers.DebugHelper.MESSAGE_TYPE;
import necesse.engine.localization.message.LocalMessage;
import necesse.engine.network.NetworkClient;
import necesse.entity.mobs.PlayerMob;
import necesse.gfx.HumanLook;
import necesse.gfx.drawOptions.DrawOptions;
import necesse.gfx.drawOptions.human.HumanDrawOptions;
import necesse.gfx.forms.components.FormContentVarToggleButton;
import necesse.gfx.forms.components.FormPlayerIcon;
import necesse.gfx.gameTexture.GameTexture;
import necesse.gfx.gameTexture.GameTexture.BlendQuality;
import necesse.inventory.InventoryItem;

public class OrcNewPlayerRaceCustomizer extends FormNewPlayerRaceCustomizer {
			
	public OrcNewPlayerRaceCustomizer(int x, int y, int width, boolean allowSupernaturalChanges, boolean allowClothesChance) {
		super(new OrcRaceLook(true), x, y, width, allowClothesChance, allowClothesChance);		
	}	
	
	@SuppressWarnings("unchecked")
	public OrcRaceLook getCustomRaceLook() {	
		if (!this.getRaceLook().getRaceID().equals(OrcRaceLook.ORC_RACE_ID)) {
			DebugHelper.handleFormattedDebugMessage("Problem converting base RaceLook class with race %s at form %s", 5, MESSAGE_TYPE.ERROR, new Object[] {this.getRaceID(), this.getClass().getName()});
			return new OrcRaceLook(true);
		}
		return (OrcRaceLook)this.getRaceLook();
	}
	
	@Override
	public RaceLook getRaceLook() {
	    return RaceDataFactory.getRaceLook(this.getPlayerHelper(), new OrcRaceLook(true));
	}
	
	@Override
	public void setLook(HumanLook look) {
		this.setRaceLook(RaceLook.fromHumanLook(look, OrcRaceLook.class));
		this.updateComponents();
	}
	
	@Override
	protected RaceLook racelookFromBase(HumanLook look) {
		return new OrcRaceLook(look);
	}

	public void initializeIcon(int x, int iconY, int width) {

		DebugHelper.handleFormattedDebugMessage("Player Icon form initialized for %s with race %s", 50, MESSAGE_TYPE.DEBUG, new Object[] {this.getPlayerHelper().playerName, this.getRaceID()});
	    FormPlayerIcon formPlayerIcon = new FormPlayerIcon(x, iconY, 128, 128, this.getPlayerHelper()) {
	        @Override
	        public void modifyHumanDrawOptions(HumanDrawOptions drawOptions) {
	            super.modifyHumanDrawOptions(drawOptions);
	            OrcNewPlayerRaceCustomizer.this.modifyHumanDrawOptions(this, (CustomHumanDrawOptions) drawOptions);
	        }
	    };

	    
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
	        case "CUSTOM_HAIR": 	return new Point(0, 0);	 
	        case "CUSTOM_HAIR_COLOR": 	return new Point(0, 0);	  
	        case "FACEHAIR": 	return new Point(0, -20);	 
	        case "FACEHAIR_COLOR": 	return new Point(0, -20);	  
	        case "FACIALFEATURES": 	return new Point(0, -10);	 
	        case "FACIALFEATURES_COLOR": 	return new Point(0, -10);	  
	        case "HEAD": 		return getHeadTypeDrawOffset();
	        case "CUSTOM_EYES": return getEyeTypeFaceDrawOffset();
	        case "CUSTOM_EYES_COLOR": return getEyeColorFaceDrawOffset();
	        default: 			return super.baseGetDrawOffset(part);
	    }
	}
	
	private Point getHeadTypeDrawOffset() 		{	return new Point(0, 0);		}

	public Point getSkinFaceDrawOffset() 		{	return new Point(-3, -4);	}
	
	public Point getEyeTypeFaceDrawOffset() 	{	return new Point(0, 5);	}
	
	public Point getEyeColorFaceDrawOffset() 	{	return new Point(0, 5);	}
		
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

	    OrcRaceLook ccr = this.getCustomRaceLook();
	    String targetPartName = colorCustomization ? part.getPartColorName() : part.getPartName();
	    switch (targetPartName) {	    
	        // Custom Race Parts	      
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
	        case "CUSTOM_HAIR": 	        		value = ccr.getCustomHairStyle();	    	break;
	        case "CUSTOM_HAIR_COLOR": 	        	value = ccr.getCustomHairColor();	    	break;	  
	        case "FACEHAIR": 	        			value = ccr.getFaceHairStyle();	    	break;
	        case "FACEHAIR_COLOR": 	        		value = ccr.getFaceHairColor();	    	break;
	        case "FACIALFEATURES": 	        		value = ccr.getFacialFeaturesStyle();	    	break;
	        case "FACIALFEATURES_COLOR": 	        value = ccr.getFacialFeaturesColor();	    	break;
	        
	        default:        value = super.baseGetCurrentBodyPartSelection(part, colorCustomization);	break;
	    }
	
	    return value;
	}


	@Override
	protected void setLookAttribute(RaceLook look, BodyPart part, Object value, boolean colorCustomization) {
		
		OrcRaceLook ccr = (OrcRaceLook)look;
	    if (value instanceof Integer) {
	        int intValue = (Integer) value;
	        switch (colorCustomization ? part.getPartColorName() : part.getPartName()) {        
	         	        
		        case "HEAD": 	    	ccr.setHeadStyle(intValue);	           	break;		 	       
		        case "BODY": 	    	ccr.setBodyStyle(intValue);	           	break;		  
		        case "ARMS": 	    	ccr.setArmsStyle(intValue);	           	break;		          
		        case "FEET": 	    	ccr.setFeetStyle(intValue);	           	break;
		        
		        case "BODY_COLOR": 	    ccr.setBodyColor(intValue);          	break;	
		        
		        case "CUSTOM_EYES": 	    	ccr.setCustomEyesStyle(intValue);	           	break;		  
		        case "CUSTOM_EYES_COLOR": 	    ccr.setCustomEyesColor(intValue);          	break;	
		        
		        case "CUSTOM_HAIR": 	        ccr.setCustomHairStyle(intValue);	    	break;
		        case "CUSTOM_HAIR_COLOR": 	    ccr.setCustomHairColor(intValue);	    	break;	  
		        
		        case "FACEHAIR": 	        	ccr.setFaceHairStyle(intValue);	   	break;
		        case "FACEHAIR_COLOR": 	        ccr.setFaceHairColor(intValue);   	break;
		        case "FACIALFEATURES": 	        		ccr.setFacialFeaturesStyle(intValue);	    	break;
		        
		        default: super.baseSetLookBodyPartValue(look, part, intValue, colorCustomization);	break;
	        }
	    } else if (value instanceof Color) {
	    	
	        Color colorValue = (Color) value;
	        switch (part.getPartName()) {	            
	            case "BASE_SHIRT": ccr.setShirtColor(colorValue); break;
	            case "BASE_SHOES": ccr.setShoesColor(colorValue); break;
	        }
	        
	    } else {
	        throw new IllegalArgumentException("Unsupported type for body part: " + part.getPartName());
	    }
	    this.updateLook();
		this.updateComponents();
	}

	// Draws the icon for each body part section
	protected void drawBodyPartIcon(FormContentVarToggleButton button, BodyPart part, int x, int y, int _width, int _height) {
		OrcRaceLook look = new OrcRaceLook(this.getCustomRaceLook());
		applyLookModifiers(look, part);
		CustomHumanDrawOptions options = new CustomHumanDrawOptions(null, look, false);
		Point offset = getDrawOffset(part);
			
		if (!part.isBaseGamePart()) {
			super.customDrawBodyPartIcon(button, look, part, options, x, y, _width, _height, offset, ()->{
				return this.getOrcFaceDrawOptions(look, options, button.size.height, x + offset.x, y + offset.y, (opt) -> {
			        opt.sprite(0, 3).dir(3);  // Set specific sprite direction
			    });
			}, (bp)->{
				return (bp.getPartName().equals("CUSTOM_EYES") || bp.getPartName().equals("CUSTOM_EYES_COLOR")
						|| bp.getPartName().equals("FACEHAIR")|| bp.getPartName().equals("FACEHAIR_COLOR")
						|| bp.getPartName().equals("FACIALFEATURES")|| bp.getPartName().equals("FACIALFEATURES_COLOR"));
			});
		}  else {			
			super.baseDrawBodyPartIcon(button, look, part, options, x, y, _width, _height, offset);
		}	
	
	}	
	
	// Draws the preview for each body part selection
	protected void drawBodyPartPreview(FormContentVarToggleButton button, BodyPart part, boolean colorCustomization, int id, int x, int y, int _width, int _height) {
		
		OrcRaceLook look = new OrcRaceLook(this.getCustomRaceLook());
		applyLookModifiers(look, part);
		
		setLookAttribute(look, part, id, colorCustomization);
		setLookAttribute(look, part, this.getCurrentBodyPartSelection(part, !colorCustomization), !colorCustomization);

		CustomHumanDrawOptions options = new CustomHumanDrawOptions(null, look, false);
		
		Point offset = this.getDrawOffset(part);
		if (!part.isBaseGamePart()) {
			super.customDrawBodyPartIcon(button, look, part, options, x, y, _width, _height, offset, ()->{
				return this.getOrcFaceDrawOptions(look, options, button.size.height, x + offset.x, y + offset.y, (opt) -> {
			        opt.sprite(0, 3).dir(3);  // Set specific sprite direction
			    });
			}, (bp)->{
				return (bp.getPartName().equals("CUSTOM_EYES") || bp.getPartName().equals("CUSTOM_EYES_COLOR")
						|| bp.getPartName().equals("FACEHAIR")|| bp.getPartName().equals("FACEHAIR_COLOR")
						|| bp.getPartName().equals("FACIALFEATURES")|| bp.getPartName().equals("FACIALFEATURES_COLOR"));
			});
		}  else {			
			super.baseDrawBodyPartIcon(button, look, part, options, x, y, _width, _height, offset);
		}		
		
	}

	protected void applyLookModifiers(RaceLook look, BodyPart part) {
		if (part.getPartName() == "BASE_SKIN_COLOR" || part.getPartName() == "BASE_EYE" ||
				part.getPartName() == "BASE_EYE_COLOR" ||
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
			return super.createBodyPartSection(part, false, isCurrent, _width);
	}
	
	protected Section createBodyPartColorSection(BodyPart part, Predicate<Section> isCurrent, int _width) {

		 return new Section(					 
    		(button, drawX, drawY, width, height) -> drawBodyPartIcon(button, part, drawX, drawY, width, height),
    			new LocalMessage(part.getLabelCategory(), part.getLabelColorKey()),			    			
    			this.getSelectionContent(	core.forms.FormNewPlayerRaceCustomizer.BUTTON_SIZE, _width, part.numColors(),
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
	
	public DrawOptions getOrcFaceDrawOptions(RaceLook look, CustomHumanDrawOptions humanDrawOptions, int size, int drawX,
			int drawY) {
		return getOrcFaceDrawOptions(look, humanDrawOptions, size, drawX, drawY, (Consumer<CustomHumanDrawOptions>) null);
	}
	
	public DrawOptions getOrcFaceDrawOptions(RaceLook look, CustomHumanDrawOptions humanDrawOptions, int size, int drawX, int drawY,
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