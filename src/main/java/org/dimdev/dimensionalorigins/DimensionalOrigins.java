package org.dimdev.dimensionalorigins;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

public class DimensionalOrigins implements ModInitializer {
	public static final String MODID = "extraorigins";

	@Override
	public void onInitialize() {
		DimensionalPowers.init();
		ServerPlayNetworking.registerGlobalReceiver(PrivatePocketPacket.ID, PrivatePocketPacket::handle);
	}
}
