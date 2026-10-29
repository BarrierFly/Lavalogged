package org.lavalogged.mixin;

import com.google.common.collect.ImmutableMap;
import net.minecraft.block.BlockState;
import net.minecraft.block.MultifaceGrowthBlock;
import net.minecraft.block.ShapeContext;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import org.lavalogged.Lavalogged;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Mixin class for the {@link MultifaceGrowthBlock Multiface Growth Block} (glow lichen).
 * <p>
 * The per-state outline shapes are built in the constructor, before the lavalogged property is
 * added, so lookups for lavalogged states would return null. Redirect them to the vanilla state.
 */
@Mixin(MultifaceGrowthBlock.class)
public abstract class MultifaceBlockMixin {

	@Shadow
	@Final
	private ImmutableMap<BlockState, VoxelShape> SHAPES;

	@Inject(method = "getOutlineShape", at = @At("HEAD"), cancellable = true)
	private void lavalogged$getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context, CallbackInfoReturnable<VoxelShape> callbackInfoReturnable) {
		if (Lavaloggable.isLavalogged(state)) {
			callbackInfoReturnable.setReturnValue(this.SHAPES.get(state.with(Lavalogged.LAVALOGGED, false)));
		}
	}

}
