package me.myogoo.myotus.api.network;

import net.minecraft.network.Connection;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.function.Consumer;

/**
 * Main-thread packet handling context exposed to Myotus packet handlers.
 */
public final class MyoPacketContext {
    private final NetworkEvent.Context forgeContext;
    private final Consumer<IMyotusPacket> replier;

    public MyoPacketContext(NetworkEvent.Context forgeContext, Consumer<IMyotusPacket> replier) {
        this.forgeContext = Objects.requireNonNull(forgeContext, "forgeContext");
        this.replier = Objects.requireNonNull(replier, "replier");
    }

    /**
     * Returns the network direction that delivered the packet.
     *
     * @return packet direction
     */
    public NetworkDirection direction() {
        return forgeContext.getDirection();
    }

    /**
     * Returns the sending player for serverbound packets, or {@code null} for clientbound packets.
     *
     * @return packet sender if available
     */
    @Nullable
    public ServerPlayer sender() {
        return forgeContext.getSender();
    }

    /**
     * Returns the underlying network connection.
     *
     * @return active connection
     */
    public Connection connection() {
        return forgeContext.getNetworkManager();
    }

    /**
     * Schedules work on the logical main thread for this packet direction.
     *
     * @param work task to run
     */
    public void enqueueWork(Runnable work) {
        forgeContext.enqueueWork(work);
    }

    /**
     * Sends a reply packet back to the origin of this packet.
     *
     * @param packet reply packet
     */
    public void reply(IMyotusPacket packet) {
        replier.accept(Objects.requireNonNull(packet, "packet"));
    }
}
