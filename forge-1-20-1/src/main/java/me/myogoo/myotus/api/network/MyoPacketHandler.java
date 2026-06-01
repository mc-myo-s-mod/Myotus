package me.myogoo.myotus.api.network;

@FunctionalInterface
public interface MyoPacketHandler<T extends IMyotusPacket> {
    void handle(T packet, MyoPacketContext context);
}
