package me.myogoo.myotus.api.network;

import net.minecraft.network.FriendlyByteBuf;

/**
 * Base contract for packets that travel through the shared Myotus network channel.
 */
public interface IMyotusPacket {

    /**
     * Writes this packet to the network payload buffer.
     *
     * @param payload payload buffer
     */
    void write(FriendlyByteBuf payload);
}
