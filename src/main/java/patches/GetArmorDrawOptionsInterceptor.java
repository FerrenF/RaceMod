package patches;

import net.bytebuddy.asm.Advice;
import net.bytebuddy.asm.Advice.This;
import overrides.CustomPlayerMob;
import necesse.gfx.drawOptions.DrawOptions;
import necesse.gfx.drawOptions.human.HumanDrawOptions;
import necesse.gfx.shader.ShaderState;
import necesse.engine.network.server.ServerClient;
import necesse.engine.util.GameUtils;
import necesse.entity.mobs.PlayerMob;

import java.lang.reflect.Field;

public class GetArmorDrawOptionsInterceptor {

    @Advice.OnMethodEnter
    public static void modifyParameters(
        @Advice.This(optional = true) Object instance,  // Access 'this' if it's an instance method
        @Advice.Argument(0) boolean isAttacking,
        @Advice.Argument(1) int mobDir,
        @Advice.Argument(2) DrawOptions behind,
        @Advice.Argument(3) DrawOptions head,
        @Advice.Argument(4) DrawOptions eyelids,
        @Advice.Argument(5) DrawOptions headBackArmor,
        @Advice.Argument(6) DrawOptions headArmor,
        @Advice.Argument(7) DrawOptions headFrontArmor,
        @Advice.Argument(8) DrawOptions chest,
        @Advice.Argument(9) DrawOptions chestBackArmor,
        @Advice.Argument(10) DrawOptions chestArmor,
        @Advice.Argument(11) DrawOptions chestFrontArmor,
        @Advice.Argument(12) DrawOptions feet,
        @Advice.Argument(13) DrawOptions feetBackArmor,
        @Advice.Argument(14) DrawOptions feetArmor,
        @Advice.Argument(15) DrawOptions feetFrontArmor,
        @Advice.Argument(16) DrawOptions leftArms,
        @Advice.Argument(17) DrawOptions frontLeftArms,
        @Advice.Argument(18) DrawOptions rightArms,
        @Advice.Argument(19) DrawOptions frontRightArms,
        @Advice.Argument(20) DrawOptions holdItem,
        @Advice.Argument(21) boolean holdItemInFrontOfArms,
        @Advice.Argument(22) DrawOptions onBody,
        @Advice.Argument(23) DrawOptions top,
        @Advice.Argument(24) float alpha,
        @Advice.Argument(25) float angle,
        @Advice.Argument(26) int rotationMidX,
        @Advice.Argument(27) int rotationMidY,
        @Advice.Argument(28) ShaderState shader,
        @Advice.Argument(29) boolean forcedBufferDraw
    ) {
      
        if (instance instanceof HumanDrawOptions) {
            HumanDrawOptions inst = (HumanDrawOptions) instance;

            // Access the private "player" field
            PlayerMob player = getPlayerFromInstance(inst);
            if (player == null) {
                return;
            }
            
            // Check if the player's secondType is "CUSTOM"
            if ("CUSTOM".equals(player.secondType)) {

                if (player instanceof CustomPlayerMob) {
                    CustomPlayerMob customPlayer = (CustomPlayerMob) player;

                    customPlayer.onDrawOptionsInterception(isAttacking,
                    		mobDir, behind, head, eyelids, headBackArmor,
                    		headArmor, headFrontArmor, chest, chestBackArmor,
                    		chestArmor, chestFrontArmor, feet, feetBackArmor,
                    		feetArmor, feetFrontArmor, leftArms, frontLeftArms, rightArms,
                    		frontRightArms, holdItem, holdItemInFrontOfArms, onBody,
                    		top, alpha, angle, rotationMidX, rotationMidY, shader,
                    		forcedBufferDraw);
                }
            }
        }
    }

    /**
     * Uses reflection to access the private "player" field in HumanDrawOptions.
     */
    private static PlayerMob getPlayerFromInstance(HumanDrawOptions inst) {
        try {
            Field playerField = HumanDrawOptions.class.getDeclaredField("player");
            playerField.setAccessible(true);
            return (PlayerMob) playerField.get(inst);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
            return null;
        }
    }
   
}
