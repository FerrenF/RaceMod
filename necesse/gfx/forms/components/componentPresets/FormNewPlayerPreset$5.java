package necesse.gfx.forms.components.componentPresets;

import java.util.function.Predicate;
import necesse.engine.localization.message.GameMessage;
import necesse.gfx.forms.Form;
import necesse.gfx.forms.FormSwitcher;

class FormNewPlayerPreset$5 extends FormNewPlayerPreset.Section {
	FormNewPlayerPreset$5(FormNewPlayerPreset this$0, FormNewPlayerPreset.DrawButtonFunction drawButton,
			GameMessage tooltip, Form selectionContent, Predicate isCurrent) {
		super(this$0, drawButton, tooltip, selectionContent, isCurrent);
		this.this$0 = this$0;
	}

	public void onClicked(FormSwitcher switcher) {
		this.this$0.randomize();
		this.this$0.onChanged();
	}
}