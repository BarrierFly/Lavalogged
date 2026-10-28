package org.lavalogged.state;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSortedMap;
import com.mojang.serialization.MapCodec;
import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.Property;

/**
 * Duck interface applied to {@link StateDefinition StateDefinition} by
 * {@link org.lavalogged.mixin.state.StateManagerMixin StateManagerMixin}, letting
 * {@link LavaStateRefresher LavaStateRefresher} append a property to an existing definition.
 *
 * @param <O> the owner type (Block)
 * @param <S> the state type (BlockState)
 */
public interface StateDefinitionDuck<O, S> {

	/**
	 * {@return the state factory the definition was built with, used to create the extra states}
	 */
	StateDefinition.Factory<O, S> lavalogged$getFactory();

	/**
	 * Rebuilds the definition's state codec so it covers the new property. The codec chain decodes
	 * into the supplied default state and applies one field per property, mirroring the vanilla
	 * constructor logic. The extended property map is passed in explicitly because the definition
	 * still holds the old map until {@link #lavalogged$setDefinition} runs.
	 *
	 * @param properties the extended sorted property map (including the appended property)
	 * @param defaultState supplier of the block's default state
	 * @return the rebuilt codec
	 */
	MapCodec<S> lavalogged$rebuildCodec(ImmutableSortedMap<String, Property<?>> properties, Supplier<S> defaultState);

	/**
	 * Swaps the definition contents to the extended property set and state list. On 26.3+ the
	 * definition also owns the codec, so it is updated here as well.
	 *
	 * @param properties the extended sorted property map
	 * @param states the extended state list (existing states first, then the new variants)
	 * @param propertiesCodec the rebuilt codec, or null when the codec lives on the states
	 */
	void lavalogged$setDefinition(ImmutableSortedMap<String, Property<?>> properties, ImmutableList<S> states, MapCodec<S> propertiesCodec);

	/**
	 * Creates a new state of this definition with the given property values.
	 *
	 * @param properties the extended sorted property map; on 26.3+ the key array is built from it,
	 *                   on 1.21.x it is unused
	 * @param values every property value of the new state, including the appended property
	 * @param codec the rebuilt codec to attach to the new state
	 * @return the freshly created state (not yet registered or initialized)
	 */
	S lavalogged$createState(ImmutableSortedMap<String, Property<?>> properties, Map<Property<?>, Comparable<?>> values, MapCodec<S> codec);

}
