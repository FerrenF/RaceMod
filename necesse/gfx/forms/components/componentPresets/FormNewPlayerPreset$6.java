package necesse.gfx.forms.components.componentPresets;

import java.awt.Point;
import necesse.gfx.HumanLook;
import necesse.gfx.drawOptions.human.HumanDrawOptions;
import necesse.gfx.forms.components.FormContentVarToggleButton;
import necesse.level.maps.Level;
import necesse.level.maps.levelData.settlementData.settler.Settler;

class FormNewPlayerPreset$6 implements FormNewPlayerPreset.DrawButtonFunction {
	FormNewPlayerPreset$6(FormNewPlayerPreset this$0) {
		this.this$0 = this$0;
	}

	public void draw(FormContentVarToggleButton button, int drawX, int drawY, int width, int height) {
		HumanLook look = new HumanLook(FormNewPlayerPreset.access$000(this.this$0).look);
		look.setHair(0);
		look.setFacialFeature(0);
		HumanDrawOptions humanOptions = new HumanDrawOptions((Level) null, look, false);
		humanOptions.drawEyes(false);
		Point offset = this.this$0.getSkinFaceDrawOffset();
		int var10002 = drawX + offset.x;
		int var10003 = drawY + offset.y;
		Settler.getHumanFaceDrawOptions(humanOptions, FormNewPlayerPreset.access$100().height, var10002, var10003)
				.draw();
	}
}