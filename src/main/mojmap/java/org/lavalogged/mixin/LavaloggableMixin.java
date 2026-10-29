package org.lavalogged.mixin;

import net.minecraft.world.level.block.*;
import org.lavalogged.Lavaloggable;
import org.spongepowered.asm.mixin.Mixin;

/**
 * Mixin class for a Lavaloggable {@link Block Block}
 * <p>
 * Targets every class that directly implements {@link SimpleWaterloggedBlock SimpleWaterloggedBlock};
 * subclasses (rails, signs, panes, corals, ...) inherit the marker from these.
 * <p>
 * Keep this list in sync with each Minecraft version: it must contain exactly the direct
 * {@link SimpleWaterloggedBlock SimpleWaterloggedBlock} implementors of the targeted version.
 * Applying this mixin makes every target block implement {@link Lavaloggable Lavaloggable},
 * so the marker and the interface's default fluid methods apply to them.
 */
@Mixin({
        AmethystClusterBlock.class,
        BarrierBlock.class,
        BaseCoralPlantTypeBlock.class,
        BaseRailBlock.class,
        BigDripleafBlock.class,
        BigDripleafStemBlock.class,
        CampfireBlock.class,
        CandleBlock.class,
        ChainBlock.class,
        ChestBlock.class,
        ConduitBlock.class,
        CopperGolemStatueBlock.class,
        CrossCollisionBlock.class,
        DecoratedPotBlock.class,
        DriedGhastBlock.class,
        EnderChestBlock.class,
        HangingRootsBlock.class,
        HeavyCoreBlock.class,
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
        TrapDoorBlock.class,
        WallBlock.class,
        WaterloggedTransparentBlock.class
})
public abstract class LavaloggableMixin implements Lavaloggable {

}
