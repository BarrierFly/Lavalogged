package org.lavalogged.mixin.state;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSortedMap;
import com.mojang.serialization.Decoder;
import com.mojang.serialization.Encoder;
import com.mojang.serialization.MapCodec;
import it.unimi.dsi.fastutil.objects.Reference2ObjectArrayMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.function.Supplier;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.StateHolder;
import net.minecraft.world.level.block.state.properties.Property;
import org.lavalogged.state.StateDefinitionDuck;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin class for the {@link StateDefinition State Definition}, capturing the state factory and
 * letting {@code LavaStateRefresher} extend the definition with an extra property.
 */
@Mixin(StateDefinition.class)
public abstract class StateManagerMixin<O, S extends StateHolder<O, S>> implements StateDefinitionDuck<O, S> {

	@Shadow
	@Final
	@Mutable
	private ImmutableSortedMap<String, Property<?>> propertiesByName;

	@Shadow
	@Final
	@Mutable
	private ImmutableList<S> states;

	@Shadow
	public abstract O getOwner();

	@Unique
	private StateDefinition.Factory<O, S> lavalogged$factory;

	// 26.3 moved the codec from the states onto the definition; on 1.21.x this field does not exist.
	//? if 26.3 {
	/*@Shadow
	@Final
	@Mutable
	private MapCodec<S> propertiesCodec;
	*///?}

	@Inject(method = "<init>", at = @At("RETURN"))
	private void lavalogged$captureFactory(final Function<O, S> defaultState, final O owner, final StateDefinition.Factory<O, S> factory, final Map<String, Property<?>> properties, final CallbackInfo info) {
		this.lavalogged$factory = factory;
	}

	@Invoker("appendPropertyCodec")
	private static <S extends StateHolder<?, S>, T extends Comparable<T>> MapCodec<S> lavalogged$appendPropertyCodec(final MapCodec<S> codec, final Supplier<S> defaultState, final String name, final Property<T> property) {
		throw new AssertionError();
	}

	@Override
	public StateDefinition.Factory<O, S> lavalogged$getFactory() {
		return this.lavalogged$factory;
	}

	@Override
	public MapCodec<S> lavalogged$rebuildCodec(final ImmutableSortedMap<String, Property<?>> properties, final Supplier<S> defaultState) {
		MapCodec<S> codec = MapCodec.of(Encoder.empty(), Decoder.unit(defaultState));
		for (final Entry<String, Property<?>> entry : properties.entrySet()) {
			codec = lavalogged$appendPropertyCodec(codec, defaultState, entry.getKey(), entry.getValue());
		}
		return codec;
	}

	@Override
	public void lavalogged$setDefinition(final ImmutableSortedMap<String, Property<?>> properties, final ImmutableList<S> newStates, final MapCodec<S> codec) {
		this.propertiesByName = properties;
		this.states = newStates;
		//? if 26.3 {
		/*this.propertiesCodec = codec;
		 *///?}
	}

	@Override
	public S lavalogged$createState(final ImmutableSortedMap<String, Property<?>> properties, final Map<Property<?>, Comparable<?>> values, final MapCodec<S> codec) {
		//? if 1.21.11 {
		Reference2ObjectArrayMap<Property<?>, Comparable<?>> map = new Reference2ObjectArrayMap<>();
		for (Entry<Property<?>, Comparable<?>> entry : values.entrySet()) {
			map.put(entry.getKey(), entry.getValue());
		}

		return this.lavalogged$factory.create(this.getOwner(), map, codec);
		//?} else {
		/*Property<?>[] keys = properties.values().toArray(new Property<?>[0]);
		Comparable<?>[] newValues = new Comparable<?>[keys.length];
		for (int i = 0; i < keys.length; i++) {
			newValues[i] = values.get(keys[i]);
		}

		return this.lavalogged$factory.create(this.getOwner(), keys, newValues);
		 *///?}
	}

}
