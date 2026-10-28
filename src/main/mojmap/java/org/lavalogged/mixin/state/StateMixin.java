package org.lavalogged.mixin.state;

import com.mojang.serialization.MapCodec;
import it.unimi.dsi.fastutil.objects.Reference2ObjectArrayMap;
import java.util.List;
import java.util.Map;
import net.minecraft.world.level.block.state.StateHolder;
import net.minecraft.world.level.block.state.properties.Property;
import org.lavalogged.state.StateHolderDuck;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;

/**
 * Mixin class for the {@link StateHolder State Holder}, letting {@code LavaStateRefresher} extend
 * existing states with the appended property and rebuild their transition tables.
 * <p>
 * The two Minecraft versions of this source set store values differently: 1.21.x keeps a value map
 * per state, 26.3+ switched to parallel property/value arrays with a neighbour matrix, so the
 * implementation branches on the version.
 */
@Mixin(StateHolder.class)
public abstract class StateMixin<O, S extends StateHolder<O, S>> implements StateHolderDuck<O, S> {

	//? if 1.21.11 {
	@Shadow
	@Final
	private Reference2ObjectArrayMap<Property<?>, Comparable<?>> values;

	@Shadow
	private Map<Property<?>, S[]> neighbours;

	@Shadow
	@Final
	@Mutable
	protected MapCodec<S> propertiesCodec;

	@Shadow
	public abstract void populateNeighbours(Map<Map<Property<?>, Comparable<?>>, S> map);
	//?} else {
	/*@Shadow
	@Final
	@Mutable
	private Property<?>[] propertyKeys;

	@Shadow
	@Final
	@Mutable
	private Comparable<?>[] propertyValues;

	@Shadow
	private S[][] neighbors;
	*///?}

	@Override
	public void lavalogged$addValue(final Property<?> property, final Comparable<?> value) {
		//? if 1.21.11 {
		this.values.put(property, value);
		//?} else {
		/*Property<?>[] newKeys = new Property<?>[this.propertyKeys.length + 1];
		System.arraycopy(this.propertyKeys, 0, newKeys, 0, this.propertyKeys.length);
		newKeys[this.propertyKeys.length] = property;
		Comparable<?>[] newValues = new Comparable<?>[this.propertyValues.length + 1];
		System.arraycopy(this.propertyValues, 0, newValues, 0, this.propertyValues.length);
		newValues[this.propertyValues.length] = value;
		this.propertyKeys = newKeys;
		this.propertyValues = newValues;
		 *///?}
	}

	@Override
	public Map<Property<?>, Comparable<?>> lavalogged$snapshotValues() {
		//? if 1.21.11 {
		Reference2ObjectArrayMap<Property<?>, Comparable<?>> snapshot = new Reference2ObjectArrayMap<>();
		snapshot.putAll(this.values);
		return snapshot;
		//?} else {
		/*Reference2ObjectArrayMap<Property<?>, Comparable<?>> snapshot = new Reference2ObjectArrayMap<>(this.propertyKeys.length);
		for (int i = 0; i < this.propertyKeys.length; i++) {
			snapshot.put(this.propertyKeys[i], this.propertyValues[i]);
		}

		return snapshot;
		 *///?}
	}

	@Override
	public void lavalogged$rebuildNeighbours(final Map<Map<Property<?>, Comparable<?>>, S> states) {
		//? if 1.21.11 {
		this.neighbours = null;
		this.populateNeighbours(states);
		//?} else {
		/*Property<?>[] keys = this.propertyKeys;
		@SuppressWarnings("unchecked")
		S[][] matrix = (S[][])(new StateHolder[keys.length][]);
		for (int i = 0; i < keys.length; i++) {
			List<?> possibleValues = keys[i].getPossibleValues();
			@SuppressWarnings("unchecked")
			S[] row = (S[])(new StateHolder[possibleValues.size()]);
			for (int j = 0; j < possibleValues.size(); j++) {
				Comparable<?> pivot = (Comparable<?>)possibleValues.get(j);
				Reference2ObjectArrayMap<Property<?>, Comparable<?>> query = new Reference2ObjectArrayMap<>(keys.length);
				for (int k = 0; k < keys.length; k++) {
					query.put(keys[k], k == i ? pivot : this.propertyValues[k]);
				}

				row[j] = states.get(query);
			}

			matrix[i] = row;
		}

		this.neighbors = matrix;
		 *///?}
	}

	@Override
	public void lavalogged$setCodec(final MapCodec<S> codec) {
		//? if 1.21.11 {
		this.propertiesCodec = codec;
		//?}
	}

}
