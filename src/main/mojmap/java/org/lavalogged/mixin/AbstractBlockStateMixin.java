package org.lavalogged.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import org.lavalogged.Lavalogged;
import org.lavalogged.mixin.Lavaloggable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Mixin class for the {@link BlockBehaviour.BlockStateBase Block State Base}: lavalogged states
 * glow like a lava block, and breaking one leaves the lava behind as a source instead of
 * destroying it.
 * <p>
 * The state level is the right choke point for the removal hook: the engine calls it for every
 * replacement, including on blocks whose own {@code affectNeighborsAfterRemoval} override would
 * otherwise shadow a block level injection.
 */
@Mixin(BlockBehaviour.BlockStateBase.class)
public abstract class AbstractBlockStateMixin {

	/**
	 * Make a {@link Lavalogged#LAVALOGGED Lavalogged} {@link BlockState Block State} glow like
	 * {@link Blocks#LAVA Lava} (light level 15)
	 *
	 * @param callbackInfoReturnable {@link CallbackInfoReturnable<Integer> The Light Emission Callback Info Returnable}
	 */
	@Inject(method = "getLightEmission", at = @At("HEAD"), cancellable = true)
	private void lavalogged$getLightEmission(CallbackInfoReturnable<Integer> callbackInfoReturnable) {
		BlockState self = (BlockState) (Object) this;
		if (Lavaloggable.isLavalogged(self)) {
			callbackInfoReturnable.setReturnValue(15);
		}
	}

	/**
	 * Leave a {@link Blocks#LAVA Lava} source behind when a {@link Lavalogged#LAVALOGGED Lavalogged}
	 * {@link BlockState Block State} is broken, so the lava phase is preserved. Draining the lava
	 * (which replaces the state with its non-lavalogged variant) and piston moves are unaffected.
	 *
	 * @param level {@link ServerLevel The Server Level reference}
	 * @param pos {@link BlockPos The Block Pos}
	 * @param movedByPiston whether the state was moved by a piston
	 */
	@Inject(method = "affectNeighborsAfterRemoval", at = @At("HEAD"))
	private void lavalogged$leaveLavaOnBreak(ServerLevel level, BlockPos pos, boolean movedByPiston, CallbackInfo callbackInfo) {
		BlockState self = (BlockState) (Object) this;
		if (!movedByPiston && Lavaloggable.isLavalogged(self) && level.getBlockState(pos).isAir()) {
			level.setBlock(pos, Blocks.LAVA.defaultBlockState(), Block.UPDATE_ALL);
		}
	}

}
