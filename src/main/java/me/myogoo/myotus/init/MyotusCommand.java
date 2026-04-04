package me.myogoo.myotus.init;

import me.myogoo.myotus.api.command.MyoCommand;
import me.myogoo.myotus.commands.CommandRegistrar;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.forgespi.language.ModFileScanData;
import net.minecraftforge.forgespi.language.ModFileScanData.AnnotationData;
import org.objectweb.asm.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

@EventBusSubscriber(modid = me.myogoo.myotus.Myotus.MODID)
public class MyotusCommand {

    private static final Logger LOGGER = LoggerFactory.getLogger(MyotusCommand.class);

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        LOGGER.info("Scanning for Myotus commands...");

        List<Class<?>> commandClasses = new ArrayList<>();
        Type myoCommandType = Type.getType(MyoCommand.class);

        // Find all classes annotated with @MyoCommand
        for (ModFileScanData scanData : ModList.get().getAllScanData()) {
            for (AnnotationData annotation : scanData.getAnnotations()) {
                if (annotation.annotationType().equals(myoCommandType)) {
                    try {
                        Class<?> clazz = Class.forName(annotation.clazz().getClassName());
                        commandClasses.add(clazz);
                    } catch (ClassNotFoundException e) {
                        LOGGER.error("Failed to load command class: {}", annotation.clazz().getClassName(), e);
                    }
                }
            }
        }

        // Register root commands (those whose parent is void.class)
        for (Class<?> clazz : commandClasses) {
            MyoCommand cmdAnnotation = clazz.getAnnotation(MyoCommand.class);
            if (cmdAnnotation != null && cmdAnnotation.parent() == void.class) {
                CommandRegistrar.register(event.getDispatcher(), clazz, commandClasses);
            }
        }
    }
}
