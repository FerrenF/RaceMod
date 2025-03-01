package necesse.gfx.forms.components.componentPresets;

import java.awt.Color;
import java.util.function.Consumer;
import necesse.gfx.forms.components.FormComponent;
import necesse.gfx.forms.floatMenu.ColorSelectorFloatMenu;

class FormNewPlayerPreset$20 extends ColorSelectorFloatMenu {
	FormNewPlayerPreset$20(FormNewPlayerPreset this$0, FormComponent parent, Color startColor, Consumer var4,
			Color var5, Consumer var6) {
		super(parent, startColor);
		this.this$0 = this$0;
		this.val$onApply = var4;
		this.val$startColor = var5;
		this.val$onSelected = var6;
	}

	public void onApplied(Color color) {
		if (color == null) {
			this.val$onApply.accept(this.val$startColor);
		} else {
			this.val$onApply.accept(color);
		}

	}

	public void onSelected(Color color) {
		this.val$onSelected.accept(color);
	}
}