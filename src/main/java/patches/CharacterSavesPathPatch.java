package patches;

import core.RaceMod;

public class CharacterSavesPathPatch {
	
	public static String getCharacterSavesPath() {
		return RaceMod.characterSavePath;	
	}
}
