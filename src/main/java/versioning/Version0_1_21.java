package versioning;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Map;
import java.util.Map.Entry;

import core.race.CustomHumanLook;
import core.race.OrcRaceLook;
import core.race.RaceLook;
import core.race.TestFurryRaceLook;
import core.race.parts.BodyPart;
import core.race.parts.HumanRaceParts;
import core.race.parts.OrcRaceParts;
import core.race.parts.RaceLookParts;
import helpers.DebugHelper;
import helpers.DebugHelper.MESSAGE_TYPE;
import necesse.engine.GlobalData;
import necesse.engine.save.CharacterSave;
import necesse.engine.save.LoadData;
import necesse.engine.save.SaveData;
import core.race.parts.TestFurryRaceParts;
public class Version0_1_21 {

	private static LoadData upgradeSaveData(LoadData in) {
		String race_string = in.getSafeString("race_id", "");
		Class<? extends RaceLookParts> raceParts;
		switch(race_string) {
			case TestFurryRaceLook.TEST_FURRY_RACE_ID:
				raceParts = TestFurryRaceParts.class;
				break;							
			case OrcRaceLook.ORC_RACE_ID:
				raceParts = OrcRaceParts.class;
				break;
			default:
				raceParts = HumanRaceParts.class;
				break;
		}
		
		Map<String, String> upgrades = Map.of(	"HAIR_COLOR", "BASE_HAIR_COLOR",
												"HAIR_STYLE","BASE_HAIR", 
												"SHIRT_COLOR", "BASE_SHIRT",
												"SHOE_COLOR", "BASE_SHOES",
												"EYE_COLOR", "BASE_EYE_COLOR",
												"EYE_TYPE", "BASE_EYE",
												"FACIAL_HAIR", "BASE_FACIAL_HAIR",
												"SKIN_COLOR", "BASE_SKIN_COLOR");
		
		SaveData inter = in.toSaveData();
		
		for(Entry<String, String> e : upgrades.entrySet()) {
			if(in.hasLoadDataByName(e.getKey())) {						
				String current_value = in.getUnsafeString(e.getKey());
				inter.removeSaveDataByName(e.getKey());
				inter.addUnsafeString(e.getValue(), current_value.replace("\\",""));
			}							
		}
		
		try {
			for(BodyPart p : raceParts.getConstructor().newInstance().getBodyParts()) {
				if(!in.hasLoadDataByName(p.getPartName())) {								
					inter.addByte(p.getPartName(), (byte) 0);
					if(p.numColors()>0) {
						inter.addByte(p.getPartColorName(), (byte) 0);
					}
				}
			}
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
				| NoSuchMethodException | SecurityException e1) {
			DebugHelper.handleDebugMessage("FAILED TO UPGRADE SAVE DATA FOR "+in.getSafeString("name", "unknown")+":" + e1.getMessage(), 20, MESSAGE_TYPE.ERROR);
			return in;
		}						
		
		return inter.toLoadData();
	}
	public static void run() {
		
		if(!GlobalData.isServer()) {
			DebugHelper.handleDebugMessage("Adding missing look features...", 60, MESSAGE_TYPE.DEBUG);
			
			File[] files = (new File(CharacterSave.getCharacterSavesPath())).listFiles();
			if (files == null) {
				files = new File[0];
			}
	
			for(File f : files) {
				try {
					LoadData cLoad = new LoadData(f);					
					LoadData pLoad = cLoad.getFirstLoadDataByName("PLAYER");
					
					if(!pLoad.hasLoadDataByName("LOOK")) {
						DebugHelper.handleFormattedDebugMessage("MISSING look section in file %s...", 60, MESSAGE_TYPE.WARNING, new Object[] {f.getAbsolutePath()});
						continue;
					}
					LoadData lookInfo = pLoad.getFirstLoadDataByName("LOOK");		
					
					if(!lookInfo.getSafeString("race_id", "").equals("")) {		
						
						lookInfo = upgradeSaveData(lookInfo);
						SaveData pSave = pLoad.toSaveData();
						pSave.removeFirstSaveDataByName("LOOK");
						pSave.addSaveData(lookInfo.toSaveData());
						
						SaveData cSave = cLoad.toSaveData();
						cSave.removeFirstSaveDataByName("PLAYER");
						cSave.addSaveData(pSave);						
						cSave.saveScript(f);
					}
					
				}
				catch(Exception e) {				
					e.printStackTrace();	
				}
				
			}
			
			
			 WorldSaveEditor.processPlayerSaves(file -> {
				 
				 try {
						LoadData cLoad = new LoadData(file);					
						LoadData pLoad = cLoad.getFirstLoadDataByName("MOB");
						
						if(!pLoad.hasLoadDataByName("LOOK")) {
							DebugHelper.handleFormattedDebugMessage("MISSING look section in file %s...", 60, MESSAGE_TYPE.WARNING, new Object[] {file.getAbsolutePath()});
							return;
						}
						LoadData lookInfo = pLoad.getFirstLoadDataByName("LOOK");				
						if(lookInfo.getSafeString("race_id", "").equals(TestFurryRaceLook.TEST_FURRY_RACE_ID)) {	
							
							lookInfo = upgradeSaveData(lookInfo);						
							SaveData pSave = pLoad.toSaveData();
							pSave.removeFirstSaveDataByName("LOOK");
							pSave.addSaveData(lookInfo.toSaveData());
							
							SaveData cSave = cLoad.toSaveData();
							cSave.removeFirstSaveDataByName("MOB");
							cSave.addSaveData(pSave);						
							cSave.saveScript(file);
						}
						
					}
					catch(Exception e) {				
						e.printStackTrace();	
					}				 	
		        });
		}
	}
}
