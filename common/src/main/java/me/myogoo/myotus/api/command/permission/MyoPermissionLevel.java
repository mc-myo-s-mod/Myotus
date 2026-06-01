package me.myogoo.myotus.api.command.permission;

public enum MyoPermissionLevel {
    NONE(-1),
    MODERATOR(1),
    GAME_MASTER(2),
    ADMIN(3),
    OWNER(4);

    private final int commandPermissionLevel;

    MyoPermissionLevel(int commandPermissionLevel) {
        this.commandPermissionLevel = commandPermissionLevel;
    }

    public int getCommandPermissionLevel() {
        return commandPermissionLevel;
    }
}
