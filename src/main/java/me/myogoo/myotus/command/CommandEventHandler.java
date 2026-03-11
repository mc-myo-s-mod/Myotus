package me.myogoo.myotus.command;

import me.myogoo.myotus.api.command.MyoCommand;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforgespi.language.ModFileScanData;
import net.neoforged.neoforgespi.language.ModFileScanData.AnnotationData;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@EventBusSubscriber(modid = me.myogoo.myotus.Myotus.MODID)
public class CommandEventHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(CommandEventHandler.class);

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        LOGGER.info("Scanning for Myotus commands...");

        List<Class<?>> commandClasses = new ArrayList<>();
        org.objectweb.asm.Type myoCommandType = org.objectweb.asm.Type.getType(MyoCommand.class);

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
