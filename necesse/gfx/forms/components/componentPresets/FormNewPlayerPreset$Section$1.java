package necesse.gfx.forms.components.componentPresets;

import java.util.function.Supplier;
import necesse.engine.localization.message.GameMessage;
import necesse.entity.mobs.PlayerMob;
import necesse.gfx.forms.components.FormContentVarToggleButton;
import necesse.gfx.forms.components.FormInputSize;
import necesse.gfx.gameTooltips.GameTooltipManager;
import necesse.gfx.gameTooltips.StringTooltips;
import necesse.gfx.gameTooltips.TooltipLocation;
import necesse.gfx.ui.ButtonColor;

class FormNewPlayerPreset$Section$1 extends FormContentVarToggleButton {
	FormNewPlayerPreset$Section$1(FormNewPlayerPreset.Section this$1, int x, int y, int width, FormInputSize size,
			ButtonColor color, Supplier isToggled, FormNewPlayerPreset var8,
			FormNewPlayerPreset.DrawButtonFunction var9, GameMessage var10) {
		super(x, y, width, size, color, isToggled);
		this.this$1 = this$1;
		this.val$this$0 = var8;
		this.val$drawButton = var9;
		this.val$tooltip = var10;
	}

	protected void drawContent(int x, int y, int width, int height) {
		this.val$drawButton.draw(this, x, y, width, height);
	}

	protected void addTooltips(PlayerMob perspective) {
		if (this.val$tooltip != null) {
			GameTooltipManager.addTooltip(new StringTooltips(this.val$tooltip.translate()), TooltipLocation.FORM_FOCUS);
		}

	}
}