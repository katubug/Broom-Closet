package heykatu.broom_closet.wearables.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemDisplayContext;
import net.neoforged.neoforge.client.model.data.ModelData;

import java.util.List;

public class HatModel extends HumanoidModel<LivingEntity> {

    private final BakedModel bakedModel;
    private HumanoidModel<?> parent;

    private static ModelPart buildRoot() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("head", CubeListBuilder.create(), PartPose.ZERO);
        root.addOrReplaceChild("hat", CubeListBuilder.create(), PartPose.ZERO);
        root.addOrReplaceChild("body", CubeListBuilder.create(), PartPose.ZERO);
        root.addOrReplaceChild("right_arm", CubeListBuilder.create(), PartPose.ZERO);
        root.addOrReplaceChild("left_arm", CubeListBuilder.create(), PartPose.ZERO);
        root.addOrReplaceChild("right_leg", CubeListBuilder.create(), PartPose.ZERO);
        root.addOrReplaceChild("left_leg", CubeListBuilder.create(), PartPose.ZERO);
        return LayerDefinition.create(mesh, 64, 32).bakeRoot();
    }

    public HatModel(ModelResourceLocation modelId) {
        super(buildRoot());
        this.bakedModel = Minecraft.getInstance().getModelManager().getModel(modelId);
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer ignored, int combinedLight, int combinedOverlay, int color) {
        // The consumer passed here targets an armor texture; our block model textures live on
        // the block atlas, so we source our own consumer from the main buffer source.
        MultiBufferSource bufferSource = Minecraft.getInstance().renderBuffers().bufferSource();
        VertexConsumer consumer = bufferSource.getBuffer(RenderType.entityCutoutNoCull(InventoryMenu.BLOCK_ATLAS));

        poseStack.pushPose();

        parent.head.translateAndRotate(poseStack);

        float scale = 0.625f;
        poseStack.translate(0.0, -0.4f * scale, 0.0);
        poseStack.mulPose(Axis.YP.rotationDegrees(180f));
        poseStack.scale(scale, -scale, -scale);

        bakedModel.applyTransform(ItemDisplayContext.HEAD, poseStack, false);
        poseStack.translate(-0.5, -0.5, -0.5);

        RandomSource random = RandomSource.create();
        renderQuads(poseStack, consumer, bakedModel.getQuads(null, null, random, ModelData.EMPTY, null), combinedLight, combinedOverlay);
        for (Direction dir : Direction.values()) {
            renderQuads(poseStack, consumer, bakedModel.getQuads(null, dir, random, ModelData.EMPTY, null), combinedLight, combinedOverlay);
        }

        poseStack.popPose();
    }

    private static void renderQuads(PoseStack poseStack, VertexConsumer consumer, List<BakedQuad> quads, int combinedLight, int combinedOverlay) {
        for (BakedQuad quad : quads) {
            consumer.putBulkData(poseStack.last(), quad, 1f, 1f, 1f, 1f, combinedLight, combinedOverlay);
        }
    }

    public void updateParent(HumanoidModel<?> parent) {
        this.parent = parent;
    }
}
