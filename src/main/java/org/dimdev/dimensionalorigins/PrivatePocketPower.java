package org.dimdev.dimensionalorigins;

import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.LivingEntity;

import io.github.apace100.apoli.power.Active;
import io.github.apace100.apoli.power.Power;
import io.github.apace100.apoli.power.PowerType;

public class PrivatePocketPower extends Power implements Active {
	private Key key;

	public PrivatePocketPower(PowerType<?> type, LivingEntity entity) {
		super(type, entity);
	}

	@Override
	public void onUse() {
		if (entity.world.isClient && entity instanceof ClientPlayerEntity) {
			PrivatePocketPacket.send();
		}
	}

	@Override
	public Key getKey() {
		return key;
	}

	@Override
	public void setKey(Key key) {
		this.key = key;
	}
}
