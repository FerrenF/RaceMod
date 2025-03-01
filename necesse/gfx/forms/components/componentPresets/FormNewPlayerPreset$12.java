package necesse.gfx.forms.components.componentPresets;

import necesse.gfx.GameHair;
import necesse.gfx.forms.components.FormContentVarToggleButton;
import necesse.gfx.gameTexture.GameTexture;

class FormNewPlayerPreset$12 implements FormNewPlayerPreset.DrawButtonFunction {
	FormNewPlayerPreset$12(FormNewPlayerPreset this$0) {
		this.this$0 = this$0;
	}

	public void draw(FormContentVarToggleButton button, int drawX, int drawY, int width, int height) {
		int hairStyleIndex = FormNewPlayerPreset.access$000(this.this$0).look.getFacialFeature();
		GameTexture wigTexture = GameHair.getFacialFeature(hairStyleIndex)
				.getWigTexture(FormNewPlayerPreset.access$000(this.this$0).look.getHairColor());
		wigTexture.initDraw().size(button.size.height).posMiddle(drawX + width / 2, drawY + height / 2).draw();
	}
}