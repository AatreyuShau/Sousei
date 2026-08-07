package pie.rusty.sousei.api;

import pie.rusty.sousei.SouseiMod;

import net.minecraft.world.level.levelgen.SurfaceRules;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.resources.FileToIdConverter;

import java.util.Map;

import com.mojang.serialization.Codec;

public class SurfaceRuleReloadListener extends SimpleJsonResourceReloadListener<SurfaceRules.RuleSource> {
	private static final Codec<SurfaceRules.RuleSource> RULE_CODEC = SurfaceRules.RuleSource.CODEC;

	public SurfaceRuleReloadListener() {
		super(RULE_CODEC, FileToIdConverter.json("surface_rules"));
	}

	@Override
	protected void apply(Map<ResourceLocation, SurfaceRules.RuleSource> prepared, ResourceManager resourceManager, ProfilerFiller profiler) {
		SurfaceRuleRegistry.clear();
		prepared.forEach((location, ruleSource) -> {
			if (ruleSource != null) {
				SurfaceRuleRegistry.register(ruleSource);
			}
		});
		SouseiMod.LOGGER.info("Successfully loaded {} Surface rules from JSON", SurfaceRuleRegistry.getRulesArray().length);
	}
}