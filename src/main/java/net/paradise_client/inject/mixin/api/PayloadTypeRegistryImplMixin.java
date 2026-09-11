package net.paradise_client.inject.mixin.api;

import net.fabricmc.fabric.impl.networking.PayloadTypeRegistryImpl;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.paradise_client.inject.accessor.PayloadTypeRegistryImplAccessor;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.*;
import java.util.stream.Collectors;

@Mixin(PayloadTypeRegistryImpl.class)
public abstract class PayloadTypeRegistryImplMixin<B extends FriendlyByteBuf> implements PayloadTypeRegistryImplAccessor {
  @Shadow(remap = false) @Final private Map<Identifier, CustomPacketPayload.TypeAndCodec<B, ? extends CustomPacketPayload>> packetTypes;

  @Inject(method = "register", at = @At(value = "RETURN"))
  public <T extends CustomPacketPayload> void register(CustomPacketPayload.Type<T> id,
    StreamCodec<? super B, T> codec,
    CallbackInfoReturnable<CustomPacketPayload.TypeAndCodec<? super B, T>> cir) {
  }

  @Override public ArrayList<String> paradiseClient$getRegisteredChannelsByName() {
    return packetTypes.keySet().stream().map(Identifier::toString).collect(Collectors.toCollection(ArrayList::new));
  }
}
