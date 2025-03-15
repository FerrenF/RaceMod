package patches;

import core.RaceMod;

public class characterSavesPathPatch {
	
	public static String getCharacterSavesPath() {
		return RaceMod.characterSavePath;	
	}
}
