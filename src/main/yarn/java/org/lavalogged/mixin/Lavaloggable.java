package org.lavalogged.mixin;

import net.minecraft.block.*;
import org.spongepowered.asm.mixin.Mixin;

/**
 * Mixin Interface for a Lavaloggable {@link Block Block}
 * <p>
 * Targets every class that directly implements {@link Waterloggable Waterloggable};
 * subclasses (rails, signs, panes, corals, ...) inherit the marker from these.
 * <p>
 * Keep this list in sync with each Minecraft version: it must contain exactly the
 * direct {@link Waterloggable Waterloggable} implementors of the targeted version.
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
public interface Lavaloggable extends Waterloggable { }
