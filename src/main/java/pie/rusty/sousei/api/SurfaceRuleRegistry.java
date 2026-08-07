package pie.rusty.sousei.api;

import net.minecraft.world.level.levelgen.SurfaceRules;

import java.util.List;
import java.util.ArrayList;

public class SurfaceRuleRegistry {
	private static final List<SurfaceRules.RuleSource> CUSTOM_RULES = new ArrayList<>();
	private static SurfaceRules.RuleSource[] CACHED_ARRAY = new SurfaceRules.RuleSource[0];

	public static synchronized void register(SurfaceRules.RuleSource rule) {
		CUSTOM_RULES.add(rule);
		CACHED_ARRAY = CUSTOM_RULES.toArray(new SurfaceRules.RuleSource[0]);
	}

	public static synchronized void clear() {
		CUSTOM_RULES.clear();
		CACHED_ARRAY = new SurfaceRules.RuleSource[0];
	}

	public static synchronized SurfaceRules.RuleSource[] getRulesArray() {
		return CACHED_ARRAY;
	}
}