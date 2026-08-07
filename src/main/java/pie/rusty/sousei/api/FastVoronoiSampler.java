package pie.rusty.sousei.api;

import net.minecraft.world.level.levelgen.synth.PerlinSimplexNoise;
import net.minecraft.world.level.levelgen.XoroshiroRandomSource;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.resources.ResourceKey;
import net.minecraft.core.Holder;

import java.util.List;

public final class FastVoronoiSampler {
	private final PerlinSimplexNoise borderWarp;

	public FastVoronoiSampler() {
		this.borderWarp = new PerlinSimplexNoise(new XoroshiroRandomSource(0x505531L), List.of(0));
	}

	public Holder<Biome> sample(BakedVoronoiEntry[] entries, int blockX, int blockY, int blockZ, float temp, float humid, float cont, float erosion, float depth, float weirdness, ResourceKey<Biome> vanillaParent) {
		if (entries == null || entries.length == 0) {
			return null;
		}
		double warpX = this.borderWarp.getValue(blockX * 0.005, blockZ * 0.005, false) * 32.0;
		double warpZ = this.borderWarp.getValue(blockZ * 0.005 + 100.0, blockX * 0.005, false) * 32.0;
		double px = blockX + warpX;
		double pz = blockZ + warpZ;
		BakedVoronoiEntry winningEntry = null;
		int bestPriority = Integer.MIN_VALUE;
		double closestDistSq = Double.MAX_VALUE;
		for (int i = 0; i < entries.length; i++) {
			BakedVoronoiEntry entry = entries[i];
			if (blockY < entry.minY || blockY > entry.maxY)
				continue;
			if (entry.priority < bestPriority)
				continue;
			if (!entry.matchesParent(vanillaParent))
				continue;
			if (!entry.testClimate(temp, humid, cont, erosion, depth, weirdness))
				continue;
			int cellSize = entry.effectiveCellSize;
			if (cellSize <= 0)
				cellSize = 400;
			int cellX = (int) Math.floor(px / cellSize);
			int cellZ = (int) Math.floor(pz / cellSize);
			for (int cx = cellX - 1; cx <= cellX + 1; cx++) {
				for (int cz = cellZ - 1; cz <= cellZ + 1; cz++) {
					long hash = ((long) cx * 3129871L) ^ ((long) cz * 116129781L) ^ entry.salt;
					hash = (hash ^ (hash >>> 16)) * 0x45d9f3bL;
					hash = (hash ^ (hash >>> 16)) * 0x45d9f3bL;
					hash = hash ^ (hash >>> 16);
					float rarityRoll = (float) ((hash & 0xFFFF) % 10000) / 100.0f;
					if (rarityRoll > entry.rarityPercent)
						continue;
					double seedX = (cx * cellSize) + (Math.abs(hash % cellSize));
					double seedZ = (cz * cellSize) + (Math.abs((hash >> 16) % cellSize));
					double dx = px - seedX;
					double dz = pz - seedZ;
					double distSq = dx * dx + dz * dz;
					double maxRad = entry.maxRadius;
					double minRad = entry.minRadius;
					if (distSq <= (maxRad * maxRad) && distSq >= (minRad * minRad)) {
						if (entry.priority > bestPriority) {
							bestPriority = entry.priority;
							closestDistSq = distSq;
							winningEntry = entry;
						} else if (entry.priority == bestPriority && distSq < closestDistSq) {
							closestDistSq = distSq;
							winningEntry = entry;
						}
					}
				}
			}
		}
		return winningEntry != null ? winningEntry.biomeHolder : null;
	}
}