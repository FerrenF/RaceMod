package necesse.gfx.forms.components.componentPresets;

import java.awt.Color;
import necesse.gfx.forms.components.FormColorPicker;
import necesse.gfx.forms.components.FormContentVarToggleButton;

class FormNewPlayerPreset$19 implements FormNewPlayerPreset.SelectionButtonDrawFunction {
	FormNewPlayerPreset$19(FormNewPlayerPreset this$0, Color[] var2,
			FormNewPlayerPreset.SelectionButtonDrawFunction var3) {
		this.this$0 = this$0;
		this.val$colors = var2;
		this.val$contentDraw = var3;
	}

	public void draw(FormContentVarToggleButton button, int id, int drawX, int drawY, int width, int height,
			boolean current, boolean hovering) {
		if (id < this.val$colors.length) {
			this.val$contentDraw.draw(button, id, drawX, drawY, width, height, current, hovering);
		} else {
			int buttonExtra = button.size.buttonDownContentDrawOffset;
			FormColorPicker.drawHueBar(drawX, drawY - buttonExtra, width, height + buttonExtra, (hue) -> {
				return Color.getHSBColor(hue, 1.0F, !current && !hovering ? 0.75F : 1.0F);
			});
		}

	}
}