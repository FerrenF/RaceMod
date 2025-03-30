package extensions;

import java.awt.Point;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

import necesse.engine.window.WindowManager;
import necesse.entity.mobs.HumanTexture;
import necesse.entity.mobs.HumanTextureFull;
import necesse.entity.mobs.MaskShaderOptions;
import necesse.entity.mobs.PlayerMob;
import necesse.gfx.GameSkin;
import necesse.gfx.HumanLook;
import necesse.gfx.drawOptions.DrawOptions;
import necesse.gfx.drawOptions.DrawOptionsList;
import necesse.gfx.drawOptions.human.HumanDrawOptions;
import necesse.gfx.drawOptions.itemAttack.HumanAttackDrawOptions;
import necesse.gfx.gameTexture.GameSprite;
import necesse.gfx.gameTexture.GameTexture;
import necesse.gfx.shader.EdgeMaskSpriteOptions;
import necesse.gfx.shader.ShaderState;
import necesse.inventory.InventoryItem;
import necesse.inventory.item.armorItem.ArmorItem;
import necesse.inventory.item.armorItem.ChestArmorItem;
import necesse.inventory.item.armorItem.HelmetArmorItem;
import necesse.inventory.item.armorItem.ArmorItem.FacialFeatureDrawMode;
import necesse.inventory.item.armorItem.ArmorItem.HairDrawMode;
import necesse.level.maps.Level;
import necesse.level.maps.light.GameLight;

import org.lwjgl.opengl.GL11;

public class CustomHumanDrawOptions extends HumanDrawOptions {
	private final Level level;
	private HumanLook look;
	private boolean onlyHumanLike;
	private boolean drawEyes;
	private int eyeTypeOverride;
	private GameTexture headTexture;
	private GameTexture eyelidsTexture;
	private GameTexture bodyTexture;
	private GameTexture leftArmsTexture;
	private GameTexture rightArmsTexture;
	private GameTexture feetTexture;
	private GameTexture hairTexture;
	private GameTexture backHairTexture;
	private GameTexture facialFeatureTexture;
	private GameTexture backFacialFeatureTexture;
	private final List<HumanDrawOptionsGetter> behindOptions;
	private final List<HumanDrawOptionsGetter> topOptions;
	private final List<HumanDrawOptionsGetter> onBodyOptions;
	private boolean mirrorX;
	private boolean mirrorY;
	private ArmorItem.HairDrawMode hairMode;
	private ArmorItem.FacialFeatureDrawMode facialFeatureMode;
	private HumanDrawOptionsGetter hatTexture;
	private GameTexture hairMaskTexture;
	private int hatXOffset;
	private int hatYOffset;
	private InventoryItem helmet;
	private InventoryItem chestplate;
	private InventoryItem boots;
	private GameLight light;
	private float alpha;
	private float allAlpha;
	private float rotation;
	private int rotationX;
	private int rotationY;
	private int drawOffsetX;
	private int drawOffsetY;
	private int width;
	private int height;
	private PlayerMob player;
	private int attackCenterX;
	private int attackCenterY;
	private int attackArmPosX;
	private int attackArmPosY;
	private int attackArmRotateX;
	private int attackArmRotateY;
	private int attackArmLength;
	private int attackArmCenterHeight;
	private int attackItemYOffset;
	private float attackArmRotationOffset;
	private HumanAttackDrawOptions attackDrawOptions;
	private InventoryItem attackItem;
	private float attackProgress;
	private float attackDirX;
	private float attackDirY;
	private int spriteX;
	private int spriteY;
	private int spriteRes;
	private int rightArmSpriteX;
	private int leftArmSpriteX;
	private int dir;
	private boolean invis;
	private boolean blinking;
	private MaskShaderOptions mask;
	private boolean forcedBufferDraw;
	private InventoryItem holdItem;
	
	public EyesDrawOptionsProvider eyeDrawOptionsGetter;
	public OnFaceDrawOptionsProvider onFaceOptionsGetter;
	
	public CustomHumanDrawOptions(Level level) {
		super(level);
		this.drawEyes = true;
		this.eyeTypeOverride = -1;
		this.light = new GameLight(150.0F);
		this.alpha = 1.0F;
		this.allAlpha = 1.0F;
		this.width = 64;
		this.height = 64;
		this.attackCenterX = 32;
		this.attackCenterY = 23;
		this.attackArmPosX = 0;
		this.attackArmPosY = 0;
		this.attackArmRotateX = 10;
		this.attackArmRotateY = 16;
		this.attackArmLength = 14;
		this.attackArmCenterHeight = 4;
		this.attackItemYOffset = 12;
		this.attackArmRotationOffset = 0.0F;
		this.spriteRes = 64;
		this.level = level;
		this.behindOptions = new LinkedList<HumanDrawOptionsGetter>();
		this.topOptions = new LinkedList<HumanDrawOptionsGetter>();
		this.onBodyOptions = new LinkedList<HumanDrawOptionsGetter>();
	}

	public CustomHumanDrawOptions(Level level, HumanLook look, boolean onlyHumanlike) {
		this(level);
		this.look = look;
		this.onlyHumanLike = onlyHumanlike;
		GameSkin gameSkin = look.getGameSkin(onlyHumanlike);
		this.headTexture = gameSkin.getHeadTexture();
		this.bodyTexture = gameSkin.getBodyTexture();
		this.leftArmsTexture = gameSkin.getLeftArmsTexture();
		this.rightArmsTexture = gameSkin.getRightArmsTexture();
		this.feetTexture = gameSkin.getFeetTexture();
		this.hairTexture = look.getHairTexture();
		this.backHairTexture = look.getBackHairTexture();
		this.facialFeatureTexture = look.getFacialFeatureTexture();
		this.backFacialFeatureTexture = look.getBackFacialFeatureTexture();
	}

	public CustomHumanDrawOptions(Level level, HumanTexture humanTexture) {
		this(level);
		this.bodyTexture = humanTexture.body;
		this.leftArmsTexture = humanTexture.leftArms;
		this.rightArmsTexture = humanTexture.rightArms;
	}

	public CustomHumanDrawOptions(Level level, HumanTextureFull humanTexture) {
		this(level);
		this.headTexture = humanTexture.head;
		this.eyelidsTexture = humanTexture.eyelids;
		this.bodyTexture = humanTexture.body;
		this.leftArmsTexture = humanTexture.leftArms;
		this.rightArmsTexture = humanTexture.rightArms;
		this.feetTexture = humanTexture.feet;
		this.hairTexture = humanTexture.hair;
		this.backHairTexture = humanTexture.backHair;
	}

	public CustomHumanDrawOptions headTexture(GameTexture headTexture) {
		this.headTexture = headTexture;
		return this;
	}

	public CustomHumanDrawOptions drawEyes(boolean drawEyes) {
		this.drawEyes = drawEyes;
		return this;
	}

	public CustomHumanDrawOptions eyeTypeOverride(int eyeType) {
		this.eyeTypeOverride = eyeType;
		return this;
	}

	public CustomHumanDrawOptions eyelidsTexture(GameTexture eyelidsTexture) {
		this.eyelidsTexture = eyelidsTexture;
		return this;
	}

	public CustomHumanDrawOptions bodyTexture(GameTexture bodyTexture) {
		this.bodyTexture = bodyTexture;
		return this;
	}

	public CustomHumanDrawOptions leftArmsTexture(GameTexture leftArmsTexture) {
		this.leftArmsTexture = leftArmsTexture;
		return this;
	}

	public CustomHumanDrawOptions rightArmsTexture(GameTexture rightArmsTexture) {
		this.rightArmsTexture = rightArmsTexture;
		return this;
	}

	public CustomHumanDrawOptions feetTexture(GameTexture feetTexture) {
		this.feetTexture = feetTexture;
		return this;
	}

	public CustomHumanDrawOptions hairTexture(GameTexture hairTexture) {
		this.hairTexture = hairTexture;
		return this;
	}

	public CustomHumanDrawOptions backHairTexture(GameTexture backHairTexture) {
		this.backHairTexture = backHairTexture;
		return this;
	}

	public CustomHumanDrawOptions facialFeatureTexture(GameTexture facialFeatureTexture) {
		this.facialFeatureTexture = facialFeatureTexture;
		return this;
	}

	public CustomHumanDrawOptions backFacialFeatureTexture(GameTexture backFacialFeatureTexture) {
		this.backFacialFeatureTexture = backFacialFeatureTexture;
		return this;
	}

	public CustomHumanDrawOptions hatTexture(HumanDrawOptionsGetter drawOptionsGetter, ArmorItem.HairDrawMode mode,
			int xOffset, int yOffset) {
		this.hatTexture = drawOptionsGetter;
		this.hairMode = mode;
		this.hatXOffset = xOffset;
		this.hatYOffset = yOffset;
		return this;
	}

	public CustomHumanDrawOptions hatTexture(HumanDrawOptionsGetter drawOptionsGetter, ArmorItem.HairDrawMode mode) {
		return this.hatTexture((HumanDrawOptionsGetter) drawOptionsGetter, mode, 0, 0);
	}

	public CustomHumanDrawOptions hatTexture(GameTexture hatTexture, ArmorItem.HairDrawMode mode, int xOffset, int yOffset) {
		return this.hatTexture((player, dir, spriteX, spriteY, spriteRes, drawX, drawY, width, height, mirrorX, mirrorY,
				light, alpha, mask) -> {
			return hatTexture.initDraw().sprite(spriteX, spriteY, spriteRes).light(light).alpha(alpha)
					.size(width, height).mirror(mirrorX, mirrorY).addMaskShader(mask).pos(drawX, drawY);
		}, mode, xOffset, yOffset);
	}

	public CustomHumanDrawOptions hatTexture(GameTexture hatTexture, ArmorItem.HairDrawMode mode) {
		return this.hatTexture((GameTexture) hatTexture, mode, 0, 0);
	}

	public CustomHumanDrawOptions helmet(InventoryItem helmet) {
		this.helmet = helmet;
		return this;
	}

	public InventoryItem getHelmet() {
		return this.helmet;
	}

	public CustomHumanDrawOptions chestplate(InventoryItem chestplate) {
		this.chestplate = chestplate;
		return this;
	}

	public InventoryItem getChestplate() {
		return this.chestplate;
	}

	public CustomHumanDrawOptions boots(InventoryItem boots) {
		this.boots = boots;
		return this;
	}

	public InventoryItem getBoots() {
		return this.boots;
	}

	public CustomHumanDrawOptions addBehindDraw(HumanDrawOptionsGetter getter) {
		this.behindOptions.add(getter);
		return this;
	}

	public CustomHumanDrawOptions addTopDraw(HumanDrawOptionsGetter getter) {
		this.topOptions.add(getter);
		return this;
	}

	public CustomHumanDrawOptions addOnBodyDraw(HumanDrawOptionsGetter getter) {
		this.onBodyOptions.add(getter);
		return this;
	}

	public CustomHumanDrawOptions mirrorX(boolean mirror) {
		this.mirrorX = mirror;
		return this;
	}

	public CustomHumanDrawOptions mirrorY(boolean mirror) {
		this.mirrorY = mirror;
		return this;
	}

	public CustomHumanDrawOptions alpha(float alpha) {
		this.alpha = alpha;
		return this;
	}

	public CustomHumanDrawOptions allAlpha(float alpha) {
		this.allAlpha = alpha;
		return this;
	}

	public CustomHumanDrawOptions rotate(float angle, int midX, int midY) {
		this.rotation = angle;
		this.rotationX = midX;
		this.rotationY = midY;
		return this;
	}

	public CustomHumanDrawOptions light(GameLight light) {
		this.light = light;
		return this;
	}

	public CustomHumanDrawOptions addDrawOffset(int x, int y) {
		this.drawOffsetX = x;
		this.drawOffsetY = y;
		return this;
	}

	public CustomHumanDrawOptions drawOffset(int x, int y) {
		this.drawOffsetX += x;
		this.drawOffsetY += y;
		return this;
	}

	public CustomHumanDrawOptions size(int width, int height) {
		this.width = width;
		this.height = height;
		return this;
	}

	public CustomHumanDrawOptions player(PlayerMob player) {
		this.player = player;
		return this;
	}

	public CustomHumanDrawOptions attackOffsets(int centerX, int centerY, int armRotateX, int armRotateY, int armLength,
			int armCenterHeight, int itemYOffset) {
		this.attackCenterX = centerX;
		this.attackCenterY = centerY;
		this.attackArmRotateX = armRotateX;
		this.attackArmRotateY = armRotateY;
		this.attackArmLength = armLength;
		this.attackArmCenterHeight = armCenterHeight;
		this.attackItemYOffset = itemYOffset;
		return this;
	}

	public CustomHumanDrawOptions attackOffsets(int centerX, int centerY, int armLength, int armCenterHeight,
			int itemYOffset) {
		this.attackCenterX = centerX;
		this.attackCenterY = centerY;
		this.attackArmLength = armLength;
		this.attackArmCenterHeight = armCenterHeight;
		this.attackItemYOffset = itemYOffset;
		return this;
	}

	public CustomHumanDrawOptions attackArmRotatePoint(int x, int y) {
		this.attackArmRotateX = x;
		this.attackArmRotateY = y;
		return this;
	}

	public CustomHumanDrawOptions attackArmPosOffset(int x, int y) {
		this.attackArmPosX = x;
		this.attackArmPosY = y;
		return this;
	}

	public CustomHumanDrawOptions attackArmRotationOffset(float rotation) {
		this.attackArmRotationOffset = rotation;
		return this;
	}

	public CustomHumanDrawOptions itemAttack(InventoryItem item, PlayerMob player, float attackProgress, float attackDirX,
			float attackDirY) {
		this.attackDrawOptions = null;
		this.attackItem = item;
		this.player = player;
		this.attackProgress = attackProgress;
		this.attackDirX = attackDirX;
		this.attackDirY = attackDirY;
		return this;
	}

	public CustomHumanDrawOptions attackAnim(HumanAttackDrawOptions drawOptions, float attackProgress) {
		this.attackItem = null;
		this.attackDrawOptions = drawOptions;
		this.attackProgress = attackProgress;
		return this;
	}

	public CustomHumanDrawOptions mask(MaskShaderOptions mask) {
		this.mask = mask;
		return this;
	}

	public CustomHumanDrawOptions mask(GameTexture mask, int xOffset, int yOffset) {
		this.mask = new MaskShaderOptions(mask, 0, 0, xOffset, yOffset);
		return this;
	}

	public CustomHumanDrawOptions mask(GameTexture mask) {
		return this.mask(mask, 0, 0);
	}

	public CustomHumanDrawOptions forceBufferDraw() {
		this.forcedBufferDraw = true;
		return this;
	}

	public CustomHumanDrawOptions invis(boolean invis) {
		this.invis = invis;
		return this;
	}

	public CustomHumanDrawOptions blinking(boolean blinking) {
		this.blinking = blinking;
		return this;
	}

	public CustomHumanDrawOptions sprite(int spriteX, int spriteY, int spriteRes) {
		this.spriteX = spriteX;
		this.spriteY = spriteY;
		this.rightArmSpriteX = spriteX;
		this.leftArmSpriteX = spriteX;
		this.spriteRes = spriteRes;
		return this;
	}

	public CustomHumanDrawOptions sprite(int spriteX, int spriteY) {
		return this.sprite(spriteX, spriteY, this.spriteRes);
	}

	public CustomHumanDrawOptions armSprite(int spriteX) {
		this.rightArmSpriteX = spriteX;
		this.leftArmSpriteX = spriteX;
		return this;
	}

	public CustomHumanDrawOptions rightArmSprite(int spriteX) {
		this.rightArmSpriteX = spriteX;
		return this;
	}

	public CustomHumanDrawOptions leftArmSprite(int spriteX) {
		this.leftArmSpriteX = spriteX;
		return this;
	}

	public CustomHumanDrawOptions holdItem(InventoryItem item) {
		if (this.dir == 3) {
			this.leftArmSprite(1);
		} else {
			this.rightArmSprite(1);
		}

		this.holdItem = item;
		return this;
	}

	public boolean hasHoldItem() {
		return this.holdItem != null;
	}

	public CustomHumanDrawOptions sprite(Point sprite, int spriteRes) {
		return this.sprite(sprite.x, sprite.y, spriteRes);
	}

	public CustomHumanDrawOptions sprite(Point sprite) {
		return this.sprite(sprite.x, sprite.y);
	}

	public CustomHumanDrawOptions dir(int dir) {
		this.dir = dir;
		return this;
	}

	public boolean isAttacking() {
		return this.attackDrawOptions != null || this.attackItem != null;
	}

	public float getAttackProgress() {
		return this.attackProgress;
	}
	
	public DrawOptions pos(int drawX, int drawY) {
		if (this.mask != null) {
			this.mask.useShader(false);
		}

		drawX += this.drawOffsetX;
		drawY += this.drawOffsetY;
		DrawOptionsList behind = new DrawOptionsList();
		DrawOptionsList headOptions = new DrawOptionsList();
		DrawOptionsList eyelidsOptions = new DrawOptionsList();
		DrawOptionsList headBackArmorOptions = new DrawOptionsList();
		DrawOptionsList headArmorOptions = new DrawOptionsList();
		DrawOptionsList headFrontArmorOptions = new DrawOptionsList();
		MaskShaderOptions hairMaskOptions;
		EdgeMaskSpriteOptions maskOptions;
		if (this.hatTexture != null) {
			if (this.hairMode != HairDrawMode.NO_HEAD) {
				
				hairMaskOptions = this.mask;
				if (this.hairMaskTexture != null) {
					maskOptions = new EdgeMaskSpriteOptions(new GameSprite(this.hairMaskTexture, this.spriteX,
							this.spriteY, this.spriteRes, this.width, this.height), 0, 0);
					if (this.mask == null) {
						hairMaskOptions = (new MaskShaderOptions(0, 0)).addMask(maskOptions);
					} else {
						hairMaskOptions = this.mask.copyAndAddMask(maskOptions);
					}
				}

				if (this.facialFeatureMode == FacialFeatureDrawMode.OVER_FACIAL_FEATURE) {
					
					if (this.facialFeatureTexture != null && !this.invis) {
						headArmorOptions.add(this.facialFeatureTexture.initDraw()
								.sprite(this.spriteX, this.spriteY, this.spriteRes).size(this.width, this.height)
								.mirror(this.mirrorX, this.mirrorY).light(this.light).alpha(this.alpha)
								.addMaskShader(this.mask).pos(drawX, drawY));
					}

					if (this.backFacialFeatureTexture != null && !this.invis) {
						behind.add(this.backFacialFeatureTexture.initDraw()
								.sprite(this.spriteX, this.spriteY, this.spriteRes).size(this.width, this.height)
								.mirror(this.mirrorX, this.mirrorY).light(this.light).alpha(this.alpha)
								.addMaskShader(this.mask).pos(drawX, drawY));
					}
					
				}

				if (this.hairMode == HairDrawMode.OVER_HAIR) {
					if (this.hairTexture != null && !this.invis) {
						headArmorOptions.add(this.hairTexture.initDraw()
								.sprite(this.spriteX, this.spriteY, this.spriteRes).size(this.width, this.height)
								.mirror(this.mirrorX, this.mirrorY).light(this.light).alpha(this.alpha)
								.addMaskShader(hairMaskOptions).pos(drawX, drawY));
					}

					if (this.backHairTexture != null && !this.invis) {
						behind.add(this.backHairTexture.initDraw().sprite(this.spriteX, this.spriteY, this.spriteRes)
								.size(this.width, this.height).mirror(this.mirrorX, this.mirrorY).light(this.light)
								.alpha(this.alpha).addMaskShader(hairMaskOptions).pos(drawX, drawY));
					}
				}

				if (this.headTexture != null && !this.invis) {
					
					headOptions.add(this.headTexture.initDraw().sprite(this.spriteX, this.spriteY, this.spriteRes)
							.size(this.width, this.height).mirror(this.mirrorX, this.mirrorY).light(this.light)
							.alpha(this.alpha).addMaskShader(this.mask).pos(drawX, drawY));
					
					
				}
				
				
				
				if (this.eyelidsTexture != null && this.blinking && !this.invis) {
					
					
					eyelidsOptions.add(this.eyelidsTexture.initDraw().sprite(this.spriteX, this.spriteY, this.spriteRes)
							.size(this.width, this.height).mirror(this.mirrorX, this.mirrorY).light(this.light)
							.alpha(this.alpha).addMaskShader(this.mask).pos(drawX, drawY));
	
				} else if (this.look != null && !this.invis && this.drawEyes) {
					
					if(this.eyeDrawOptionsGetter!=null) {
						
						// CUSTOM EYES MARKER 1
						
						headOptions.add(this.eyeDrawOptionsGetter.getEyesDrawOptions(this.eyeTypeOverride == -1 ? this.look.getEyeType() : this.eyeTypeOverride,
								this.look.getEyeColor(), this.look.getSkin(), this.onlyHumanLike, this.blinking, drawX,
								drawY, this.spriteX, this.spriteY, this.width, this.height, this.mirrorX, this.mirrorY,
								this.alpha, this.light, this.mask));
					}
					else {						
					
						headOptions.add(HumanLook.getEyesDrawOptions(
							this.eyeTypeOverride == -1 ? this.look.getEyeType() : this.eyeTypeOverride,
							this.look.getEyeColor(), this.look.getSkin(), this.onlyHumanLike, this.blinking, drawX,
							drawY, this.spriteX, this.spriteY, this.width, this.height, this.mirrorX, this.mirrorY,
							this.alpha, this.light, this.mask));
					}
				}
				
				// ON FACE MARKER 1
				if(this.onFaceOptionsGetter != null) {
					headOptions.add(
							this.onFaceOptionsGetter.getOnFaceDrawOptionsProvider(
									player,
									dir,
									spriteRes,
									drawX, drawY,
									spriteX, spriteY,
									width, height,
									mirrorX, mirrorY,
									alpha,
									light,
									mask));
				}
				
				headArmorOptions.add(this.hatTexture.getDrawOptions(this.player, this.dir, this.spriteX, this.spriteY,
						this.spriteRes, drawX + this.hatXOffset, drawY + this.hatYOffset, this.width, this.height,
						this.mirrorX, this.mirrorY, this.light, this.alpha, this.mask));
				if (this.facialFeatureMode == FacialFeatureDrawMode.UNDER_FACIAL_FEATURE) {
					if (this.facialFeatureTexture != null && !this.invis) {
						headArmorOptions.add(this.facialFeatureTexture.initDraw()
								.sprite(this.spriteX, this.spriteY, this.spriteRes).size(this.width, this.height)
								.mirror(this.mirrorX, this.mirrorY).light(this.light).alpha(this.alpha)
								.addMaskShader(this.mask).pos(drawX, drawY));
					}

					if (this.backFacialFeatureTexture != null && !this.invis) {
						behind.add(this.backFacialFeatureTexture.initDraw()
								.sprite(this.spriteX, this.spriteY, this.spriteRes).size(this.width, this.height)
								.mirror(this.mirrorX, this.mirrorY).light(this.light).alpha(this.alpha)
								.addMaskShader(this.mask).pos(drawX, drawY));
					}
				}

				if (this.hairMode == HairDrawMode.UNDER_HAIR) {
					if (this.hairTexture != null && !this.invis) {
						headArmorOptions.add(this.hairTexture.initDraw()
								.sprite(this.spriteX, this.spriteY, this.spriteRes).size(this.width, this.height)
								.mirror(this.mirrorX, this.mirrorY).light(this.light).alpha(this.alpha)
								.addMaskShader(hairMaskOptions).pos(drawX, drawY));
					}

					if (this.backHairTexture != null && !this.invis) {
						behind.add(this.backHairTexture.initDraw().sprite(this.spriteX, this.spriteY, this.spriteRes)
								.size(this.width, this.height).mirror(this.mirrorX, this.mirrorY).light(this.light)
								.alpha(this.alpha).addMaskShader(hairMaskOptions).pos(drawX, drawY));
					}
				}
			}
		} else if (this.helmet != null && this.helmet.item.isArmorItem()) {

			((ArmorItem) this.helmet.item).addExtraDrawOptions(this, this.helmet);
			ArmorItem.HairDrawMode headDrawOptions = ((ArmorItem) this.helmet.item).hairDrawOptions;
			ArmorItem.FacialFeatureDrawMode facialFeatureDrawOptions = ((ArmorItem) this.helmet.item).facialFeatureDrawOptions;
			if (headDrawOptions != HairDrawMode.NO_HEAD) {
				GameTexture hairMask = this.hairMaskTexture == null
						? (this.helmet.item instanceof HelmetArmorItem
								? ((HelmetArmorItem) this.helmet.item).hairMaskTexture
								: null)
						: this.hairMaskTexture;
				hairMaskOptions = this.mask;
				if (hairMask != null) {
					maskOptions = new EdgeMaskSpriteOptions(new GameSprite(hairMask, this.spriteX,
							this.spriteY, this.spriteRes, this.width, this.height), 0, 0);
					if (this.mask == null) {
						hairMaskOptions = (new MaskShaderOptions(0, 0)).addMask(maskOptions);
					} else {
						hairMaskOptions = this.mask.copyAndAddMask(maskOptions);
					}
				}

				if (facialFeatureDrawOptions == FacialFeatureDrawMode.OVER_FACIAL_FEATURE) {
					if (this.facialFeatureTexture != null && !this.invis) {
						headArmorOptions.add(this.facialFeatureTexture.initDraw()
								.sprite(this.spriteX, this.spriteY, this.spriteRes).size(this.width, this.height)
								.mirror(this.mirrorX, this.mirrorY).light(this.light).alpha(this.alpha)
								.addMaskShader(this.mask).pos(drawX, drawY));
					}

					if (this.backFacialFeatureTexture != null && !this.invis) {
						behind.add(this.backFacialFeatureTexture.initDraw()
								.sprite(this.spriteX, this.spriteY, this.spriteRes).size(this.width, this.height)
								.mirror(this.mirrorX, this.mirrorY).light(this.light).alpha(this.alpha)
								.addMaskShader(this.mask).pos(drawX, drawY));
					}
				}

				if (headDrawOptions == HairDrawMode.OVER_HAIR) {
					if (this.hairTexture != null && !this.invis) {
						headArmorOptions.add(this.hairTexture.initDraw()
								.sprite(this.spriteX, this.spriteY, this.spriteRes).size(this.width, this.height)
								.mirror(this.mirrorX, this.mirrorY).light(this.light).alpha(this.alpha)
								.addMaskShader(hairMaskOptions).pos(drawX, drawY));
					}

					if (this.backHairTexture != null && !this.invis) {
						behind.add(this.backHairTexture.initDraw().sprite(this.spriteX, this.spriteY, this.spriteRes)
								.size(this.width, this.height).mirror(this.mirrorX, this.mirrorY).light(this.light)
								.alpha(this.alpha).addMaskShader(hairMaskOptions).pos(drawX, drawY));
					}
				}

				if (this.headTexture != null && !this.invis) {
					headOptions.add(this.headTexture.initDraw().sprite(this.spriteX, this.spriteY, this.spriteRes)
							.size(this.width, this.height).mirror(this.mirrorX, this.mirrorY).light(this.light)
							.alpha(this.alpha).addMaskShader(this.mask).pos(drawX, drawY));	
				}
				
				
				
				if (this.eyelidsTexture != null && this.blinking && !this.invis) {
					eyelidsOptions.add(this.eyelidsTexture.initDraw().sprite(this.spriteX, this.spriteY, this.spriteRes)
							.size(this.width, this.height).mirror(this.mirrorX, this.mirrorY).light(this.light)
							.alpha(this.alpha).addMaskShader(this.mask).pos(drawX, drawY));
				} else if (this.look != null && !this.invis && this.drawEyes) {
					
					// CUSTOM EYES MARKER 2
					if(this.eyeDrawOptionsGetter!=null) {
						headOptions.add(this.eyeDrawOptionsGetter.getEyesDrawOptions(this.eyeTypeOverride == -1 ? this.look.getEyeType() : this.eyeTypeOverride,
								this.look.getEyeColor(), this.look.getSkin(), this.onlyHumanLike, this.blinking, drawX,
								drawY, this.spriteX, this.spriteY, this.width, this.height, this.mirrorX, this.mirrorY,
								this.alpha, this.light, this.mask));
					}
					else {	headOptions.add(HumanLook.getEyesDrawOptions(
								this.eyeTypeOverride == -1 ? this.look.getEyeType() : this.eyeTypeOverride,
								this.look.getEyeColor(), this.look.getSkin(), this.onlyHumanLike, this.blinking, drawX,
								drawY, this.spriteX, this.spriteY, this.width, this.height, this.mirrorX, this.mirrorY,
								this.alpha, this.light, this.mask));
						
					}
					
				}
				// ON FACE MARKER 2
				if(this.onFaceOptionsGetter != null) {
					headOptions.add(
							this.onFaceOptionsGetter.getOnFaceDrawOptionsProvider(
									player,
									dir,
									spriteRes,
									drawX, drawY,
									spriteX, spriteY,
									width, height,
									mirrorX, mirrorY,
									alpha,
									light,
									mask));
				}
				headArmorOptions.add(((ArmorItem) this.helmet.item).getArmorDrawOptions(this.helmet, this.level,
						this.player, this.helmet, this.chestplate, this.boots, this.spriteX, this.spriteY,
						this.spriteRes, drawX, drawY, this.width, this.height, this.mirrorX, this.mirrorY, this.light,
						this.alpha, this.mask));
				if (facialFeatureDrawOptions == FacialFeatureDrawMode.UNDER_FACIAL_FEATURE) {
					if (this.facialFeatureTexture != null && !this.invis) {
						headArmorOptions.add(this.facialFeatureTexture.initDraw()
								.sprite(this.spriteX, this.spriteY, this.spriteRes).size(this.width, this.height)
								.mirror(this.mirrorX, this.mirrorY).light(this.light).alpha(this.alpha)
								.addMaskShader(this.mask).pos(drawX, drawY));
					}

					if (this.backFacialFeatureTexture != null && !this.invis) {
						behind.add(this.backFacialFeatureTexture.initDraw()
								.sprite(this.spriteX, this.spriteY, this.spriteRes).size(this.width, this.height)
								.mirror(this.mirrorX, this.mirrorY).light(this.light).alpha(this.alpha)
								.addMaskShader(this.mask).pos(drawX, drawY));
					}
				}

				if (headDrawOptions == HairDrawMode.UNDER_HAIR) {
					if (this.hairTexture != null && !this.invis) {
						headArmorOptions.add(this.hairTexture.initDraw()
								.sprite(this.spriteX, this.spriteY, this.spriteRes).size(this.width, this.height)
								.mirror(this.mirrorX, this.mirrorY).light(this.light).alpha(this.alpha)
								.addMaskShader(hairMaskOptions).pos(drawX, drawY));
					}

					if (this.backHairTexture != null && !this.invis) {
						behind.add(this.backHairTexture.initDraw().sprite(this.spriteX, this.spriteY, this.spriteRes)
								.size(this.width, this.height).mirror(this.mirrorX, this.mirrorY).light(this.light)
								.alpha(this.alpha).addMaskShader(hairMaskOptions).pos(drawX, drawY));
					}
				}
			} else {
				headArmorOptions.add(((ArmorItem) this.helmet.item).getArmorDrawOptions(this.helmet, this.level,
						this.player, this.helmet, this.chestplate, this.boots, this.spriteX, this.spriteY,
						this.spriteRes, drawX, drawY, this.width, this.height, this.mirrorX, this.mirrorY, this.light,
						this.alpha, this.mask));
			}

			DrawOptions headBackArmorOption = ((ArmorItem) this.helmet.item).getBackArmorDrawOptions(this.helmet,
					this.player, this.spriteX, this.spriteY, this.spriteRes, drawX, drawY, this.width, this.height,
					this.mirrorX, this.mirrorY, this.light, this.alpha, this.mask);
			if (headBackArmorOption != null) {
				headBackArmorOptions.add(headBackArmorOption);
			}

			DrawOptions headFrontDrawOption = ((ArmorItem) this.helmet.item).getFrontArmorDrawOptions(this.helmet,
					this.player, this.spriteX, this.spriteY, this.spriteRes, drawX, drawY, this.width, this.height,
					this.mirrorX, this.mirrorY, this.light, this.alpha, this.mask);
			if (headFrontDrawOption != null) {
				headFrontArmorOptions.add(headFrontDrawOption);
			}
		} else {
			hairMaskOptions = this.mask;
			if (this.hairMaskTexture != null) {
				maskOptions = new EdgeMaskSpriteOptions(new GameSprite(this.hairMaskTexture, this.spriteX, this.spriteY,
						this.spriteRes, this.width, this.height), 0, 0);
				if (this.mask == null) {
					hairMaskOptions = (new MaskShaderOptions(0, 0)).addMask(maskOptions);
				} else {
					hairMaskOptions = this.mask.copyAndAddMask(maskOptions);
				}
			}

			if (this.backFacialFeatureTexture != null && !this.invis) {
				behind.add(this.backFacialFeatureTexture.initDraw().sprite(this.spriteX, this.spriteY, this.spriteRes)
						.size(this.width, this.height).mirror(this.mirrorX, this.mirrorY).light(this.light)
						.alpha(this.alpha).addMaskShader(this.mask).pos(drawX, drawY));
			}

			if (this.backHairTexture != null && !this.invis) {
				behind.add(this.backHairTexture.initDraw().sprite(this.spriteX, this.spriteY, this.spriteRes)
						.size(this.width, this.height).mirror(this.mirrorX, this.mirrorY).light(this.light)
						.alpha(this.alpha).addMaskShader(hairMaskOptions).pos(drawX, drawY));
			}

			if (this.headTexture != null && !this.invis) {
				headOptions.add(this.headTexture.initDraw().sprite(this.spriteX, this.spriteY, this.spriteRes)
						.size(this.width, this.height).mirror(this.mirrorX, this.mirrorY).light(this.light)
						.alpha(this.alpha).addMaskShader(this.mask).pos(drawX, drawY));
			}
			
		
			
			if (this.eyelidsTexture != null && this.blinking && !this.invis) {
				eyelidsOptions.add(this.eyelidsTexture.initDraw().sprite(this.spriteX, this.spriteY, this.spriteRes)
						.size(this.width, this.height).mirror(this.mirrorX, this.mirrorY).light(this.light)
						.alpha(this.alpha).addMaskShader(this.mask).pos(drawX, drawY));
			} else if (this.look != null && !this.invis && this.drawEyes) {
				
				// CUSTOM EYES MARKER 3
				if(this.eyeDrawOptionsGetter!=null) {
					headOptions.add(eyeDrawOptionsGetter.getEyesDrawOptions(this.eyeTypeOverride == -1 ? this.look.getEyeType() : this.eyeTypeOverride,
							this.look.getEyeColor(), this.look.getSkin(), this.onlyHumanLike, this.blinking, drawX, drawY,
							this.spriteX, this.spriteY, this.width, this.height, this.mirrorX, this.mirrorY, this.alpha,
							this.light, this.mask));
				}
				else {
					headOptions.add(HumanLook.getEyesDrawOptions(
						this.eyeTypeOverride == -1 ? this.look.getEyeType() : this.eyeTypeOverride,
						this.look.getEyeColor(), this.look.getSkin(), this.onlyHumanLike, this.blinking, drawX, drawY,
						this.spriteX, this.spriteY, this.width, this.height, this.mirrorX, this.mirrorY, this.alpha,
						this.light, this.mask));
				}
				
			}
			// ON FACE MARKER 3
			if(this.onFaceOptionsGetter != null) {
				headOptions.add(
						this.onFaceOptionsGetter.getOnFaceDrawOptionsProvider(
								player,
								dir,
								spriteRes,
								drawX, drawY,
								spriteX, spriteY,
								width, height,
								mirrorX, mirrorY,
								alpha,
								light,
								mask));
			}
			if (this.facialFeatureTexture != null && !this.invis) {
				headArmorOptions
						.add(this.facialFeatureTexture.initDraw().sprite(this.spriteX, this.spriteY, this.spriteRes)
								.size(this.width, this.height).mirror(this.mirrorX, this.mirrorY).light(this.light)
								.alpha(this.alpha).addMaskShader(this.mask).pos(drawX, drawY));
			}

			if (this.hairTexture != null && !this.invis) {
				headArmorOptions.add(this.hairTexture.initDraw().sprite(this.spriteX, this.spriteY, this.spriteRes)
						.size(this.width, this.height).mirror(this.mirrorX, this.mirrorY).light(this.light)
						.alpha(this.alpha).addMaskShader(hairMaskOptions).pos(drawX, drawY));
			}
		}

		DrawOptionsList chestOptions = new DrawOptionsList();
		DrawOptionsList chestBackArmorOptions = new DrawOptionsList();
		DrawOptionsList chestArmorOptions = new DrawOptionsList();
		DrawOptionsList chestFrontArmorOptions = new DrawOptionsList();
		DrawOptionsList leftArmsOptions = new DrawOptionsList();
		DrawOptionsList rightArmsOptions = new DrawOptionsList();
		DrawOptionsList leftArmsFrontOptions = new DrawOptionsList();
		DrawOptionsList rightArmsFrontOptions = new DrawOptionsList();
		DrawOptionsList holdItemOptions = new DrawOptionsList();
		boolean holdItemInFrontOfArms = false;
		boolean addLeftArm = true;
		boolean addRightArm = true;
		int attackXOffset;
		int attackYOffset;
		DrawOptions attackOptions;
		DrawOptions chestFrontArmorOption;
		if (this.attackDrawOptions != null) {
			attackXOffset = 0;
			attackYOffset = isSpriteXOffset(this.spriteX) ? -2 : 0;
			if (this.mask != null) {
				attackXOffset += this.mask.drawXOffset;
				attackYOffset += this.mask.drawYOffset;
			}

			if (this.chestplate != null && this.chestplate.item.isArmorItem()) {
				
				//SuperUnsafeDrawOptionsHelper.invokeExtraDrawOptions((ArmorItem) this.chestplate.item, this,  this.chestplate);
				((ArmorItem) this.chestplate.item).addExtraDrawOptions(this, this.chestplate);
				
				
				if (((ArmorItem) this.chestplate.item).drawBodyPart(this.chestplate, this.player)
						&& this.bodyTexture != null && !this.invis) {
					chestOptions.add(this.bodyTexture.initDraw().sprite(this.spriteX, this.spriteY, this.spriteRes)
							.size(this.width, this.height).mirror(this.mirrorX, this.mirrorY).light(this.light)
							.alpha(this.alpha).addMaskShader(this.mask).pos(drawX, drawY));
				}

				chestArmorOptions.add(((ArmorItem) this.chestplate.item).getArmorDrawOptions(this.chestplate,
						this.level, this.player, this.helmet, this.chestplate, this.boots, this.spriteX, this.spriteY,
						this.spriteRes, drawX, drawY, this.width, this.height, this.mirrorX, this.mirrorY, this.light,
						this.alpha, this.mask));
				attackOptions = ((ArmorItem) this.chestplate.item).getBackArmorDrawOptions(this.chestplate, this.player,
						this.spriteX, this.spriteY, this.spriteRes, drawX, drawY, this.width, this.height, this.mirrorX,
						this.mirrorY, this.light, this.alpha, this.mask);
				if (attackOptions != null) {
					chestBackArmorOptions.add(attackOptions);
				}

				chestFrontArmorOption = ((ArmorItem) this.chestplate.item).getFrontArmorDrawOptions(this.chestplate,
						this.player, this.spriteX, this.spriteY, this.spriteRes, drawX, drawY, this.width, this.height,
						this.mirrorX, this.mirrorY, this.light, this.alpha, this.mask);
				if (chestFrontArmorOption != null) {
					chestFrontArmorOptions.add(chestFrontArmorOption);
				}
			} else if (this.bodyTexture != null && !this.invis) {
				chestOptions.add(this.bodyTexture.initDraw().sprite(this.spriteX, this.spriteY, this.spriteRes)
						.size(this.width, this.height).mirror(this.mirrorX, this.mirrorY).light(this.light)
						.alpha(this.alpha).addMaskShader(this.mask).pos(drawX, drawY));
			}

			attackOptions = this.attackDrawOptions.light(this.light)
					.setOffsets(this.attackCenterX, this.attackCenterY, this.attackArmPosX, this.attackArmPosY,
							this.attackArmRotationOffset, this.attackArmRotateX, this.attackArmRotateY,
							this.attackArmLength, this.attackArmCenterHeight, this.attackItemYOffset)
					.pos(drawX + attackXOffset, drawY + attackYOffset);
			if (this.dir != 3) {
				rightArmsOptions.add(attackOptions);
				addRightArm = false;
			} else {
				leftArmsOptions.add(attackOptions);
				addLeftArm = false;
			}
		} else if (this.attackItem != null) {
			attackXOffset = 0;
			attackYOffset = isSpriteXOffset(this.spriteX) ? -2 : 0;
			if (this.mask != null) {
				attackXOffset += this.mask.drawXOffset;
				attackYOffset += this.mask.drawYOffset;
			}

			if (this.chestplate != null && this.chestplate.item.isArmorItem()) {
				//SuperUnsafeDrawOptionsHelper.invokeExtraDrawOptions((ArmorItem) this.chestplate.item, this,  this.chestplate);
				((ArmorItem) this.chestplate.item).addExtraDrawOptions(this, this.chestplate);
				if (((ArmorItem) this.chestplate.item).drawBodyPart(this.chestplate, this.player)
						&& this.bodyTexture != null && !this.invis) {
					chestOptions.add(this.bodyTexture.initDraw().sprite(this.spriteX, this.spriteY, this.spriteRes)
							.size(this.width, this.height).mirror(this.mirrorX, this.mirrorY).light(this.light)
							.alpha(this.alpha).addMaskShader(this.mask).pos(drawX, drawY));
				}

				chestArmorOptions.add(((ArmorItem) this.chestplate.item).getArmorDrawOptions(this.chestplate,
						this.level, this.player, this.helmet, this.chestplate, this.boots, this.spriteX, this.spriteY,
						this.spriteRes, drawX, drawY, this.width, this.height, this.mirrorX, this.mirrorY, this.light,
						this.alpha, this.mask));
				attackOptions = ((ArmorItem) this.chestplate.item).getBackArmorDrawOptions(this.chestplate, this.player,
						this.spriteX, this.spriteY, this.spriteRes, drawX, drawY, this.width, this.height, this.mirrorX,
						this.mirrorY, this.light, this.alpha, this.mask);
				if (attackOptions != null) {
					chestBackArmorOptions.add(attackOptions);
				}

				chestFrontArmorOption = ((ArmorItem) this.chestplate.item).getFrontArmorDrawOptions(this.chestplate,
						this.player, this.spriteX, this.spriteY, this.spriteRes, drawX, drawY, this.width, this.height,
						this.mirrorX, this.mirrorY, this.light, this.alpha, this.mask);
				if (chestFrontArmorOption != null) {
					chestFrontArmorOptions.add(chestFrontArmorOption);
				}
			} else if (this.bodyTexture != null && !this.invis) {
				chestOptions.add(this.bodyTexture.initDraw().sprite(this.spriteX, this.spriteY, this.spriteRes)
						.size(this.width, this.height).mirror(this.mirrorX, this.mirrorY).light(this.light)
						.alpha(this.alpha).addMaskShader(this.mask).pos(drawX, drawY));
			}

			attackOptions = this.attackItem
					.getAttackDrawOptions(this.level, this.player, this.helmet, this.chestplate, this.boots, this.dir,
							this.attackDirX, this.attackDirY,
							!this.invis && this.bodyTexture != null
									? new GameSprite(this.bodyTexture, 0, 8, this.spriteRes / 2)
									: null,
							this.attackProgress)
					.light(this.light)
					.setOffsets(this.attackCenterX, this.attackCenterY, this.attackArmPosX, this.attackArmPosY,
							this.attackArmRotationOffset, this.attackArmRotateX, this.attackArmRotateY,
							this.attackArmLength, this.attackArmCenterHeight, this.attackItemYOffset)
					.pos(drawX + attackXOffset, drawY + attackYOffset);
			if (this.dir != 3) {
				rightArmsOptions.add(attackOptions);
				addRightArm = false;
			} else {
				leftArmsOptions.add(attackOptions);
				addLeftArm = false;
			}
		}

		if (addLeftArm || addRightArm) {
			boolean leftArmOffset = isSpriteXOffset(this.leftArmSpriteX);
			boolean rightArmOffset = isSpriteXOffset(this.rightArmSpriteX);
			boolean isSpriteOffset = isSpriteXOffset(this.spriteX);
			int leftArmXOffset = 0;
			int leftArmYOffset = 0;
			int rightArmXOffset = 0;
			int rightArmYOffset = 0;
			if (leftArmOffset != isSpriteOffset) {
				leftArmYOffset = isSpriteOffset ? 0 : 2;
			}

			if (rightArmOffset != isSpriteOffset) {
				rightArmYOffset = isSpriteOffset ? 0 : 2;
			}

			if (this.chestplate != null && this.chestplate.item.isArmorItem()) {
				//SuperUnsafeDrawOptionsHelper.invokeExtraDrawOptions((ArmorItem) this.chestplate.item, this,  this.chestplate);
				((ArmorItem) this.chestplate.item).addExtraDrawOptions(this, this.chestplate);
				if (((ArmorItem) this.chestplate.item).drawBodyPart(this.chestplate, this.player)) {
					if (this.bodyTexture != null && !this.invis) {
						chestOptions.add(this.bodyTexture.initDraw().sprite(this.spriteX, this.spriteY, this.spriteRes)
								.size(this.width, this.height).mirror(this.mirrorX, this.mirrorY).light(this.light)
								.alpha(this.alpha).addMaskShader(this.mask).pos(drawX, drawY));
					}

					if (addLeftArm && this.leftArmsTexture != null && !this.invis) {
						leftArmsOptions.add(this.leftArmsTexture.initDraw()
								.sprite(this.leftArmSpriteX, this.spriteY, this.spriteRes).size(this.width, this.height)
								.mirror(this.mirrorX, this.mirrorY).light(this.light).alpha(this.alpha)
								.addMaskShader(this.mask).pos(drawX + leftArmXOffset, drawY + leftArmYOffset));
					}

					if (addRightArm && this.rightArmsTexture != null && !this.invis) {
						rightArmsOptions.add(this.rightArmsTexture.initDraw()
								.sprite(this.rightArmSpriteX, this.spriteY, this.spriteRes)
								.size(this.width, this.height).mirror(this.mirrorX, this.mirrorY).light(this.light)
								.alpha(this.alpha).addMaskShader(this.mask)
								.pos(drawX + rightArmXOffset, drawY + rightArmYOffset));
					}
				}

				chestArmorOptions.add(((ArmorItem) this.chestplate.item).getArmorDrawOptions(this.chestplate,
						this.level, this.player, this.helmet, this.chestplate, this.boots, this.spriteX, this.spriteY,
						this.spriteRes, drawX, drawY, this.width, this.height, this.mirrorX, this.mirrorY, this.light,
						this.alpha, this.mask));
				DrawOptions chestBackArmorOption = ((ArmorItem) this.chestplate.item).getBackArmorDrawOptions(
						this.chestplate, this.player, this.spriteX, this.spriteY, this.spriteRes, drawX, drawY,
						this.width, this.height, this.mirrorX, this.mirrorY, this.light, this.alpha, this.mask);
				if (chestBackArmorOption != null) {
					chestBackArmorOptions.add(chestBackArmorOption);
				}

				chestFrontArmorOption = ((ArmorItem) this.chestplate.item).getFrontArmorDrawOptions(
						this.chestplate, this.player, this.spriteX, this.spriteY, this.spriteRes, drawX, drawY,
						this.width, this.height, this.mirrorX, this.mirrorY, this.light, this.alpha, this.mask);
				if (chestFrontArmorOption != null) {
					chestFrontArmorOptions.add(chestFrontArmorOption);
				}

				if (this.chestplate.item instanceof ChestArmorItem) {
					DrawOptions chestFrontArmorRightArmsOption;
					if (addLeftArm) {
						leftArmsOptions.add(((ChestArmorItem) this.chestplate.item).getArmorLeftArmsDrawOptions(
								this.chestplate, this.level, this.player, this.helmet, this.chestplate, this.boots,
								this.leftArmSpriteX, this.spriteY, this.spriteRes, drawX + leftArmXOffset,
								drawY + leftArmYOffset, this.width, this.height, this.mirrorX, this.mirrorY, this.light,
								this.alpha, this.mask));
						chestFrontArmorRightArmsOption = ((ChestArmorItem) this.chestplate.item)
								.getFrontArmorLeftArmsDrawOptions(this.chestplate, this.player, this.leftArmSpriteX,
										this.spriteY, this.spriteRes, drawX + leftArmXOffset, drawY + leftArmYOffset,
										this.width, this.height, this.mirrorX, this.mirrorY, this.light, this.alpha,
										this.mask);
						if (chestFrontArmorRightArmsOption != null) {
							leftArmsFrontOptions.add(chestFrontArmorRightArmsOption);
						}
					}

					if (addRightArm) {
						rightArmsOptions.add(((ChestArmorItem) this.chestplate.item).getArmorRightArmsDrawOptions(
								this.chestplate, this.level, this.player, this.helmet, this.chestplate, this.boots,
								this.rightArmSpriteX, this.spriteY, this.spriteRes, drawX + rightArmXOffset,
								drawY + rightArmYOffset, this.width, this.height, this.mirrorX, this.mirrorY,
								this.light, this.alpha, this.mask));
						chestFrontArmorRightArmsOption = ((ChestArmorItem) this.chestplate.item)
								.getFrontArmorRightArmsDrawOptions(this.chestplate, this.player, this.rightArmSpriteX,
										this.spriteY, this.spriteRes, drawX + rightArmXOffset, drawY + rightArmYOffset,
										this.width, this.height, this.mirrorX, this.mirrorY, this.light, this.alpha,
										this.mask);
						if (chestFrontArmorRightArmsOption != null) {
							rightArmsFrontOptions.add(chestFrontArmorRightArmsOption);
						}
					}
				}
			} else {
				if (this.bodyTexture != null && !this.invis) {
					chestOptions.add(this.bodyTexture.initDraw().sprite(this.spriteX, this.spriteY, this.spriteRes)
							.size(this.width, this.height).mirror(this.mirrorX, this.mirrorY).light(this.light)
							.alpha(this.alpha).addMaskShader(this.mask).pos(drawX, drawY));
				}

				if (addLeftArm && this.leftArmsTexture != null && !this.invis) {
					leftArmsOptions.add(this.leftArmsTexture.initDraw()
							.sprite(this.leftArmSpriteX, this.spriteY, this.spriteRes).size(this.width, this.height)
							.mirror(this.mirrorX, this.mirrorY).light(this.light).alpha(this.alpha)
							.addMaskShader(this.mask).pos(drawX + leftArmXOffset, drawY + leftArmYOffset));
				}

				if (addRightArm && this.rightArmsTexture != null && !this.invis) {
					rightArmsOptions.add(this.rightArmsTexture.initDraw()
							.sprite(this.rightArmSpriteX, this.spriteY, this.spriteRes).size(this.width, this.height)
							.mirror(this.mirrorX, this.mirrorY).light(this.light).alpha(this.alpha)
							.addMaskShader(this.mask).pos(drawX + rightArmXOffset, drawY + rightArmYOffset));
				}
			}

			if (this.holdItem != null && addLeftArm && addRightArm) {
				holdItemOptions.add(this.holdItem.item.getHoldItemDrawOptions(this.holdItem, this.player, this.spriteX,
						this.spriteY, drawX, drawY, this.width, this.height, this.mirrorX, this.mirrorY, this.light,
						this.alpha, this.mask));
				holdItemInFrontOfArms = this.holdItem.item.holdItemInFrontOfArms(this.holdItem, this.player,
						this.spriteX, this.spriteY, drawX, drawY, this.width, this.height, this.mirrorX, this.mirrorY,
						this.light, this.alpha, this.mask);
			}
		}

		DrawOptionsList feetOptions = new DrawOptionsList();
		DrawOptionsList feetBackArmorOptions = new DrawOptionsList();
		DrawOptionsList feetArmorOptions = new DrawOptionsList();
		DrawOptionsList feetFrontArmorOptions = new DrawOptionsList();
		if (this.boots != null && this.boots.item.isArmorItem()) {
			
			//SuperUnsafeDrawOptionsHelper.invokeExtraDrawOptions(((ArmorItem) this.boots.item), this, this.boots);
			((ArmorItem) this.boots.item).addExtraDrawOptions(this, this.boots);
			if (((ArmorItem) this.boots.item).drawBodyPart(this.boots, this.player) && this.feetTexture != null
					&& !this.invis) {
				feetOptions.add(this.feetTexture.initDraw().sprite(this.spriteX, this.spriteY, this.spriteRes)
						.size(this.width, this.height).mirror(this.mirrorX, this.mirrorY).light(this.light)
						.alpha(this.alpha).addMaskShader(this.mask).pos(drawX, drawY));
			}

			feetArmorOptions.add(((ArmorItem) this.boots.item).getArmorDrawOptions(this.boots, this.level, this.player,
					this.helmet, this.chestplate, this.boots, this.spriteX, this.spriteY, this.spriteRes, drawX, drawY,
					this.width, this.height, this.mirrorX, this.mirrorY, this.light, this.alpha, this.mask));
			DrawOptions feetBackArmorOption = ((ArmorItem) this.boots.item).getBackArmorDrawOptions(this.boots,
					this.player, this.spriteX, this.spriteY, this.spriteRes, drawX, drawY, this.width, this.height,
					this.mirrorX, this.mirrorY, this.light, this.alpha, this.mask);
			if (feetBackArmorOption != null) {
				feetBackArmorOptions.add(feetBackArmorOption);
			}

			DrawOptions feetFrontArmorOption = ((ArmorItem) this.boots.item).getFrontArmorDrawOptions(this.boots,
					this.player, this.spriteX, this.spriteY, this.spriteRes, drawX, drawY, this.width, this.height,
					this.mirrorX, this.mirrorY, this.light, this.alpha, this.mask);
			if (feetFrontArmorOption != null) {
				feetFrontArmorOptions.add(feetFrontArmorOption);
			}
		} else if (this.feetTexture != null && !this.invis) {
			feetOptions.add(this.feetTexture.initDraw().sprite(this.spriteX, this.spriteY, this.spriteRes)
					.size(this.width, this.height).mirror(this.mirrorX, this.mirrorY).light(this.light)
					.alpha(this.alpha).addMaskShader(this.mask).pos(drawX, drawY));
		}

		Iterator<HumanDrawOptionsGetter> var51 = this.behindOptions.iterator();

		while (var51.hasNext()) {
			HumanDrawOptionsGetter behindOption = (HumanDrawOptionsGetter) var51.next();
			behind.add(behindOption.getDrawOptions(this.player, this.dir, this.spriteX, this.spriteY, this.spriteRes,
					drawX, drawY, this.width, this.height, this.mirrorX, this.mirrorY, this.light, this.alpha,
					this.mask));
		}

		DrawOptionsList onBody = new DrawOptionsList();
		Iterator<HumanDrawOptionsGetter> var52 = this.onBodyOptions.iterator();

		while (var52.hasNext()) {
			HumanDrawOptionsGetter onBodyOption = (HumanDrawOptionsGetter) var52.next();
			onBody.add(onBodyOption.getDrawOptions(this.player, this.dir, this.spriteX, this.spriteY, this.spriteRes,
					drawX, drawY, this.width, this.height, this.mirrorX, this.mirrorY, this.light, this.alpha,
					this.mask));
		}

		DrawOptionsList top = new DrawOptionsList();
		Iterator<HumanDrawOptionsGetter> var57 = this.topOptions.iterator();

		while (var57.hasNext()) {
			HumanDrawOptionsGetter topOption = (HumanDrawOptionsGetter) var57.next();
			top.add(topOption.getDrawOptions(this.player, this.dir, this.spriteX, this.spriteY, this.spriteRes, drawX,
					drawY, this.width, this.height, this.mirrorX, this.mirrorY, this.light, this.alpha, this.mask));
		}

		return getArmorDrawOptions(this.isAttacking(), this.dir, behind, headOptions, eyelidsOptions,
				headBackArmorOptions, headArmorOptions, headFrontArmorOptions, chestOptions, chestBackArmorOptions,
				chestArmorOptions, chestFrontArmorOptions, feetOptions, feetBackArmorOptions, feetArmorOptions,
				feetFrontArmorOptions, leftArmsOptions, leftArmsFrontOptions, rightArmsOptions, rightArmsFrontOptions,
				holdItemOptions.isEmpty() ? null : holdItemOptions, holdItemInFrontOfArms, onBody, top, this.allAlpha,
				this.rotation, drawX + this.rotationX, drawY + this.rotationY, this.mask, this.forcedBufferDraw);
	}

	public static boolean isSpriteXOffset(int spriteX) {
		return spriteX == 1 || spriteX == 3;
	}

	public void draw(int drawX, int drawY) {
		this.pos(drawX, drawY).draw();
	}

	public static void addArmorDrawOptions(DrawOptionsList list, boolean isAttacking, int mobDir, DrawOptions behind,
			DrawOptions head, DrawOptions eyelids, DrawOptions headBackArmor, DrawOptions headArmor,
			DrawOptions headFrontArmor, DrawOptions chest, DrawOptions chestBackArmor, DrawOptions chestArmor,
			DrawOptions chestFrontArmor, DrawOptions feet, DrawOptions feetBackArmor, DrawOptions feetArmor,
			DrawOptions feetFrontArmor, DrawOptions leftArms, DrawOptions frontLeftArms, DrawOptions rightArms,
			DrawOptions frontRightArms, DrawOptions holdItem, boolean holdItemInFrontOfArms, DrawOptions onBody,
			DrawOptions top, ShaderState shader) {
		if (shader != null) {
			Objects.requireNonNull(shader);
			list.add(shader::use);
		}

		if (behind != null) {
			list.add(behind);
		}

		if (mobDir == 0) {
			if (holdItem != null && holdItemInFrontOfArms) {
				list.add(holdItem);
			}

			if (feetBackArmor != null) {
				list.add(feetBackArmor);
			}

			if (chestBackArmor != null) {
				list.add(chestBackArmor);
			}

			if (headBackArmor != null) {
				list.add(headBackArmor);
			}

			if (isAttacking) {
				if (shader != null) {
					Objects.requireNonNull(shader);
					list.add(shader::stop);
				}

				if (rightArms != null) {
					list.add(rightArms);
				}

				if (head != null) {
					list.add(head);
				}

				if (shader != null) {
					Objects.requireNonNull(shader);
					list.add(shader::use);
				}
			} else if (rightArms != null) {
				list.add(rightArms);
			}

			if (leftArms != null) {
				list.add(leftArms);
			}

			if (holdItem != null && !holdItemInFrontOfArms) {
				list.add(holdItem);
			}

			if (feet != null) {
				list.add(feet);
			}

			if (chest != null) {
				list.add(chest);
			}

			if (head != null) {
				list.add(head);
			}

			if (eyelids != null) {
				list.add(eyelids);
			}

			if (onBody != null) {
				list.add(onBody);
			}

			if (feetArmor != null) {
				list.add(feetArmor);
			}

			if (chestArmor != null) {
				list.add(chestArmor);
			}

			if (headArmor != null) {
				list.add(headArmor);
			}

			if (frontLeftArms != null) {
				list.add(frontLeftArms);
			}

			if (frontRightArms != null) {
				list.add(frontRightArms);
			}

			if (feetFrontArmor != null) {
				list.add(feetFrontArmor);
			}

			if (chestFrontArmor != null) {
				list.add(chestFrontArmor);
			}

			if (headFrontArmor != null) {
				list.add(headFrontArmor);
			}
		} else {
			if (feetBackArmor != null) {
				list.add(feetBackArmor);
			}

			if (chestBackArmor != null) {
				list.add(chestBackArmor);
			}

			if (headBackArmor != null) {
				list.add(headBackArmor);
			}

			if (feet != null) {
				list.add(feet);
			}

			if (mobDir == 1 && leftArms != null) {
				list.add(leftArms);
				if (frontLeftArms != null) {
					list.add(frontLeftArms);
				}
			} else if (mobDir == 3 && rightArms != null) {
				list.add(rightArms);
				if (frontRightArms != null) {
					list.add(frontRightArms);
				}
			}

			if (chest != null) {
				list.add(chest);
			}

			if (onBody != null) {
				list.add(onBody);
			}

			if (feetArmor != null) {
				list.add(feetArmor);
			}

			if (feetFrontArmor != null) {
				list.add(feetFrontArmor);
			}

			if (holdItem != null) {
				if (chestArmor != null) {
					list.add(chestArmor);
				}

				if (head != null) {
					list.add(head);
				}

				if (eyelids != null) {
					list.add(eyelids);
				}

				if (headArmor != null) {
					list.add(headArmor);
				}

				if (!isAttacking) {
					if (!holdItemInFrontOfArms) {
						list.add(holdItem);
					}

					if (mobDir == 1 && rightArms != null) {
						list.add(rightArms);
					} else if (mobDir == 3 && leftArms != null) {
						list.add(leftArms);
					} else if (mobDir == 2) {
						if (leftArms != null) {
							list.add(leftArms);
						}

						if (rightArms != null) {
							list.add(rightArms);
						}
					}

					if (holdItemInFrontOfArms) {
						list.add(holdItem);
					}
				} else if (mobDir == 2 && leftArms != null) {
					list.add(leftArms);
				}

				if (frontLeftArms != null) {
					list.add(frontLeftArms);
				}

				if (frontRightArms != null) {
					list.add(frontRightArms);
				}

				if (feetFrontArmor != null) {
					list.add(feetFrontArmor);
				}

				if (chestFrontArmor != null) {
					list.add(chestFrontArmor);
				}

				if (headFrontArmor != null) {
					list.add(headFrontArmor);
				}
			} else {
				if (chestArmor != null) {
					list.add(chestArmor);
				}

				if (chestFrontArmor != null) {
					list.add(chestFrontArmor);
				}

				if (!isAttacking) {
					if (mobDir == 1 && rightArms != null) {
						list.add(rightArms);
						if (frontRightArms != null) {
							list.add(frontRightArms);
						}
					} else if (mobDir == 3 && leftArms != null) {
						list.add(leftArms);
						if (frontLeftArms != null) {
							list.add(frontLeftArms);
						}
					} else if (mobDir == 2) {
						if (leftArms != null) {
							list.add(leftArms);
						}

						if (frontLeftArms != null) {
							list.add(frontLeftArms);
						}

						if (rightArms != null) {
							list.add(rightArms);
						}

						if (frontRightArms != null) {
							list.add(frontRightArms);
						}
					}
				} else if (mobDir == 2) {
					if (leftArms != null) {
						list.add(leftArms);
					}

					if (frontLeftArms != null) {
						list.add(frontLeftArms);
					}
				}

				if (head != null) {
					list.add(head);
				}

				if (eyelids != null) {
					list.add(eyelids);
				}

				if (headArmor != null) {
					list.add(headArmor);
				}

				if (headFrontArmor != null) {
					list.add(headFrontArmor);
				}
			}

			if (isAttacking) {
				if (mobDir == 1 && rightArms != null) {
					if (shader != null) {
						Objects.requireNonNull(shader);
						list.add(shader::stop);
					}

					list.add(rightArms);
					if (frontRightArms != null) {
						list.add(frontRightArms);
					}

					if (shader != null) {
						Objects.requireNonNull(shader);
						list.add(shader::use);
					}
				} else if (mobDir == 3 && leftArms != null) {
					if (shader != null) {
						Objects.requireNonNull(shader);
						list.add(shader::stop);
					}

					list.add(leftArms);
					if (shader != null) {
						Objects.requireNonNull(shader);
						list.add(shader::use);
					}
				} else if (mobDir == 2) {
					if (shader != null) {
						Objects.requireNonNull(shader);
						list.add(shader::stop);
					}

					if (rightArms != null) {
						list.add(rightArms);
					}

					if (frontRightArms != null) {
						list.add(frontRightArms);
					}

					if (shader != null) {
						Objects.requireNonNull(shader);
						list.add(shader::use);
					}
				}
			}
		}

		if (top != null) {
			list.add(top);
		}

		if (shader != null) {
			Objects.requireNonNull(shader);
			list.add(shader::stop);
		}

	}

	public static DrawOptionsList getArmorDrawOptions(boolean isAttacking, int mobDir, DrawOptions behind,
			DrawOptions head, DrawOptions eyelids, DrawOptions headBackArmor, DrawOptions headArmor,
			DrawOptions headFrontArmor, DrawOptions chest, DrawOptions chestBackArmor, DrawOptions chestArmor,
			DrawOptions chestFrontArmor, DrawOptions feet, DrawOptions feetBackArmor, DrawOptions feetArmor,
			DrawOptions feetFrontArmor, DrawOptions leftArms, DrawOptions frontLeftArms, DrawOptions rightArms,
			DrawOptions frontRightArms, DrawOptions holdItem, boolean holdItemInFrontOfArms, DrawOptions onBody,
			DrawOptions top, final float alpha, final float angle, final int rotationMidX, final int rotationMidY,
			ShaderState shader, final boolean forcedBufferDraw) {
		DrawOptionsList draws = new DrawOptionsList() {
			public void draw() {
				if (alpha == 1.0F && angle == 0.0F && !forcedBufferDraw) {
					super.draw();
				} else {
					WindowManager.getWindow().applyDraw(() -> {
						super.draw();
					}, () -> {
						GL11.glTranslatef((float) rotationMidX, (float) rotationMidY, 0.0F);
						GL11.glRotatef(angle, 0.0F, 0.0F, 1.0F);
						GL11.glTranslatef((float) (-rotationMidX), (float) (-rotationMidY), 0.0F);
						GL11.glColor4f(1.0F, 1.0F, 1.0F, alpha);
					}, (Runnable) null);
					GL11.glLoadIdentity();
				}

			}
		};
		addArmorDrawOptions(draws, isAttacking, mobDir, behind, head, eyelids, headBackArmor, headArmor, headFrontArmor,
				chest, chestBackArmor, chestArmor, chestFrontArmor, feet, feetBackArmor, feetArmor, feetFrontArmor,
				leftArms, frontLeftArms, rightArms, frontRightArms, holdItem, holdItemInFrontOfArms, onBody, top,
				shader);
		return draws;
	}
	
	public interface EyesDrawOptionsProvider {
	   public DrawOptions getEyesDrawOptions(
	        int eyeType, 
	        int eyeColor, 
	        int skinColor, 
	        boolean humanlikeOnly, 
	        boolean closed, 
	        int drawX, 
	        int drawY, 
	        int spriteX, 
	        int spriteY, 
	        int width, 
	        int height, 
	        boolean mirrorX, 
	        boolean mirrorY, 
	        float alpha, 
	        GameLight light, 
	        MaskShaderOptions mask
	    );
	}
	
	public interface OnFaceDrawOptionsProvider {
		   public DrawOptions getOnFaceDrawOptionsProvider(
				PlayerMob player,
				int dir,
				int spriteRes,
		        int drawX, 
		        int drawY, 
		        int spriteX, 
		        int spriteY, 
		        int width, 
		        int height, 
		        boolean mirrorX, 
		        boolean mirrorY, 
		        float alpha, 
		        GameLight light, 
		        MaskShaderOptions mask
		    );
		}
	
}