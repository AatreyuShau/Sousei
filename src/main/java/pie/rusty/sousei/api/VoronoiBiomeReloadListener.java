package pie.rusty.sousei.api;

import pie.rusty.sousei.SouseiMod;

import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.resources.FileToIdConverter;

import java.util.Map;
import java.util.List;
import java.util.ArrayList;

public class VoronoiBiomeReloadListener extends SimpleJsonResourceReloadListener<VoronoiBiomeEntry> {
	public VoronoiBiomeReloadListener() {
		super(VoronoiBiomeEntry.CODEC, FileToIdConverter.json("voronoi_biomes"));
	}

	@Override
	protected void apply(Map<ResourceLocation, VoronoiBiomeEntry> prepared, ResourceManager resourceManager, ProfilerFiller profiler) {
		List<VoronoiBiomeEntry> loadedEntries = new ArrayList<>(prepared.size());
		for (VoronoiBiomeEntry entry : prepared.values()) {
			if (entry != null) {
				loadedEntries.add(entry);
			}
		}
		VoronoiBiomeRegistry.setJsonEntries(loadedEntries);
		SouseiMod.LOGGER.info("Successfully loaded {} Biome rules from JSON", loadedEntries.size());
	}
}