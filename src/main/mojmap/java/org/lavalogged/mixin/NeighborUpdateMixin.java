package org.lavalogged.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import org.lavalogged.Lavalogged;
import org.lavalogged.Lavaloggable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Mixin class for handling neighbor updates of a {@link Lavaloggable Lavaloggable} {@link Block Block}
 * <p>
 * Targets every lavaloggable class (marker inherited from {@link Lavaloggable Lavaloggable}) that
 * declares its own {@code updateShape} override.
 */
@Mixin({
        AmethystClusterBlock.class,
        BarrierBlock.class,
        BaseCoralPlantTypeBlock.class,
        BaseCoralWallFanBlock.class,
        BaseRailBlock.class,
        BigDripleafBlock.class,
        BigDripleafStemBlock.class,
        CampfireBlock.class,
        CandleBlock.class,
        CeilingHangingSignBlock.class,
        ChainBlock.class,
        ChestBlock.class,
        ConduitBlock.class,
        CopperChestBlock.class,
        CopperGolemStatueBlock.class,
        CoralFanBlock.class,
        CoralPlantBlock.class,
        CoralWallFanBlock.class,
        DecoratedPotBlock.class,
        DriedGhastBlock.class,
        EnderChestBlock.class,
        FenceBlock.class,
        HangingRootsBlock.class,
        HeavyCoreBlock.class,
        IronBarsBlock.class,
        LadderBlock.class,
        LanternBlock.class,
        LeavesBlock.class,
        LightBlock.class,
        LightningRodBlock.class,
        MangrovePropaguleBlock.class,
        MangroveRootsBlock.class,
        MultifaceBlock.class,
        //? if 1.21.11 {
        PointedDripstoneBlock.class,
        //?} else {
        /*SpeleothemBlock.class,
         *///?}
        ScaffoldingBlock.class,
        SculkSensorBlock.class,
        SculkShriekerBlock.class,
        SeaPickleBlock.class,
        ShelfBlock.class,
        SignBlock.class,
        SlabBlock.class,
        SmallDripleafBlock.class,
        StairBlock.class,
        StandingSignBlock.class,
        TrapDoorBlock.class,
        WallBlock.class,
        WallHangingSignBlock.class,
        WallSignBlock.class,
        WaterloggedTransparentBlock.class
})
public class NeighborUpdateMixin {

    /**
     * Handle neighbor updates for a {@link Lavaloggable Lavaloggable} {@link Block Block}: a
     * lavalogged block keeps scheduling lava ticks so the lava behaves like a source block
     *
     * @param blockState {@link BlockState The current Block State}
     * @param levelReader {@link LevelReader The Level reference}
     * @param scheduledTickAccess {@link ScheduledTickAccess The scheduled tick accessor}
     * @param blockPos {@link BlockPos The current Block Pos}
     * @param direction {@link Direction The update Direction}
     * @param neighborBlockPos {@link BlockPos The neighbor Block Pos}
     * @param neighborBlockState {@link BlockState The neighbor Block State}
     * @param random {@link RandomSource The Random Source}
     * @param callbackInfoReturnable {@link CallbackInfoReturnable<BlockState> The Block State Callback Info Returnable}
     */
    @Inject(method = "updateShape", at = @At("HEAD"))
    private void lavalogged$updateShape(final BlockState blockState, final LevelReader levelReader, final ScheduledTickAccess scheduledTickAccess, final BlockPos blockPos, final Direction direction, final BlockPos neighborBlockPos, final BlockState neighborBlockState, final RandomSource random, final CallbackInfoReturnable<BlockState> callbackInfoReturnable) {
        if (this instanceof Lavaloggable && blockState.hasProperty(Lavalogged.LAVALOGGED) && blockState.getValue(Lavalogged.LAVALOGGED) && levelReader instanceof ServerLevel level) {
            level.scheduleTick(blockPos, Fluids.LAVA, Fluids.LAVA.getTickDelay(level));
        }
    }

}
