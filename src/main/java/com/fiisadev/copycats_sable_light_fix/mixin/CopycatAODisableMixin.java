package com.fiisadev.copycats_sable_light_fix.mixin;

import com.fiisadev.copycats_sable_light_fix.Config;
import com.simibubi.create.content.decoration.copycat.CopycatModel;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.data.ModelData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ModelBlockRenderer.class)
public class CopycatAODisableMixin {

    @Redirect(
        method = "tesselateBlock(Lnet/minecraft/world/level/BlockAndTintGetter;Lnet/minecraft/client/resources/model/BakedModel;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/BlockPos;Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;ZLnet/minecraft/util/RandomSource;JILnet/neoforged/neoforge/client/model/data/ModelData;Lnet/minecraft/client/renderer/RenderType;)V",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/block/ModelBlockRenderer;tesselateWithAO(Lnet/minecraft/world/level/BlockAndTintGetter;Lnet/minecraft/client/resources/model/BakedModel;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/BlockPos;Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;ZLnet/minecraft/util/RandomSource;JILnet/neoforged/neoforge/client/model/data/ModelData;Lnet/minecraft/client/renderer/RenderType;)V"
        )
    )
    private void copycats_sable_light_fix$redirectAO(ModelBlockRenderer self,
                                                     BlockAndTintGetter level,
                                                     BakedModel model,
                                                     BlockState state,
                                                     BlockPos pos,
                                                     PoseStack pose,
                                                     VertexConsumer consumer,
                                                     boolean shouldTransform,
                                                     RandomSource random,
                                                     long seed,
                                                     int overlay,
                                                     ModelData modelData,
                                                     RenderType renderType) {
        if (isExtraCopycats(model)) {
            self.tesselateWithoutAO(level, model, state, pos, pose, consumer, shouldTransform, random, seed, overlay, modelData, renderType);
        } else {
            self.tesselateWithAO(level, model, state, pos, pose, consumer, shouldTransform, random, seed, overlay, modelData, renderType);
        }
    }

    private static boolean isExtraCopycats(BakedModel model) {
        if (!Config.ENABLED.get()) {
            return false;
        }
        if (model == null) {
            return false;
        }
        try {
            if (model instanceof CopycatModel) {
                return true;
            }
        } catch (Throwable ignored) {
        }
        String name = model.getClass().getName();
        return name.startsWith("com.fiisadev.extra_copycats.");
    }
}
