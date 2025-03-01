package necesse.gfx.forms.components.componentPresets;

import necesse.entity.mobs.PlayerMob;
import necesse.gfx.drawOptions.human.HumanDrawOptions;
import necesse.gfx.forms.components.FormPlayerIcon;

class FormNewPlayerPreset$1 extends FormPlayerIcon {
	FormNewPlayerPreset$1(FormNewPlayerPreset this$0, int x, int y, int width, int height, PlayerMob player) {
		super(x, y, width, height, player);
		this.this$0 = this$0;
	}

	public void modifyHumanDrawOptions(HumanDrawOptions drawOptions) {
		super.modifyHumanDrawOptions(drawOptions);
		this.this$0.modifyHumanDrawOptions(drawOptions);
	}
}