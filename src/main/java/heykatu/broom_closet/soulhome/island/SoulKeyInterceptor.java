package heykatu.broom_closet.soulhome.island;

import heykatu.broom_closet.BroomCloset;
import heykatu.broom_closet.network.OpenSoulIslandPickerPayload;
import heykatu.broom_closet.soulhome.SoulHomeCompat;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.storage.LevelResource;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import java.nio.file.Files;
import java.util.UUID;

// Stops a player's first soul key use and shows the island picker instead, so the island gets
// built from their pick rather than from SoulHome's UUID roll. See DimensionRegistryMixin.
@EventBusSubscriber(modid = BroomCloset.MODID, bus = EventBusSubscriber.Bus.GAME)
public final class SoulKeyInterceptor {
    private static final ResourceLocation SOUL_KEY = ResourceLocation.fromNamespaceAndPath(SoulHomeCompat.MODID, "soulkey");
    private static final ResourceLocation PERSONAL_SOUL_KEY = ResourceLocation.fromNamespaceAndPath(SoulHomeCompat.MODID, "personal_soulkey");

    private SoulKeyInterceptor() {}

    @SubscribeEvent
    public static void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        ItemStack stack = event.getItemStack();
        if (!isSoulKey(stack)) return;

        // Inside a soul home the key just sends you back, it never creates anything.
        if (SoulHomeCompat.isSoulHome(player.level())) return;

        UUID owner = homeOwner(stack, player);
        MinecraftServer server = player.server;
        if (!needsPick(server, owner) || SoulIslandOptions.isEmpty()) return;

        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.FAIL);

        if (!owner.equals(player.getUUID())) {
            // Visiting would build the owner's home with a random island and lock them out of picking.
            player.displayClientMessage(Component.translatable("gui.broom_closet.soul_island.owner_not_picked", ownerName(stack)), true);
            return;
        }

        PacketDistributor.sendToPlayer(player, new OpenSoulIslandPickerPayload(SoulIslandOptions.entries(), event.getHand()));
    }

    public static boolean isSoulKey(ItemStack stack) {
        ResourceLocation id = BuiltInRegistries.ITEM.getKey(stack.getItem());
        return id.equals(SOUL_KEY) || id.equals(PERSONAL_SOUL_KEY);
    }

    // Whose home this key opens. An unbound personal key binds to whoever uses it.
    public static UUID homeOwner(ItemStack stack, ServerPlayer player) {
        CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        if (BuiltInRegistries.ITEM.getKey(stack.getItem()).equals(PERSONAL_SOUL_KEY) && tag.hasUUID("soul_uuid")) {
            return tag.getUUID("soul_uuid");
        }
        return player.getUUID();
    }

    private static String ownerName(ItemStack stack) {
        String name = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().getString("soul_name");
        return name.isEmpty() ? "?" : name;
    }

    // No pick yet and no home yet, show the screen again
    public static boolean needsPick(MinecraftServer server, UUID owner) {
        if (SoulIslandChoices.get(server).has(owner)) return false;

        ResourceKey<Level> key = ResourceKey.create(Registries.DIMENSION,
                ResourceLocation.fromNamespaceAndPath(SoulHomeCompat.MODID, owner.toString()));
        if (server.getLevel(key) != null) return false;
        return !Files.isDirectory(DimensionType.getStorageFolder(key, server.getWorldPath(LevelResource.ROOT)));
    }
}
