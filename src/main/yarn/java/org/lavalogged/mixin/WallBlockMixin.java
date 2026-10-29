package org.lavalogged.mixin;

import java.util.Map;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.WallBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import org.lavalogged.Lavalogged;
import org.spongepowered.asm.mixin.Final;
import org.lavalogged.Lavaloggable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Mixin class for the {@link WallBlock Wall Block}.
 * <p>
 * The per-state shape maps are built in the constructor, before the lavalogged property is added,
 * so lookups for lavalogged states would return null. Redirect them to the vanilla state, whose
 * connections - and therefore shape - are identical.
 */
@Mixin(WallBlock.class)
public abstract class WallBlockMixin {

	@Shadow
	@Final
	private Map<BlockState, VoxelShape> shapeMap;

	@Shadow
	@Final
	private Map<BlockState, VoxelShape> collisionShapeMap;

	@Inject(method = "getOutlineShape", at = @At("HEAD"), cancellable = true)
	private void lavalogged$getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context, CallbackInfoReturnable<VoxelShape> callbackInfoReturnable) {
		if (Lavaloggable.isLavalogged(state)) {
			callbackInfoReturnable.setReturnValue(this.shapeMap.get(state.with(Lavalogged.LAVALOGGED, false)));
		}
	}

	@Inject(method = "getCollisionShape", at = @At("HEAD"), cancellable = true)
	private void lavalogged$getCollisionShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context, CallbackInfoReturnable<VoxelShape> callbackInfoReturnable) {
		if (Lavaloggable.isLavalogged(state)) {
			callbackInfoReturnable.setReturnValue(this.collisionShapeMap.get(state.with(Lavalogged.LAVALOGGED, false)));
		}
	}

}
