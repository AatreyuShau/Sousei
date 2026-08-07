package pie.rusty.sousei.api;

import net.minecraft.world.level.biome.Biome;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.resources.ResourceKey;
import net.minecraft.core.Holder;

import java.util.Set;
import java.util.HashSet;

public final class BakedVoronoiEntry {
	public static final int BASE_SECTOR_SIZE = 400;
	public final Holder<Biome> biomeHolder;
	public final VoronoiBiomeEntry rawEntry;
	public final int priority;
	public final int step;
	public final int effectiveCellSize;
	public final double minRadius;
	public final double maxRadius;
	public final double innerRadius;
	public final long salt;
	public final float rarityPercent;
	public final int minY;
	public final int maxY;
	public final float minTemp, maxTemp;
	public final float minHumid, maxHumid;
	public final float minCont, maxCont;
	public final float minEros, maxEros;
	public final float minDepth, maxDepth;
	public final float minWeird, maxWeird;
	public final Set<ResourceKey<Biome>> validParents;
	public final Set<ResourceLocation> dimensions;

	public BakedVoronoiEntry(VoronoiBiomeEntry raw, Holder<Biome> holder) {
		this.rawEntry = raw;
		this.biomeHolder = holder;
		this.priority = raw.priority();
		int rawCellSize = raw.gridCellSize();
		this.step = Math.max(1, rawCellSize + 1);
		int multiplier = Math.max(1, rawCellSize);
		this.effectiveCellSize = BASE_SECTOR_SIZE * multiplier;
		this.minRadius = Math.max(0.0, raw.minRadius());
		this.maxRadius = Math.max(this.minRadius, raw.maxRadius());
		double ratio = Math.max(0.0, Math.min(1.0, raw.minRadiusRatio()));
		this.innerRadius = Math.max(this.minRadius, this.maxRadius * ratio);
		this.salt = raw.salt();
		this.rarityPercent = raw.rarity() * 100.0f;
		this.minY = raw.minY();
		this.maxY = raw.maxY();
		var c = raw.climate();
		this.minTemp = c.temperature().min();
		this.maxTemp = c.temperature().max();
		this.minHumid = c.humidity().min();
		this.maxHumid = c.humidity().max();
		this.minCont = c.continentalness().min();
		this.maxCont = c.continentalness().max();
		this.minEros = c.erosion().min();
		this.maxEros = c.erosion().max();
		this.minDepth = c.depth().min();
		this.maxDepth = c.depth().max();
		this.minWeird = c.weirdness().min();
		this.maxWeird = c.weirdness().max();
		this.validParents = raw.validParentBiomes().map(HashSet::new).orElse(null);
		this.dimensions = raw.dimensions().map(HashSet::new).orElse(null);
	}

	public boolean testClimate(float temp, float humid, float cont, float eros, float dep, float weird) {
		return temp >= minTemp && temp <= maxTemp && humid >= minHumid && humid <= maxHumid && cont >= minCont && cont <= maxCont && eros >= minEros && eros <= maxEros && dep >= minDepth && dep <= maxDepth && weird >= minWeird && weird <= maxWeird;
	}

	public boolean matchesParent(ResourceKey<Biome> parentKey) {
		if (validParents == null)
			return true;
		if (parentKey == null)
			return false;
		return validParents.contains(parentKey);
	}

	public boolean matchesDimension(ResourceLocation dim) {
		if (dimensions == null)
			return true;
		return dimensions.contains(dim);
	}
}
/*public final class BakedVoronoiEntry {
	public final Holder<Biome> biomeHolder;
	public final VoronoiBiomeEntry rawEntry;
	public final int priority;
	public final int gridCellSize;
	public final double minRadiusSq;
	public final double maxRadiusSq;
	public final double radiusDiff;
	public final double minRadiusRatioSq;
	public final long salt;
	public final float rarityPercent;
	public final int minY;
	public final int maxY;
	public final float minTemp, maxTemp;
	public final float minHumid, maxHumid;
	public final float minCont, maxCont;
	public final float minEros, maxEros;
	public final float minDepth, maxDepth;
	public final float minWeird, maxWeird;
	public final ResourceKey<Biome>[] validParents;
	public final ResourceLocation[] dimensions;
	@SuppressWarnings("unchecked")
	public BakedVoronoiEntry(VoronoiBiomeEntry raw, Holder<Biome> holder) {
		this.rawEntry = raw;
		this.biomeHolder = holder;
		this.priority = raw.priority();
		this.gridCellSize = raw.gridCellSize();
		this.minRadiusSq = raw.minRadius() * raw.minRadius();
		this.maxRadiusSq = raw.maxRadius() * raw.maxRadius();
		this.radiusDiff = raw.maxRadius() - raw.minRadius();
		double minRad = raw.minRadiusRatio() * raw.maxRadius();
		this.minRadiusRatioSq = minRad * minRad;
		this.salt = raw.salt();
		this.rarityPercent = raw.rarity() * 100.0f;
		this.minY = raw.minY();
		this.maxY = raw.maxY();
		var c = raw.climate();
		this.minTemp = c.temperature().min();
		this.maxTemp = c.temperature().max();
		this.minHumid = c.humidity().min();
		this.maxHumid = c.humidity().max();
		this.minCont = c.continentalness().min();
		this.maxCont = c.continentalness().max();
		this.minEros = c.erosion().min();
		this.maxEros = c.erosion().max();
		this.minDepth = c.depth().min();
		this.maxDepth = c.depth().max();
		this.minWeird = c.weirdness().min();
		this.maxWeird = c.weirdness().max();
		this.validParents = raw.validParentBiomes().map(list -> list.toArray(new ResourceKey[0])).orElse(null);
		this.dimensions = raw.dimensions().map(list -> list.toArray(new ResourceLocation[0])).orElse(null);
	}
	public boolean testClimate(float temp, float humid, float cont, float eros, float dep, float weird) {
		return temp >= minTemp && temp <= maxTemp && humid >= minHumid && humid <= maxHumid && cont >= minCont && cont <= maxCont && eros >= minEros && eros <= maxEros && dep >= minDepth && dep <= maxDepth && weird >= minWeird && weird <= maxWeird;
	}
	public boolean matchesDimension(ResourceLocation dim) {
		if (dimensions == null)
			return true;
		for (ResourceLocation location : dimensions) {
			if (location.equals(dim))
				return true;
		}
		return false;
	}
	public boolean matchesParent(ResourceKey<Biome> parentKey) {
		if (validParents == null)
			return true;
		if (parentKey == null)
			return false;
		for (ResourceKey<Biome> key : validParents) {
			if (key.equals(parentKey))
				return true;
		}
		return false;
	}
}*/