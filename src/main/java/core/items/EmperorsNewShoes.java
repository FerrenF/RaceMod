package core.items;

import core.gfx.texture.TextureManager;
import necesse.engine.util.GameBlackboard;
import necesse.entity.mobs.PlayerMob;
import necesse.gfx.gameTexture.GameTexture;
import necesse.gfx.gameTooltips.ListGameTooltips;
import necesse.inventory.InventoryItem;
import necesse.inventory.item.armorItem.BootsArmorItem;

public class EmperorsNewShoes extends BootsArmorItem{
	public EmperorsNewShoes(int armor) {
		super(armor, 0, (String) null);
	}

	public void loadItemTextures() {
		this.itemTexture = GameTexture.fromFile("items/emperorsnewshoes");
	}

	public ListGameTooltips getPreEnchantmentTooltips(InventoryItem item, PlayerMob perspective,
			GameBlackboard blackboard) {
		ListGameTooltips tooltips = super.getPreEnchantmentTooltips(item, perspective, blackboard);
		tooltips.add("The merchant said...");
		return tooltips;
	}

	protected void loadArmorTexture() {
		this.armorTexture = TextureManager.BLANK_TEXTURE;
	}
	
	public InventoryItem getDefaultItem(PlayerMob player, int amount) {
		return super.getDefaultItem(player, amount);
	}

}
