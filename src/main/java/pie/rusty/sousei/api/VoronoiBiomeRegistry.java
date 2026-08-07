package pie.rusty.sousei.api;

import net.minecraft.world.level.biome.Biome;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.HolderGetter;

import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;
import java.util.List;
import java.util.HashMap;
import java.util.ArrayList;

public class VoronoiBiomeRegistry {
	private static final List<VoronoiBiomeEntry> RAW_ENTRIES = new ArrayList<>();
	private static final Map<ResourceLocation, BakedVoronoiEntry[]> BAKED_CACHE = new ConcurrentHashMap<>();
	private static HolderGetter<Biome> CURRENT_GETTER = null;

	public static synchronized void clearCache() {
		BAKED_CACHE.clear();
		CURRENT_GETTER = null;
	}

	public static synchronized void setJsonEntries(List<VoronoiBiomeEntry> entries) {
		RAW_ENTRIES.clear();
		RAW_ENTRIES.addAll(entries);
		BAKED_CACHE.clear();
	}

	public static void bakeAll(HolderGetter<Biome> getter) {
		if (getter == null)
			return;
		if (CURRENT_GETTER != getter) {
			clearCache();
			CURRENT_GETTER = getter;
		}
		if (!BAKED_CACHE.isEmpty() || RAW_ENTRIES.isEmpty()) {
			return;
		}
		synchronized (VoronoiBiomeRegistry.class) {
			if (!BAKED_CACHE.isEmpty())
				return;
			Map<ResourceLocation, List<BakedVoronoiEntry>> tempDimMap = new HashMap<>();
			for (VoronoiBiomeEntry raw : RAW_ENTRIES) {
				getter.get(raw.biomeKey()).ifPresent(holder -> {
					BakedVoronoiEntry baked = new BakedVoronoiEntry(raw, holder);
					if (raw.dimensions().isPresent() && !raw.dimensions().get().isEmpty()) {
						for (ResourceLocation dim : raw.dimensions().get()) {
							tempDimMap.computeIfAbsent(dim, k -> new ArrayList<>()).add(baked);
						}
					} else {
						tempDimMap.computeIfAbsent(ResourceLocation.fromNamespaceAndPath("minecraft", "overworld"), k -> new ArrayList<>()).add(baked);
						tempDimMap.computeIfAbsent(ResourceLocation.fromNamespaceAndPath("minecraft", "the_nether"), k -> new ArrayList<>()).add(baked);
						tempDimMap.computeIfAbsent(ResourceLocation.fromNamespaceAndPath("minecraft", "the_end"), k -> new ArrayList<>()).add(baked);
					}
				});
			}
			for (Map.Entry<ResourceLocation, List<BakedVoronoiEntry>> entry : tempDimMap.entrySet()) {
				BAKED_CACHE.put(entry.getKey(), entry.getValue().toArray(new BakedVoronoiEntry[0]));
			}
		}
	}

	public static BakedVoronoiEntry[] getBakedForDimension(ResourceLocation dimension) {
		return BAKED_CACHE.getOrDefault(dimension, new BakedVoronoiEntry[0]);
	}
}