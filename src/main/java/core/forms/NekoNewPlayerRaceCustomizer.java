package core.forms;

import java.awt.Color;
import java.awt.Point;
import java.util.ArrayList;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import core.race.NekoRaceLook;
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
import necesse.inventory.InventoryItem;

public class NekoNewPlayerRaceCustomizer extends FormNewPlayerRaceCustomizer {
			
	public NekoNewPlayerRaceCustomizer(int x, int y, int width, boolean allowSupernaturalChanges, boolean allowClothesChance) {
		super(new NekoRaceLook(true), x, y, width, allowClothesChance, allowClothesChance);		
	}	
	
	@SuppressWarnings("unchecked")
	public NekoRaceLook getCustomRaceLook() {	
		if (!this.getRaceLook().getRaceID().equals(NekoRaceLook.NEKO_RACE_ID)) {
			DebugHelper.handleFormattedDebugMessage("Problem converting base RaceLook class with race %s at form %s", 5, MESSAGE_TYPE.ERROR, new Object[] {this.getRaceID(), this.getClass().getName()});
			return new NekoRaceLook(true);
		}
		return (NekoRaceLook)this.getRaceLook();
	}
	
	@Override
	public RaceLook getRaceLook() {
	    return RaceDataFactory.getRaceLook(this.getPlayerHelper(), new NekoRaceLook(true));
	}
	
	@Override
	public void setLook(HumanLook look) {
		this.setRaceLook(RaceLook.fromHumanLook(look, NekoRaceLook.class));
		this.updateComponents();
	}
	
	@Override
	protected RaceLook racelookFromBase(HumanLook look) {
		return new NekoRaceLook(look);
	}

	public void initializeIcon(int x, int iconY, int width) {
		DebugHelper.handleFormattedDebugMessage("Player Icon form initialized for %s with race %s", 50, MESSAGE_TYPE.DEBUG, new Object[] {this.getPlayerHelper().playerName, this.getRaceID()});
	    FormPlayerIcon formPlayerIcon = new FormPlayerIcon(x, iconY, 128, 128, this.getPlayerHelper()) {
	        @Override
	        public void modifyHumanDrawOptions(HumanDrawOptions drawOptions) {
	            super.modifyHumanDrawOptions(drawOptions);
	            NekoNewPlayerRaceCustomizer.this.modifyHumanDrawOptions(drawOptions);
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
	
	@Override
	public Point getDrawOffset(BodyPart part) {
	    switch (part.getPartName()) {	     
	        case "TAIL": 		return new Point(0, 0);
	        case "EARS": 		return new Point(0, 8);	
	        default: 			return super.baseGetDrawOffset(part);
	    }
	}
	
	protected ArrayList<InventoryItem> getPartModificationCost(Color color) {
		return null; // Customize as needed
	}

	protected void updateBodyPartSelection(BodyPart part, Object id, boolean colorCustomization) {
		this.setLookAttribute(this.getRaceLook(), part, id, colorCustomization);		
		this.onChanged();			
	}

	@Override
	protected Object getCurrentBodyPartSelection(BodyPart part, boolean colorCustomization) {
	    Object value;  // Store the selected value for logging

	    NekoRaceLook ccr = this.getCustomRaceLook();
	    String targetPartName = colorCustomization ? part.getPartColorName() : part.getPartName();
	    switch (targetPartName) {	    
	        
	        case "TAIL": 	            	value = ccr.getTailStyle();	        break;
	        case "TAIL_COLOR": 	            value = ccr.getTailColor();	        break;
	        case "EARS": 	        		value = ccr.getEarsStyle();	        break;
	        case "EARS_COLOR": 	        	value = ccr.getEarsColor();	        break;	     
	        default:        value = super.baseGetCurrentBodyPartSelection(part, colorCustomization);	break;
	    }
	
	    return value;
	}


	@Override
	protected void setLookAttribute(RaceLook look, BodyPart part, Object value, boolean colorCustomization) {
		
		NekoRaceLook ccr = (NekoRaceLook)look;
	    if (value instanceof Integer) {
	        int intValue = (Integer) value;
	        switch (colorCustomization ? part.getPartColorName() : part.getPartName()) {           
	            
	            case "TAIL": 	    	ccr.setTailStyle(intValue);	            break;
		        case "TAIL_COLOR": 		ccr.setTailColor(intValue);	            break;
		        
		        case "EARS": 	    	ccr.setEarsStyle(intValue);	            break;
		        case "EARS_COLOR": 		ccr.setEarsColor(intValue);	            break;		      
		        
		        default: super.baseSetLookBodyPartValue(look, part, intValue, colorCustomization); break;
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
			NekoRaceLook look = new NekoRaceLook(this.getCustomRaceLook());
			applyLookModifiers(look, part);
			CustomHumanDrawOptions options = new CustomHumanDrawOptions(null, look, false);
			Point offset = getDrawOffset(part);

			
			if (part.getAccessoryTextureMapSize() != null) {
				super.customDrawBodyPartIcon(button, look, part, options, x, y, _width, _height, offset, ()->{
					return this.getNekoFaceDrawOptions(look, options, button.size.height*2, x, y, (opt) -> {
				        opt.sprite(0, 3).dir(3);  // Set specific sprite direction
				    }, offset);
				}, (bp)->{
					return (bp.getPartName().equals("BASE_EYE"));
				});
			}  else {			
				super.baseDrawBodyPartIcon(button, look, part, options, x, y, _width, _height, offset);
			}		
		
		}	
		
		// Draws the preview for each body part selection
		protected void drawBodyPartPreview(FormContentVarToggleButton button, BodyPart part, boolean colorCustomization, int id, int x, int y, int _width, int _height) {
			
			NekoRaceLook look = new NekoRaceLook(this.getRaceLook());
			applyLookModifiers(look, part);			
			setLookAttribute(look, part, id, colorCustomization);
			setLookAttribute(look, part, this.getCurrentBodyPartSelection(part, !colorCustomization), !colorCustomization);

			CustomHumanDrawOptions options = new CustomHumanDrawOptions(null, look, false);
			Point offset = this.getDrawOffset(part);
			

			if (part.getAccessoryTextureMapSize() != null) {
				super.customDrawBodyPartIcon(button, look, part, options, x, y, _width, _height, offset, ()->{
					return this.getNekoFaceDrawOptions(look, options, button.size.height*2, x, y, (opt) -> {
				        opt.sprite(0, 3).dir(3);  // Set specific sprite direction
				    }, offset);
				}, (bp)->{
					return (bp.getPartName().equals("CUSTOM_EYES"));
				});
			}  else {			
				super.baseDrawBodyPartIcon(button, look, part, options, x, y, _width, _height, offset);
			}		
		}

		protected void applyLookModifiers(RaceLook look, BodyPart part) {
			if (part.getPartName() == "BASE_SKIN" 	||	part.getPartName() == "BASE_EYE" ||
					part.getPartName() == "EARS" 	||		part.getPartName() == "CUSTOM_EYES") {
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
	    			this.getSelectionContent( core.forms.FormNewPlayerRaceCustomizer.BUTTON_SIZE, _width, part.numColors(),
			            (button, id, x, y, w, h, current, hovering) -> drawBodyPartPreview(button, part, true, id, x, y, w, h),
			            id -> id == (Integer)getCurrentBodyPartSelection(part, true),
			            (id, event) -> updateBodyPartSelection(part, id, true),
			            (color)->this.getPartModificationCost(new Color(color)),
			            false),
	    			
	        		isCurrent
	    		);
		}

	// Generates sections dynamically for each body part
	protected Section createBodyPartSection(BodyPart part, boolean isColorPart, Predicate<Section> isCurrent, int _width) {
		return super.createBodyPartSection(part, isColorPart, isCurrent, _width);
	}

	public Point getSkinFaceDrawOffset() 		{	return new Point(-3, -4);	}
	
	public Point getEyeTypeFaceDrawOffset() 	{	return new Point(-22, -26);	}
	
	public Point getEyeColorFaceDrawOffset() 	{	return new Point(-22, -26);	}


	public void randomize() {		
		this.getCustomRaceLook().randomizeLook();	
		this.updateComponents();
	}
		
	public void onChanged() {	
		super.onChanged();
	}

	public void modifyHumanDrawOptions(HumanDrawOptions drawOptions) {	}

	@Override
	public void reset() {
		this.setPlayerHelper(new PlayerMob(0L, (NetworkClient) null));
	}

	public DrawOptions getNekoFaceDrawOptions(RaceLook look, CustomHumanDrawOptions humanDrawOptions, int size, int drawX,
			int drawY, Point offset) {
		return getNekoFaceDrawOptions(look, humanDrawOptions, size, drawX, drawY, (Consumer<CustomHumanDrawOptions>) null, offset);
	}
	
	public DrawOptions getNekoFaceDrawOptions(RaceLook look, CustomHumanDrawOptions humanDrawOptions, int size, int drawX, int drawY,
			Consumer<CustomHumanDrawOptions> additionalModifiers, Point offset) {
		
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