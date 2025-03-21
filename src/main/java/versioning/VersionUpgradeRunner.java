package versioning;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import helpers.DebugHelper;
import helpers.DebugHelper.MESSAGE_TYPE;
import necesse.engine.GlobalData;
import necesse.engine.modLoader.LoadedMod;

public class VersionUpgradeRunner {

	public static Map<String, Object> parseVersion(String versionString) {
	    Map<String, Object> result = new HashMap<>();
	    
	    String[] parts = versionString.split(" ");
	    if (parts.length < 2) {
	        throw new IllegalArgumentException("Invalid version format");
	    }

	    String[] versionNumbers = parts[0].split("\\.");
	    if (versionNumbers.length != 3) {
	        throw new IllegalArgumentException("Version number must have three parts: major.minor.patch");
	    }

	    try {
	        result.put("major", Integer.parseInt(versionNumbers[0]));
	        result.put("minor", Integer.parseInt(versionNumbers[1]));
	        result.put("patch", Integer.parseInt(versionNumbers[2]));
	    } catch (NumberFormatException e) {
	        throw new IllegalArgumentException("Version numbers must be integers", e);
	    }

	    result.put("releaseType", parts[1]); // ALPHA, BETA, RELEASE, etc.

	    return result;
	}

	public static List<String> getVersionSequence(String fromVersion, String toVersion) {
	    List<String> versionList = new ArrayList<>();

	    Map<String, Object> fromParsed = parseVersion(fromVersion);
	    Map<String, Object> toParsed = parseVersion(toVersion);

	    int fromMajor = (int) fromParsed.get("major");
	    int fromMinor = (int) fromParsed.get("minor");
	    int fromPatch = (int) fromParsed.get("patch");

	    int toMajor = (int) toParsed.get("major");
	    int toMinor = (int) toParsed.get("minor");
	    int toPatch = (int) toParsed.get("patch");

	    if (!isVersionLessThan(fromMajor, fromMinor, fromPatch, toMajor, toMinor, toPatch)) {
	        throw new IllegalArgumentException("fromVersion must be less than toVersion");
	    }

	    while (fromMajor < toMajor || fromMinor < toMinor || fromPatch < toPatch) {
	        if (fromPatch < 99) { 
	            fromPatch++;
	        } else {
	            fromPatch = 0;
	            if (fromMinor < 99) { 
	                fromMinor++;
	            } else {
	                fromMinor = 0;
	                fromMajor++;
	            }
	        }
	        versionList.add(fromMajor + "." + fromMinor + "." + fromPatch);
	    }

	    return versionList;
	}

	private static boolean isVersionLessThan(int major1, int minor1, int patch1, int major2, int minor2, int patch2) {
	    if (major1 != major2) return major1 < major2;
	    if (minor1 != minor2) return minor1 < minor2;
	    return patch1 < patch2;
	}
	
    public static void runUpgradeScripts(String fromVersion, String toVersion) {
        List<String> versions = getVersionSequence(fromVersion, toVersion);
        DebugHelper.handleDebugMessage("Looking for scripts for: "+versions.toString(), 60, MESSAGE_TYPE.DEBUG);
        for (String version : versions) {
            String className = UPGRADE_PACKAGE + ".Version" + version.replace('.', '_');
            try {
                Class<?> upgradeClass = findAndLoadClass(className);
                if (upgradeClass != null) {
                    DebugHelper.handleDebugMessage("Running upgrade script: " + className, 25, MESSAGE_TYPE.INFO);
                    upgradeClass.getMethod("run").invoke(null);
                }
                else {
                	DebugHelper.handleDebugMessage("No upgrade script found for: " + className, 50, MESSAGE_TYPE.DEBUG);
                }
            } catch (Exception e) {
            	DebugHelper.handleDebugMessage("No upgrade script found for: " + className, 50, MESSAGE_TYPE.DEBUG);
            }
        }
    }
    
    private static final String MOD_PATH = LoadedMod.getRunningMod().loadLocation.path.getAbsolutePath(); 
    private static final String UPGRADE_PACKAGE = "versioning";

    private static Class<?> findAndLoadClass(String className) {
        File jarFile = new File(MOD_PATH);
        if (!jarFile.exists()) {
            throw new IllegalArgumentException("JAR file not found: " + MOD_PATH);
        }

        try (JarFile jar = new JarFile(jarFile);
             URLClassLoader classLoader = new URLClassLoader(new URL[]{jarFile.toURI().toURL()}, VersionUpgradeRunner.class.getClassLoader())) {
            
            Enumeration<JarEntry> entries = jar.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                String entryName = entry.getName().replace('/', '.'); // Convert path to class format

                if (entryName.endsWith(".class") && entryName.equals(className + ".class")) {
                    return Class.forName(className, true, classLoader);
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            return null; // Class not found, return null
        }

        return null; // No matching class found
    }
}

