package pie.rusty.sousei.mixin;

import pie.rusty.sousei.api.BiomeSourceDimensionHolder;

import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.Mixin;

import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.resources.ResourceKey;
import net.minecraft.core.Holder;

import java.util.Optional;

@Mixin(NoiseBasedChunkGenerator.class)
public abstract class NoiseBasedChunkGeneratorMixin {
	@Unique
	private static final ResourceLocation SOUSEI$OVERWORLD = ResourceLocation.fromNamespaceAndPath("minecraft", "overworld");
	@Unique
	private static final ResourceLocation SOUSEI$THE_NETHER = ResourceLocation.fromNamespaceAndPath("minecraft", "the_nether");
	@Unique
	private static final ResourceLocation SOUSEI$THE_END = ResourceLocation.fromNamespaceAndPath("minecraft", "the_end");

	@Inject(method = "<init>", at = @At("TAIL"))
	private void sousei$attachDimensionToBiomeSource(BiomeSource biomeSource, Holder<NoiseGeneratorSettings> settings, CallbackInfo ci) {
		if (biomeSource instanceof BiomeSourceDimensionHolder holder) {
			Optional<ResourceKey<NoiseGeneratorSettings>> key = settings.unwrapKey();
			if (key.isPresent()) {
				ResourceLocation settingsId = key.get().location();
				ResourceLocation dimLocation = sousei$resolveDimensionFromSettings(settingsId);
				holder.sousei$setDimension(dimLocation);
			}
		}
	}

	@Unique
	private static ResourceLocation sousei$resolveDimensionFromSettings(ResourceLocation settingsId) {
		String path = settingsId.getPath();
		if (path.contains("nether")) {
			return SOUSEI$THE_NETHER;
		} else if (path.contains("end")) {
			return SOUSEI$THE_END;
		} else {
			return SOUSEI$OVERWORLD;
		}
	}
}