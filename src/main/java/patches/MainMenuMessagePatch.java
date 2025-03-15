package patches;

import java.awt.Color;
import core.RaceMod;
import necesse.engine.util.GameMath;
import necesse.engine.window.GameWindow;
import necesse.gfx.GameBackground;
import necesse.gfx.forms.MainMenuFormManager;
import necesse.gfx.forms.components.FormContentBox;
import necesse.gfx.forms.components.FormFlow;
import necesse.gfx.forms.components.FormLabel;
import necesse.gfx.gameFont.FontOptions;
import net.bytebuddy.asm.Advice;

public class MainMenuMessagePatch {
	@Advice.OnMethodExit
    public static void onExit(@Advice.This MainMenuFormManager thf) {	      
		
		GameWindow w = necesse.engine.window.WindowManager.getWindow();
		FormContentBox wrapper = new FormContentBox(w.getHudWidth()-375, w.getHudHeight()-100, 350, 50, GameBackground.itemTooltip);
		FormFlow flow = new FormFlow(0);
		
		FormLabel row1 = new FormLabel(String.format("Race Mod %s",RaceMod.VERSION_STRING),
				new FontOptions(16).shadow(Color.BLACK, 1, -1).color(Color.WHITE),wrapper.getWidth()/2,0,0);
		FormLabel row2 = new FormLabel(String.format("Characters loaded from %s",RaceMod.characterSavePath),
				new FontOptions(10).shadow(Color.BLACK, 1, -1).color(Color.WHITE),wrapper.getWidth()/2,0,0);
		
		wrapper.addComponent(
				(FormLabel)flow.nextY((row1),2));
		wrapper.addComponent(
				(FormLabel)flow.nextY((row2),2));
		
		int nw = GameMath.max(row1.getBoundingBox().width, row2.getBoundingBox().width);
		wrapper.setWidth((int)Math.round(nw*1.1));
		wrapper.setX(w.getHudWidth() - (int)Math.round(nw*1.25));
		thf.addComponent(wrapper);
		
		
		
    }
}
