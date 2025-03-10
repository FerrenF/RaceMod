package patches;

import java.awt.Point;
import java.util.function.Consumer;
import necesse.engine.window.GameWindow;
import necesse.engine.window.WindowManager;
import necesse.entity.mobs.MaskShaderOptions;
import necesse.entity.mobs.Mob;
import necesse.entity.mobs.PlayerMob;
import necesse.entity.mobs.buffs.BuffModifiers;
import necesse.gfx.GameResources;
import necesse.gfx.camera.GameCamera;
import necesse.gfx.drawOptions.DrawOptions;
import necesse.gfx.drawOptions.human.HumanDrawOptions;
import necesse.inventory.InventoryItem;
import necesse.level.maps.Level;
import necesse.level.maps.light.GameLight;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL14;

import core.RaceMod;
import core.gfx.TestFurryDrawOptions;
import core.race.TestFurryRaceLook;
import core.race.parts.BodyPart;
import extensions.RaceLook;
import factory.RaceDataFactory;
import factory.RaceDataFactory.RaceData;

public class PlayerSpriteHooks {
	public static void drawInForms(DrawInFormsLogic drawLogic, int drawX, int drawY) {
		GameWindow window = WindowManager.getWindow();
		drawInForms(drawLogic, drawX, drawY-100, window.getHudWidth() / 2, window.getHudHeight() / 2);
	}
	
	public static void drawInForms(DrawInFormsLogic drawLogic, int drawX, int drawY, int width, int height) {
		GameWindow window = WindowManager.getWindow();
		int translateX = Math.max(window.getHudWidth() / 2 - width / 2, 0);
		int translateY = Math.max(window.getHudHeight() / 2 - height / 2, 0);
		window.applyDraw(() -> {
			GameResources.formShader.stop();
			drawLogic.draw(translateX, translateY);
			GameResources.formShader.usePrevState();
		}, () -> {
			GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
			GL11.glTranslatef((float) (-translateX + drawX), (float) (-translateY + drawY), 0.0F);
			GL14.glBlendFuncSeparate(770, 771, 1, 771);
			GameResources.formShader.usePrevState();
		}, () -> {
			GL11.glTranslatef((float) (translateX - drawX), (float) (translateY - drawY), 0.0F);
			GameResources.formShader.stop();
		});
	}

	public static DrawOptions getIconDrawOptions(int drawX, int drawY, int width, int height, PlayerMob player,
			int spriteX, int dir) {
		return getIconDrawOptions(drawX, drawY, width, height, player, spriteX, dir, 1.0F, new GameLight(150.0F));
	}

	public static DrawOptions getIconDrawOptions(int drawX, int drawY, int width, int height, PlayerMob player,
			int spriteX, int dir, float alpha, GameLight light) {
		return getIconDrawOptions(width, height, player, spriteX, dir, light, (Consumer) null).alpha(alpha).pos(drawX,
				drawY);
	}

	public static HumanDrawOptions getIconDrawOptions(int width, int height, PlayerMob player, int spriteX, int dir,
			GameLight light, Consumer<HumanDrawOptions> humanDrawOptionsModifier) {
		
		
		RaceLook custom = null;
		if(RaceDataFactory.hasRaceData(player)) {			
			RaceData customRaceData = RaceDataFactory.getRaceData(player);	
			custom = customRaceData.getRaceLook();
		}
		
		InventoryItem helmet = getPlayerDisplayArmor(player, 0);
		InventoryItem chestplate = getPlayerDisplayArmor(player, 1);
		InventoryItem boots = getPlayerDisplayArmor(player, 2);
		HumanDrawOptions humanDrawOptions = (new HumanDrawOptions(player.getLevel(),  player.look, false)).player(player)
				.blinking(player.isBlinking()).helmet(helmet).chestplate(chestplate).boots(boots).size(width, height)
				.invis((Boolean) player.buffManager.getModifier(BuffModifiers.INVISIBILITY)).sprite(spriteX, dir)
				.dir(dir).light(light);
		
		
		if (humanDrawOptionsModifier != null) {
			humanDrawOptionsModifier.accept(humanDrawOptions);
		}
		if (custom != null) {
			custom.modifyHumanDrawOptions(humanDrawOptions);	
		}
	
		
		return humanDrawOptions;
	}

	private static InventoryItem getPlayerDisplayArmor(PlayerMob player, int slot) {
		if (player.getInv().equipment.getSelectedCosmeticSlot(slot).isSlotClear()
				&& !player.getInv().equipment.getSelectedArmorSlot(slot).isSlotClear()) {
			if (player.getInv().equipment.getSelectedArmorSlot(slot).getItemSlot().isArmorItem()) {
				return player.getInv().equipment.getSelectedArmorSlot(slot).getItem();
			}
		} else if (!player.getInv().equipment.getSelectedCosmeticSlot(slot).isSlotClear()
				&& player.getInv().equipment.getSelectedCosmeticSlot(slot).getItemSlot().isArmorItem()) {
			return player.getInv().equipment.getSelectedCosmeticSlot(slot).getItem();
		}

		return null;
	}

	public static DrawOptions getIconAnimationDrawOptions(int x, int y, int width, int height, PlayerMob player) {
		int dir = player.getDir();
		Point sprite = player.getAnimSprite(player.getX(), player.getY(), dir);
		return getIconDrawOptions(x, y, width, height, player, sprite.x, dir);
	}

	public static DrawOptions getIconDrawOptions(int x, int y, PlayerMob player) {
		return getIconDrawOptions(x, y, 32, 32, player, 0, 2);
	}

	public static DrawOptions getDrawOptions(PlayerMob player, int x, int y, GameLight light, GameCamera camera,
			Consumer<HumanDrawOptions> humanDrawOptionsModifier) {
		Level level = player.getLevel();
		if (level == null) {
			return () -> {
			};
		} else {
			int drawX = camera.getDrawX(x) - 32;
			int drawY = camera.getDrawY(y) - 51;
			int dir = player.getDir();
			InventoryItem helmet = getPlayerDisplayArmor(player, 0);
			InventoryItem chestplate = getPlayerDisplayArmor(player, 1);
			InventoryItem boots = getPlayerDisplayArmor(player, 2);
			Mob mount = null;
			if (player.isRiding()) {
				mount = player.getMount();
			}

			if (mount != null && !player.isAttacking) {
				dir = mount.getRiderDir(dir);
			}

			Point sprite = player.getAnimSprite(x, y, dir);
			float depthPercent = player.inLiquidFloat(x, y);
			if (mount != null) {
				drawY += mount.getBobbing(x, y);
			} else {
				drawY += (int) ((float) player.getBobbing(x, y) * depthPercent);
			}

			drawY += level.getTile(x / 32, y / 32).getMobSinkingAmount(player);
			int armSpriteX = sprite.x;
			MaskShaderOptions mask;
			if (mount != null) {
				armSpriteX = mount.getRiderArmSpriteX();
				sprite.x = mount.getRiderSpriteX();
				mask = mount.getRiderMaskOptions(x, y);
			} else {
				mask = player.getSwimMaskShaderOptions(depthPercent);
			}

			float alpha = player.getInvincibilityFrameAlpha();
			HumanDrawOptions options = (new HumanDrawOptions(level, player.look, false)).player(player).helmet(helmet)
					.chestplate(chestplate).boots(boots).dir(dir).allAlpha(alpha)
					.invis((Boolean) player.buffManager.getModifier(BuffModifiers.INVISIBILITY))
					.blinking(player.isBlinking()).sprite(sprite).armSprite(armSpriteX).light(light);
			InventoryItem selectedItem = player.getSelectedItem();
			if (selectedItem != null && selectedItem.item.holdsItem(selectedItem, player)) {
				options.holdItem(selectedItem);
			}

			if (mask != null) {
				options.mask(mask);
			}

			player.setupAttackDraw(options);
			player.buffManager.addHumanDraws(options);
			player.modifyExpressionDrawOptions(options);
			if (humanDrawOptionsModifier != null) {
				humanDrawOptionsModifier.accept(options);
			}
			
			RaceLook custom = null;
			if(RaceDataFactory.hasRaceData(player)) {			
				RaceData customRaceData = RaceDataFactory.getRaceData(player);	
				custom = customRaceData.getRaceLook();
				custom.modifyHumanDrawOptions(options);
			}

			return options.pos(drawX, drawY);
		}
	}

	@FunctionalInterface
	public interface DrawInFormsLogic {
		void draw(int var1, int var2);
	}
}