package org.lavalogged;

import java.util.Optional;
import net.minecraft.block.*;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.sound.SoundEvent;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import net.minecraft.world.WorldAccess;
import org.lavalogged.Lavalogged;
import org.lavalogged.LavaDrainSound;

/**
 * Marker interface for a Lavaloggable {@link Block Block}
 * <p>
 * Applied to every class that directly implements {@link Waterloggable Waterloggable};
 * subclasses (rails, signs, panes, corals, ...) inherit the marker from these.
 * <p>
 * The list lives in {@link org.lavalogged.mixin.LavaloggableMixin LavaloggableMixin}; the mixin makes every target
 * block implement this interface so the default methods below apply.
 * <p>
 * The default methods mirror the vanilla {@link Waterloggable Waterloggable} fluid logic with
 * water replaced by lava, so the vanilla bucket, dispenser and fluid-spread paths all work:
 * flowing lava only fills a block once the game itself promotes it to a source (which requires
 * the {@code lavaSourceConversion} game rule plus vanilla's neighbor conditions). A block
 * accepts lava exactly in the states where vanilla accepts water, so double slabs never accept
 * lava either - they refuse water through {@code SlabBlock}'s own {@code canFillWithFluid} override.
 */
public interface Lavaloggable extends Waterloggable {

	/**
	 * {@return whether the {@link BlockState Block State} can receive lava right now}
	 * <p>
	 * Gated on the block's own water acceptance instead of the collision shape: full-cube blocks
	 * that vanilla happily waterlogs (leaves, mangrove roots) must stay lavaloggable, while
	 * per-class refusals such as double slabs are honored.
	 *
	 * @param state {@link BlockState The current Block State}
	 * @param world {@link BlockView The World reference}
	 * @param pos {@link BlockPos The Block Pos}
	 */
	static boolean isLavaFillable(BlockState state, BlockView world, BlockPos pos) {
		return state.contains(Lavalogged.LAVALOGGED)
			&& !state.get(Lavalogged.LAVALOGGED)
			&& state.contains(Properties.WATERLOGGED)
			&& !state.get(Properties.WATERLOGGED)
			&& acceptsWater(state, world, pos);
	}

	/**
	 * {@return whether vanilla would fill the {@link BlockState Block State} with water}
	 * <p>
	 * Asking the block itself keeps every vanilla {@code canFillWithFluid} override in the loop.
	 * No recursion risk: for lava the water probe never re-enters {@link #isLavaFillable isLavaFillable}.
	 */
	private static boolean acceptsWater(BlockState state, BlockView world, BlockPos pos) {
		return !(state.getBlock() instanceof FluidFillable)
			|| ((FluidFillable) state.getBlock()).canFillWithFluid(world, pos, state, Fluids.WATER);
	}

	/**
	 * Flip the {@link Lavalogged#LAVALOGGED Lavalogged} property to {@code true} and schedule a
	 * lava tick, mirroring what the bucket-use mixin does for a direct click. Callers own the
	 * sounds and item exchange.
	 *
	 * @param world {@link WorldAccess The World reference}
	 * @param pos {@link BlockPos The Block Pos}
	 * @param state {@link BlockState The current Block State}
	 * @return whether the state was filled
	 */
	static boolean fillWithLava(final WorldAccess world, final BlockPos pos, final BlockState state) {
		if (!isLavaFillable(state, world, pos)) {
			return false;
		}
		world.setBlockState(pos, state.with(Lavalogged.LAVALOGGED, true), Block.NOTIFY_ALL);
		world.scheduleFluidTick(pos, Fluids.LAVA, Fluids.LAVA.getTickRate(world));
		return true;
	}

	/**
	 * {@return whether the {@link BlockState Block State} currently holds lava}
	 */
	static boolean isLavalogged(BlockState state) {
		return state.contains(Lavalogged.LAVALOGGED) && state.get(Lavalogged.LAVALOGGED);
	}

	@Override
	default boolean canFillWithFluid(BlockView world, BlockPos pos, BlockState state, Fluid fluid) {
		if (fluid == Fluids.LAVA) {
			return isLavaFillable(state, world, pos);
		}
		if (isLavalogged(state)) {
			return false;
		}
		return Waterloggable.super.canFillWithFluid(world, pos, state, fluid);
	}

	@Override
	default boolean tryFillWithFluid(WorldAccess world, BlockPos pos, BlockState state, FluidState fluidState) {
		if (isLavalogged(state) && fluidState.getFluid() != Fluids.LAVA) {
			return false;
		}
		if (fluidState.getFluid() == Fluids.LAVA && fillWithLava(world, pos, state)) {
			return true;
		}
		return Waterloggable.super.tryFillWithFluid(world, pos, state, fluidState);
	}

	@Override
	default ItemStack tryDrainFluid(WorldAccess world, BlockPos pos, BlockState state) {
		if (isLavalogged(state)) {
			world.setBlockState(pos, state.with(Lavalogged.LAVALOGGED, false), Block.NOTIFY_ALL);
			if (!state.canPlaceAt(world, pos)) {
				world.breakBlock(pos, true);
			}
			LavaDrainSound.mark();
			return new ItemStack(Items.LAVA_BUCKET);
		}
		return Waterloggable.super.tryDrainFluid(world, pos, state);
	}

	@Override
	default Optional<SoundEvent> getBucketFillSound() {
		if (LavaDrainSound.consume()) {
			return Fluids.LAVA.getBucketFillSound();
		}
		return Waterloggable.super.getBucketFillSound();
	}

}
