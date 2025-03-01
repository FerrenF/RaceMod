package extensions;

import java.awt.Color;
import java.awt.Point;
import java.util.ArrayList;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import core.RaceMod;
import core.race.CustomHumanLook;
import core.race.parts.BodyPart;
import necesse.engine.Settings;
import necesse.engine.localization.message.LocalMessage;
import necesse.engine.network.NetworkClient;
import necesse.entity.mobs.PlayerMob;
import necesse.gfx.GameHair;
import necesse.gfx.HumanLook;
import necesse.gfx.drawOptions.DrawOptions;
import overrides.CustomPlayerMob;
import necesse.gfx.drawOptions.human.HumanDrawOptions;
import necesse.gfx.forms.components.FormContentVarToggleButton;
import necesse.gfx.forms.components.FormPlayerIcon;
import necesse.gfx.gameTexture.GameSprite;
import necesse.gfx.gameTexture.GameTexture;
import necesse.gfx.gameTexture.GameTexture.BlendQuality;
import necesse.inventory.InventoryItem;
import necesse.inventory.item.armorItem.cosmetics.misc.ShirtArmorItem;
import necesse.inventory.item.armorItem.cosmetics.misc.ShoesArmorItem;
import necesse.level.maps.light.GameLight;

public class HumanNewPlayerRaceCustomizer extends FormNewPlayerRaceCustomizer {
			
	public HumanNewPlayerRaceCustomizer(int x, int y, int width, boolean allowSupernaturalChanges, boolean allowClothesChance) {
		super(new CustomHumanLook(true), x, y, width, allowClothesChance, allowClothesChance);		
	}	
	
	@SuppressWarnings("unchecked")
	public CustomHumanLook getCustomRaceLook() {	
		if (this.getRaceID() != CustomHumanLook.HUMAN_RACE_ID) {
			RaceMod.handleDebugMessage("Problem converting base RaceLook class to super "+this.getClass().getName(), 25);
			return null;
		}
		return (CustomHumanLook)this.getRaceLook();
	}
	
	@Override
	protected RaceLook racelookFromBase(HumanLook look) {
		// TODO Auto-generated method stub
		return new CustomHumanLook(look);
	}

	public void initializeIcon(int x, int iconY, int width) {
		// Create the FormPlayerIcon and cast it to FormComponent if necessary
	    FormPlayerIcon formPlayerIcon = new FormPlayerIcon(x, iconY, 128, 128, this.newPlayer) {
	        @Override
	        public void modifyHumanDrawOptions(HumanDrawOptions drawOptions) {
	            super.modifyHumanDrawOptions(drawOptions);
	            HumanNewPlayerRaceCustomizer.this.modifyHumanDrawOptions(drawOptions);
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
	
	public Point getDrawOffset(BodyPart part) {
	    switch (part.getPartName()) {
	        case "SKIN_COLOR": return getSkinFaceDrawOffset();
	        case "EYE_TYPE":	return getEyeTypeFaceDrawOffset();
	        case "EYE_COLOR":  return getEyeColorFaceDrawOffset();
	        case "HAIR_STYLE": 
	        case "FACIAL_HAIR":
	        case "HAIR_COLOR": return new Point(0, 0);
	        default: return new Point(0, 0);
	    }
	}

	// Returns the modification cost (stub for customization)
	protected ArrayList<InventoryItem> getPartModificationCost(Color color) {
		return null; // Customize as needed
	}

	// Updates the player look and triggers necessary updates
	protected void updateBodyPartSelection(BodyPart part, Object id) {
		this.setLookAttribute(this.getRaceLook(), part, id);		
		this.onChanged();		
	}

	
	//TODO: hey, this can be de-duplicated by moving part of it to the base new player preset form, and overridden only if needed.
	@Override
	protected Object getCurrentBodyPartSelection(BodyPart part) {
		int DEBUG_VALUE = 80;
	    Object value;  // Store the selected value for logging

	    switch (part.getPartName()) {
	    	// Base Game Parts
	        case "SKIN_COLOR": 	            value = this.getRaceLook().getSkin();	            	break;
	        case "EYE_TYPE": 	            value = this.getRaceLook().getEyeType();	            break;
	        case "EYE_COLOR": 	            value = this.getRaceLook().getEyeColor();	            break;
	        case "HAIR_STYLE": 	            value = this.getRaceLook().getHair();	            	break;
	        case "FACIAL_HAIR": 	        value = this.getRaceLook().getFacialFeature();	        break;
	        case "HAIR_COLOR": 	            value = this.getRaceLook().getHairColor();	            break;
	        case "SHIRT_COLOR": 	        value = this.getRaceLook().getShirtColor();	            break;
	        case "SHOES_COLOR": 	        value = this.getRaceLook().getShoesColor();	            break;
	      
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
	    if (value instanceof Integer) {
	        int intValue = (Integer) value;
	        switch (part.getPartName()) {
	            case "SKIN_COLOR": look.setSkin(intValue); break;
	            case "EYE_TYPE": look.setEyeType(intValue); break;
	            case "HAIR_STYLE": look.setHair(intValue); break;
	            case "FACIAL_HAIR": look.setFacialFeature(intValue); break;
	            case "EYE_COLOR": look.setEyeColor(intValue); break;
	            case "HAIR_COLOR": look.setHairColor(intValue); break;
	        }
	    } else if (value instanceof Color) {
	        Color colorValue = (Color) value;
	        switch (part.getPartName()) {	            
	            case "SHIRT_COLOR": look.setShirtColor(colorValue); break;
	            case "SHOES_COLOR": look.setShoesColor(colorValue); break;
	        }
	    } else {
	        throw new IllegalArgumentException("Unsupported type for body part: " + part.getPartName());
	    }
	    this.updateLook();
		this.updateComponents();
	}

	// Draws the icon for each body part section
	protected void drawBodyPartIcon(FormContentVarToggleButton button, BodyPart part, int x, int y, int _width, int _height) {
		RaceLook look = new CustomHumanLook(this.getRaceLook());
		applyLookModifiers(look, part);
		HumanDrawOptions options = new HumanDrawOptions(null, look, false);
		Point offset = getDrawOffset(part);
		super.baseDrawBodyPartIcon(button, look, part, options, x, y, _width, _height, offset);		
	}
	
	
	// Draws the preview for each body part selection
	protected void drawBodyPartPreview(FormContentVarToggleButton button, BodyPart part, int id, int x, int y, int _width, int _height) {
		RaceLook look = new CustomHumanLook(this.getRaceLook());
		applyLookModifiers(look, part);
		setLookAttribute(look, part, id);
		HumanDrawOptions options = new HumanDrawOptions(null, look, false);
		// Center position for the preview
		
		
		super.baseDrawBodyPartPreview(button, look, part, options, id, x, y, _width, _height);
	}

	protected void applyLookModifiers(RaceLook look, BodyPart part) {
		if (part.getPartName() == "SKIN_COLOR" || part.getPartName() == "EYE_TYPE" || part.getPartName() == "EYE_COLOR") {
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

	public Point getSkinFaceDrawOffset() 		{	return new Point(-3, -4);	}
	
	public Point getEyeTypeFaceDrawOffset() 	{	return new Point(-22, -26);	}
	
	public Point getEyeColorFaceDrawOffset() 	{	return new Point(-22, -26);	}

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

	public void modifyHumanDrawOptions(HumanDrawOptions drawOptions) {	}

	public ArrayList<InventoryItem> getSkinColorCost(int id) 		{		return null;	}
	
	public ArrayList<InventoryItem> getEyeTypeCost(int id) 			{		return null;	}
	
	public ArrayList<InventoryItem> getEyeColorCost(int id) 		{		return null;	}
	
	public ArrayList<InventoryItem> getHairStyleCost(int id) 		{		return null;	}
	
	public ArrayList<InventoryItem> getFacialFeatureCost(int id) 	{		return null;	}
	
	public ArrayList<InventoryItem> getHairColorCost(int id) 		{		return null;	}
	
	public ArrayList<InventoryItem> getShirtColorCost(Color color) 	{		return null;	}
	
	public ArrayList<InventoryItem> getShoesColorCost(Color color) 	{		return null;	}

	


	

}