package necesse.gfx.forms.components.componentPresets;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.function.Function;
import java.util.function.Supplier;
import necesse.engine.localization.message.GameMessage;
import necesse.engine.localization.message.LocalMessage;
import necesse.engine.util.GameUtils;
import necesse.entity.mobs.PlayerMob;
import necesse.gfx.GameBackground;
import necesse.gfx.fairType.FairColorChangeGlyph;
import necesse.gfx.fairType.FairGlyph;
import necesse.gfx.fairType.FairItemGlyph;
import necesse.gfx.fairType.FairSpacerGlyph;
import necesse.gfx.fairType.FairType;
import necesse.gfx.forms.components.FormContentVarToggleButton;
import necesse.gfx.forms.components.FormInputSize;
import necesse.gfx.gameFont.FontOptions;
import necesse.gfx.gameTooltips.FairTypeTooltip;
import necesse.gfx.gameTooltips.GameTooltipManager;
import necesse.gfx.gameTooltips.ListGameTooltips;
import necesse.gfx.gameTooltips.TooltipLocation;
import necesse.gfx.ui.ButtonColor;
import necesse.inventory.InventoryItem;

class FormNewPlayerPreset$21 extends FormContentVarToggleButton {
	FormNewPlayerPreset$21(FormNewPlayerPreset this$0, int x, int y, int width, FormInputSize size, ButtonColor color,
			Supplier isToggled, FormNewPlayerPreset.SelectionButtonDrawFunction var8, int var9, Function var10,
			Function var11) {
		super(x, y, width, size, color, isToggled);
		this.this$0 = this$0;
		this.val$contentDraw = var8;
		this.val$finalI = var9;
		this.val$tooltipGetter = var10;
		this.val$costGetter = var11;
	}

	protected void drawContent(int x, int y, int width, int height) {
		this.val$contentDraw.draw(this, this.val$finalI, x, y, width, height, this.isToggled(), this.isHovering());
	}

	protected void addTooltips(PlayerMob perspective) {
		super.addTooltips(perspective);
		GameBackground background = null;
		ListGameTooltips tooltips = new ListGameTooltips();
		if (this.val$tooltipGetter != null) {
			GameMessage tooltip = (GameMessage) this.val$tooltipGetter.apply(this.val$finalI);
			if (tooltip != null) {
				tooltips.add(tooltip);
			}
		}

		if (this.val$costGetter != null) {
			ArrayList<InventoryItem> cost = (ArrayList) this.val$costGetter.apply(this.val$finalI);
			FontOptions fontOptions = (new FontOptions(16)).outline();
			if (cost != null && !cost.isEmpty()) {
				background = GameBackground.getItemTooltipBackground();
				tooltips.add(new LocalMessage("ui", "stylistcost"));
				Iterator var6 = cost.iterator();

				while (var6.hasNext()) {
					InventoryItem inventoryItem = (InventoryItem) var6.next();
					FairType fairType = new FairType();
					fairType.append(new FairGlyph[]{
							new FairColorChangeGlyph(inventoryItem.item.getRarityColor(inventoryItem))});
					fairType.append(new FairGlyph[]{new FairItemGlyph(fontOptions.getSize(), inventoryItem)});
					fairType.append(new FairGlyph[]{new FairSpacerGlyph(5.0F, 2.0F)});
					fairType.append(fontOptions, GameUtils.formatNumber((long) inventoryItem.getAmount()));
					fairType.append(fontOptions, " " + inventoryItem.getItemDisplayName());
					tooltips.add(new FairTypeTooltip(fairType, 10));
				}
			}
		}

		if (!tooltips.isEmpty()) {
			GameTooltipManager.addTooltip(tooltips, background, TooltipLocation.FORM_FOCUS);
		}

	}
}