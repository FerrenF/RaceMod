package overrides;

import java.awt.Rectangle;
import java.io.File;
import java.util.List;
import necesse.engine.Settings;
import necesse.engine.gameLoop.tickManager.TickManager;
import necesse.engine.localization.message.GameMessage;
import necesse.engine.localization.message.LocalMessage;
import necesse.engine.save.CharacterSave;
import necesse.engine.util.GameUtils;
import necesse.entity.mobs.PlayerMob;

import overrides.PlayerSprite;

import necesse.gfx.fairType.FairType.TextAlign;

import necesse.gfx.forms.Form;
import necesse.gfx.forms.components.FormButton;
import necesse.gfx.forms.components.FormContentIconButton;
import necesse.gfx.forms.components.FormFlow;
import necesse.gfx.forms.components.FormCustomDraw;
import necesse.gfx.forms.components.FormLabel;
import necesse.gfx.forms.components.FormFairTypeLabel;
import necesse.gfx.forms.components.FormInputSize;

import necesse.gfx.gameFont.FontOptions;
import necesse.gfx.gameTexture.GameTexture;
import necesse.gfx.gameTexture.GameTexture.BlendQuality;
import necesse.gfx.ui.ButtonColor;

public abstract class FormCharacterSaveComponent extends Form {
	public final File file;
	public final CharacterSave character;

	public FormCharacterSaveComponent(int width, File file, CharacterSave character, boolean worldHasCheatsEnabled,
			boolean canEnableCheats) {
		
		super("character" + character.characterUniqueID, width, 74);
		this.file = file;
		this.character = character;
		this.drawBase = false;
		this.addComponent(new FormCustomDraw(5, 5, 64, 64) {
			
			public void draw(TickManager tickManager, PlayerMob perspective, Rectangle renderBox) {
				PlayerSprite.drawInForms((drawX, drawY) -> {
					GameTexture.overrideBlendQuality = BlendQuality.NEAREST;
					PlayerSprite.getIconDrawOptions(drawX, drawY, 64, 64,
							(CustomPlayerMob) FormCharacterSaveComponent.this.character.player, 0, 2).draw();
					GameTexture.overrideBlendQuality = null;
				}, 5, 0, 64, 64);
			}
			
		});
		GameMessage canUseError = null;
		GameMessage useWarning = null;
		if (!worldHasCheatsEnabled) {
			if (character.cheatsEnabled) {
				if (canEnableCheats) {
					useWarning = new LocalMessage("ui", "characterhascheats");
				} else {
					canUseError = new LocalMessage("ui", "charactercheatserror");
				}
			}
		} else if (!character.cheatsEnabled) {
			useWarning = new LocalMessage("ui", "characterisclean");
		}

		FormInputSize buttonSize = FormInputSize.SIZE_24;
		FormFlow buttonFlow = new FormFlow(this.getWidth() - 5);
		FormContentIconButton selectButton = (FormContentIconButton) this.addComponent(
				(FormContentIconButton) buttonFlow.prevX(new FormContentIconButton(0, 5, buttonSize, ButtonColor.GREEN,
						Settings.UI.button_collapsed_24, new GameMessage[]{new LocalMessage("ui", "selectbutton")}),
						2));
		selectButton.onClicked((e) -> {
			this.onSelectPressed();
		});
		if (canUseError != null) {
			selectButton.setActive(false);
			selectButton.setTooltips(new GameMessage[]{canUseError});
		}

		if (file != null) {
			((FormContentIconButton) this.addComponent((FormContentIconButton) buttonFlow
					.prevX(new FormContentIconButton(0, 5, buttonSize, ButtonColor.BASE, Settings.UI.container_rename,
							new GameMessage[]{new LocalMessage("ui", "renamebutton")}), 2)))
					.onClicked((e) -> {
						this.onRenamePressed();
					});
			((FormContentIconButton) this.addComponent((FormContentIconButton) buttonFlow
					.prevX(new FormContentIconButton(0, 5, buttonSize, ButtonColor.RED, Settings.UI.button_trash_24,
							new GameMessage[]{new LocalMessage("ui", "deletebutton")}), 2)))
					.onClicked((e) -> {
						this.onDeletePressed();
					});
		} else {
			((FormContentIconButton) this.addComponent((FormContentIconButton) buttonFlow
					.prevX(new FormContentIconButton(0, 5, buttonSize, ButtonColor.BASE, Settings.UI.container_loot_all,
							new GameMessage[]{new LocalMessage("ui", "downloadcharacter")}), 2)))
					.onClicked((e) -> {
						this.onDownloadPressed();
						((FormButton) e.from).startCooldown(5000);
					});
		}

		int textX = 74;
		FormFlow textFlow = new FormFlow(5);
		int textMaxWidth = buttonFlow.next() - textX - 5;
		FontOptions headerOptions = new FontOptions(20);
		String header = GameUtils.maxString(character.player.playerName, headerOptions, textMaxWidth);
		this.addComponent((FormLabel) textFlow.nextY(new FormLabel(header, headerOptions, -1, textX, 0), 5));
		FontOptions subtitleOptions = new FontOptions(12);
		int subtitleMaxWidth = this.getWidth() - textX - 5;
		LocalMessage playTime = new LocalMessage("ui", "characterplaytime", "time",
				GameUtils.formatSeconds(character.timePlayed));
		this.addComponent((FormFairTypeLabel) textFlow
				.nextY((new FormFairTypeLabel(playTime, subtitleOptions, TextAlign.LEFT, textX, 0))
						.setMax(subtitleMaxWidth, 1, true), 2));
		if (file == null) {
			this.addComponent((FormFairTypeLabel) textFlow
					.nextY((new FormFairTypeLabel(new LocalMessage("ui", "characterfromworld"), subtitleOptions,
							TextAlign.LEFT, textX, 0)).setMax(subtitleMaxWidth, 1, true), 2));
		} else {
			GameMessage lastPlayed = character.lastUsed;
			if (lastPlayed == null) {
				lastPlayed = new LocalMessage("ui", "characternotplayed");
			}

			this.addComponent((FormFairTypeLabel) textFlow
					.nextY((new FormFairTypeLabel((GameMessage) lastPlayed, subtitleOptions, TextAlign.LEFT, textX, 0))
							.setMax(subtitleMaxWidth, 1, true), 2));
			if (useWarning != null) {
				this.addComponent((FormFairTypeLabel) textFlow.nextY(
						(new FormFairTypeLabel(useWarning, subtitleOptions.copy().color(Settings.UI.errorTextColor),
								TextAlign.LEFT, textX, 0)).setMax(subtitleMaxWidth, 1, true),
						2));
			}
		}

		textFlow.next(8);
		this.setHeight(Math.max(this.getHeight(), textFlow.next()));
	}

	public List<Rectangle> getHitboxes() {
		return singleBox(new Rectangle(this.getX(), this.getY(), this.getWidth(), this.getHeight()));
	}

	public abstract void onSelectPressed();

	public abstract void onRenamePressed();

	public abstract void onDeletePressed();

	public abstract void onDownloadPressed();
}