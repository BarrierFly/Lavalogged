package org.lavalogged.mixin;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.lavalogged.Lavalogged;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Mixin class for the {@link AbstractBlock.AbstractBlockState Abstract Block State}: lavalogged
 * states glow like a lava block, and breaking one leaves the lava behind as a source instead of
 * destroying it.
 */
@Mixin(AbstractBlock.AbstractBlockState.class)
public abstract class AbstractBlockStateMixin {

	/**
	 * Make a {@link Lavalogged#LAVALOGGED Lavalogged} {@link BlockState Block State} glow like
	 * {@link Blocks#LAVA Lava} (light level 15)
	 *
	 * @param callbackInfoReturnable {@link CallbackInfoReturnable<Integer> The Luminance Callback Info Returnable}
	 */
	@Inject(method = "getLuminance", at = @At("HEAD"), cancellable = true)
	private void lavalogged$getLuminance(CallbackInfoReturnable<Integer> callbackInfoReturnable) {
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
	 * @param world {@link World The World reference}
	 * @param pos {@link BlockPos The Block Pos}
	 * @param newState {@link BlockState The state that replaced this one}
	 * @param moved whether the state was moved by a piston
	 */
	@Inject(method = "onStateReplaced", at = @At("HEAD"))
	private void lavalogged$leaveLavaOnBreak(World world, BlockPos pos, BlockState newState, boolean moved, CallbackInfo callbackInfo) {
		BlockState self = (BlockState) (Object) this;
		if (!world.isClient() && !moved && newState.isAir() && Lavaloggable.isLavalogged(self)) {
			world.setBlockState(pos, Blocks.LAVA.getDefaultState(), Block.NOTIFY_ALL);
		}
	}

}
