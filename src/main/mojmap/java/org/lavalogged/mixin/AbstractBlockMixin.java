package org.lavalogged.mixin;

import net.minecraft.world.level.block.state.BlockBehaviour;
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
 * Mixin class for the {@link BlockBehaviour Block Behaviour class}
 */
@Mixin(BlockBehaviour.class)
public abstract class AbstractBlockMixin {

    /**
     * Get the {@link FluidState Fluid State} of a {@link Lavalogged#LAVALOGGED Lavalogged} {@link Block Block}
     *
     * @param state {@link BlockState The current Block State}
     * @param callbackInfoReturnable {@link CallbackInfoReturnable<FluidState> The Fluid State Callback Info Returnable}
     */
    @Inject(method = "getFluidState", at = @At("RETURN"), cancellable = true)
    private void lavalogged$getFluidState(final BlockState state, final CallbackInfoReturnable<FluidState> callbackInfoReturnable) {
        if (this instanceof Lavaloggable && state.hasProperty(Lavalogged.LAVALOGGED) && state.getValue(Lavalogged.LAVALOGGED)) {
            callbackInfoReturnable.setReturnValue(Fluids.LAVA.defaultFluidState());
        }
    }

}
