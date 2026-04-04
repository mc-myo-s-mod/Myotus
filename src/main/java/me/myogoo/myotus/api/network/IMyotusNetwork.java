package me.myogoo.myotus.api.network;

import net.minecraft.server.level.ServerPlayer;

/**
 * Shared networking facade for custom packets that travel through the Myotus packet channel.
 *
 * <p>Packets registered here are encoded and delivered through Myotus' own Forge
 * {@code SimpleChannel}, so add-on mods do not need to depend on AE2 networking.</p>
 */
public interface IMyotusNetwork {
    int MIN_PACKET_ID = 0;

    /**
     * Registers a packet that may only be sent from the client to the server.
     *
     * @param packetId unique packet id, must be at least {@link #MIN_PACKET_ID}
     * @param packetType packet implementation class
     * @param decoder decoder used to reconstruct packets from the network payload
     * @param handler main-thread packet handler
     * @return {@code this} for chaining
     */
    <T extends IMyotusPacket> IMyotusNetwork registerServerbound(int packetId, Class<T> packetType,
            MyoPacketDecoder<T> decoder, MyoPacketHandler<T> handler);

    /**
     * Registers a packet that may only be sent from the server to the client.
     *
     * @param packetId unique packet id, must be at least {@link #MIN_PACKET_ID}
     * @param packetType packet implementation class
     * @param decoder decoder used to reconstruct packets from the network payload
     * @param handler main-thread packet handler
     * @return {@code this} for chaining
     */
    <T extends IMyotusPacket> IMyotusNetwork registerClientbound(int packetId, Class<T> packetType,
            MyoPacketDecoder<T> decoder, MyoPacketHandler<T> handler);

    /**
     * Returns the packet id registered for the given type.
     *
     * @param packetType packet implementation class
     * @return registered packet id
     */
    int getPacketId(Class<? extends IMyotusPacket> packetType);

    /**
     * Sends a packet to the server.
     *
     * @param packet packet to send
     */
    void sendToServer(IMyotusPacket packet);

    /**
     * Sends a packet to a specific player.
     *
     * @param player recipient
     * @param packet packet to send
     */
    void sendToPlayer(ServerPlayer player, IMyotusPacket packet);

    /**
     * Broadcasts a packet to all connected clients.
     *
     * @param packet packet to broadcast
     */
    void sendToAllClients(IMyotusPacket packet);

    /**
     * Sends a reply packet back to the origin of the supplied handling context.
     *
     * @param packet packet to send
     * @param context packet handling context
     */
    void reply(IMyotusPacket packet, MyoPacketContext context);
}
