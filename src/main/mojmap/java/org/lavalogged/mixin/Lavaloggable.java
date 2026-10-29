package org.lavalogged.mixin;

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
 * The list lives in {@link LavaloggableMixin LavaloggableMixin}; the mixin makes every target
 * block implement this interface so the default methods below apply.
 * <p>
 * The default methods mirror the vanilla {@link SimpleWaterloggedBlock SimpleWaterloggedBlock}
 * fluid logic with water replaced by lava, so the vanilla bucket, dispenser and fluid-spread paths
 * all work: flowing lava only fills a block once the game itself promotes it to a source (which
 * requires the {@code lavaSourceConversion} game rule plus vanilla's neighbor conditions), and
 * blocks whose collision shape is a full block (double slabs, heavy cores) never accept lava
 * because there is no room for it.
 */
public interface Lavaloggable extends SimpleWaterloggedBlock {

	/**
	 * {@return whether the {@link BlockState Block State} can receive lava right now}
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
			&& !state.isCollisionShapeFullBlock(level, pos);
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
		if (!level.isClientSide() && fluidState.is(Fluids.LAVA) && isLavaFillable(state, level, pos)) {
			level.setBlock(pos, state.setValue(Lavalogged.LAVALOGGED, true), Block.UPDATE_ALL);
			level.scheduleTick(pos, Fluids.LAVA, Fluids.LAVA.getTickDelay(level));
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
