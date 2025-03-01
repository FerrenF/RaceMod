package necesse.gfx.forms.components.componentPresets;

import java.awt.Color;
import necesse.engine.Settings;
import necesse.gfx.GameHair;
import necesse.gfx.forms.components.FormContentVarToggleButton;

class FormNewPlayerPreset$13 implements FormNewPlayerPreset.DrawButtonFunction {
	FormNewPlayerPreset$13(FormNewPlayerPreset this$0) {
		this.this$0 = this$0;
	}

	public void draw(FormContentVarToggleButton button, int drawX, int drawY, int width, int height) {
		int hairColorIndex = FormNewPlayerPreset.access$000(this.this$0).look.getHairColor();
		Color color = (Color) GameHair.colors.getSkinColor(hairColorIndex).colors.get(3);
		Settings.UI.paintbrush_grayscale.initDraw().color(color).posMiddle(drawX + width / 2, drawY + height / 2)
				.draw();
		Settings.UI.paintbrush_handle.initDraw().posMiddle(drawX + width / 2, drawY + height / 2).draw();
	}
}