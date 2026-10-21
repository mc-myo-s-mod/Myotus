package me.myogoo.myotus.gametest;

import me.myogoo.myotus.Myotus;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(Myotus.MODID)
@PrefixGameTestTemplate(false)
public final class CommandGameTests {
    private CommandGameTests() { }

    @GameTest(template = "empty", timeoutTicks = 20)
    public static void ssecRegistersMyotusCommandsAndRetainsAdminPermission(GameTestHelper helper) throws Exception {
        var server = helper.getLevel().getServer();
        var dispatcher = server.getCommands().getDispatcher();
        var source = server.createCommandSourceStack().withPermission(0).withSuppressedOutput();
        helper.assertTrue(dispatcher.execute("myotus active_mod", source) == 1, "SSEC active_mod must execute");
        helper.assertTrue(dispatcher.execute("myotus loaded_mod", source) == 1, "SSEC loaded_mod must execute");
        boolean before = Myotus.DEV_MODE;
        helper.assertTrue(dispatcher.execute("myotus dev", source) == 0, "Non-admin must be denied");
        helper.assertTrue(Myotus.DEV_MODE == before, "Denied dev command must not change state");
        try {
            helper.assertTrue(dispatcher.execute("myotus dev", source.withPermission(3)) == 1, "Admin command must execute");
            helper.assertTrue(Myotus.DEV_MODE != before, "Admin command must toggle dev mode");
        } finally {
            Myotus.DEV_MODE = before;
        }
        helper.succeed();
    }
}
