package helpers;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.zip.DataFormatException;

import necesse.engine.GlobalData;
import necesse.engine.save.LoadData;
import necesse.engine.save.SaveData;

public class SettingsHelper{
	
	public static String MOD_NAME = "RaceMod";
	public static String settingsLocation = GlobalData.cfgPath().replace('\\', '/')+ "mods/" + MOD_NAME+"/settings.cfg";
	public static SaveData loadedSettings;
	public static boolean settingsLoaded = false;
	
	public static void initialize() {
	    if (settingsLoaded) return;
	    
	    boolean init_empty = false;
	    File loadSettingsFile = new File(settingsLocation);
	    File parentDirectory = loadSettingsFile.getParentFile(); // Get the parent directory
	    
	    // Ensure parent directories exist before creating the file
	    if (parentDirectory != null && !parentDirectory.exists()) {
	        if (!parentDirectory.mkdirs()) {
	            System.err.println("Failed to create directories for settings file at " + settingsLocation);
	            return;
	        }
	    }

	    // Create the settings file if it does not exist
	    if (!loadSettingsFile.exists()) {
	        try {
	            if (loadSettingsFile.createNewFile()) {
	                init_empty = true;
	            }
	        } catch (IOException e) {
	            System.err.println("Failed to generate new settings file at " + settingsLocation);
	            e.printStackTrace();
	            return;
	        }
	    }

	    if (!init_empty) {
		    try {
		        loadedSettings = LoadData.newRaw(loadSettingsFile, false).toSaveData();
		        
		    } catch (IOException e) {
		        System.err.println("IOException loading settings file at " + settingsLocation);
		        e.printStackTrace();
		        return;
		    } catch (DataFormatException e) {
		        System.err.println("DataFormatException loading settings file at " + settingsLocation);
		        e.printStackTrace();
		        return;
		    }
	    }

	    settingsLoaded = true;

	    // If the file was newly created, initialize default settings
	    if (init_empty) {	  
	    	loadedSettings = new SaveData("RACE_MOD_SETTINGS");
	        init_default_settings();
	    }
	}
	
	public static void init_default_settings() {		
		setSettingsString("DEBUG", "debug_level", String.valueOf(DebugHelper.DEFAULT_DEBUG_LEVEL));
		save();
	}
	
	public static boolean reload() {
		if(!settingsLoaded) initialize();		
		try {
			loadedSettings = LoadData.newRaw(new File(settingsLocation), false).toSaveData();		
			return true;
		} catch (IOException e) {
			System.err.print("IOException while trying to reload settings file at "+ settingsLocation);
			e.printStackTrace();
			return false;
		} catch (DataFormatException e) {
			System.err.print("DataFormatException while trying to reload settings file at "+ settingsLocation);
			e.printStackTrace();
			return false;
		}	
	}
	
	public static void save() {
		if(!settingsLoaded) initialize();		
		try {
			loadedSettings.saveScriptRaw(new File(settingsLocation), false);
		} catch (IOException e) {			
			e.printStackTrace();
		}
	}
	
	public static void setSettingsString(String category, String setting, String value) {
	    if (!settingsLoaded) return;  
	    
	    LoadData stl = loadedSettings.toLoadData();
	    if(!stl.hasLoadDataByName(category)) addSettingsCategory(category);
	    SaveData l1 = stl.getFirstLoadDataByName(category).toSaveData();	    
	    loadedSettings.removeFirstSaveDataByName(category);
	    l1.addSafeString(setting, value);
	    loadedSettings.addSaveData(l1);
	    save();
	}

	
	public static String getSettingsString(String category, String setting) {
	    LoadData stl = loadedSettings.toLoadData();
	    if(!stl.hasLoadDataByName(category)) return null;
	    LoadData l1 = stl.getFirstLoadDataByName(category);
	    return l1.getSafeString(setting, null);
	}
	
	public static List<LoadData> getSettingsCategory(String category) {
		return loadedSettings.toLoadData().getLoadDataByName(category);
	}
	
	public static boolean addSettingsCategory(String category) {
		 if (!settingsLoaded) return false;		 		
		if(loadedSettings.toLoadData().hasLoadDataByName(category)) {
			return false;
		}
		else {
			loadedSettings.addSaveData(new SaveData(category));
			save();
			return true;
		}
	}
		
}