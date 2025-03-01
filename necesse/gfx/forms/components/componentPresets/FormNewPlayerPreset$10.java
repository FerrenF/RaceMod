package necesse.gfx.forms.components.componentPresets;

import java.awt.Point;
import necesse.gfx.HumanLook;
import necesse.gfx.drawOptions.human.HumanDrawOptions;
import necesse.gfx.forms.components.FormContentVarToggleButton;
import necesse.gfx.gameTexture.GameTexture;
import necesse.gfx.gameTexture.GameTexture.BlendQuality;
import necesse.level.maps.Level;
import necesse.level.maps.levelData.settlementData.settler.Settler;

class FormNewPlayerPreset$10 implements FormNewPlayerPreset.SelectionButtonDrawFunction {
	FormNewPlayerPreset$10(FormNewPlayerPreset this$0) {
		this.this$0 = this$0;
	}

	public void draw(FormContentVarToggleButton button, int id, int drawX, int drawY, int width, int height,
			boolean current, boolean hovering) {
		HumanLook look = new HumanLook(FormNewPlayerPreset.access$000(this.this$0).look);
		look.setHair(0);
		look.setFacialFeature(0);
		look.setEyeColor(id);
		HumanDrawOptions humanOptions = new HumanDrawOptions((Level) null, look, false);
		GameTexture.overrideBlendQuality = BlendQuality.NEAREST;
		Point offset = this.this$0.getEyeColorFaceDrawOffset();
		Settler.getHumanFaceDrawOptions(humanOptions, button.size.height * 2, drawX + offset.x, drawY + offset.y,
				(options) -> {
					options.sprite(0, 3).dir(3);
				}).draw();
		GameTexture.overrideBlendQuality = null;
	}
}