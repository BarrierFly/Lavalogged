package org.lavalogged.state;

import com.mojang.serialization.MapCodec;
import java.util.Map;
import net.minecraft.world.level.block.state.properties.Property;

/**
 * Duck interface applied to {@link net.minecraft.world.level.block.state.StateHolder StateHolder}
 * by {@link org.lavalogged.mixin.state.StateMixin StateMixin}, letting
 * {@link LavaStateRefresher LavaStateRefresher} extend existing states with the new property.
 *
 * @param <O> the owner type (Block)
 * @param <S> the state type (BlockState)
 */
public interface StateHolderDuck<O, S> {

	/**
	 * Appends a property and its default value to this state's value set.
	 *
	 * @param property the property to append
	 * @param value the default value for the appended property
	 */
	void lavalogged$addValue(Property<?> property, Comparable<?> value);

	/**
	 * {@return a fresh map of every property value currently held by this state}
	 */
	Map<Property<?>, Comparable<?>> lavalogged$snapshotValues();

	/**
	 * Rebuilds this state's property transition table against the given state lookup, so that
	 * {@code setValue} on the appended property resolves to the matching new state. The map must
	 * contain every property combination of the definition, keyed by value maps equal to the ones
	 * produced by {@link #lavalogged$snapshotValues()}.
	 *
	 * @param states lookup of property values to state for the whole definition
	 */
	void lavalogged$rebuildNeighbours(Map<Map<Property<?>, Comparable<?>>, S> states);

	/**
	 * Attaches a rebuilt codec to this state. Only versions whose states own a codec (1.21.x) do
	 * anything here; on 26.3+ the codec lives on the definition and this is a no-op.
	 *
	 * @param codec the rebuilt state codec
	 */
	void lavalogged$setCodec(MapCodec<S> codec);

}
