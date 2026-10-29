package org.lavalogged.mixin.state;

import net.minecraft.world.level.block.FenceBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.lavalogged.Lavalogged;
import org.lavalogged.Lavaloggable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.function.Function;

/**
 * Mixin class for the {@link FenceBlock Fence Block}.
 * <p>
 * Fences build their own per-state occlusion shape table in the constructor, so lookups for
 * lavalogged states would return null. Redirect them to the vanilla state, whose connections -
 * and therefore shape - are identical. (The outline/collision shapes live on
 * {@link CrossCollisionBlockMixin CrossCollisionBlockMixin}.)
 */
@Mixin(FenceBlock.class)
public abstract class FenceBlockMixin {

	@Shadow
	@Final
	private Function<BlockState, VoxelShape> occlusionShapes;

	@Inject(method = "getOcclusionShape", at = @At("HEAD"), cancellable = true)
	private void lavalogged$getOcclusionShape(BlockState state, CallbackInfoReturnable<VoxelShape> callbackInfoReturnable) {
		if (Lavaloggable.isLavalogged(state)) {
			callbackInfoReturnable.setReturnValue(this.occlusionShapes.apply(state.setValue(Lavalogged.LAVALOGGED, false)));
		}
	}

}
