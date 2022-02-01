package net.ludocrypt.dynmus.mixin;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.Music;
import net.minecraft.sounds.Musics;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.ludocrypt.dynmus.DynamicMusic;
import net.ludocrypt.dynmus.DynamicMusicSounds;
import net.ludocrypt.dynmus.config.MusicConfig;
import net.minecraft.client.Minecraft;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;

@OnlyIn(value = Dist.CLIENT)
@Mixin(Minecraft.class)
public class MinecraftMixin {

	@Shadow
	public LocalPlayer player;
	@Shadow
	public ClientLevel level;

	@Inject(method = "getSituationalMusic", at = @At("RETURN"), cancellable = true)
	private void dynmus$getMusic(CallbackInfoReturnable<Music> ci) {
		if (ci.getReturnValue() == Musics.GAME || ci.getReturnValue() == Musics.CREATIVE) {
			if (this.level != null) {
				if (DynamicMusic.isInCave(level, player.blockPosition())) {
					dynmus$setReturnType(ci, DynamicMusicSounds.MUSIC_CAVE);
				} else if ((level.getBiomeManager().getBiome(this.player.blockPosition()).getBaseTemperature() < 0.15F) || (level.isRaining()) && MusicConfig.coldMusic.get()) {
					dynmus$setReturnType(ci, DynamicMusicSounds.MUSIC_COLD);
				} else if ((level.getBiomeManager().getBiome(this.player.blockPosition()).getBaseTemperature() > 0.95F) && (!level.isRaining()) && MusicConfig.hotMusic.get()) {
					dynmus$setReturnType(ci, DynamicMusicSounds.MUSIC_HOT);
				} else if (level.getDayTime() <= 12500 && MusicConfig.niceMusic.get()) {
					dynmus$setReturnType(ci, DynamicMusicSounds.MUSIC_NICE);
				} else if (level.getDayTime() > 12500 && MusicConfig.downMusic.get()) {
					dynmus$setReturnType(ci, DynamicMusicSounds.MUSIC_DOWN);
				}
			}
		} else if (ci.getReturnValue() == Musics.END_BOSS) {
			ci.setReturnValue(new Music(DynamicMusicSounds.MUSIC_END_BOSS, 0, 0, true));
		} else if (ci.getReturnValue() == Musics.END) {
			if (this.player.getAbilities().instabuild && this.player.getAbilities().flying) {
				ci.setReturnValue(new Music(DynamicMusicSounds.MUSIC_END_CREATIVE, 1200, 8000, true));
			}
		}
	}
	
	@Unique
	private void dynmus$setReturnType(CallbackInfoReturnable<Music> ci, SoundEvent e) {
		ci.setReturnValue(Musics.createGameMusic(new SoundEvent(ci.getReturnValue() == Musics.CREATIVE ? e.getRegistryName() : new ResourceLocation(String.join("", e.getRegistryName().toString(), ".creative")))));
	}
}
