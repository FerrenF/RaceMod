package versioning;

import java.io.File;
import java.util.ArrayList;

import core.race.TestFurryRaceLook;
import core.race.parts.BodyPart;
import helpers.DebugHelper;
import helpers.DebugHelper.MESSAGE_TYPE;
import necesse.engine.GlobalData;
import necesse.engine.save.CharacterSave;
import necesse.engine.save.LoadData;
import necesse.engine.save.SaveData;
import core.race.parts.TestFurryRaceParts;
public class Version0_0_16 {

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
					LoadData lookInfo = pLoad.getFirstLoadDataByName("LOOK")	;				
					if(lookInfo.getSafeString("race_id", "").equals(TestFurryRaceLook.TEST_FURRY_RACE_ID)) {					
						SaveData inter = lookInfo.toSaveData();
						for(BodyPart p : new TestFurryRaceParts().getCustomBodyParts()) {
							if(!lookInfo.hasLoadDataByName(p.getPartName())) {								
								inter.addByte(p.getPartName(), (byte) 0);
								if(p.numColors()) {
									inter.addByte(p.getPartColorName(), (byte) 0);
								}
							}
						}
						lookInfo = inter.toLoadData();
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
						LoadData lookInfo = pLoad.getFirstLoadDataByName("LOOK")	;				
						if(lookInfo.getSafeString("race_id", "").equals(TestFurryRaceLook.TEST_FURRY_RACE_ID)) {					
							SaveData inter = lookInfo.toSaveData();
							for(BodyPart p : new TestFurryRaceParts().getCustomBodyParts()) {
								if(!lookInfo.hasLoadDataByName(p.getPartName())) {								
									inter.addByte(p.getPartName(), (byte) 0);
									if(p.numColors()) {
										inter.addByte(p.getPartColorName(), (byte) 0);
									}
								}
							}
							lookInfo = inter.toLoadData();
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
