package patches;

import necesse.engine.modLoader.annotations.ModConstructorPatch;
import necesse.entity.mobs.friendly.human.ElderHumanMob;
import necesse.entity.mobs.friendly.human.humanShop.SellingShopItem;
import net.bytebuddy.asm.Advice;

@ModConstructorPatch(target = ElderHumanMob.class, arguments = {})
public class ElderShopPatch {
	
	@Advice.OnMethodExit
    static void onExit(@Advice.This ElderHumanMob th) {	      
		th.shop.addSellingItem("emperorsnewshirt",  new SellingShopItem().setRandomPrice(5, 50));
		th.shop.addSellingItem("emperorsnewshoes",  new SellingShopItem().setRandomPrice(5, 50));
    }
}
