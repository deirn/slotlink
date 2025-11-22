package badasintended.slotlink.mixin;

import badasintended.slotlink.block.TransferCableBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.LeverBlock;
import net.minecraft.world.level.block.FaceAttachedHorizontalDirectionalBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.LevelReader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FaceAttachedHorizontalDirectionalBlock.class)
public abstract class WallMountedBlockMixin {

    @Shadow
    protected static Direction getConnectedDirection(BlockState state) {
        throw new AssertionError();
    }

    @Inject(at = @At("RETURN"), method = "canSurvive(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/LevelReader;Lnet/minecraft/core/BlockPos;)Z", cancellable = true)
    private void slotlink$placeLeverAtCable(BlockState state, LevelReader world, BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        Block wanted = state.getBlock();
        Block on = world.getBlockState(pos.relative(getConnectedDirection(state).getOpposite())).getBlock();
        if (wanted instanceof LeverBlock && on instanceof TransferCableBlock) {
            cir.setReturnValue(true);
        }
    }

}
