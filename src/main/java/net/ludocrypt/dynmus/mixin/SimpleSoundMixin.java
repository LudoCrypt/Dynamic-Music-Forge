package net.ludocrypt.dynmus.mixin;

import java.util.Random;

import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.ludocrypt.dynmus.config.MusicConfig;
import net.minecraft.client.Minecraft;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(value = Dist.CLIENT)
@Mixin(value = SimpleSoundInstance.class, priority = 80)
public class SimpleSoundMixin {

	@Inject(method = "Lnet/minecraft/client/resources/sounds/SimpleSoundInstance;forMusic(Lnet/minecraft/sounds/SoundEvent;)Lnet/minecraft/client/resources/sounds/SimpleSoundInstance;", at = @At("RETURN"), cancellable = true)
	private static void dynmus$changePitch(SoundEvent music, CallbackInfoReturnable<SimpleSoundInstance> ci) {
		if (MusicConfig.dynamicPitch.get()) {
			Random random = new Random();
			Minecraft client = Minecraft.getInstance();
			if (client.level != null) {
				client.level.getDayTime();
				long absTime = Math.abs(client.level.getDayTime() - MusicConfig.dynamicPitchAnchor.get());
				double delta = absTime * (0.0001832172957);
				double chance = Mth.lerp(delta, 1, 0);
				if (random.nextDouble() < chance) {
					double minPitch = Mth.lerp(delta, -12, 0);
					double maxPitch = Mth.lerp(random.nextDouble(), minPitch / 3, random.nextDouble() * -1);
					double note = Mth.lerp(random.nextDouble(), MusicConfig.dynamicPitchFaster.get() ? -minPitch : minPitch, MusicConfig.dynamicPitchFaster.get() ? -maxPitch : maxPitch);
					double newPitch = Math.pow(2.0D, note / 12.0D);
					ci.setReturnValue(new SimpleSoundInstance(music.getRegistryName(), SoundSource.MUSIC, 1.0F, (float) newPitch, false, 0, SoundInstance.Attenuation.NONE, 0.0D, 0.0D, 0.0D, true));
				}
			}
		}
	}

}
