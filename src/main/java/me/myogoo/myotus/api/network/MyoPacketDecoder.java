package me.myogoo.myotus.api.network;

import net.minecraft.network.FriendlyByteBuf;

@FunctionalInterface
public interface MyoPacketDecoder<T extends IMyotusPacket> {
    T decode(FriendlyByteBuf payload);
}
