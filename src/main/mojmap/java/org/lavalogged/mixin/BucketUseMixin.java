package org.lavalogged.mixin;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.BlockHitResult;
import org.lavalogged.Lavalogged;
import org.lavalogged.Lavaloggable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Mixin class for handling the use of a {@link net.minecraft.world.item.BucketItem Bucket} on a
 * {@link Lavaloggable Lavaloggable} {@link Block Block}
 * <p>
 * Targets {@link BlockBehaviour BlockBehaviour} (covers every block that does not override
 * {@code useItemOn}) plus every lavaloggable class that declares its own {@code useItemOn} override,
 * so the lava bucket interaction is intercepted before vanilla handles it.
 * <p>
 * Since 1.21.2 the swing animation is driven by the returned {@link InteractionResult#SUCCESS}, so no
 * manual swing call is needed.
 */
@Mixin({
        BlockBehaviour.class,
        CampfireBlock.class,
        CandleBlock.class,
        CopperGolemStatueBlock.class,
        DecoratedPotBlock.class,
        CeilingHangingSignBlock.class,
        ShelfBlock.class,
        SignBlock.class,
        WallHangingSignBlock.class,
        WeatheringCopperGolemStatueBlock.class
})
public class BucketUseMixin {

    /**
     * Make the {@link Block Block} {@link Lavalogged#LAVALOGGED Lavalogged} or not when a lava
     * bucket is used on it
     *
     * @param stack {@link ItemStack The ItemStack the Player is holding}
     * @param blockState {@link BlockState The current Block State}
     * @param level {@link Level The Level reference}
     * @param blockPos {@link BlockPos The current Block Pos}
     * @param player {@link Player The Player interacting with the Block}
     * @param hand {@link InteractionHand The hand the Player is using}
     * @param blockHitResult {@link BlockHitResult The Block Hit Result}
     * @param callbackInfoReturnable {@link CallbackInfoReturnable<InteractionResult> The Action Result Callback Info Returnable}
     */
    @Inject(method = "useItemOn", at = @At("HEAD"), cancellable = true)
    private void lavalogged$useItemOn(final ItemStack stack, final BlockState blockState, final Level level, final BlockPos blockPos, final Player player, final InteractionHand hand, final BlockHitResult blockHitResult, final CallbackInfoReturnable<InteractionResult> callbackInfoReturnable) {
        if (this instanceof Lavaloggable && blockState.hasProperty(Lavalogged.LAVALOGGED) && !stack.isEmpty()) {
            if (stack.is(Items.LAVA_BUCKET) && Lavaloggable.fillWithLava(level, blockPos, blockState)) {
                player.playSound(SoundEvents.BUCKET_EMPTY_LAVA, 1.0F, 1.0F);
                if (!player.isCreative()) {
                    stack.shrink(1);
                    player.getInventory().add(Items.BUCKET.getDefaultInstance());
                }
                callbackInfoReturnable.setReturnValue(InteractionResult.SUCCESS);
                return;
            }
            if (stack.is(Items.BUCKET) && Lavaloggable.isLavalogged(blockState)) {
                level.setBlock(blockPos, blockState.setValue(Lavalogged.LAVALOGGED, false), Block.UPDATE_ALL);
                if (!blockState.canSurvive(level, blockPos)) {
                    level.destroyBlock(blockPos, true);
                }
                player.playSound(SoundEvents.BUCKET_FILL_LAVA, 1.0F, 1.0F);
                if (!player.isCreative()) {
                    stack.shrink(1);
                    player.getInventory().add(Items.LAVA_BUCKET.getDefaultInstance());
                }
                callbackInfoReturnable.setReturnValue(InteractionResult.SUCCESS);
            }
        }
    }

}
