package me.myogoo.myotus.impl.network;

import me.myogoo.myotus.Myotus;
import me.myogoo.myotus.api.network.IMyotusPacket;
import me.myogoo.myotus.api.network.IMyotusNetwork;
import me.myogoo.myotus.api.network.MyoPacketContext;
import me.myogoo.myotus.api.network.MyoPacketDecoder;
import me.myogoo.myotus.api.network.MyoPacketHandler;
import me.myogoo.myotus.util.MyoLogger;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public enum MyotusNetwork implements IMyotusNetwork {
    INSTANCE;

    private static final String PROTOCOL_VERSION = "1";

    private final SimpleChannel channel = NetworkRegistry.newSimpleChannel(
            Myotus.makeId("network"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals);

    private final Map<Integer, PacketRegistration<?>> packetsById = new HashMap<>();
    private final Map<Class<? extends IMyotusPacket>, Integer> idsByType = new HashMap<>();

    @Override
    public synchronized <T extends IMyotusPacket> IMyotusNetwork registerServerbound(int packetId, Class<T> packetType,
            MyoPacketDecoder<T> decoder, MyoPacketHandler<T> handler) {
        register(packetId, packetType, decoder, handler, NetworkDirection.PLAY_TO_SERVER);
        return this;
    }

    @Override
    public synchronized <T extends IMyotusPacket> IMyotusNetwork registerClientbound(int packetId, Class<T> packetType,
            MyoPacketDecoder<T> decoder, MyoPacketHandler<T> handler) {
        register(packetId, packetType, decoder, handler, NetworkDirection.PLAY_TO_CLIENT);
        return this;
    }

    private <T extends IMyotusPacket> void register(int packetId, Class<T> packetType, MyoPacketDecoder<T> decoder,
            MyoPacketHandler<T> handler, NetworkDirection direction) {
        Objects.requireNonNull(packetType, "packetType");
        Objects.requireNonNull(decoder, "decoder");
        Objects.requireNonNull(handler, "handler");

        if (packetId < MIN_PACKET_ID) {
            throw new IllegalArgumentException(
                    "Custom packet id must be >= " + MIN_PACKET_ID + ": " + packetId);
        }

        if (idsByType.containsKey(packetType)) {
            throw new IllegalStateException("Packet type is already registered: " + packetType.getName());
        }

        PacketRegistration<?> existingPacket = packetsById.get(packetId);
        if (existingPacket != null) {
            throw new IllegalStateException("Packet id already registered: " + packetId);
        }

        channel.messageBuilder(packetType, packetId, direction)
                .encoder((message, payload) -> message.write(payload))
                .decoder(decoder::decode)
                .consumerMainThread((message, contextSupplier) -> {
                    var forgeContext = contextSupplier.get();
                    var context = new MyoPacketContext(forgeContext, replyPacket -> channel.reply(replyPacket, forgeContext));
                    handler.handle(message, context);
                })
                .add();

        packetsById.put(packetId, new PacketRegistration<>(packetType, direction));
        idsByType.put(packetType, packetId);
        MyoLogger.debug("Registered Myotus {} packet {} with id {}", direction, packetType.getName(), packetId);
    }

    @Override
    public synchronized int getPacketId(Class<? extends IMyotusPacket> packetType) {
        Integer packetId = idsByType.get(packetType);
        if (packetId == null) {
            throw new IllegalStateException("Packet type is not registered: " + packetType.getName());
        }
        return packetId;
    }

    @Override
    public void sendToServer(IMyotusPacket packet) {
        channel.sendToServer(Objects.requireNonNull(packet, "packet"));
    }

    @Override
    public void sendToPlayer(ServerPlayer player, IMyotusPacket packet) {
        ServerPlayer recipient = Objects.requireNonNull(player, "player");
        channel.send(PacketDistributor.PLAYER.with(() -> recipient), Objects.requireNonNull(packet, "packet"));
    }

    @Override
    public void sendToAllClients(IMyotusPacket packet) {
        channel.send(PacketDistributor.ALL.noArg(), Objects.requireNonNull(packet, "packet"));
    }

    @Override
    public void reply(IMyotusPacket packet, MyoPacketContext context) {
        Objects.requireNonNull(context, "context").reply(Objects.requireNonNull(packet, "packet"));
    }

    private record PacketRegistration<T extends IMyotusPacket>(Class<T> packetType, NetworkDirection direction) {
    }
}
