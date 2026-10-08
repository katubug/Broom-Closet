package heykatu.broom_closet.network;

import heykatu.broom_closet.BroomCloset;
import heykatu.broom_closet.Config;
import heykatu.broom_closet.soulhome.SoulHomeCompat;
import heykatu.broom_closet.soulhome.island.SoulIslandChoices;
import heykatu.broom_closet.soulhome.island.SoulIslandOption;
import heykatu.broom_closet.soulhome.island.SoulIslandOptions;
import heykatu.broom_closet.soulhome.island.SoulKeyInterceptor;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.UUID;

// Client -> server: the player confirmed an island in the picker.
public record ChooseSoulIslandPayload(ResourceLocation optionId, InteractionHand hand) implements CustomPacketPayload {
    public static final Type<ChooseSoulIslandPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(BroomCloset.MODID, "choose_soul_island"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ChooseSoulIslandPayload> STREAM_CODEC = StreamCodec.composite(
            ResourceLocation.STREAM_CODEC, ChooseSoulIslandPayload::optionId,
            BroomClosetNetwork.HAND_STREAM_CODEC, ChooseSoulIslandPayload::hand,
            ChooseSoulIslandPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    // Everything is re-checked here: the screen may be stale
    static void handle(ChooseSoulIslandPayload payload, IPayloadContext context) {
        if (!(context.player() instanceof ServerPlayer player)) return;
        if (!Config.soulhomeIslandPicker) return; // got turned off while the screen was open

        SoulIslandOption option = SoulIslandOptions.get(payload.optionId());
        if (option == null) return;

        UUID self = player.getUUID();
        if (!SoulKeyInterceptor.needsPick(player.server, self)) return;
        SoulIslandChoices.get(player.server).set(self, option.structure(), option.spawn().orElse(null));

        // Run the key's own finish-use, which is how SoulHome teleports.
        // just in case the key's gone from that hand, the pick is saved and they just use it again.
        ItemStack stack = player.getItemInHand(payload.hand());
        boolean canTeleport = player.isAlive()
                && !SoulHomeCompat.isSoulHome(player.level())
                && SoulKeyInterceptor.isSoulKey(stack)
                && SoulKeyInterceptor.homeOwner(stack, player).equals(self);

        if (canTeleport) {
            stack.getItem().finishUsingItem(stack, player.level(), player);
        } else {
            player.displayClientMessage(Component.translatable("gui.broom_closet.soul_island.use_key_again"), true);
        }
    }
}
