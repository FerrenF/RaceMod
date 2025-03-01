package necesse.gfx.forms.components.componentPresets;

import necesse.gfx.GameHair;
import necesse.gfx.forms.components.FormContentVarToggleButton;
import necesse.gfx.gameTexture.GameTexture;

class FormNewPlayerPreset$11 implements FormNewPlayerPreset.DrawButtonFunction {
	FormNewPlayerPreset$11(FormNewPlayerPreset this$0) {
		this.this$0 = this$0;
	}

	public void draw(FormContentVarToggleButton button, int drawX, int drawY, int width, int height) {
		int hairStyleIndex = FormNewPlayerPreset.access$000(this.this$0).look.getHair();
		GameTexture wigTexture = GameHair.getHair(hairStyleIndex)
				.getWigTexture(FormNewPlayerPreset.access$000(this.this$0).look.getHairColor());
		wigTexture.initDraw().size(button.size.height).posMiddle(drawX + width / 2, drawY + height / 2).draw();
	}
}