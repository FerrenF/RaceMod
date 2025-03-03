package extensions;

import java.awt.Color;
import java.awt.Point;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.IntPredicate;
import java.util.function.Predicate;
import java.util.function.Supplier;

import core.RaceMod;
import core.race.CustomHumanLook;
import core.race.parts.BodyPart;
import core.race.parts.RaceLookParts;
import extensions.FormNewPlayerRaceCustomizer.Section;
import necesse.engine.Settings;
import necesse.engine.gameLoop.tickManager.TickManager;
import necesse.engine.localization.message.GameMessage;
import necesse.engine.localization.message.LocalMessage;
import necesse.engine.network.NetworkClient;
import necesse.engine.util.GameMath;
import necesse.engine.util.GameUtils;
import necesse.entity.mobs.MaskShaderOptions;
import necesse.entity.mobs.PlayerMob;
import necesse.gfx.GameBackground;
import necesse.gfx.GameEyes;
import necesse.gfx.GameHair;
import necesse.gfx.HumanLook;
import necesse.gfx.Renderer;
import necesse.gfx.drawOptions.DrawOptions;
import necesse.gfx.drawOptions.DrawOptionsList;
import necesse.gfx.drawOptions.human.HumanDrawOptions;
import necesse.gfx.fairType.FairColorChangeGlyph;
import necesse.gfx.fairType.FairGlyph;
import necesse.gfx.fairType.FairItemGlyph;
import necesse.gfx.fairType.FairSpacerGlyph;
import necesse.gfx.fairType.FairType;
import necesse.gfx.fairType.FairType.TextAlign;
import necesse.gfx.forms.Form;
import necesse.gfx.forms.FormSwitcher;
import necesse.gfx.forms.components.FormButton;
import necesse.gfx.forms.components.FormColorPicker;
import necesse.gfx.forms.components.FormContentVarToggleButton;
import necesse.gfx.forms.components.FormFlow;
import necesse.gfx.forms.components.FormInputSize;
import necesse.gfx.forms.components.FormPlayerIcon;
import necesse.gfx.forms.components.FormTextureButton;
import necesse.gfx.forms.components.localComponents.FormLocalLabel;
import necesse.gfx.forms.events.FormInputEvent;
import necesse.gfx.forms.floatMenu.ColorSelectorFloatMenu;
import necesse.gfx.gameFont.FontManager;
import necesse.gfx.gameFont.FontOptions;
import necesse.gfx.gameTexture.GameSprite;
import necesse.gfx.gameTexture.GameTexture;
import necesse.gfx.gameTexture.GameTexture.BlendQuality;
import necesse.gfx.gameTooltips.FairTypeTooltip;
import necesse.gfx.gameTooltips.GameTooltipManager;
import necesse.gfx.gameTooltips.ListGameTooltips;
import necesse.gfx.gameTooltips.StringTooltips;
import necesse.gfx.gameTooltips.TooltipLocation;
import necesse.gfx.ui.ButtonColor;
import necesse.gfx.ui.ButtonIcon;
import necesse.inventory.InventoryItem;
import necesse.inventory.item.armorItem.cosmetics.misc.ShirtArmorItem;
import necesse.inventory.item.armorItem.cosmetics.misc.ShoesArmorItem;
import necesse.level.maps.light.GameLight;
import overrides.CustomPlayerMob;
import overrides.FormNewPlayerPreset;

public abstract class FormNewPlayerRaceCustomizer extends Form {
			
	static int PREVIEW_TEXTURE_X_POSITION = 0;
	static FormInputSize BUTTON_SIZE = FormInputSize.SIZE_32;
	protected CustomPlayerMob newPlayer;
	protected boolean allowSupernaturalChanges;
	protected boolean allowClothesChance;
	public FormPlayerIcon icon;
	
	public abstract <T extends RaceLook> T getCustomRaceLook();
	// Getter for RaceLook (using reflection)
	public RaceLook getRaceLook() {	
		
	    if ("CUSTOM".equals(this.newPlayer.secondType)) {  // Safe string comparison
	        if (this.newPlayer.look instanceof RaceLook) {
	            return (RaceLook) this.newPlayer.look;
	        } else {
	        	
	        	RaceMod.handleDebugMessage( String.format("Error: Failed %s is not instance of %s. Encountered in %s!", 
		    			this.newPlayer.look.getClass().getName(), RaceLook.class.getName(), this.getClass().getName()), 20  );
	        	
	            System.err.println("Error: newPlayer.look is not an instance of RaceLook!");
	            return null;  // Handle invalid state gracefully
	        }
	    }
	    
	    RaceLook converted = this.racelookFromBase(this.newPlayer.look);
	    if (converted == null) {	    	
	    	RaceMod.handleDebugMessage( String.format("Error: Failed to convert class %s into %s in %s!", 
	    			this.newPlayer.look.getClass().getName(), RaceLook.class.getName(), this.getClass().getName()), 20  );
	    }
	    return converted;
	}


	protected abstract RaceLook racelookFromBase(HumanLook look);


	public RaceLookParts getRaceLookParts() {
	    RaceLook look = this.getRaceLook();
	    if (look == null) {
	    	RaceMod.handleDebugMessage( String.format("Error: getRaceLook returned null in %s!", this.getClass().getName()), 20  );	      
	        return null;
	    }
	    if (look.partsList == null) {
	    	RaceMod.handleDebugMessage( String.format("Error: getRaceLook returned a race %s with null parts in %s!",
	    			look.getClass().getName(), this.getClass().getName()), 20  );	    
	        System.err.println("Error: RaceLook partsList is null!");
	    }
	    return look.partsList;
	}
    // Setter for RaceLook (using reflection)
    public void setRaceLook(RaceLook raceLook) {
    	this.newPlayer.look = raceLook;
    	// will need update methods
    }
    
    public String getRaceID() {
    	return this.getRaceLook().getRaceID();
    }

	public FormNewPlayerRaceCustomizer(RaceLook raceLook, int x, int y, int width, boolean allowSupernaturalChanges, boolean allowClothesChance) {		
		super(width, 0);
		this.newPlayer = new CustomPlayerMob(0L, (NetworkClient) null);
		this.setRaceLook(raceLook);
		this.setPosition(x, y);
		
		this.drawBase = false;
		this.allowSupernaturalChanges = allowSupernaturalChanges;
		this.allowClothesChance = allowClothesChance;		
		

		FormFlow flow = new FormFlow(5);		
		this.addComponent((FormLocalLabel) flow.nextY(
				new FormLocalLabel("ui", "clickplayerrotate", new FontOptions(12), 0, x + (width / 2) - FormNewPlayerPreset.RACE_SWITCH_FORM_WIDTH, 0, width - 20)));
		
		int iconY = flow.next(128);		
		
		int iconX = (width / 2 - 64);
		this.initializeIcon(iconX, iconY, width);		
		
		int buttonX = (width / 2) - FormNewPlayerPreset.RACE_SWITCH_FORM_WIDTH;
		this.setupRotationButtons(buttonX, width, iconY);
		
		this.initializeFormSwitcher(width, flow);
	}
	
	protected void initializeFormSwitcher(int width, FormFlow flow) {
	    FormSwitcher contentSwitcher = (FormSwitcher) this.addComponent(new FormSwitcher());

	    contentSwitcher.useInactiveHitBoxes = true;
	    ArrayList<Section> sections = this.getSections((s) -> s.selectionContent != null && contentSwitcher.isCurrent(s.selectionContent), width);

	    int buttonPadding = 1;
	    int totalButtonWidth = BUTTON_SIZE.height + buttonPadding * 2;
	    int buttonsPerRow = GameMath.limit(width / totalButtonWidth, 1, sections.size());
	    int totalRows = (int) Math.ceil((double) sections.size() / (double) buttonsPerRow);
	    int startX = 0;
	    int startY = flow.next();

	    int maxSectionHeight = 0;

	    for (maxSectionHeight = 0; maxSectionHeight < sections.size(); ++maxSectionHeight) {
	        Section section = sections.get(maxSectionHeight);
	        int column = maxSectionHeight % buttonsPerRow;
	        int row = maxSectionHeight / buttonsPerRow;
	        int buttonsThisRow = Math.min(sections.size() - buttonsPerRow * row, buttonsPerRow);
	        int xOffset = width / 2 - buttonsThisRow * totalButtonWidth / 2 - buttonPadding;
	        int buttonX = startX + xOffset + column * (BUTTON_SIZE.height + buttonPadding * 2) + buttonPadding;
	        int buttonY = startY + row * (BUTTON_SIZE.height + buttonPadding * 2) + buttonPadding;
	        this.addComponent(section.button);
	        
	        if (section.selectionContent != null) {
	            contentSwitcher.addComponent(section.selectionContent);
	        }

	        section.button.setPosition(buttonX, buttonY);
	        section.button.onClicked((e) -> {
	            section.onClicked(contentSwitcher);
	        });
	    }

	    flow.next(totalRows * (BUTTON_SIZE.height + buttonPadding * 2) + 5);

	    maxSectionHeight = 0;
	    Form first = null;
	    Iterator<Section> var27 = sections.iterator();

	    while (var27.hasNext()) {
	        Section section = var27.next();
	        if (section.selectionContent != null) {
	            if (first == null) {
	                first = section.selectionContent;
	            }

	            section.selectionContent.setPosition(0, flow.next());
	            maxSectionHeight = Math.max(maxSectionHeight, section.selectionContent.getHeight());
	        }
	    }

	    if (first != null) {
	        contentSwitcher.makeCurrent(first);
	    }

	    flow.next(maxSectionHeight);
	    flow.next(10);
	    this.setHeight(flow.next());
	    this.reset();
	}
	
	protected abstract void initializeIcon(int x, int iconY, int width);
	
	private FormTextureButton createRotateButton(int x, int y, boolean isLeft) {
	    // Return a new FormTextureButton for rotating
	    return new FormTextureButton(x, y, () -> {
	        return isLeft ? new GameSprite(Settings.UI.rotate_arrow).mirrorX().mirrorY() : new GameSprite(Settings.UI.rotate_arrow);
	    }, -1, 128, TextAlign.RIGHT, TextAlign.CENTER) {
	        public Color getDrawColor() {
	            return (Color) this.getButtonState().textColorGetter.apply(Settings.UI);
	        }
	    };
	}
	
	public void setupRotationButtons(int x, int width, int iconY) {
	    // Create the left rotate button
	    FormTextureButton leftRotateButton = createRotateButton(x - 64 + 15, iconY + 64 + 20, true);

	    // Create the right rotate button
	    FormTextureButton rightRotateButton = createRotateButton(x + 64 - 15, iconY + 64 + 20, false);

	    // Additional button setup as needed
	    leftRotateButton.acceptRightClicks = true;
	    leftRotateButton.onClicked((e) -> {
	        if (e.event.getID() == -99) {
	            this.icon.setRotation(this.icon.getRotation() + 1);
	        } else {
	            this.icon.setRotation(this.icon.getRotation() - 1);
	        }
	    });

	    rightRotateButton.acceptRightClicks = true;
	    rightRotateButton.onClicked((e) -> {
	        if (e.event.getID() == -99) {
	            this.icon.setRotation(this.icon.getRotation() + 1);
	        } else {
	            this.icon.setRotation(this.icon.getRotation() - 1);
	        }
	    });
	}
	
	protected abstract Point getDrawOffset(BodyPart part);


	// Returns the modification cost (stub for customization)
	protected abstract ArrayList<InventoryItem> getPartModificationCost(Color color);

	// Updates the player look and triggers necessary updates
	protected abstract void updateBodyPartSelection(BodyPart part, Object id, boolean colorCustomization);

	// Returns the current selection for a given body part
	protected abstract Object getCurrentBodyPartSelection(BodyPart part, boolean colorCustomization);
	
	// Updates the look with the selected value
	protected abstract void setLookAttribute(RaceLook look, BodyPart part, Object id, boolean colorCustomization);

	// Draws the icon for each body part section
	protected abstract void drawBodyPartIcon(FormContentVarToggleButton button, BodyPart part, int x, int y, int _width, int _height);

	// Draws the preview for each body part selection
	protected abstract void drawBodyPartPreview(FormContentVarToggleButton button, BodyPart part, boolean colorCustomization, int id, int x, int y, int _width, int _height);

	protected abstract void applyLookModifiers(RaceLook look, BodyPart part);

	//protected abstract Section createColorCustomSection(final BodyPart part, String labelKey, Supplier<Color> currentColor, 
	//	Consumer<Color> colorSetFunc, Function<Color, ArrayList<InventoryItem>> costFunc, Predicate<Section> isCurrent, int _width);
	
	
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
			    defaultColors,
			    ()->{return colorGetter.get();}, 
			    (color) -> {		    	
			    	this.setLookAttribute(this.getRaceLook(), part, color, false);
			       // this.updateLook();
			    },
			    (color) -> {
			    	this.updateBodyPartSelection(part, color, false);              
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
		            ()->(Color)(this.getCurrentBodyPartSelection(part, false)),            
		            (color) -> {updateBodyPartSelection(part, (Color)color, false);},		            
		            (color)->this.getPartModificationCost(color),
		            isCurrent,
		            _width
		        );
		}
		else {
			
		 return new Section(					 
    		(button, drawX, drawY, width, height) -> drawBodyPartIcon(button, part, drawX, drawY, width, height),
    			new LocalMessage(part.getLabelCategory(), part.getLabelKey()),			    			
    			this.getSelectionContent(	extensions.FormNewPlayerRaceCustomizer.BUTTON_SIZE, _width, part.getTotalTextureOptions(),
            (button, id, x, y, w, h, current, hovering) -> drawBodyPartPreview(button, part, false, id, x, y, w, h),
            id -> id == (Integer)getCurrentBodyPartSelection(part, false),
            (id, event) -> updateBodyPartSelection(part, id, false),
            (color)->this.getPartModificationCost(new Color(color))	),
        		isCurrent
    		);
		}	 
	}

	protected ArrayList<Section> getSections(Predicate<Section> isCurrent, int width) {
		ArrayList<Section> sections = new ArrayList<Section>();		
		sections.add(new Section(new DrawButtonFunction() {
			
			public void draw(FormContentVarToggleButton button, int drawX, int drawY, int width, int height) {
				ButtonIcon icon = Settings.UI.inventory_sort;
				Color color = (Color) icon.colorGetter.apply(button.getButtonState());
				icon.texture.initDraw().color(color).posMiddle(drawX + width / 2, drawY + height / 2).draw();
			}
			
		}, new LocalMessage("ui", "randomappearance"), (Form) null, isCurrent) {
			
			public void onClicked(FormSwitcher switcher) {
				FormNewPlayerRaceCustomizer.this.randomize();
				FormNewPlayerRaceCustomizer.this.onChanged();
			}
			
		});
		
		RaceLookParts parts = this.getRaceLookParts();
	    if (parts == null) {
	        System.err.println("Error: getRaceLookParts() returned null! Skipping body parts.");
	        return sections;
	    }

	    RaceMod.handleDebugMessage("Parts Initializing:",50);
	    this.getRaceLookParts().getBodyParts().forEach((part)->RaceMod.handleDebugMessage(part.getPartName()+",",50));	
	    for (BodyPart part : parts.getBodyParts()) {
	        sections.add(createBodyPartSection(part, isCurrent, width));
	        if(part.isHasColor() && !part.isBaseGamePart()) {
	        	 sections.add(createBodyPartCustomColorSection(part, isCurrent, width));
	        }
	    }
		return sections;
	}
	protected abstract Section createBodyPartCustomColorSection(BodyPart part, Predicate<Section> isCurrent, int _width);
	public void baseDrawBodyPartIcon(FormContentVarToggleButton button, RaceLook look, BodyPart part, HumanDrawOptions options, int x, int y, int _width, int _height, Point offset){
		int drawX = x + _width / 2;
		int drawY = y + _height / 2;
	
		if (part.getPartName() == "HAIR_STYLE") {
			
			 // Handle Hair style drawing
		    int hairStyleIndex = look.getHair();
		    GameTexture wigTexture = GameHair.getHair(hairStyleIndex).getWigTexture(look.getHairColor());
		    wigTexture.initDraw()
		              .size(button.size.height)
		              .posMiddle(drawX, drawY)
		              .draw();
		    
		} else if (part.getPartName() == "FACIAL_HAIR") {
			
			// Handle Facial Hair drawing
		    int facialHairIndex = look.getFacialFeature();
		    GameTexture wigTexture = GameHair.getFacialFeature(facialHairIndex).getWigTexture(look.getHairColor());
		    wigTexture.initDraw()
		              .size(button.size.height)
		              .posMiddle(drawX, drawY)
		              .draw();
		    
		} else if (part.getPartName() == "EYE_COLOR" || part.getPartName() == "EYE_TYPE"){	    
			
			GameTexture.overrideBlendQuality = BlendQuality.NEAREST;
			 getHumanFaceDrawOptions(options, button.size.height * 2, x+offset.x, y+offset.y, (opt) -> {
					opt.sprite(0, 3).dir(3);
				}).draw();
			 GameTexture.overrideBlendQuality = null;
			 
		} else {
			
			GameTexture.overrideBlendQuality = BlendQuality.NEAREST;
		    getHumanFaceDrawOptions(options, button.size.height, x+offset.x, y+offset.y).draw();
		    GameTexture.overrideBlendQuality = null;
		    
		}
	}
	public void baseDrawBodyPartPreview(FormContentVarToggleButton button, RaceLook look, BodyPart part, HumanDrawOptions options, int id, int x, int y, int _width, int _height)
	{
		// Center position for the preview
			int drawX = x + _width / 2;
			int drawY = y + _height / 2;
		// handles base game body part cases
			if (part.getPartName() == "HAIR_STYLE") {
				
			    // Hair Style preview
			    GameTexture wigTexture = GameHair.getHair(id).getWigTexture(look.getHairColor());
			    wigTexture.initDraw().size(_height).posMiddle(drawX, drawY).draw();
			    
			} else if (part.getPartName() == "FACIAL_HAIR") {
				
			    // Facial Hair preview
			    GameTexture wigTexture = GameHair.getFacialFeature(id).getWigTexture(look.getHairColor());
			    wigTexture.initDraw().size(_height).posMiddle(drawX, drawY).draw();
			    
			} else if (part.getPartName() == "HAIR_COLOR") {
				
			    // Hair Color preview (if bald, show paintbrush)
			    if (look.getHair() == 0) {
			    	
			        Color color = (Color) GameHair.colors.getSkinColor(id).colors.get(3);
			        Settings.UI.paintbrush_grayscale.initDraw().color(color).posMiddle(drawX, drawY).draw();
			        Settings.UI.paintbrush_handle.initDraw().posMiddle(drawX, drawY).draw();
			        
			    } else {
			    	
			        // Otherwise, show the hair preview
			        GameSprite hairSprite = new GameSprite(GameHair.getHair(look.getHair()).getWigTexture(id), button.size.height);
			        hairSprite.initDraw().light(new GameLight(136.36363F)).posMiddle(drawX, drawY).draw();
			        
			    }
			    
			} else if (part.getPartName() == "EYE_COLOR" || part.getPartName() == "EYE_TYPE") {
				
			    // Eye preview (using HumanFaceDrawOptions)
				GameTexture.overrideBlendQuality = BlendQuality.NEAREST;
				this.getHumanFaceDrawOptions(options, button.size.height, x, y, (opt) -> {
			        opt.sprite(0, 3).dir(3);  // Set specific sprite direction
			    }).draw();
			    GameTexture.overrideBlendQuality = null;
			    
			} 		else {
			    // General body part preview (head, torso, etc.)
				GameTexture.overrideBlendQuality = BlendQuality.NEAREST;
			    this.getHumanFaceDrawOptions(options, button.size.height, x, y).draw();
			    GameTexture.overrideBlendQuality = null;
			}
	}
	
	private static Point getFaceHairTextureOffset() {	return new Point(-16, -12);	}

	public DrawOptions getHumanFaceDrawOptions(HumanDrawOptions humanDrawOptions, int size, int drawX,
			int drawY) {
		return getHumanFaceDrawOptions(humanDrawOptions, size, drawX, drawY, (Consumer) null);
	}
	
	public DrawOptions getHumanFaceDrawOptions(HumanDrawOptions humanDrawOptions, int size, int drawX, int drawY,
			Consumer<HumanDrawOptions> additionalModifiers) {
		Point offset = getFaceHairTextureOffset();
		float sizeChange = 32.0F / (float) size;
		int offsetX = (int) ((float) offset.x / sizeChange);
		int offsetY = (int) ((float) offset.y / sizeChange);
		humanDrawOptions = humanDrawOptions.sprite(0, 2).dir(2).bodyTexture((GameTexture) null)
				.feetTexture((GameTexture) null).size(size * 2, size * 2).leftArmsTexture((GameTexture) null)
				.rightArmsTexture((GameTexture) null).chestplate((InventoryItem) null).boots((InventoryItem) null)
				.holdItem((InventoryItem) null);
		if (additionalModifiers != null) {
			additionalModifiers.accept(humanDrawOptions);
		}

		return humanDrawOptions.pos(drawX + offsetX, drawY + offsetY);
	}
	
	public static <T> List<T> getClosedEyesDrawOptions(int eyeType, int eyeColor, int skinColor, boolean humanlikeOnly,
			Function<GameTexture, T> mapper) {
		return GameEyes.getEyes(eyeType).getClosedColorTextures(eyeColor, skinColor, humanlikeOnly, mapper);
	}

	public static <T> List<T> getOpenEyesDrawOptions(int eyeType, int eyeColor, int skinColor, boolean humanlikeOnly,
			Function<GameTexture, T> mapper) {
		return GameEyes.getEyes(eyeType).getOpenColorTextures(eyeColor, skinColor, humanlikeOnly, mapper);
	}

	public static DrawOptions getEyesDrawOptions(int eyeType, int eyeColor, int skinColor, boolean humanlikeOnly,
			boolean closed, int drawX, int drawY, int spriteX, int spriteY, int width, int height, boolean mirrorX,
			boolean mirrorY, float alpha, GameLight light, MaskShaderOptions mask) {
		Function<GameTexture, DrawOptions> mapper = (texture) -> {
			return texture.initDraw().sprite(spriteX, spriteY, 64).light(light).alpha(alpha).size(width, height)
					.mirror(mirrorX, mirrorY).addMaskShader(mask).pos(drawX, drawY);
		};
		return closed
				? new DrawOptionsList(getClosedEyesDrawOptions(eyeType, eyeColor, skinColor, humanlikeOnly, mapper))
				: new DrawOptionsList(getOpenEyesDrawOptions(eyeType, eyeColor, skinColor, humanlikeOnly, mapper));
	}

	public DrawOptions getEyesDrawOptions(boolean humanlikeOnly, boolean closed, int drawX, int drawY, int spriteX,
			int spriteY, int width, int height, boolean mirrorX, boolean mirrorY, float alpha, GameLight light,
			MaskShaderOptions mask) {
		return getEyesDrawOptions(this.getRaceLook().getEyeType(), this.getRaceLook().getEyeColor(), this.getRaceLook().getSkin(), humanlikeOnly, closed, drawX,
				drawY, spriteX, spriteY, width, height, mirrorX, mirrorY, alpha, light, mask);
	}	
	
	public Form getSelectionContentIcons(FormInputSize buttonSize, int width, int count,
		IntFunction<GameSprite> buttonContent, IntPredicate isCurrent,
		BiConsumer<Integer, FormInputEvent<FormButton>> onClicked,
		Function<Integer, ArrayList<InventoryItem>> costGetter) {
	return this.getSelectionContent(buttonSize, width, count,
			(button, id, drawX, drawY, w, h, current, hovering) -> {
				GameSprite sprite = (GameSprite) buttonContent.apply(id);
				if (sprite != null) {
					sprite.initDraw().light(new GameLight(!current && !hovering ? 136.36363F : 150.0F))
							.posMiddle(drawX + w / 2, drawY + h / 2).draw();
				}
	
			}, isCurrent, onClicked, costGetter);
	}

	public Form getSelectionContentColors(FormInputSize buttonSize, int width, int count,
		IntFunction<Color> buttonColor, IntPredicate isCurrent,
		BiConsumer<Integer, FormInputEvent<FormButton>> onClicked,
		Function<Integer, ArrayList<InventoryItem>> costGetter) {
	return this.getSelectionContent(buttonSize, width, count,
			(button, id, drawX, drawY, w, h, current, hovering) -> {
				int buttonExtra = button.size.buttonDownContentDrawOffset;
				Renderer.initQuadDraw(w, h + buttonExtra)
						.colorLight((Color) buttonColor.apply(id),
								new GameLight(!current && !hovering ? 120.0F : 150.0F))
						.draw(drawX, drawY - buttonExtra);
			}, isCurrent, onClicked, costGetter);
	}

	public Form getSelectionContentNumber(FormInputSize buttonSize, int width, int count, IntPredicate isCurrent,
		BiConsumer<Integer, FormInputEvent<FormButton>> onClicked,
		Function<Integer, ArrayList<InventoryItem>> costGetter) {
	return this.getSelectionContent(buttonSize, width, count,
			(button, id, drawX, drawY, w, h, current, hovering) -> {
				FontOptions fontOptions = button.size.getFontOptions().color(Settings.UI.activeTextColor);
				String text = "" + (id + 1);
				int textWidth = FontManager.bit.getWidthCeil(text, fontOptions);
				FontManager.bit.drawString((float) (drawX + w / 2 - textWidth / 2),
						(float) (drawY + button.size.fontDrawOffset - 2), text, fontOptions);
			}, isCurrent, onClicked, costGetter);
	}

	public Form getSelectionColorOrCustom(FormInputSize buttonSize, int width,
		final SelectionButtonDrawFunction contentDraw, Color[] colorOptions, Supplier<Color> currentColorGetter,
		Consumer<Color> onSelected, Consumer<Color> onApply, Function<Color, ArrayList<InventoryItem>> costGetter) {		
		
	return this.getSelectionContent(buttonSize, width, colorOptions.length + 1, new SelectionButtonDrawFunction() {
		
		public void draw(FormContentVarToggleButton button, int id, int drawX, int drawY, int width, int height,
				boolean current, boolean hovering) {
			if (id < colorOptions.length) {
				contentDraw.draw(button, id, drawX, drawY, width, height, current, hovering);
			} else {
				int buttonExtra = button.size.buttonDownContentDrawOffset;
				FormColorPicker.drawHueBar(drawX, drawY - buttonExtra, width, height + buttonExtra, (hue) -> {
					return Color.getHSBColor(hue, 1.0F, !current && !hovering ? 0.75F : 1.0F);
				});
			}
	
		}
	}, (id) -> {
		return id < colorOptions.length && ((Color) currentColorGetter.get()).equals(colorOptions[id]);
	}, (id, event) -> {
		final Color color;
		if (id < colorOptions.length) {
			color = colorOptions[id];
			onApply.accept(color);
		} else {
			
			color = (Color) currentColorGetter.get();
			((FormButton) event.from).getManager().openFloatMenu(new ColorSelectorFloatMenu(event.from, color) {
				public void onApplied(Color colorx) {
					if (colorx == null) {
						onApply.accept(color);
					} else {
						onApply.accept(colorx);
					}
	
				}
	
				public void onSelected(Color colorx) {
					onSelected.accept(colorx);
				}
			});
		}
	
	}, (id) -> {
		return id < colorOptions.length
				? (ArrayList) costGetter.apply(colorOptions[id])
				: (ArrayList) costGetter.apply((Color) null);
	});
	}

	public Form getSelectionContent(FormInputSize buttonSize, int width, int count,
		SelectionButtonDrawFunction contentDraw, IntPredicate isCurrent,
		BiConsumer<Integer, FormInputEvent<FormButton>> onClicked,
		Function<Integer, ArrayList<InventoryItem>> costGetter) {
	return this.getSelectionContent(buttonSize, width, count, contentDraw, isCurrent, onClicked, costGetter,
			(Function) null);
	}

	public Form getSelectionContent(FormInputSize buttonSize, int width, int count,
		final SelectionButtonDrawFunction contentDraw, IntPredicate isCurrent,
		BiConsumer<Integer, FormInputEvent<FormButton>> onClicked,
		final Function<Integer, ArrayList<InventoryItem>> costGetter,
		final Function<Integer, GameMessage> tooltipGetter) {
		
	Form form = new Form(width, 10);
	form.drawBase = false;
	int contentPadding = 4;
	int buttonPadding = 1;
	int totalButtonWidth = buttonSize.height + buttonPadding * 2;
	int buttonsPerRow = GameMath.limit(width / totalButtonWidth, 1, count);
	int totalRows = (int) Math.ceil((double) count / (double) buttonsPerRow);
	
	for (int i = 0; i < count; ++i) {
		final int index = i;
		int column = i % buttonsPerRow;
		int row = i / buttonsPerRow;
		int buttonsThisRow = Math.min(count - buttonsPerRow * row, buttonsPerRow);
		int xOffset = width / 2 - buttonsThisRow * totalButtonWidth / 2 - buttonPadding;
		int buttonX = contentPadding + xOffset + column * totalButtonWidth + buttonPadding;
		int buttonY = contentPadding + row * totalButtonWidth + buttonPadding;
		FormContentVarToggleButton button = (FormContentVarToggleButton) form
				.addComponent(new FormContentVarToggleButton(buttonX, buttonY, buttonSize.height, buttonSize,
						ButtonColor.BASE, () -> {
							return isCurrent.test(index);
						}) {
					protected void drawContent(int x, int y, int width, int height) {
						contentDraw.draw(this, index, x, y, width, height, this.isToggled(), this.isHovering());
					}
	
					protected void addTooltips(PlayerMob perspective) {
						super.addTooltips(perspective);
						GameBackground background = null;
						ListGameTooltips tooltips = new ListGameTooltips();
						if (tooltipGetter != null) {
							GameMessage tooltip = (GameMessage) tooltipGetter.apply(index);
							if (tooltip != null) {
								tooltips.add(tooltip);
							}
						}
	
						if (costGetter != null) {
							ArrayList<InventoryItem> cost = (ArrayList) costGetter.apply(index);
							FontOptions fontOptions = (new FontOptions(16)).outline();
							if (cost != null && !cost.isEmpty()) {
								background = GameBackground.getItemTooltipBackground();
								tooltips.add(new LocalMessage("ui", "stylistcost"));
								Iterator var6 = cost.iterator();
	
								while (var6.hasNext()) {
									InventoryItem inventoryItem = (InventoryItem) var6.next();
									FairType fairType = new FairType();
									fairType.append(new FairGlyph[]{new FairColorChangeGlyph(
											inventoryItem.item.getRarityColor(inventoryItem))});
									fairType.append(new FairGlyph[]{
											new FairItemGlyph(fontOptions.getSize(), inventoryItem)});
									fairType.append(new FairGlyph[]{new FairSpacerGlyph(5.0F, 2.0F)});
									fairType.append(fontOptions,
											GameUtils.formatNumber((long) inventoryItem.getAmount()));
									fairType.append(fontOptions, " " + inventoryItem.getItemDisplayName());
									tooltips.add(new FairTypeTooltip(fairType, 10));
								}
							}
						}
	
						if (!tooltips.isEmpty()) {
							GameTooltipManager.addTooltip(tooltips, background, TooltipLocation.FORM_FOCUS);
						}
	
					}
				});
		button.onClicked((e) -> {
			onClicked.accept(index, e);
		});
	}

	form.setHeight(totalRows * totalButtonWidth + contentPadding * 2);
		return form;
	}

	public Form addHeader(GameMessage message, int size, Form form) {
		Form out = new Form(form.getWidth(), 0);
		out.drawBase = false;
		FormFlow flow = new FormFlow();
		out.addComponent((FormLocalLabel) flow.nextY(
				new FormLocalLabel(message, new FontOptions(size), 0, form.getWidth() / 2, 0, form.getWidth() - 20)));
		out.addComponent((Form) flow.nextY(form));
		out.setHeight(flow.next() + 10);
		return out;
	}

	public Form combineContent(Form... forms) {
		Form out = new Form(0, 0);
		out.drawBase = false;
		int height = 0;
		Form[] var4 = forms;
		int var5 = forms.length;
		
		for (int var6 = 0; var6 < var5; ++var6) {
			Form form = var4[var6];
			out.addComponent(form);
			form.setPosition(0, height);
			height += form.getHeight();
			out.setWidth(Math.max(out.getWidth(), form.getWidth()));
		}
		
		out.setHeight(height);
		return out;
	}

	public abstract void reset();

	public abstract void randomize();
	
	public void setPlayer(CustomPlayerMob player) {
		this.newPlayer = player;
		this.updateComponents();
	}
	
	public void setLook(HumanLook look) {
		this.setRaceLook(new RaceLook(this.getRaceLook(), look));
		this.updateComponents();
	}
	
	public void setLook(RaceLook look) {
		this.setRaceLook(look);
		this.updateComponents();
	}

	public void updateComponents() {
		this.icon.setPlayer(this.newPlayer);
		this.updateLook();
	}

	public HumanLook getLook() {
		return this.getRaceLook();
	}

	public abstract void onChanged();

	protected abstract void updateLook();

	public PlayerMob getNewPlayer() {
		this.newPlayer.getInv().giveStarterItems();
		return this.newPlayer;
	}


	public class Section {
		public FormContentVarToggleButton button;
		public Form selectionContent;
	
		public Section(FormContentVarToggleButton button, Form selectionContent) {
			this.button = button;
			this.selectionContent = selectionContent;
		}
	
		public Section(final DrawButtonFunction drawButton, final GameMessage tooltip, Form selectionContent,
				Predicate<Section> isCurrent) {
			
			this.button = new FormContentVarToggleButton(0, 0, BUTTON_SIZE.height,
					BUTTON_SIZE, ButtonColor.BASE, () -> {
						return isCurrent.test(this);
					}) {
				protected void drawContent(int x, int y, int width, int height) {
					drawButton.draw(this, x, y, width, height);
				}
		
				protected void addTooltips(PlayerMob perspective) {
					if (tooltip != null) {
						GameTooltipManager.addTooltip(new StringTooltips(tooltip.translate()),
								TooltipLocation.FORM_FOCUS);
					}
		
				}
			};
			
			this.selectionContent = selectionContent;
		}
	
		public void onClicked(FormSwitcher switcher) {
			switcher.makeCurrent(this.selectionContent);
		}
	}

	public interface DrawButtonFunction {
		void draw(FormContentVarToggleButton var1, int var2, int var3, int var4, int var5);
	}
	
	@FunctionalInterface
	public interface SelectionButtonDrawFunction {
		void draw(FormContentVarToggleButton var1, int var2, int var3, int var4, int var5, int var6, boolean var7,
			boolean var8);
	}
	
	public Object baseGetCurrentBodypartSelection(BodyPart part) {
		  switch (part.getPartName()) {
	    	// Base Game Parts
	        case "SKIN_COLOR": 	            return this.getRaceLook().getSkin();
	        case "EYE_TYPE": 	            return this.getRaceLook().getEyeType();
	        case "EYE_COLOR": 	            return this.getRaceLook().getEyeColor();
	        case "HAIR_STYLE": 	            return this.getRaceLook().getHair();
	        case "FACIAL_HAIR": 	        return this.getRaceLook().getFacialFeature();
	        case "HAIR_COLOR": 	            return this.getRaceLook().getHairColor();
	        case "SHIRT_COLOR": 	        return this.getRaceLook().getShirtColor();
	        case "SHOES_COLOR": 	        return this.getRaceLook().getShoesColor();
	      
	        default: 
	        	return 0; // Fallback value for unknown parts
	    }
	}
	public Point getSkinFaceDrawOffset() 		{	return new Point(-3, -4);	}
	
	public Point getEyeTypeFaceDrawOffset() 	{	return new Point(-22, -26);	}
	
	public Point getEyeColorFaceDrawOffset() 	{	return new Point(-22, -26);	}
	
	public Point baseGetDrawOffset(BodyPart part) {
		 switch (part.getPartName()) {
	        case "SKIN_COLOR": 	return getSkinFaceDrawOffset();
	        case "EYE_TYPE":	return getEyeTypeFaceDrawOffset();
	        case "EYE_COLOR":  	return getEyeColorFaceDrawOffset();
	        case "HAIR_STYLE": 
	        case "FACIAL_HAIR":
	        case "HAIR_COLOR": 	return new Point(0, 0);
	        default: 			return new Point(0, 0);
	    }
	}
	
}