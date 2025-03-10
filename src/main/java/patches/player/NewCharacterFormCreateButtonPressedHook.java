import net.bytebuddy.asm.Advice.This;
import net.bytebuddy.implementation.bind.annotation.Argument;
import net.bytebuddy.implementation.bind.annotation.SuperCall;
import overrides.NewCharacterForm;
import necesse.entity.mobs.PlayerMob;

import java.util.concurrent.Callable;

public class NewCharacterFormCreateButtonPressedHook {
    public static void intercept(@This Object instance, @Argument(0) PlayerMob player, @SuperCall Callable<Void> original) {
        System.out.println("Intercepted onCreatePressed! Player: " + player.playerName);
        if (instance instanceof NewCharacterForm) {
        	NewCharacterForm form = (NewCharacterForm) instance;
        	form.initSavePlayer();
            System.out.println("Intercepted inside CharacterSelectForm!");
        }
        
        try {
            original.call(); // Call the original method
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}