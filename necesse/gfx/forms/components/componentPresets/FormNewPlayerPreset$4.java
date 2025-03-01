package necesse.gfx.forms.components.componentPresets;

import java.awt.Color;
import necesse.engine.Settings;
import necesse.gfx.forms.components.FormContentVarToggleButton;
import necesse.gfx.ui.ButtonIcon;

class FormNewPlayerPreset$4 implements FormNewPlayerPreset.DrawButtonFunction {
	FormNewPlayerPreset$4(FormNewPlayerPreset this$0) {
		this.this$0 = this$0;
	}

	public void draw(FormContentVarToggleButton button, int drawX, int drawY, int width, int height) {
		ButtonIcon icon = Settings.UI.inventory_sort;
		Color color = (Color) icon.colorGetter.apply(button.getButtonState());
		icon.texture.initDraw().color(color).posMiddle(drawX + width / 2, drawY + height / 2).draw();
	}
}