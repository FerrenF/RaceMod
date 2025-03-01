package necesse.gfx.forms.components.componentPresets;

import java.util.function.Predicate;
import necesse.engine.localization.message.GameMessage;
import necesse.entity.mobs.PlayerMob;
import necesse.gfx.forms.Form;
import necesse.gfx.forms.FormSwitcher;
import necesse.gfx.forms.components.FormContentVarToggleButton;
import necesse.gfx.gameTooltips.GameTooltipManager;
import necesse.gfx.gameTooltips.StringTooltips;
import necesse.gfx.gameTooltips.TooltipLocation;
import necesse.gfx.ui.ButtonColor;

public class FormNewPlayerPreset$Section {
	public FormContentVarToggleButton button;
	public Form selectionContent;

	public FormNewPlayerPreset$Section(FormNewPlayerPreset this$0, FormContentVarToggleButton button,
			Form selectionContent) {
		this.this$0 = this$0;
		this.button = button;
		this.selectionContent = selectionContent;
	}

	public FormNewPlayerPreset$Section(final FormNewPlayerPreset this$0,
			final FormNewPlayerPreset.DrawButtonFunction drawButton, final GameMessage tooltip, Form selectionContent,
			Predicate isCurrent) {
		this.this$0 = this$0;
		this.button = new FormContentVarToggleButton(0, 0, FormNewPlayerPreset.access$100().height,
				FormNewPlayerPreset.access$100(), ButtonColor.BASE, () -> {
					return isCurrent.test(this);
				}) {
			protected void drawContent(int x, int y, int width, int height) {
				drawButton.draw(this, x, y, width, height);
			}

			protected void addTooltips(PlayerMob perspective) {
				if (tooltip != null) {
					GameTooltipManager.addTooltip(new StringTooltips(tooltip.translate()), TooltipLocation.FORM_FOCUS);
				}

			}
		};
		this.selectionContent = selectionContent;
	}

	public void onClicked(FormSwitcher switcher) {
		switcher.makeCurrent(this.selectionContent);
	}
}