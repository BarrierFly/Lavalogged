package org.lavalogged.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import org.lavalogged.Lavalogged;
import org.lavalogged.Lavaloggable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Mixin class for handling the placement of a {@link Lavaloggable Lavaloggable} {@link Block Block}
 * <p>
 * Targets {@link Block Block} (covers every block that does not override {@code getStateForPlacement})
 * plus every lavaloggable class that declares its own {@code getStateForPlacement} override.
 */
@Mixin({
        AmethystClusterBlock.class,
        BarrierBlock.class,
        BaseCoralPlantTypeBlock.class,
        BaseCoralWallFanBlock.class,
        BaseRailBlock.class,
        BigDripleafBlock.class,
        Block.class,
        CalibratedSculkSensorBlock.class,
        CampfireBlock.class,
        CandleBlock.class,
        CeilingHangingSignBlock.class,
        ChainBlock.class,
        ChestBlock.class,
        ConduitBlock.class,
        CopperChestBlock.class,
        CopperGolemStatueBlock.class,
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
public class PlacementMixin {

    /**
     * Make the {@link Block Block} automatically {@link Lavalogged#LAVALOGGED Lavalogged} when placed
     * inside {@link Fluids#LAVA Lava}
     *
     * @param blockPlaceContext {@link BlockPlaceContext The Block Place Context}
     * @param callbackInfoReturnable {@link CallbackInfoReturnable<BlockState> The Block State Callback Info Returnable}
     */
    @Inject(method = "getStateForPlacement", at = @At("RETURN"), cancellable = true)
    private void lavalogged$getStateForPlacement(final BlockPlaceContext blockPlaceContext, final CallbackInfoReturnable<BlockState> callbackInfoReturnable) {
        if (this instanceof Lavaloggable && callbackInfoReturnable.getReturnValue() != null && callbackInfoReturnable.getReturnValue().hasProperty(Lavalogged.LAVALOGGED)) {
            BlockState placed = callbackInfoReturnable.getReturnValue();
            BlockPos pos = blockPlaceContext.getClickedPos();
            FluidState fluidState = blockPlaceContext.getLevel().getFluidState(pos);
            boolean inLavaSource = fluidState.is(Fluids.LAVA) && fluidState.isSource();
            callbackInfoReturnable.setReturnValue(placed.setValue(Lavalogged.LAVALOGGED, inLavaSource && Lavaloggable.isLavaFillable(placed, blockPlaceContext.getLevel(), pos)));
        }
    }

}
