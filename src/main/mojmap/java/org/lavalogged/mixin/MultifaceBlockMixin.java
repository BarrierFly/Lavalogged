package org.lavalogged.mixin;

import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.MultifaceBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.lavalogged.Lavalogged;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Mixin class for the {@link MultifaceBlock Multiface Block} (glow lichen family).
 * <p>
 * The per-state outline shapes are built in the constructor, before the lavalogged property is
 * added, so lookups for lavalogged states would return null. Redirect them to the vanilla state.
 */
@Mixin(MultifaceBlock.class)
public abstract class MultifaceBlockMixin {

	@Shadow
	@Final
	private Function<BlockState, VoxelShape> shapes;

	@Inject(method = "getShape", at = @At("HEAD"), cancellable = true)
	private void lavalogged$getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context, CallbackInfoReturnable<VoxelShape> callbackInfoReturnable) {
		if (Lavaloggable.isLavalogged(state)) {
			callbackInfoReturnable.setReturnValue(this.shapes.apply(state.setValue(Lavalogged.LAVALOGGED, false)));
		}
	}

}
