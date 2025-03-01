package necesse.gfx.forms.components.componentPresets;

import java.awt.Color;
import necesse.entity.mobs.PlayerMob;
import necesse.gfx.forms.components.FormContentVarToggleButton;
import necesse.inventory.InventoryItem;
import necesse.inventory.item.armorItem.cosmetics.misc.ShoesArmorItem;

class FormNewPlayerPreset$17 implements FormNewPlayerPreset.DrawButtonFunction {
	FormNewPlayerPreset$17(FormNewPlayerPreset this$0) {
		this.this$0 = this$0;
	}

	public void draw(FormContentVarToggleButton button, int drawX, int drawY, int width, int height) {
		InventoryItem item = ShoesArmorItem.addColorData(new InventoryItem("shoes"),
				FormNewPlayerPreset.access$000(this.this$0).look.getShoesColor());
		int size = Math.min(width, height);
		item.drawIcon((PlayerMob) null, drawX + width / 2 - size / 2, drawY + height / 2 - size / 2, size,
				(Color) null);
	}
}