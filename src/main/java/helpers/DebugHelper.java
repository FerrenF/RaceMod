package helpers;

import java.util.function.Consumer;

import core.RaceMod;
import necesse.engine.GameLog;

public class DebugHelper{
	public static enum MESSAGE_TYPE{
		ERROR,
		WARNING,
		INFO,
		DEBUG
	}
	public static int DEFAULT_DEBUG_LEVEL = 0;
	public static int RACE_MOD_DEBUG_LEVEL = 0;
	public static int getDebugLevel() {
		int debug_level = 0;
		String getDbg = RaceMod.settings.getSettingsString("DEBUG", "debug_level");
		try {
			debug_level = Integer.parseInt(getDbg);
		} catch (Exception e){}
		return debug_level;
	}
	
	public static void initialize() {
		RACE_MOD_DEBUG_LEVEL = getDebugLevel();
	}
	
	public static void handleDebugMessage(String msg, int messageLevel) {
		handleDebugMessage(msg, messageLevel, MESSAGE_TYPE.INFO);
	}
	
	public static void handleFormattedDebugMessage(String msg, int messageLevel, MESSAGE_TYPE msgType, Object[] args) {
		handleDebugMessage(String.format(msg, args), messageLevel, msgType);
	}
	
	public static void handleFormattedDebugMessage(String msg, int messageLevel, Object[] args) {
		handleDebugMessage(String.format(msg, args), messageLevel, MESSAGE_TYPE.INFO);
	}
	
	public static void handleDebugMessage(String msg, int messageLevel, Consumer<String> msgConsumer) {
		if(messageLevel <= RACE_MOD_DEBUG_LEVEL) msgConsumer.accept(msg);
	}
	
	public static void handleDebugMessage(String msg, int messageLevel, MESSAGE_TYPE msgType) {
		switch(msgType) {
		case ERROR:
			handleDebugMessage(msg, messageLevel, (m)->{
				GameLog.err.println(m);
			});
			break;
		case INFO:
			handleDebugMessage(msg, messageLevel, (m)->{
				GameLog.out.println(m);
			});
			break;
		case DEBUG:
			handleDebugMessage(msg, messageLevel, (m)->{
				GameLog.debug.println(m);
			});
			break;
		case WARNING:
			handleDebugMessage(msg, messageLevel, (m)->{
				GameLog.warn.println(m);
			});
			break;
		default:
			break;
		
		}
	}

	public static void handleDebugMessage(String msg) {
		handleDebugMessage(msg, DEFAULT_DEBUG_LEVEL, MESSAGE_TYPE.INFO);		
	}
}