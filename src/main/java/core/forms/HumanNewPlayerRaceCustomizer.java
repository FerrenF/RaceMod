package core.forms;

import java.awt.Color;
import java.awt.Point;
import java.util.ArrayList;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import core.race.CustomHumanLook;
import core.race.RaceLook;
import core.race.TestFurryRaceLook;
import core.race.factory.RaceDataFactory;
import core.race.parts.BodyPart;
import helpers.DebugHelper;
import helpers.DebugHelper.MESSAGE_TYPE;
import necesse.engine.Settings;
import necesse.engine.localization.message.LocalMessage;
import necesse.engine.network.NetworkClient;
import necesse.entity.mobs.PlayerMob;
import necesse.gfx.GameHair;
import necesse.gfx.HumanLook;
import necesse.gfx.drawOptions.DrawOptions;
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
		if (!this.getRaceLook().getRaceID().equals(CustomHumanLook.HUMAN_RACE_ID)) {
			DebugHelper.handleFormattedDebugMessage("Problem converting base RaceLook class with race %s at form %s", 5, MESSAGE_TYPE.ERROR, new Object[] {this.getRaceID(), this.getClass().getName()});
			return new CustomHumanLook(true);
		}
		return (CustomHumanLook)this.getRaceLook();
	}
	
	@Override
	public RaceLook getRaceLook() {
	    return RaceDataFactory.getRaceLook(this.getPlayerHelper(), new CustomHumanLook(true));
	}
	
	@Override
	public void setLook(HumanLook look) {
		this.setRaceLook(RaceLook.fromHumanLook(look, CustomHumanLook.class));
		this.updateComponents();
	}
	
	@Override
	protected RaceLook racelookFromBase(HumanLook look) {
		// TODO Auto-generated method stub
		return new CustomHumanLook(look);
	}

	public void initializeIcon(int x, int iconY, int width) {
		DebugHelper.handleFormattedDebugMessage("Player Icon form initialized for %s with race %s", 50, MESSAGE_TYPE.DEBUG, new Object[] {this.getPlayerHelper().playerName, this.getRaceID()});
	    FormPlayerIcon formPlayerIcon = new FormPlayerIcon(x, iconY, 128, 128, this.getPlayerHelper()) {
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
	   return super.baseGetDrawOffset(part);
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
		Object value = super.baseGetCurrentBodyPartSelection(part, colorCustomization);		   
	    return value;
	}


	@Override
	protected void setLookAttribute(RaceLook look, BodyPart part, Object value, boolean colorCustomization) {
	    if (value instanceof Integer) {
	        int intValue = (Integer) value;
	        super.baseSetLookBodyPartValue(look, part, intValue, colorCustomization);	      
	    } else if (value instanceof Color) {
	        Color colorValue = (Color) value;
	        switch (part.getPartName()) {	            
	            case "BASE_SHIRT": look.setShirtColor(colorValue); break;
	            case "BASE_SHOES": look.setShoesColor(colorValue); break;
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
	protected void drawBodyPartPreview(FormContentVarToggleButton button, BodyPart part, boolean colorCustomization, int id, int x, int y, int _width, int _height) {
		RaceLook look = new CustomHumanLook(this.getRaceLook());
		applyLookModifiers(look, part);
		setLookAttribute(look, part, id, colorCustomization);
		HumanDrawOptions options = new HumanDrawOptions(null, look, false);		
		super.baseDrawBodyPartPreview(button, look, part, colorCustomization, options, id, x, y, _width, _height);
	}

	protected void applyLookModifiers(RaceLook look, BodyPart part) {
		if (part.getPartName() == "BASE_SKIN" || part.getPartName() == "BASE_EYE") {
		    look.setHair(0);
		    look.setFacialFeature(0);
		}
	}

	protected Section createColorCustomSection(BodyPart part, String labelKey, Supplier<Color> colorGetter, 
		Consumer<Color> colorSetFunc, Function<Color, ArrayList<InventoryItem>> costFunc, Predicate<Section> isCurrent, int _width) {
		return super.createColorCustomSection(part, labelKey, colorGetter, colorSetFunc, costFunc, isCurrent, _width);
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


}