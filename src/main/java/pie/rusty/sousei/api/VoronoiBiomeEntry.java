package pie.rusty.sousei.api;

import net.minecraft.world.level.biome.Biome;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.resources.ResourceKey;
import net.minecraft.core.registries.Registries;

import java.util.Optional;
import java.util.List;

import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mojang.serialization.Codec;

public record VoronoiBiomeEntry(ResourceKey<Biome> biomeKey, int priority, int gridCellSize, double minRadius, double maxRadius, double minRadiusRatio, long salt, float rarity, ClimateBounds climate, int minY, int maxY,
		Optional<List<ResourceKey<Biome>>> validParentBiomes, Optional<List<ResourceLocation>> dimensions) {
	public record ClimateBounds(FloatRange temperature, FloatRange humidity, FloatRange continentalness, FloatRange erosion, FloatRange depth, FloatRange weirdness) {
		public static final Codec<ClimateBounds> CODEC = RecordCodecBuilder.create(instance -> instance
				.group(FloatRange.CODEC.optionalFieldOf("temperature", new FloatRange(-1f, 1f)).forGetter(ClimateBounds::temperature), FloatRange.CODEC.optionalFieldOf("humidity", new FloatRange(-1f, 1f)).forGetter(ClimateBounds::humidity),
						FloatRange.CODEC.optionalFieldOf("continentalness", new FloatRange(-1f, 1f)).forGetter(ClimateBounds::continentalness), FloatRange.CODEC.optionalFieldOf("erosion", new FloatRange(-1f, 1f)).forGetter(ClimateBounds::erosion),
						FloatRange.CODEC.optionalFieldOf("depth", new FloatRange(-1f, 1f)).forGetter(ClimateBounds::depth), FloatRange.CODEC.optionalFieldOf("weirdness", new FloatRange(-1f, 1f)).forGetter(ClimateBounds::weirdness))
				.apply(instance, ClimateBounds::new));
		public static final ClimateBounds DEFAULT = new ClimateBounds(new FloatRange(-1f, 1f), new FloatRange(-1f, 1f), new FloatRange(-1f, 1f), new FloatRange(-1f, 1f), new FloatRange(-1f, 1f), new FloatRange(-1f, 1f));
	}

	public VoronoiBiomeEntry {
		if (gridCellSize <= 0)
			gridCellSize = 100;
		if (minRadius > maxRadius) {
			double temp = minRadius;
			minRadius = maxRadius;
			maxRadius = temp;
		}
		minRadius = Math.max(0.0, minRadius);
		maxRadius = Math.max(0.0, maxRadius);
		minRadiusRatio = Math.max(0.0, Math.min(1.0, minRadiusRatio));
		rarity = Math.max(0.0f, Math.min(1.0f, rarity));
		if (climate == null)
			climate = ClimateBounds.DEFAULT;
	}

	public static final Codec<VoronoiBiomeEntry> CODEC = RecordCodecBuilder.create(instance -> instance.group(ResourceKey.codec(Registries.BIOME).fieldOf("biome").forGetter(VoronoiBiomeEntry::biomeKey),
			Codec.INT.optionalFieldOf("priority", 0).forGetter(VoronoiBiomeEntry::priority), Codec.INT.fieldOf("grid_cell_size").forGetter(VoronoiBiomeEntry::gridCellSize),
			Codec.DOUBLE.optionalFieldOf("min_radius", 0.0).forGetter(VoronoiBiomeEntry::minRadius), Codec.DOUBLE.fieldOf("max_radius").forGetter(VoronoiBiomeEntry::maxRadius),
			Codec.DOUBLE.optionalFieldOf("inner_radius", 0.0).forGetter(VoronoiBiomeEntry::minRadiusRatio), Codec.LONG.fieldOf("salt").forGetter(VoronoiBiomeEntry::salt), Codec.FLOAT.fieldOf("rarity").forGetter(VoronoiBiomeEntry::rarity),
			ClimateBounds.CODEC.optionalFieldOf("climate", ClimateBounds.DEFAULT).forGetter(VoronoiBiomeEntry::climate), Codec.INT.optionalFieldOf("min_y", -64).forGetter(VoronoiBiomeEntry::minY),
			Codec.INT.optionalFieldOf("max_y", 320).forGetter(VoronoiBiomeEntry::maxY), ResourceKey.codec(Registries.BIOME).listOf().optionalFieldOf("valid_parent_biomes").forGetter(VoronoiBiomeEntry::validParentBiomes),
			ResourceLocation.CODEC.listOf().optionalFieldOf("dimensions").forGetter(VoronoiBiomeEntry::dimensions)).apply(instance, VoronoiBiomeEntry::new));
}