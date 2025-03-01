package overrides;

import java.awt.Color;
import java.awt.Point;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.IntPredicate;
import java.util.function.Predicate;
import java.util.function.Supplier;
import necesse.engine.Settings;
import necesse.engine.localization.message.GameMessage;
import necesse.engine.localization.message.LocalMessage;
import necesse.engine.network.NetworkClient;
import necesse.engine.util.GameMath;
import necesse.engine.util.GameUtils;
import necesse.entity.mobs.PlayerMob;
import necesse.gfx.GameBackground;
import necesse.gfx.GameEyes;
import necesse.gfx.GameHair;
import necesse.gfx.GameSkin;
import necesse.gfx.HumanLook;
import necesse.gfx.Renderer;
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
import necesse.level.maps.Level;
import necesse.level.maps.levelData.settlementData.settler.Settler;
import necesse.level.maps.light.GameLight;
import net.bytebuddy.asm.Advice.This;
import overrides.FormNewPlayerPreset.Section;

public class FormNewPlayerPreset extends Form {	
	
	public static Color[] DEFAULT_SHIRT_AND_SHOES_COLORS = new Color[]{new Color(1644825), new Color(4934475),
			new Color(9868950), new Color(14803425), new Color(14249068), new Color(14221312), new Color(8388608),
			new Color(14262124), new Color(14247168), new Color(8403968), new Color(14276460), new Color(14275840),
			new Color(8420608), new Color(8313196), new Color(2414848), new Color(1409024), new Color(7133621),
			new Color(55696), new Color(32853), new Color(7127001), new Color(42713), new Color(25216),
			new Color(7107289), new Color(3033), new Color(1664), new Color(10775769), new Color(7340249),
			new Color(4325504), new Color(14249157), new Color(14221489), new Color(8388712)};
	
	// Enum to define customizable body parts
	private enum BodyPartType {
	    SKIN_COLOR("ui", "skincolor", GameSkin.getTotalSkins()),
	    EYE_TYPE("ui", "eyetype", GameEyes.getTotalEyeTypes()), // Separated Eye Type
	    EYE_COLOR("ui", "eyecolor", GameEyes.getTotalColors()), // Added Eye Color
	    HAIR_STYLE("ui", "hairstyle", GameHair.getTotalHair()),
	    FACIAL_HAIR("ui", "facialhair", GameHair.getTotalFacialFeatures()),
	    HAIR_COLOR("ui", "haircolor", GameHair.getTotalHairColors()),
	    SHIRT_COLOR("ui", "shirtcolor", DEFAULT_SHIRT_AND_SHOES_COLORS.length),
	    SHOES_COLOR("ui", "shoescolor", DEFAULT_SHIRT_AND_SHOES_COLORS.length);

	    final String category;
	    final String labelKey;
	    final int totalOptions;

	    BodyPartType(String category, String labelKey, int totalOptions) {
	        this.category = category;
	        this.labelKey = labelKey;
	        this.totalOptions = totalOptions;
	    }
	}
	
	private static FormInputSize BUTTON_SIZE;
	private PlayerMob newPlayer;
	private boolean allowSupernaturalChanges;
	private boolean allowClothesChance;
	public FormPlayerIcon icon;

	public FormNewPlayerPreset(int x, int y, int width, boolean allowSupernaturalChanges, boolean allowClothesChance) {
		super(width, 0);
		this.setPosition(x, y);
		this.drawBase = false;
		this.newPlayer = new PlayerMob(0L, (NetworkClient) null);
		this.allowSupernaturalChanges = allowSupernaturalChanges;
		this.allowClothesChance = allowClothesChance;
		
		FormFlow flow = new FormFlow(5);
		
		this.addComponent((FormLocalLabel) flow.nextY(
				new FormLocalLabel("ui", "clickplayerrotate", new FontOptions(12), 0, x + width / 2, 0, width - 20)));
		
		int iconY = flow.next(128);		
		
		this.icon = (FormPlayerIcon) overrides.FormNewPlayerPreset.this
				.addComponent(new FormPlayerIcon(x + width / 2 - 64, iconY, 128, 128, this.newPlayer) {
					public void modifyHumanDrawOptions(HumanDrawOptions drawOptions) {
						super.modifyHumanDrawOptions(drawOptions);
						overrides.FormNewPlayerPreset.this.modifyHumanDrawOptions(drawOptions);
					}
				});
		
		this.icon.acceptRightClicks = true;
		
		this.icon.onClicked((e) -> {
			if (e.event.getID() == -99) {
				this.icon.setRotation(this.icon.getRotation() - 1);
			} else {
				this.icon.setRotation(this.icon.getRotation() + 1);
			}

		});
		
		this.setupButtons(x, width, iconY);
		
		FormSwitcher contentSwitcher = (FormSwitcher) this.addComponent(new FormSwitcher());
		
		contentSwitcher.useInactiveHitBoxes = true;
		ArrayList<Section> sections = this.getSections((s) -> {
			return s.selectionContent != null && contentSwitcher.isCurrent(s.selectionContent);
		}, width);
		
		int buttonPadding = 1;
		int totalButtonWidth = BUTTON_SIZE.height + buttonPadding * 2;
		int buttonsPerRow = GameMath.limit(width / totalButtonWidth, 1, sections.size());
		int totalRows = (int) Math.ceil((double) sections.size() / (double) buttonsPerRow);
		int startX = 0;
		int startY = flow.next();

		int maxSectionHeight;
		
		for (maxSectionHeight = 0; maxSectionHeight < sections.size(); ++maxSectionHeight) {
			Section section = (Section) sections.get(maxSectionHeight);
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
			Section section = (Section) var27.next();
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

	public void setupButtons(int x, int width, int iconY) {
	    FormTextureButton leftRotateButton = (FormTextureButton) this.addComponent(
	        new FormTextureButton(x + width / 2 - 64 + 15, iconY + 64 + 20, () -> {
	            return (new GameSprite(Settings.UI.rotate_arrow)).mirrorX().mirrorY();
	        }, -1, 128, TextAlign.RIGHT, TextAlign.CENTER) {
	            public Color getDrawColor() {
	                return (Color) this.getButtonState().textColorGetter.apply(Settings.UI);
	            }
	        }, 10);

	    FormTextureButton rightRotateButton = (FormTextureButton) this.addComponent(
	        new FormTextureButton(x + width / 2 + 64 - 15, iconY + 64 + 20, () -> {
	            return new GameSprite(Settings.UI.rotate_arrow);
	        }, -1, 128, TextAlign.LEFT, TextAlign.CENTER) {
	            public Color getDrawColor() {
	                return (Color) this.getButtonState().textColorGetter.apply(Settings.UI);
	            }
	        }, 10);

	    // Additional setup for buttons, similar to original code
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
	
	private Point getDrawOffset(BodyPartType part) {
	    switch (part) {
	        case SKIN_COLOR: return getSkinFaceDrawOffset();
	        case EYE_TYPE:	return getEyeTypeFaceDrawOffset();
	        case EYE_COLOR:  return getEyeColorFaceDrawOffset();
	        case HAIR_STYLE: 
	        case FACIAL_HAIR:
	        case HAIR_COLOR: return new Point(0, 0);
	        default: return new Point(0, 0);
	    }
	}


	// Returns the modification cost (stub for customization)
	private ArrayList<InventoryItem> getPartModificationCost(Color color) {
	    return null; // Customize as needed
	}
	
	// Updates the player look and triggers necessary updates
	private void updateBodyPartSelection(BodyPartType part, int id) {
	    switch (part) {
	        case SKIN_COLOR: newPlayer.look.setSkin(id); break;
	        case EYE_TYPE: newPlayer.look.setEyeType(id); break;
	        case EYE_COLOR: newPlayer.look.setEyeColor(id); break;
	        case HAIR_STYLE: newPlayer.look.setHair(id); break;
	        case FACIAL_HAIR: newPlayer.look.setFacialFeature(id); break;
	        case HAIR_COLOR: newPlayer.look.setHairColor(id); break;
	        case SHIRT_COLOR: newPlayer.look.setShirtColor(HumanLook.limitClothesColor(new Color(id))); break;
	        case SHOES_COLOR: newPlayer.look.setShoesColor(HumanLook.limitClothesColor(new Color(id))); break;
	    }
	    this.onChanged();
	}
	
	// Returns the current selection for a given body part
	private int getCurrentBodyPartSelection(BodyPartType part) {
	    switch (part) {
	        case SKIN_COLOR: return newPlayer.look.getSkin();
	        case EYE_TYPE: return newPlayer.look.getEyeType();
	        case EYE_COLOR: return newPlayer.look.getEyeColor();
	        case HAIR_STYLE: return newPlayer.look.getHair();
	        case FACIAL_HAIR: return newPlayer.look.getFacialFeature(); 
	        case HAIR_COLOR: return newPlayer.look.getHairColor(); 
	        case SHIRT_COLOR: return newPlayer.look.getShirtColor().getRGB(); 
	        case SHOES_COLOR: return newPlayer.look.getShoesColor().getRGB();
	        default: return -1;
	    }
	}

	
	// Updates the look with the selected value
	private void setLookAttribute(HumanLook look, BodyPartType part, int id) {
	    switch (part) {
	        case SKIN_COLOR : look.setSkin(id); break;
	        case EYE_TYPE : look.setEyeType(id); break;
	        case EYE_COLOR: look.setEyeColor(id); break;
	        case HAIR_STYLE : look.setHair(id); break;
	        case FACIAL_HAIR : look.setFacialFeature(id); break;
	        case HAIR_COLOR : look.setHairColor(id); break;
	        case SHIRT_COLOR : look.setShirtColor(HumanLook.limitClothesColor(new Color(id))); break;
	        case SHOES_COLOR : look.setShoesColor(HumanLook.limitClothesColor(new Color(id))); break;
	    }
	}

	// Draws the icon for each body part section
	private void drawBodyPartIcon(FormContentVarToggleButton button, BodyPartType part, int x, int y, int _width, int _height) {
	    HumanLook look = new HumanLook(FormNewPlayerPreset.this.newPlayer.look);
	    applyLookModifiers(look, part);
	    HumanDrawOptions options = new HumanDrawOptions(null, look, false);
	    Point offset = getDrawOffset(part);
	    int drawX = x + _width / 2;
	    int drawY = y + _height / 2;
	    
	    if (part == BodyPartType.HAIR_STYLE) {
	    	 // Handle Hair style drawing
	        int hairStyleIndex = FormNewPlayerPreset.this.newPlayer.look.getHair();
	        GameTexture wigTexture = GameHair.getHair(hairStyleIndex).getWigTexture(look.getHairColor());
	        wigTexture.initDraw()
	                  .size(button.size.height)
	                  .posMiddle(drawX, drawY)
	                  .draw();
	    } else if (part == BodyPartType.FACIAL_HAIR) {
	    	// Handle Facial Hair drawing
	        int facialHairIndex = FormNewPlayerPreset.this.newPlayer.look.getFacialFeature();
	        GameTexture wigTexture = GameHair.getFacialFeature(facialHairIndex).getWigTexture(look.getHairColor());
	        wigTexture.initDraw()
	                  .size(button.size.height)
	                  .posMiddle(drawX, drawY)
	                  .draw();
	    } else if (part == BodyPartType.EYE_COLOR || part == BodyPartType.EYE_TYPE){	    
	    	GameTexture.overrideBlendQuality = BlendQuality.NEAREST;
	    	 Settler.getHumanFaceDrawOptions(options, button.size.height * 2, x+offset.x, y+offset.y, (opt) -> {
					opt.sprite(0, 3).dir(3);
				}).draw();
	    	 GameTexture.overrideBlendQuality = null;
	    } else {
	    	GameTexture.overrideBlendQuality = BlendQuality.NEAREST;
	        Settler.getHumanFaceDrawOptions(options, BUTTON_SIZE.height, x+offset.x, y+offset.y).draw();
	        GameTexture.overrideBlendQuality = null;
	    }
	}

	// Draws the preview for each body part selection
	private void drawBodyPartPreview(FormContentVarToggleButton button, BodyPartType part, int id, int x, int y, int _width, int _height) {
	    HumanLook look = new HumanLook(FormNewPlayerPreset.this.newPlayer.look);
	    applyLookModifiers(look, part);
	    setLookAttribute(look, part, id);
	    HumanDrawOptions options = new HumanDrawOptions(null, look, false);

	    // Center position for the preview
	    int drawX = x + _width / 2;
	    int drawY = y + _height / 2;

	    // Drawing based on body part type
	    if (part == BodyPartType.HAIR_STYLE) {
	        // Hair Style preview
	        GameTexture wigTexture = GameHair.getHair(id).getWigTexture(look.getHairColor());
	        wigTexture.initDraw().size(_height).posMiddle(drawX, drawY).draw();
	    } else if (part == BodyPartType.FACIAL_HAIR) {
	        // Facial Hair preview
	        GameTexture wigTexture = GameHair.getFacialFeature(id).getWigTexture(look.getHairColor());
	        wigTexture.initDraw().size(_height).posMiddle(drawX, drawY).draw();
	    } else if (part == BodyPartType.HAIR_COLOR) {
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
	    } else if (part == BodyPartType.EYE_COLOR || part == BodyPartType.EYE_TYPE) {
	        // Eye preview (using HumanFaceDrawOptions)
	    	GameTexture.overrideBlendQuality = BlendQuality.NEAREST;
	        Settler.getHumanFaceDrawOptions(options, button.size.height, x, y, (opt) -> {
	            opt.sprite(0, 3).dir(3);  // Set specific sprite direction
	        }).draw();
	        GameTexture.overrideBlendQuality = null;
	        
	    } else {
	        // General body part preview (head, torso, etc.)
	    	GameTexture.overrideBlendQuality = BlendQuality.NEAREST;
	        Settler.getHumanFaceDrawOptions(options, button.size.height, x, y).draw();
	        GameTexture.overrideBlendQuality = null;
	    }
	}


	
	private void applyLookModifiers(HumanLook look, BodyPartType part) {
	    if (part == BodyPartType.SKIN_COLOR || part == BodyPartType.EYE_TYPE || part == BodyPartType.EYE_COLOR) {
	        look.setHair(0);
	        look.setFacialFeature(0);
	    }
	}
	
	private Section createColorCustomSection(final BodyPartType part, final  String labelKey,final  Color[] defaultColors, final Color currentColor, 
			Consumer<Color> colorSetFunc, Function<Color, ArrayList<InventoryItem>> costFunc, Predicate<Section> isCurrent, int _width) {
		
		String itemnm = (part == BodyPartType.SHIRT_COLOR) ? "shirt" : (part == BodyPartType.SHOES_COLOR) ? "shoes" : "";
		return new Section(
        (button, drawX, drawY, width, height) -> {
            InventoryItem item = ShirtArmorItem.addColorData(new InventoryItem(itemnm), currentColor);
            int size = Math.min(width, height);
            item.drawIcon((PlayerMob) null, drawX + width / 2 - size / 2, drawY + height / 2 - size / 2, size, null);
        },
        new LocalMessage("ui", labelKey), 
        this.getSelectionColorOrCustom(BUTTON_SIZE, _width, defaultColors, 
            (button, id, drawX, drawY, width, height, current, hovering) -> {
                Color color = defaultColors[id];
                InventoryItem item = ShirtArmorItem.addColorData(new InventoryItem(itemnm), color);
                int size = Math.min(width, height);
                item.drawIcon((PlayerMob) null, drawX + width / 2 - size / 2, drawY + height / 2 - size / 2, size, null);
            }, 
            ()->{return currentColor;}, 
            (color) -> {
            	this.setLookAttribute(this.getLook(), part, color.getRGB());
                this.updateLook();
            },
            (color) -> {
            	this.updateBodyPartSelection(part, _width);              
                this.updateLook();
                this.updateComponents();          
            },
            (color)->{return costFunc.apply(color);}
        ),
        isCurrent
    );
}					
	// Generates sections dynamically for each body part
	private Section createBodyPartSection(final BodyPartType part, Predicate<Section> isCurrent, int _width) {
		
		if (part == BodyPartType.SHIRT_COLOR || part == BodyPartType.SHOES_COLOR) {
			return createColorCustomSection(
					part,
					part.labelKey,
					DEFAULT_SHIRT_AND_SHOES_COLORS, 		            
		            new Color(this.getCurrentBodyPartSelection(part)),            
		            (color) -> {this.setLookAttribute(this.getLook(), part, color.getRGB());},		            
		            (color)->this.getPartModificationCost(color),
		            isCurrent,
		            _width
		        );
		}
		else {
			
			 return new Section(
			    		(button, drawX, drawY, width, height) -> drawBodyPartIcon(button, part, drawX, drawY, width, height),
			    			new LocalMessage(part.category, part.labelKey),	    			
			        this.getSelectionContent(
			            BUTTON_SIZE, _width, part.totalOptions,
			            (button, id, x, y, w, h, current, hovering) -> drawBodyPartPreview(button, part, id, x, y, w, h),
			            id -> id == getCurrentBodyPartSelection(part),
			            (id, event) -> updateBodyPartSelection(part, id),
			            (color)->this.getPartModificationCost(new Color(color))
			        		),
			        		isCurrent
			    		);
			 
		}	 
		
	}
	
	protected ArrayList<Section> getSections(Predicate<Section> isCurrent, int width) {
		ArrayList<Section> sections = new ArrayList();
		
		sections.add(new Section(new DrawButtonFunction() {
			public void draw(FormContentVarToggleButton button, int drawX, int drawY, int width, int height) {
				ButtonIcon icon = Settings.UI.inventory_sort;
				Color color = (Color) icon.colorGetter.apply(button.getButtonState());
				icon.texture.initDraw().color(color).posMiddle(drawX + width / 2, drawY + height / 2).draw();
			}
		}, new LocalMessage("ui", "randomappearance"), (Form) null, isCurrent) {
			public void onClicked(FormSwitcher switcher) {
				overrides.FormNewPlayerPreset.this.randomize();
				overrides.FormNewPlayerPreset.this.onChanged();
			}
		});
		
		for (BodyPartType part : BodyPartType.values()) {
		    sections.add(createBodyPartSection(part, isCurrent, width));
		}
		
		
		
		if (this.allowSupernaturalChanges) {			
			

			
			// end allow supernatural
		}

		
		if (this.allowClothesChance) {
			
		
		
		}

		return sections;
	}

	public Point getSkinFaceDrawOffset() {
		return new Point(-3, -4);
	}

	public Point getEyeTypeFaceDrawOffset() {
		return new Point(-22, -26);
	}

	public Point getEyeColorFaceDrawOffset() {
		return new Point(-22, -26);
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

	public Form getSelectionColorOrCustom(FormInputSize buttonSize, int width, final Color[] colors,
			final SelectionButtonDrawFunction contentDraw, Supplier<Color> currentColorGetter,
			Consumer<Color> onSelected, Consumer<Color> onApply, Function<Color, ArrayList<InventoryItem>> costGetter) {
		return this.getSelectionContent(buttonSize, width, colors.length + 1, new SelectionButtonDrawFunction() {
			public void draw(FormContentVarToggleButton button, int id, int drawX, int drawY, int width, int height,
					boolean current, boolean hovering) {
				if (id < colors.length) {
					contentDraw.draw(button, id, drawX, drawY, width, height, current, hovering);
				} else {
					int buttonExtra = button.size.buttonDownContentDrawOffset;
					FormColorPicker.drawHueBar(drawX, drawY - buttonExtra, width, height + buttonExtra, (hue) -> {
						return Color.getHSBColor(hue, 1.0F, !current && !hovering ? 0.75F : 1.0F);
					});
				}

			}
		}, (id) -> {
			return id < colors.length && ((Color) currentColorGetter.get()).equals(colors[id]);
		}, (id, event) -> {
			final Color color;
			if (id < colors.length) {
				color = colors[id];
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
			return id < colors.length
					? (ArrayList) costGetter.apply(colors[id])
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

	public void reset() {
		this.setPlayer(new PlayerMob(0L, (NetworkClient) null));
	}

	public void randomize() {
		if (this.allowSupernaturalChanges) {
			this.newPlayer.look.randomizeLook(false);
		} else {
			this.newPlayer.look.randomizeLook(false, true, false, false, false);
		}

		this.updateComponents();
	}

	public void setPlayer(PlayerMob player) {
		this.newPlayer = player;
		this.updateComponents();
	}

	public void setLook(HumanLook look) {
		this.newPlayer.look = look;
		this.updateComponents();
	}

	public void updateComponents() {
		this.icon.setPlayer(this.newPlayer);
		this.updateLook();
	}

	public HumanLook getLook() {
		return this.newPlayer.look;
	}

	public void onChanged() {
	}

	protected void updateLook() {
		this.newPlayer.getInv().giveLookArmor();
	}

	public PlayerMob getNewPlayer() {
		this.newPlayer.getInv().giveStarterItems();
		return this.newPlayer;
	}

	public void modifyHumanDrawOptions(HumanDrawOptions drawOptions) {
	}

	public ArrayList<InventoryItem> getSkinColorCost(int id) {
		return null;
	}

	public ArrayList<InventoryItem> getEyeTypeCost(int id) {
		return null;
	}

	public ArrayList<InventoryItem> getEyeColorCost(int id) {
		return null;
	}

	public ArrayList<InventoryItem> getHairStyleCost(int id) {
		return null;
	}

	public ArrayList<InventoryItem> getFacialFeatureCost(int id) {
		return null;
	}

	public ArrayList<InventoryItem> getHairColorCost(int id) {
		return null;
	}

	public ArrayList<InventoryItem> getShirtColorCost(Color color) {
		return null;
	}

	public ArrayList<InventoryItem> getShoesColorCost(Color color) {
		return null;
	}

	static {
		for (int i = 0; i < DEFAULT_SHIRT_AND_SHOES_COLORS.length; ++i) {
			DEFAULT_SHIRT_AND_SHOES_COLORS[i] = HumanLook.limitClothesColor(DEFAULT_SHIRT_AND_SHOES_COLORS[i]);
		}

		BUTTON_SIZE = FormInputSize.SIZE_32;
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
			this.button = new FormContentVarToggleButton(0, 0, overrides.FormNewPlayerPreset.BUTTON_SIZE.height,
					overrides.FormNewPlayerPreset.BUTTON_SIZE, ButtonColor.BASE, () -> {
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
}