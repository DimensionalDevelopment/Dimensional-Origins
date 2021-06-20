package org.dimdev.dimensionalorigins;

import java.util.LinkedHashMap;
import java.util.Map;

import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.Active;
import io.github.apace100.apoli.power.Power;
import io.github.apace100.apoli.power.factory.PowerFactory;
import io.github.apace100.apoli.registry.ApoliRegistries;
import io.github.apace100.calio.data.SerializableData;

public class DimensionalPowers {
	private static final Map<PowerFactory<?>, Identifier> POWER_FACTORIES = new LinkedHashMap<>();

	public static final PowerFactory<Power> PRIVATE_POCKET_SHIFT = create(new PowerFactory<>(new Identifier("dimensionalorigins", "private_pocket_shift"), new SerializableData().add("key", ApoliDataTypes.KEY), data -> (type, entity) -> {
		PrivatePocketPower power = new PrivatePocketPower(type, entity);
		power.setKey((Active.Key) data.get("key"));
		return power;
	}).allowCondition());

	private static <T extends Power> PowerFactory<T> create(PowerFactory<T> factory) {
		POWER_FACTORIES.put(factory, factory.getSerializerId());
		return factory;
	}

	public static void init() {
		POWER_FACTORIES.keySet().forEach(powerType -> Registry.register(ApoliRegistries.POWER_FACTORY, POWER_FACTORIES.get(powerType), powerType));
	}
}
