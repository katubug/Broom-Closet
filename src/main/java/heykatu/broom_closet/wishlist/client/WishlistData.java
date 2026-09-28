package heykatu.broom_closet.wishlist.client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.mojang.logging.LogUtils;
import heykatu.broom_closet.BroomCloset;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

// Client-only wishlist of item ids, persisted to a flat JSON in config
public class WishlistData {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private static Set<ResourceLocation> wishlistedIds = new LinkedHashSet<>();
    private static Set<Item> resolvedItems = Set.of();
    private static List<Item> resolvedItemList = List.of();
    private static Set<Block> resolvedBlocks = Set.of();
    // Bumped on every mutation - the world-highlight renderer polls this instead of needing a
    // dedicated listener interface
    private static int version = 0;

    private WishlistData() {
    }

    // writes to config/broom_closet/data/wishlist.json
    private static Path file() {
        return Minecraft.getInstance().gameDirectory.toPath()
                .resolve("config").resolve(BroomCloset.MODID).resolve("data").resolve("wishlist.json");
    }

    public static void load() {
        Path path = file();
        if (!Files.exists(path)) {
            return;
        }

        try (Reader reader = Files.newBufferedReader(path)) {
            String[] ids = GSON.fromJson(reader, String[].class);
            if (ids == null) {
                return;
            }

            Set<ResourceLocation> loaded = new LinkedHashSet<>();
            for (String id : ids) {
                ResourceLocation parsed = ResourceLocation.tryParse(id);
                if (parsed != null) {
                    loaded.add(parsed);
                } else {
                    LOGGER.warn("Ignoring malformed wishlist item id '{}' in {}", id, path);
                }
            }
            wishlistedIds = loaded;
            resolve();
        } catch (IOException | JsonSyntaxException e) {
            LOGGER.warn("Failed to load wishlist from {}, starting with an empty wishlist", path, e);
        }
    }

    private static void save() {
        Path path = file();
        try {
            Files.createDirectories(path.getParent());
            String[] ids = wishlistedIds.stream().map(ResourceLocation::toString).toArray(String[]::new);
            try (Writer writer = Files.newBufferedWriter(path)) {
                GSON.toJson(ids, writer);
            }
        } catch (IOException e) {
            LOGGER.warn("Failed to save wishlist to {}", path, e);
        }
    }

    private static void resolve() {
        // wishlistedIds is a LinkedHashSet, so this preserves the order items were added in.
        // resolvedItemList keeps that order (for the wishlist screen's grid); resolvedItems is
        // just the same items as a Set for O(1) contains() checks where order doesn't matter. I think
        resolvedItemList = wishlistedIds.stream()
                .map(BuiltInRegistries.ITEM::getOptional)
                .flatMap(Optional::stream)
                .collect(Collectors.toUnmodifiableList());
        resolvedItems = Set.copyOf(resolvedItemList);
        resolvedBlocks = resolvedItems.stream()
                .filter(item -> item instanceof BlockItem)
                .map(item -> ((BlockItem) item).getBlock())
                .collect(Collectors.toUnmodifiableSet());
    }

    // Returns whether the item ended up wishlisted
    public static boolean toggle(Item item) {
        ResourceLocation id = BuiltInRegistries.ITEM.getKey(item);
        boolean added = wishlistedIds.add(id);
        if (!added) {
            wishlistedIds.remove(id);
        }
        resolve();
        version++;
        save();
        return added;
    }

    public static boolean contains(ItemStack stack) {
        return !stack.isEmpty() && resolvedItems.contains(stack.getItem());
    }

    public static Set<Block> wishlistedBlocks() {
        return resolvedBlocks;
    }

    // Insertion order, for the wishlist screen's grid.
    public static List<Item> items() {
        return resolvedItemList;
    }

    public static int version() {
        return version;
    }
}
