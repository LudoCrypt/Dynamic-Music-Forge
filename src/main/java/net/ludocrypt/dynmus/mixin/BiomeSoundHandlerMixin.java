package net.ludocrypt.dynmus.mixin;

import java.util.Optional;
import java.util.Random;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.resources.sounds.BiomeAmbientSoundsHandler;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.AmbientMoodSettings;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.ludocrypt.dynmus.DynamicMusic;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(value = Dist.CLIENT)
@Mixin(BiomeAmbientSoundsHandler.class)
public class BiomeSoundHandlerMixin {

	@Shadow
	private float moodiness;

	@Shadow
	@Final
	private LocalPlayer player;

	@Shadow
	private Optional<AmbientMoodSettings> moodSettings;

	@Shadow
	@Final
	private Random random;

	@Inject(method = "tick", at = @At("HEAD"))
	private void dynmus$tick(CallbackInfo ci) {
		this.moodSettings.ifPresent((biomeMoodSound) -> {
			Level world = this.player.level;
			if (DynamicMusic.isInCave(world, player.blockPosition()) && DynamicMusic.isInPseudoMineshaft(world, player.blockPosition())) {
				this.moodiness += (float) ((15 - DynamicMusic.getAverageDarkness(world, player.blockPosition())) / (float) biomeMoodSound.getTickDelay());
			}
		});
	}
}
