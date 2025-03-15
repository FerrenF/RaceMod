package patches.server;

import core.race.CustomHumanLook;
import core.race.RaceLook;
import necesse.engine.GameLog;
import necesse.engine.save.LoadData;
import necesse.gfx.HumanLook;

public class ServerClientLoadClientLookPatch {

	public static HumanLook loadClientLook(LoadData save) {
		RaceLook out = new CustomHumanLook(true);
		if (save.hasLoadDataByName("MOB")) {
			LoadData mob = save.getFirstLoadDataByName("MOB");
			if (mob.hasLoadDataByName("LOOK")) {
				LoadData look = mob.getFirstLoadDataByName("LOOK");
				out.applyLoadData(look);
			} else {
				GameLog.warn.println("Could not load client look: Doesn't have MOB.LOOK component");
			}
		} else {
			GameLog.warn.println("Could not load client look: Doesn't have MOB component");
		}

		return out;
	}
	
}
