package org.lavalogged.mixin;

import net.minecraft.block.*;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemPlacementContext;
import org.lavalogged.Lavalogged;
import org.lavalogged.Lavaloggable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Mixin class for handling the placement of a {@link Lavaloggable Lavaloggable} {@link Block Block}
 * <p>
 * Targets {@link Block Block} (covers every block that does not override {@code getPlacementState}) plus every
 * lavaloggable class that declares its own {@code getPlacementState} override.
 */
@Mixin({
        AbstractRailBlock.class,
        AmethystClusterBlock.class,
        BigDripleafBlock.class,
        Block.class,
        CampfireBlock.class,
        CandleBlock.class,
        ChainBlock.class,
        ChestBlock.class,
        ConduitBlock.class,
        CoralParentBlock.class,
        //? if 1.20.1 {
        CalibratedSculkSensorBlock.class,
        DecoratedPotBlock.class,
        //?}
        DeadCoralWallFanBlock.class,
        EnderChestBlock.class,
        FenceBlock.class,
        HangingRootsBlock.class,
        HangingSignBlock.class,
        LadderBlock.class,
        LanternBlock.class,
        LeavesBlock.class,
        LightningRodBlock.class,
        MangroveRootsBlock.class,
        PaneBlock.class,
        PointedDripstoneBlock.class,
        PropaguleBlock.class,
        ScaffoldingBlock.class,
        SculkSensorBlock.class,
        SculkShriekerBlock.class,
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
public class PlacementMixin {

    /**
     * Make the {@link Block Block} automatically {@link Lavalogged#LAVALOGGED Lavalogged} when placed inside {@link Fluids#LAVA Lava}
     *
     * @param itemPlacementContext {@link ItemPlacementContext The Item Placement Context}
     * @param callbackInfoReturnable {@link CallbackInfoReturnable<BlockState> The Block State Callback Info Returnable}
     */
    @Inject(method = "getPlacementState(Lnet/minecraft/item/ItemPlacementContext;)Lnet/minecraft/block/BlockState;", at = @At("RETURN"), cancellable = true)
    private void getPlacementState(final ItemPlacementContext itemPlacementContext, final CallbackInfoReturnable<BlockState> callbackInfoReturnable) {
        if (this instanceof Lavaloggable && callbackInfoReturnable.getReturnValue() != null && callbackInfoReturnable.getReturnValue().contains(Lavalogged.LAVALOGGED)) {
            BlockState placed = callbackInfoReturnable.getReturnValue();
            boolean inLavaSource = itemPlacementContext.getWorld().getFluidState(itemPlacementContext.getBlockPos()).getFluid() == Fluids.LAVA;
            callbackInfoReturnable.setReturnValue(placed.with(Lavalogged.LAVALOGGED, inLavaSource && Lavaloggable.isLavaFillable(placed, itemPlacementContext.getWorld(), itemPlacementContext.getBlockPos())));
        }
    }

}
