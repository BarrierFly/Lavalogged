package org.lavalogged.mixin;

import net.minecraft.block.*;
import net.minecraft.fluid.Fluids;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.WorldAccess;
import org.lavalogged.Lavalogged;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Mixin class for handling neighbor updates of a {@link Lavaloggable Lavaloggable} {@link Block Block}
 * <p>
 * Targets every lavaloggable class (marker inherited from {@link Lavaloggable Lavaloggable}) that declares its own
 * {@code getStateForNeighborUpdate} override; the base {@link AbstractBlock} declaration is intentionally not targeted
 * because every entry here overrides it.
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
        CoralBlock.class,
        CoralFanBlock.class,
        CoralParentBlock.class,
        CoralWallFanBlock.class,
        DeadCoralWallFanBlock.class,
        //? if 1.20.1
        DecoratedPotBlock.class,
        EnderChestBlock.class,
        FenceBlock.class,
        GlowLichenBlock.class,
        HangingRootsBlock.class,
        HangingSignBlock.class,
        LadderBlock.class,
        LanternBlock.class,
        LeavesBlock.class,
        LightBlock.class,
        LightningRodBlock.class,
        MangroveRootsBlock.class,
        PaneBlock.class,
        PointedDripstoneBlock.class,
        PropaguleBlock.class,
        ScaffoldingBlock.class,
        SculkSensorBlock.class,
        SculkShriekerBlock.class,
        SculkVeinBlock.class,
        SeaPickleBlock.class,
        SignBlock.class,
        SlabBlock.class,
        SmallDripleafBlock.class,
        StairsBlock.class,
        TrapdoorBlock.class,
        WallBlock.class,
        WallHangingSignBlock.class,
        WallSignBlock.class
})
public class NeighborUpdateMixin {

    /**
     * Handle neighbor updates for a {@link Lavaloggable Lavaloggable} {@link Block Block}
     *
     * @param blockState {@link BlockState The current Block State}
     * @param direction {@link Direction The update Direction}
     * @param neighborBlockState {@link BlockState The neighbor Block State}
     * @param world {@link WorldAccess The World reference}
     * @param blockPos {@link BlockPos The current Block Pos}
     * @param neighborBlockPos {@link BlockPos The neighbor Block Pos}
     * @param callbackInfoReturnable {@link CallbackInfoReturnable<BlockState> The Block State Callback Info Returnable}
     */
    @Inject(method = "getStateForNeighborUpdate", at = @At("HEAD"))
    private void getStateForNeighborUpdate(final BlockState blockState, final Direction direction, final BlockState neighborBlockState, final WorldAccess world, final BlockPos blockPos, final BlockPos neighborBlockPos, final CallbackInfoReturnable<BlockState> callbackInfoReturnable) {
        if (this instanceof Lavaloggable && blockState.contains(Lavalogged.LAVALOGGED) && blockState.get(Lavalogged.LAVALOGGED)) {
            world.scheduleFluidTick(blockPos, Fluids.LAVA, Fluids.LAVA.getTickRate(world));
        }
    }

}
