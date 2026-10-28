package org.lavalogged.state;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSortedMap;
import com.google.common.collect.Iterables;
import com.mojang.serialization.MapCodec;
import it.unimi.dsi.fastutil.objects.Reference2ObjectArrayMap;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.Property;

/**
 * Appends the {@code lavalogged} property to an existing block's {@link StateDefinition}, in the
 * spirit of what the Statement API does for older versions but scoped down to one boolean property.
 * <p>
 * The existing states are mutated in place (keeping their registry and network ids), the missing
 * {@code lavalogged=true} variants are created through the block's own state factory and appended
 * to the end of {@link Block#BLOCK_STATE_REGISTRY}, and every state's property transition table is
 * rebuilt so {@code setValue} resolves across the new property.
 * <p>
 * Must run after registries are frozen and before any world loads, exactly like a
 * {@code ModInitializer#onInitialize} body does.
 */
public final class LavaStateRefresher {

	private LavaStateRefresher() { }

	/**
	 * Appends {@code property} to the given block, unless the block already has it.
	 *
	 * @param block the block to extend
	 * @param property the boolean property to append
	 * @param defaultValue the default value of the appended property
	 */
	public static void addBlockProperty(final Block block, final Property<Boolean> property, final boolean defaultValue) {
		final StateDefinition<Block, BlockState> definition = block.getStateDefinition();
		if (definition.getProperty(property.getName()) != null) {
			return;
		}

		@SuppressWarnings("unchecked")
		final StateDefinitionDuck<Block, BlockState> definitionDuck = (StateDefinitionDuck<Block, BlockState>) definition;
		final StateDefinition.Factory<Block, BlockState> factory = definitionDuck.lavalogged$getFactory();
		if (factory == null) {
			throw new IllegalStateException("No state factory captured for " + block);
		}

		// Rebuild the sorted property map with the new property appended.
		final ImmutableSortedMap.Builder<String, Property<?>> propertiesBuilder = ImmutableSortedMap.naturalOrder();
		for (final Property<?> existing : definition.getProperties()) {
			propertiesBuilder.put(existing.getName(), existing);
		}
		propertiesBuilder.put(property.getName(), property);
		final ImmutableSortedMap<String, Property<?>> properties = propertiesBuilder.build();

		// Rebuild the codec so the new property survives JSON/NBT round trips. The extended
		// property map is passed in explicitly: the definition still holds the old one here.
		final Supplier<BlockState> defaultState = () -> block.defaultBlockState();
		@SuppressWarnings("unchecked")
		final MapCodec<BlockState> codec = (MapCodec<BlockState>) definitionDuck.lavalogged$rebuildCodec(properties, defaultState);

		// Extend every existing state with the default value, then create the missing variants.
		final ImmutableList<BlockState> oldStates = definition.getPossibleStates();
		final Map<Map<Property<?>, Comparable<?>>, BlockState> statesByValues = new LinkedHashMap<>();
		for (final BlockState state : oldStates) {
			((StateHolderDuck<Block, BlockState>) state).lavalogged$addValue(property, defaultValue);
			statesByValues.put(snapshot(state), state);
		}

		final List<BlockState> newStates = new ArrayList<>(oldStates.size());
		for (final BlockState state : oldStates) {
			final Map<Property<?>, Comparable<?>> values = new LinkedHashMap<>(((StateHolderDuck<Block, BlockState>) state).lavalogged$snapshotValues());
			values.put(property, Boolean.TRUE);
			final BlockState created = definitionDuck.lavalogged$createState(properties, values, codec);
			newStates.add(created);
			statesByValues.put(snapshot(created), created);
		}

		// Rebuild the property transition tables, then initialize the runtime caches of the new
		// states: vanilla only calls initCache during registration, so the cached fluid state of
		// the new variants would otherwise stay empty and never report lava.
		for (final BlockState state : Iterables.concat(oldStates, newStates)) {
			((StateHolderDuck<Block, BlockState>) state).lavalogged$rebuildNeighbours(statesByValues);
		}
		for (final BlockState state : newStates) {
			state.initCache();
		}

		definitionDuck.lavalogged$setDefinition(properties, ImmutableList.<BlockState>builder().addAll(oldStates).addAll(newStates).build(), codec);

		// Register the new variants at the end of the id list; existing ids stay untouched so the
		// client and server agree on ids as long as both run this in the same registry order.
		for (final BlockState state : newStates) {
			if (Block.BLOCK_STATE_REGISTRY.getId(state) == -1) {
				Block.BLOCK_STATE_REGISTRY.add(state);
			}
		}
	}

	private static Map<Property<?>, Comparable<?>> snapshot(final BlockState state) {
		return new Reference2ObjectArrayMap<>(((StateHolderDuck<Block, BlockState>) state).lavalogged$snapshotValues());
	}

}
