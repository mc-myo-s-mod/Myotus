package me.myogoo.myotus.gametest;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.myogoo.myotus.Myotus;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.permissions.PermissionSet;

public final class CommandGameTests {
    private CommandGameTests() { }

    public static void ssecRegistersMyotusCommandsAndRetainsAdminPermission(GameTestHelper helper) {
        var server = helper.getLevel().getServer();
        var dispatcher = server.getCommands().getDispatcher();
        var source = server.createCommandSourceStack().withPermission(PermissionSet.NO_PERMISSIONS).withSuppressedOutput();
        boolean before = Myotus.DEV_MODE;
        try {
            helper.assertTrue(dispatcher.execute("myotus active_mod", source) == 1, "SSEC active_mod must execute");
            helper.assertTrue(dispatcher.execute("myotus loaded_mod", source) == 1, "SSEC loaded_mod must execute");
            helper.assertTrue(dispatcher.execute("myotus dev", source) == 0, "Non-admin must be denied");
            helper.assertTrue(Myotus.DEV_MODE == before, "Denied dev command must not change state");
            helper.assertTrue(dispatcher.execute("myotus dev", source.withPermission(PermissionSet.ALL_PERMISSIONS)) == 1,
                    "Admin command must execute");
            helper.assertTrue(Myotus.DEV_MODE != before, "Admin command must toggle dev mode");
        } catch (CommandSyntaxException e) {
            helper.fail("SSEC command execution failed: " + e.getMessage());
        } finally {
            Myotus.DEV_MODE = before;
        }
        helper.succeed();
    }
}
