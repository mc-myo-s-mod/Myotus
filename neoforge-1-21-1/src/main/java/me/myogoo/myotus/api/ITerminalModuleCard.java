package me.myogoo.myotus.api;

/**
 * A terminal card that adds a slot or GUI module, distinct from an upgrade card.
 * Register native slots through {@link MyotusAPI#terminalModules()} and optional client widgets
 * through {@link MyotusAPI.Client#registerTerminalModule}. Both registrations are explicit;
 * implementing this interface alone only makes the item installable in a terminal card slot.
 */
public interface ITerminalModuleCard extends ITerminalCard {
}
