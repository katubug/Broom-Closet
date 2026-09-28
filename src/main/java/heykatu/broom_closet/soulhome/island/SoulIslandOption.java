package heykatu.broom_closet.soulhome.island;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

// One entry in the soul home picker, loaded from data/<ns>/soul_island/<id>.json
public record SoulIslandOption(ResourceLocation structure, ResourceLocation screenshot,
                               Component name, Component description, int order) {

    public static final Codec<SoulIslandOption> CODEC = RecordCodecBuilder.create(i -> i.group(
            ResourceLocation.CODEC.fieldOf("structure").forGetter(SoulIslandOption::structure),
            ResourceLocation.CODEC.fieldOf("screenshot").forGetter(SoulIslandOption::screenshot),
            ComponentSerialization.CODEC.fieldOf("name").forGetter(SoulIslandOption::name),
            ComponentSerialization.CODEC.optionalFieldOf("description", Component.empty()).forGetter(SoulIslandOption::description),
            Codec.INT.optionalFieldOf("order", 0).forGetter(SoulIslandOption::order)
    ).apply(i, SoulIslandOption::new));

    // What the client needs to draw a slide
    public record Entry(ResourceLocation id, ResourceLocation screenshot, Component name, Component description) {
        public static final StreamCodec<RegistryFriendlyByteBuf, Entry> STREAM_CODEC = StreamCodec.composite(
                ResourceLocation.STREAM_CODEC, Entry::id,
                ResourceLocation.STREAM_CODEC, Entry::screenshot,
                ComponentSerialization.STREAM_CODEC, Entry::name,
                ComponentSerialization.STREAM_CODEC, Entry::description,
                Entry::new);

        public static final StreamCodec<RegistryFriendlyByteBuf, List<Entry>> LIST_STREAM_CODEC =
                STREAM_CODEC.apply(ByteBufCodecs.list());
    }
}
