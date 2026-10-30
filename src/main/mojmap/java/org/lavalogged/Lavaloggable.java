package org.lavalogged;

import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import org.lavalogged.Lavalogged;
import org.lavalogged.LavaDrainSound;

/**
 * Marker interface for a Lavaloggable {@link Block Block}
 * <p>
 * Applied to every class that directly implements {@link SimpleWaterloggedBlock SimpleWaterloggedBlock};
 * subclasses (rails, signs, panes, corals, ...) inherit the marker from these.
 * <p>
 * The list lives in {@link org.lavalogged.mixin.LavaloggableMixin LavaloggableMixin}; the mixin makes every target
 * block implement this interface so the default methods below apply.
 * <p>
 * The default methods mirror the vanilla {@link SimpleWaterloggedBlock SimpleWaterloggedBlock}
 * fluid logic with water replaced by lava, so the vanilla bucket, dispenser and fluid-spread paths
 * all work: flowing lava only fills a block once the game itself promotes it to a source (which
 * requires the {@code lavaSourceConversion} game rule plus vanilla's neighbor conditions). A block
 * accepts lava exactly in the states where vanilla accepts water, so double slabs never accept
 * lava either - they refuse water through {@code SlabBlock}'s own {@code canPlaceLiquid} override.
 */
public interface Lavaloggable extends SimpleWaterloggedBlock {

	/**
	 * {@return whether the {@link BlockState Block State} can receive lava right now}
	 * <p>
	 * Gated on the block's own water acceptance instead of the collision shape: full-cube blocks
	 * that vanilla happily waterlogs (leaves, grates, mangrove roots, heavy cores) must stay
	 * lavaloggable, while per-class refusals such as double slabs are honored.
	 *
	 * @param state {@link BlockState The current Block State}
	 * @param level {@link BlockGetter The Level reference}
	 * @param pos {@link BlockPos The Block Pos}
	 */
	static boolean isLavaFillable(BlockState state, BlockGetter level, BlockPos pos) {
		return state.hasProperty(Lavalogged.LAVALOGGED)
			&& !state.getValue(Lavalogged.LAVALOGGED)
			&& state.hasProperty(BlockStateProperties.WATERLOGGED)
			&& !state.getValue(BlockStateProperties.WATERLOGGED)
			&& acceptsWater(state, level, pos);
	}

	/**
	 * {@return whether vanilla would fill the {@link BlockState Block State} with water}
	 * <p>
	 * Asking the block itself keeps every vanilla {@code canPlaceLiquid} override in the loop; the
	 * {@code null} user matches what {@code FlowingFluid#canHoldSpecificFluid} passes. No recursion
	 * risk: for lava the water probe never re-enters {@link #isLavaFillable isLavaFillable}.
	 */
	private static boolean acceptsWater(BlockState state, BlockGetter level, BlockPos pos) {
		return !(state.getBlock() instanceof LiquidBlockContainer container)
			|| container.canPlaceLiquid(null, level, pos, state, Fluids.WATER);
	}

	/**
	 * Flip the {@link Lavalogged#LAVALOGGED Lavalogged} property to {@code true} and schedule a
	 * lava tick, mirroring what the bucket-use mixin does for a direct click. Callers own the
	 * sounds and item exchange.
	 *
	 * @param level {@link LevelAccessor The Level reference}
	 * @param pos {@link BlockPos The Block Pos}
	 * @param state {@link BlockState The current Block State}
	 * @return whether the state was filled
	 */
	static boolean fillWithLava(final LevelAccessor level, final BlockPos pos, final BlockState state) {
		if (!isLavaFillable(state, level, pos)) {
			return false;
		}
		level.setBlock(pos, state.setValue(Lavalogged.LAVALOGGED, true), Block.UPDATE_ALL);
		level.scheduleTick(pos, Fluids.LAVA, Fluids.LAVA.getTickDelay(level));
		return true;
	}

	/**
	 * {@return whether the {@link BlockState Block State} currently holds lava}
	 */
	static boolean isLavalogged(BlockState state) {
		return state.hasProperty(Lavalogged.LAVALOGGED) && state.getValue(Lavalogged.LAVALOGGED);
	}

	@Override
	default boolean canPlaceLiquid(final LivingEntity user, final BlockGetter level, final BlockPos pos, final BlockState state, final Fluid fluid) {
		if (fluid == Fluids.LAVA) {
			return isLavaFillable(state, level, pos);
		}
		if (isLavalogged(state)) {
			return false;
		}
		return SimpleWaterloggedBlock.super.canPlaceLiquid(user, level, pos, state, fluid);
	}

	@Override
	default boolean placeLiquid(final LevelAccessor level, final BlockPos pos, final BlockState state, final FluidState fluidState) {
		if (isLavalogged(state) && !fluidState.is(Fluids.LAVA)) {
			return false;
		}
		if (fluidState.is(Fluids.LAVA) && fillWithLava(level, pos, state)) {
			return true;
		}
		return SimpleWaterloggedBlock.super.placeLiquid(level, pos, state, fluidState);
	}

	@Override
	default ItemStack pickupBlock(final LivingEntity user, final LevelAccessor level, final BlockPos pos, final BlockState state) {
		if (isLavalogged(state)) {
			level.setBlock(pos, state.setValue(Lavalogged.LAVALOGGED, false), Block.UPDATE_ALL);
			if (!state.canSurvive(level, pos)) {
				level.destroyBlock(pos, true);
			}
			LavaDrainSound.mark();
			return new ItemStack(Items.LAVA_BUCKET);
		}
		return SimpleWaterloggedBlock.super.pickupBlock(user, level, pos, state);
	}

	@Override
	default Optional<SoundEvent> getPickupSound() {
		if (LavaDrainSound.consume()) {
			return Optional.of(SoundEvents.BUCKET_FILL_LAVA);
		}
		return SimpleWaterloggedBlock.super.getPickupSound();
	}

}
