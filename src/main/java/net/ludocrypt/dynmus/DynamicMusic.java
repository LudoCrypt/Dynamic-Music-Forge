package net.ludocrypt.dynmus;

import net.ludocrypt.dynmus.config.DynamicMusicConfig;
import net.ludocrypt.dynmus.config.MusicConfig;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.Material;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.loading.FMLPaths;

@Mod("dynmus")
public class DynamicMusic {

	public DynamicMusic() {
		ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, DynamicMusicConfig.client_config);
		DynamicMusicConfig.loadConfig(DynamicMusicConfig.client_config, FMLPaths.CONFIGDIR.get().resolve("dynmus-client.toml").toString());
		MinecraftForge.EVENT_BUS.register(this);
		DynamicMusicSounds.init();
	}

	public static ResourceLocation id(String id) {
		return new ResourceLocation("dynmus", id);
	}

	public static boolean isInCave(Level level, BlockPos pos) {
		int searchRange = MusicConfig.searchRange.get();

		if (searchRange >= 1 && !level.canSeeSky(pos)) {
			int darkBlocks = 0;
			int stoneBlocks = 0;
			int airBlocks = 0;

			for (int x = -searchRange; x < searchRange; x++) {
				for (int y = -searchRange; y < searchRange; y++) {
					for (int z = -searchRange; z < searchRange; z++) {
						BlockPos offsetPos = pos.offset(x, y, z);
						if (level.isEmptyBlock(offsetPos)) {
							airBlocks++;
							if (level.getMaxLocalRawBrightness(offsetPos) <= MusicConfig.darknessCap.get()) {
								darkBlocks++;
							}
						}
						if (level.getBlockState(offsetPos).getMaterial() == Material.LAVA) {
							darkBlocks++;
						}
						if (level.getBlockState(offsetPos).getMaterial() == Material.STONE) {
							stoneBlocks++;
						}
					}
				}
			}

			double blockCount = Math.pow(searchRange * 2, 3);

			double stonePercentage = ((double) stoneBlocks) / ((double) blockCount);
			double darkPercentage = ((double) darkBlocks) / ((double) airBlocks);

			if (darkPercentage >= MusicConfig.darknessPercent.get()) {
				if (stonePercentage >= MusicConfig.stonePercent.get()) {
					return true;
				}
			}
		}
		return false;
	}

	public static double getAverageDarkness(Level level, BlockPos pos) {
		int searchRange = MusicConfig.searchRange.get();

		if (searchRange >= 1) {
			int airBlocks = 0;
			int lightTogether = 0;

			for (int x = -searchRange; x < searchRange; x++) {
				for (int y = -searchRange; y < searchRange; y++) {
					for (int z = -searchRange; z < searchRange; z++) {
						BlockPos offsetPos = pos.offset(x, y, z);
						if (level.isEmptyBlock(offsetPos)) {
							airBlocks++;
							lightTogether += level.getMaxLocalRawBrightness(offsetPos);
						}
					}
				}
			}

			return (((double) lightTogether) / ((double) airBlocks));

		}
		return 15;
	}

	public static boolean isInPseudoMineshaft(Level level, BlockPos pos) {
		int searchRange = MusicConfig.pseudoMineshaftSearchRange.get();

		if (searchRange >= 1) {

			int pseudoMineshaftBlocks = 0;
			int airBlocks = 0;

			for (int x = -searchRange; x < searchRange; x++) {
				for (int y = -searchRange; y < searchRange; y++) {
					for (int z = -searchRange; z < searchRange; z++) {
						BlockPos offsetPos = pos.offset(x, y, z);

						if (level.getBlockState(offsetPos).getMaterial() == Material.WOOD || level.getBlockState(offsetPos).getBlock() == Blocks.RAIL || level.getBlockState(offsetPos).getMaterial() == Material.WEB) {
							pseudoMineshaftBlocks++;
						}

						if (level.isEmptyBlock(offsetPos)) {
							airBlocks++;
						}

					}
				}
			}

			double mineshaftPercentage = ((double) pseudoMineshaftBlocks) / ((double) airBlocks);

			if (mineshaftPercentage >= MusicConfig.pseudoMineshaftPercent.get()) {
				return true;
			}

		}

		return false;
	}

}
