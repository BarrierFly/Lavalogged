package org.lavalogged.mixin;

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
import org.spongepowered.asm.mixin.Mixin;

/**
 * Mixin Interface for a Lavaloggable {@link Block Block}
 * <p>
 * Targets every class that directly implements {@link Waterloggable Waterloggable};
 * subclasses (rails, signs, panes, corals, ...) inherit the marker from these.
 * <p>
 * Keep this list in sync with each Minecraft version: it must contain exactly the
 * direct {@link Waterloggable Waterloggable} implementors of the targeted version.
 * <p>
 * The default methods mirror the vanilla {@link Waterloggable Waterloggable} fluid logic with
 * water replaced by lava, so the vanilla bucket, dispenser and fluid-spread paths all work:
 * flowing lava only fills a block once the game itself promotes it to a source (which requires
 * the {@code lavaSourceConversion} game rule plus vanilla's neighbor conditions), and blocks
 * whose collision shape is a full cube (double slabs, heavy cores) never accept lava because
 * there is no room for it.
 */
@Mixin({
        AbstractRailBlock.class,
        AbstractSignBlock.class,
        AmethystClusterBlock.class,
        BigDripleafBlock.class,
        BigDripleafStemBlock.class,
        CampfireBlock.class,
        CandleBlock.class,
        ChainBlock.class,
        ChestBlock.class,
        ConduitBlock.class,
        CoralParentBlock.class,
        //? if 1.20.1
        DecoratedPotBlock.class,
        EnderChestBlock.class,
        GlowLichenBlock.class,
        HangingRootsBlock.class,
        HorizontalConnectingBlock.class,
        LadderBlock.class,
        LanternBlock.class,
        LeavesBlock.class,
        LightBlock.class,
        LightningRodBlock.class,
        MangroveRootsBlock.class,
        PointedDripstoneBlock.class,
        PropaguleBlock.class,
        ScaffoldingBlock.class,
        SculkSensorBlock.class,
        SculkShriekerBlock.class,
        SculkVeinBlock.class,
        SeaPickleBlock.class,
        SlabBlock.class,
        SmallDripleafBlock.class,
        StairsBlock.class,
        TrapdoorBlock.class,
        WallBlock.class
})
public interface Lavaloggable extends Waterloggable {

	/**
	 * {@return whether the {@link BlockState Block State} can receive lava right now}
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
			&& !Block.isShapeFullCube(state.getCollisionShape(world, pos));
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
		if (!world.isClient() && fluidState.getFluid() == Fluids.LAVA && isLavaFillable(state, world, pos)) {
			world.setBlockState(pos, state.with(Lavalogged.LAVALOGGED, true), Block.NOTIFY_ALL);
			world.scheduleFluidTick(pos, Fluids.LAVA, Fluids.LAVA.getTickRate(world));
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
