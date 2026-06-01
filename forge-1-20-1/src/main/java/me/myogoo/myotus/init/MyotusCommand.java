package me.myogoo.myotus.init;

import me.myogoo.myotus.api.annotation.commands.MyoCommand;
import me.myogoo.myotus.command.MyoCommandRegistrar;
import me.myogoo.myotus.util.reflect.annotation.AnnotationScanner;
import me.myogoo.myotus.util.MyoLogger;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import org.objectweb.asm.Type;

import java.util.ArrayList;
import java.util.List;

@EventBusSubscriber(modid = me.myogoo.myotus.Myotus.MODID)
public class MyotusCommand {
    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        MyoLogger.info("Scanning for Myotus commands...");

        List<Class<?>> commandClasses = new ArrayList<>();
        Type myoCommandType = Type.getType(MyoCommand.class);

        for (AnnotationScanner.ScannedAnnotation annotation : AnnotationScanner.find(myoCommandType)) {
            try {
                Class<?> clazz = Class.forName(annotation.className());
                commandClasses.add(clazz);
            } catch (ClassNotFoundException e) {
                MyoLogger.error("Failed to load command class: {}", annotation.className(), e);
            }
        }

        MyoCommandRegistrar.registerAll(event.getDispatcher(), commandClasses, me.myogoo.myotus.Myotus.DEV_MODE);
    }
}
