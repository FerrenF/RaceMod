package necesse.gfx.forms.components.componentPresets;

import java.awt.Color;
import java.util.function.Supplier;
import necesse.engine.Settings;
import necesse.gfx.fairType.FairType;
import necesse.gfx.forms.components.FormTextureButton;

class FormNewPlayerPreset$3 extends FormTextureButton {
	FormNewPlayerPreset$3(FormNewPlayerPreset this$0, int x, int y, Supplier textureSupplier, int maxWidth,
			int maxHeight, FairType.TextAlign xAlign, FairType.TextAlign yAlign) {
		super(x, y, textureSupplier, maxWidth, maxHeight, xAlign, yAlign);
		this.this$0 = this$0;
	}

	public Color getDrawColor() {
		return (Color) this.getButtonState().textColorGetter.apply(Settings.UI);
	}
}