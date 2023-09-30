package vice.magnesium_extras.mixins.fadeinchunks;

import me.jellysquid.mods.sodium.client.gl.device.CommandList;
import me.jellysquid.mods.sodium.client.render.chunk.ChunkCameraContext;
import me.jellysquid.mods.sodium.client.render.chunk.ChunkGraphicsState;
import me.jellysquid.mods.sodium.client.render.chunk.backends.multidraw.ChunkDrawParamsVector;
import me.jellysquid.mods.sodium.client.render.chunk.backends.multidraw.MultidrawChunkRenderBackend;
import me.jellysquid.mods.sodium.client.render.chunk.backends.multidraw.MultidrawGraphicsState;
import me.jellysquid.mods.sodium.client.render.chunk.lists.ChunkRenderListIterator;
import me.jellysquid.mods.sodium.client.render.chunk.passes.BlockRenderPass;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import vice.magnesium_extras.features.fadeinchunks.ChunkDrawParamsVectorExt;
import vice.magnesium_extras.features.fadeinchunks.ChunkGraphicsStateExt;

@Mixin(value = {MultidrawChunkRenderBackend.class}, remap = false)
public abstract class GL43ChunkRenderBackendMixin extends ChunkRenderShaderBackendMixin<MultidrawGraphicsState> {
    @Shadow
    @Final
    private ChunkDrawParamsVector uniformBufferBuilder;

    @Inject(method = {"setupDrawBatches"}, at = {@At(value = "INVOKE", target = "Lme/jellysquid/mods/sodium/client/render/chunk/backends/multidraw/ChunkDrawParamsVector;pushChunkDrawParams(FFF)V", shift = At.Shift.AFTER)})
    private void pushChunkDrawParamFadeInProgress(CommandList i, ChunkRenderListIterator<MultidrawGraphicsState> it, ChunkCameraContext visible, CallbackInfo ci) {
        ClientPlayerEntity player = (Minecraft.getInstance()).player;
        if (player == null)
            return;
        ChunkGraphicsState state = it.getGraphicsState();
        float progress = ChunkGraphicsStateExt.ext(state).getFadeInProgress(this.rbpe$currentTime);
        double x = player.getX() - 16.0D - state.getX();
        double z = player.getZ() - 16.0D - state.getZ();
        if (x * x + z * z < 576.0D) {
            ChunkDrawParamsVectorExt.ext(this.uniformBufferBuilder).rbpe$pushChunkDrawParamFadeIn(1.0F);
        } else {
            ChunkDrawParamsVectorExt.ext(this.uniformBufferBuilder).rbpe$pushChunkDrawParamFadeIn(progress);
        }
    }

    @ModifyArg(method = {"upload"}, at = @At(value = "INVOKE", target = "Lme/jellysquid/mods/sodium/client/render/chunk/ChunkRenderContainer;setGraphicsState(Lme/jellysquid/mods/sodium/client/render/chunk/passes/BlockRenderPass;Lme/jellysquid/mods/sodium/client/render/chunk/ChunkGraphicsState;)V", ordinal = 0))
    private ChunkGraphicsState setLoadTime(BlockRenderPass pass, ChunkGraphicsState newState) {
        ChunkGraphicsState oldState = ChunkGraphicsStateExt.ext(newState).getRbpe$container().getGraphicsState(pass);
        ChunkGraphicsStateExt.ext(newState).setRbpe$loadTime((oldState == null) ? this.rbpe$currentTime : ChunkGraphicsStateExt.ext(oldState).getRbpe$loadTime());
        return newState;
    }
}

