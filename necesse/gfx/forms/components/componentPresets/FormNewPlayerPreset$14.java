package necesse.gfx.forms.components.componentPresets;

import java.awt.Color;
import necesse.engine.Settings;
import necesse.gfx.GameHair;
import necesse.gfx.forms.components.FormContentVarToggleButton;
import necesse.gfx.gameTexture.GameSprite;
import necesse.level.maps.light.GameLight;

class FormNewPlayerPreset$14 implements FormNewPlayerPreset.SelectionButtonDrawFunction {
	FormNewPlayerPreset$14(FormNewPlayerPreset this$0) {
		this.this$0 = this$0;
	}

	public void draw(FormContentVarToggleButton button, int id, int drawX, int drawY, int width, int height,
			boolean current, boolean hovering) {
		if (FormNewPlayerPreset.access$000(this.this$0).look.getHair() == 0) {
			Color color = (Color) GameHair.colors.getSkinColor(id).colors.get(3);
			Settings.UI.paintbrush_grayscale.initDraw().color(color).posMiddle(drawX + width / 2, drawY + height / 2)
					.draw();
			Settings.UI.paintbrush_handle.initDraw().posMiddle(drawX + width / 2, drawY + height / 2).draw();
		} else {
			GameSprite hairSprite = new GameSprite(
					GameHair.getHair(FormNewPlayerPreset.access$000(this.this$0).look.getHair()).getWigTexture(id),
					FormNewPlayerPreset.access$100().height);
			hairSprite.initDraw().light(new GameLight(!current && !hovering ? 136.36363F : 150.0F))
					.posMiddle(drawX + width / 2, drawY + height / 2).draw();
		}

	}
}