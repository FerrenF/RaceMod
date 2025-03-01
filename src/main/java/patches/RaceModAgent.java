package patches;

import java.lang.instrument.*;
import java.lang.reflect.Modifier;
import java.security.ProtectionDomain;

import extensions.RaceLook;
import necesse.entity.mobs.PlayerMob;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.dynamic.loading.ClassLoadingStrategy;
import net.bytebuddy.dynamic.loading.ClassReloadingStrategy;
import net.bytebuddy.implementation.FieldAccessor;

public class RaceModAgent {
    public static void premain(String agentArgs, Instrumentation inst) {
        // Register the ClassFileTransformer to intercept class loading
        inst.addTransformer(new PlayerMobTransformer(), true);
    }

    public static class PlayerMobTransformer implements ClassFileTransformer {
        @Override
        public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined,
                                ProtectionDomain protectionDomain, byte[] classfileBuffer) {
            if (className.equals("necesse/entity/mobs/PlayerMob")) {
                // Modify the PlayerMob class before it is loaded
                try {                	

                 // Manually create the field accessors for the new field
                	return new ByteBuddy()
                     .redefine(PlayerMob.class) // Redefine PlayerMob class again
                     .defineField("look", RaceLook.class, Modifier.PUBLIC) // Redefine 'look' field type
                     .implement(FieldAccessor.class) // Implement FieldAccessor for the 'look' field
                     .make()
                     .getBytes();
                	 
                   // return new ByteBuddy()
                   //         .redefine(PlayerMob.class)  // Redefine the class
                   //         .defineField("look", RaceLook.class, Modifier.PUBLIC)  // Add 'look' field
                   //         .visit(FieldAccessor.ofField("look"))  // Ensure getter/setter accessors
                  //          .make()
                  //          .getBytes();  // Return the modified bytecode
                    
                    
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return null;  // No changes to other classes
        }
    }
}
