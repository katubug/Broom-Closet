package heykatu.broom_closet.network;

import heykatu.broom_closet.BroomCloset;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.InteractionHand;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

@EventBusSubscriber(modid = BroomCloset.MODID)
public final class BroomClosetNetwork {
    static final StreamCodec<ByteBuf, InteractionHand> HAND_STREAM_CODEC =
            ByteBufCodecs.BOOL.map(main -> main ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND, hand -> hand == InteractionHand.MAIN_HAND);

    private BroomClosetNetwork() {}

    @SubscribeEvent
    public static void register(RegisterPayloadHandlersEvent event) {
        event.registrar("1")
                .playToClient(OpenSoulIslandPickerPayload.TYPE, OpenSoulIslandPickerPayload.STREAM_CODEC, OpenSoulIslandPickerPayload::handle)
                .playToServer(ChooseSoulIslandPayload.TYPE, ChooseSoulIslandPayload.STREAM_CODEC, ChooseSoulIslandPayload::handle);
    }
}
