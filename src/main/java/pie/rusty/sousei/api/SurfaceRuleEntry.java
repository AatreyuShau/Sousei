package pie.rusty.sousei.api;

import net.minecraft.world.level.levelgen.placement.CaveSurface;
import net.minecraft.world.level.levelgen.SurfaceRules;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.resources.ResourceKey;
import net.minecraft.core.registries.Registries;

import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mojang.serialization.Codec;

public record SurfaceRuleEntry(ResourceKey<Biome> biomeKey, BlockState topBlock, BlockState underBlock, BlockState underwaterBlock) {
	public static final Codec<SurfaceRuleEntry> CODEC = RecordCodecBuilder
			.create(instance -> instance.group(ResourceKey.codec(Registries.BIOME).fieldOf("biome").forGetter(SurfaceRuleEntry::biomeKey), BlockState.CODEC.fieldOf("top_block").forGetter(SurfaceRuleEntry::topBlock),
					BlockState.CODEC.fieldOf("under_block").forGetter(SurfaceRuleEntry::underBlock), BlockState.CODEC.fieldOf("underwater_block").forGetter(SurfaceRuleEntry::underwaterBlock)).apply(instance, SurfaceRuleEntry::new));

	public SurfaceRules.RuleSource buildRule() {
		return SurfaceRules.ifTrue(SurfaceRules.isBiome(this.biomeKey),
				SurfaceRules.sequence(
						SurfaceRules.ifTrue(SurfaceRules.stoneDepthCheck(0, false, 0, CaveSurface.FLOOR),
								SurfaceRules.sequence(SurfaceRules.ifTrue(SurfaceRules.waterBlockCheck(-1, 0), SurfaceRules.state(this.topBlock)), SurfaceRules.state(this.underwaterBlock))),
						SurfaceRules.ifTrue(SurfaceRules.stoneDepthCheck(0, true, 0, CaveSurface.FLOOR), SurfaceRules.state(this.underBlock))));
	}
}