package org.lavalogged.mixin;

import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.WallBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
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
 * The per-state shape functions are built in the constructor, before the lavalogged property is
 * added, so lookups for lavalogged states would return null. Redirect them to the vanilla state,
 * whose connections - and therefore shape - are identical.
 */
@Mixin(WallBlock.class)
public abstract class WallBlockMixin {

	@Shadow
	@Final
	private Function<BlockState, VoxelShape> shapes;

	@Shadow
	@Final
	private Function<BlockState, VoxelShape> collisionShapes;

	@Inject(method = "getShape", at = @At("HEAD"), cancellable = true)
	private void lavalogged$getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context, CallbackInfoReturnable<VoxelShape> callbackInfoReturnable) {
		if (Lavaloggable.isLavalogged(state)) {
			callbackInfoReturnable.setReturnValue(this.shapes.apply(state.setValue(Lavalogged.LAVALOGGED, false)));
		}
	}

	@Inject(method = "getCollisionShape", at = @At("HEAD"), cancellable = true)
	private void lavalogged$getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context, CallbackInfoReturnable<VoxelShape> callbackInfoReturnable) {
		if (Lavaloggable.isLavalogged(state)) {
			callbackInfoReturnable.setReturnValue(this.collisionShapes.apply(state.setValue(Lavalogged.LAVALOGGED, false)));
		}
	}

}
