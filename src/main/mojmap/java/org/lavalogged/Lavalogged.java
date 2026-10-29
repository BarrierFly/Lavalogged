package org.lavalogged;

import net.fabricmc.api.ModInitializer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import org.lavalogged.Lavaloggable;
import org.lavalogged.state.LavaStateRefresher;

/**
 * {@link Lavalogged Lavalogged}
 * Make waterloggable blocks also be able to be lavalogged!
 * <p>
 * 1.21.11 and 26.3 no longer depend on Statement: the property is appended by
 * {@link LavaStateRefresher LavaStateRefresher} directly, keeping every vanilla
 * block state id stable (new variants are appended to the end of the id list).
 */
public final class Lavalogged implements ModInitializer {

	/**
	 * The {@link BooleanProperty Lavalogged} property
	 */
	public static final BooleanProperty LAVALOGGED = BooleanProperty.create("lavalogged");

	/**
	 * Initialize the mod
	 */
	@Override
	public void onInitialize() {
		// Registry iteration order equals raw id order, so the appended lavalogged=true
		// states receive identical ids on client and server.
		BuiltInRegistries.BLOCK.forEach(block -> {
			if (block instanceof Lavaloggable) {
				LavaStateRefresher.addBlockProperty(block, LAVALOGGED, false);
			}
		});
	}

}
