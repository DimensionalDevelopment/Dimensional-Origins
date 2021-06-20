package org.dimdev.dimensionalorigins;

import java.util.UUID;

import net.minecraft.block.BlockState;
import net.minecraft.block.Fertilizable;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.EulerAngle;
import net.minecraft.util.math.Vec3d;

import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import org.dimdev.dimdoors.api.rift.target.EntityTarget;
import org.dimdev.dimdoors.api.util.EntityUtils;
import org.dimdev.dimdoors.api.util.Location;
import org.dimdev.dimdoors.block.entity.RiftBlockEntity;
import org.dimdev.dimdoors.pockets.PocketGenerator;
import org.dimdev.dimdoors.world.ModDimensions;
import org.dimdev.dimdoors.world.level.registry.DimensionalRegistry;
import org.dimdev.dimdoors.world.pocket.VirtualLocation;
import org.dimdev.dimdoors.world.pocket.type.Pocket;
import org.dimdev.dimdoors.world.pocket.type.PrivatePocket;

public class PrivatePocketPacket {
	public static final Identifier ID = new Identifier("dimensionalorigins", "bone_meal");
	
	public static void send() {
		PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
		ClientPlayNetworking.send(ID, buf);
	}
	
	public static void handle(MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler network, PacketByteBuf buf, PacketSender sender) {
		server.execute(() -> {
			Location location = new Location(player.getServerWorld(), player.getBlockPos());

			Vec3d relativePos = new Vec3d(0, 0, 0);
			EulerAngle relativeAngle = new EulerAngle(player.getPitch(), player.getYaw(), 0);
			Vec3d relativeVelocity = player.getVelocity();

			UUID uuid = player.getUuid();

			Pocket pocket = DimensionalRegistry.getPrivateRegistry().getPrivatePocket(uuid);

			if (ModDimensions.isPrivatePocketDimension(location.getWorld())) {
				if (pocket != null && DimensionalRegistry.getPocketDirectory(pocket.getWorld()).getPocketAt(location.pos).equals(pocket)) {
					exitLogic(player, uuid, location, pocket, relativePos, relativeAngle, relativeVelocity);
				}
			} else {
				entranceLogic(player, uuid, location, relativePos, relativeAngle, relativeVelocity);
			}
		});
	}

	public static void exitLogic(ServerPlayerEntity entity, UUID uuid, Location location, Pocket pocket, Vec3d relativePos, EulerAngle relativeAngle, Vec3d relativeVelocity) {
		Location destLoc = DimensionalRegistry.getRiftRegistry().getPrivatePocketExit(uuid);

		DimensionalRegistry.getRiftRegistry().setLastPrivatePocketEntrance(uuid, location); // Remember which exit was used for next time the pocket is entered

		if (destLoc == null || !(destLoc.getBlockEntity() instanceof RiftBlockEntity)) {
			if (destLoc == null) {
				EntityUtils.chat(entity, new TranslatableText("rifts.destinations.private_pocket_exit.did_not_use_rift"));
			} else {
				EntityUtils.chat(entity, new TranslatableText("rifts.destinations.private_pocket_exit.rift_has_closed"));
			}
		} else {
			((EntityTarget) destLoc.getBlockEntity()).receiveEntity(entity, relativePos, relativeAngle, relativeVelocity);
		}
	}

	public static void entranceLogic(ServerPlayerEntity entity, UUID uuid, Location virtualLocation, Vec3d relativePos, EulerAngle relativeAngle, Vec3d relativeVelocity) {
		PrivatePocket pocket = DimensionalRegistry.getPrivateRegistry().getPrivatePocket(uuid);
		if (pocket == null) { // generate the private pocket and get its entrances
			// set to where the pocket was first created
			Pocket unknownTypePocket = PocketGenerator.generatePrivatePocketV2(new VirtualLocation(virtualLocation.getWorldId(), virtualLocation.getX(), virtualLocation.getZ(), -1));
			if (! (unknownTypePocket instanceof PrivatePocket)) throw new RuntimeException("Pocket generated for private pocket is not of type PrivatePocket");
			pocket = (PrivatePocket) unknownTypePocket;


			DimensionalRegistry.getPrivateRegistry().setPrivatePocketID(uuid, pocket);
			BlockEntity be = DimensionalRegistry.getRiftRegistry().getPocketEntrance(pocket).getBlockEntity();
			processEntity(be, entity, uuid, relativePos, relativeAngle, relativeVelocity, virtualLocation);
		} else {
			Location destLoc = DimensionalRegistry.getRiftRegistry().getPrivatePocketEntrance(uuid); // get the last used entrances
			if (destLoc == null)
				destLoc = DimensionalRegistry.getRiftRegistry().getPocketEntrance(pocket); // if there's none, then set the target to the main entrances
			if (destLoc == null) { // if the pocket entrances is gone, then create a new private pocket
				//LOGGER.info("All entrances are gone, creating a new private pocket!");
				Pocket unknownTypePocket = PocketGenerator.generatePrivatePocketV2(new VirtualLocation(virtualLocation.getWorldId(), virtualLocation.getX(), virtualLocation.getZ(), -1));
				if (! (unknownTypePocket instanceof PrivatePocket)) throw new RuntimeException("Pocket generated for private pocket is not of type PrivatePocket");
				pocket = (PrivatePocket) unknownTypePocket;

				DimensionalRegistry.getPrivateRegistry().setPrivatePocketID(uuid, pocket);
				destLoc = DimensionalRegistry.getRiftRegistry().getPocketEntrance(pocket);
			}

			processEntity(destLoc.getBlockEntity(), entity, uuid, relativePos, relativeAngle, relativeVelocity, virtualLocation);
		}
	}

	private static void processEntity(BlockEntity blockEntity, Entity entity, UUID uuid, Vec3d relativePos, EulerAngle relativeAngle, Vec3d relativeVelocity, Location location) {
		((EntityTarget) blockEntity).receiveEntity(entity, relativePos, relativeAngle, relativeVelocity);
		DimensionalRegistry.getRiftRegistry().setLastPrivatePocketExit(uuid, location);
	}
}