package core.items;

import core.gfx.texture.TextureManager;
import necesse.engine.util.GameBlackboard;
import necesse.entity.mobs.PlayerMob;
import necesse.gfx.gameTexture.GameTexture;
import necesse.gfx.gameTooltips.ListGameTooltips;
import necesse.inventory.InventoryItem;
import necesse.inventory.item.armorItem.ChestArmorItem;

public class EmperorsNewShirt extends ChestArmorItem {
	public EmperorsNewShirt(int armor) {
		super(armor, 0, (String) null, (String) null);
	}

	public ListGameTooltips getPreEnchantmentTooltips(InventoryItem item, PlayerMob perspective,
			GameBlackboard blackboard) {
		ListGameTooltips tooltips = super.getPreEnchantmentTooltips(item, perspective, blackboard);
		tooltips.add("Is anyone going to tell him...?");

		return tooltips;
	}

	public void loadItemTextures() {
		this.itemTexture = GameTexture.fromFile("items/emperorsnewshirt");
	}

	protected void loadArmorTexture() {
		this.armorTexture = TextureManager.BLANK_TEXTURE;
		this.leftArmsTexture = TextureManager.BLANK_TEXTURE;
		this.rightArmsTexture = TextureManager.BLANK_TEXTURE;
	}

	public InventoryItem getDefaultItem(PlayerMob player, int amount) {
		return super.getDefaultItem(player, amount);
	}

}
