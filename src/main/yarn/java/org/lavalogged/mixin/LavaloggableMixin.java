package org.lavalogged.mixin;

import net.minecraft.block.*;
import org.lavalogged.Lavaloggable;
import org.spongepowered.asm.mixin.Mixin;

/**
 * Mixin class for a Lavaloggable {@link Block Block}
 * <p>
 * Targets every class that directly implements {@link Waterloggable Waterloggable};
 * subclasses (rails, signs, panes, corals, ...) inherit the marker from these.
 * <p>
 * Keep this list in sync with each Minecraft version: it must contain exactly the
 * direct {@link Waterloggable Waterloggable} implementors of the targeted version.
 * Applying this mixin makes every target block implement {@link Lavaloggable Lavaloggable},
 * so the marker and the interface's default fluid methods apply to them.
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
public abstract class LavaloggableMixin implements Lavaloggable {

}
