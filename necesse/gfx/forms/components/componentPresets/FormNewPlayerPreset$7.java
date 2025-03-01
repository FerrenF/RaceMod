package necesse.gfx.forms.components.componentPresets;

import java.awt.Point;
import necesse.gfx.HumanLook;
import necesse.gfx.drawOptions.human.HumanDrawOptions;
import necesse.gfx.forms.components.FormContentVarToggleButton;
import necesse.level.maps.Level;
import necesse.level.maps.levelData.settlementData.settler.Settler;

class FormNewPlayerPreset$7 implements FormNewPlayerPreset.SelectionButtonDrawFunction {
	FormNewPlayerPreset$7(FormNewPlayerPreset this$0) {
		this.this$0 = this$0;
	}

	public void draw(FormContentVarToggleButton button, int id, int drawX, int drawY, int width, int height,
			boolean current, boolean hovering) {
		HumanLook look = new HumanLook(FormNewPlayerPreset.access$000(this.this$0).look);
		look.setHair(0);
		look.setFacialFeature(0);
		look.setSkin(id);
		HumanDrawOptions humanOptions = new HumanDrawOptions((Level) null, look, false);
		Point offset = this.this$0.getSkinFaceDrawOffset();
		Settler.getHumanFaceDrawOptions(humanOptions, button.size.height, drawX + offset.x, drawY + offset.y).draw();
	}
}