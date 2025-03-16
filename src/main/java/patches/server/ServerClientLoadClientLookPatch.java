package patches.server;

import core.race.CustomHumanLook;
import core.race.RaceLook;
import helpers.DebugHelper;
import helpers.DebugHelper.MESSAGE_TYPE;
import necesse.engine.GameLog;
import necesse.engine.save.LoadData;
import necesse.gfx.HumanLook;

public class ServerClientLoadClientLookPatch {
	public static HumanLook loadClientLook(LoadData save) {
		RaceLook out = null;
		if (save.hasLoadDataByName("MOB")) {
			LoadData mob = save.getFirstLoadDataByName("MOB");
			if (mob.hasLoadDataByName("LOOK")) {
				LoadData look = mob.getFirstLoadDataByName("LOOK");
				out = RaceLook.raceFromLoadData(look, new CustomHumanLook(true));
				out.applyLoadData(look);
				DebugHelper.handleFormattedDebugMessage("loadClientLook processed race %s", 50, MESSAGE_TYPE.DEBUG, new Object[] {out.getRaceID()});
			} else {
				GameLog.warn.println("Could not load client look: Doesn't have MOB.LOOK component");
			}
		} else {
			GameLog.warn.println("Could not load client look: Doesn't have MOB component");
		}

		return out;
	}
	
}
