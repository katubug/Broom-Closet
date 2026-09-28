package heykatu.broom_closet.network;

import heykatu.broom_closet.BroomCloset;
import heykatu.broom_closet.soulhome.client.SoulIslandPickerOpener;
import heykatu.broom_closet.soulhome.island.SoulIslandOption;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.List;

// Server -> client: show the soul island picker. hand is echoed back on Select so the server
// knows which key to use for the teleport.
public record OpenSoulIslandPickerPayload(List<SoulIslandOption.Entry> entries, InteractionHand hand) implements CustomPacketPayload {
    public static final Type<OpenSoulIslandPickerPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(BroomCloset.MODID, "open_soul_island_picker"));

    public static final StreamCodec<RegistryFriendlyByteBuf, OpenSoulIslandPickerPayload> STREAM_CODEC = StreamCodec.composite(
            SoulIslandOption.Entry.LIST_STREAM_CODEC, OpenSoulIslandPickerPayload::entries,
            BroomClosetNetwork.HAND_STREAM_CODEC, OpenSoulIslandPickerPayload::hand,
            OpenSoulIslandPickerPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    // Only runs on the client
    static void handle(OpenSoulIslandPickerPayload payload, IPayloadContext context) {
        SoulIslandPickerOpener.open(payload.entries(), payload.hand());
    }
}
