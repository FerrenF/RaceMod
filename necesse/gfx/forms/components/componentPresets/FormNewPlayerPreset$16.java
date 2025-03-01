package necesse.gfx.forms.components.componentPresets;

import java.awt.Color;
import necesse.entity.mobs.PlayerMob;
import necesse.gfx.forms.components.FormContentVarToggleButton;
import necesse.inventory.InventoryItem;
import necesse.inventory.item.armorItem.cosmetics.misc.ShoesArmorItem;

class FormNewPlayerPreset$16 implements FormNewPlayerPreset.SelectionButtonDrawFunction {
	FormNewPlayerPreset$16(FormNewPlayerPreset this$0) {
		this.this$0 = this$0;
	}

	public void draw(FormContentVarToggleButton button, int id, int drawX, int drawY, int width, int height,
			boolean current, boolean hovering) {
		Color color = FormNewPlayerPreset.DEFAULT_SHIRT_AND_SHOES_COLORS[id];
		InventoryItem item = ShoesArmorItem.addColorData(new InventoryItem("shirt"), color);
		int size = Math.min(width, height);
		item.drawIcon((PlayerMob) null, drawX + width / 2 - size / 2, drawY + height / 2 - size / 2, size,
				(Color) null);
	}
}