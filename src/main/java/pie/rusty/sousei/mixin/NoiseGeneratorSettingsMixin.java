package pie.rusty.sousei.mixin;

import pie.rusty.sousei.api.SurfaceRuleRegistry;

import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.Mixin;

import net.minecraft.world.level.levelgen.SurfaceRules;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;

@Mixin(NoiseGeneratorSettings.class)
public class NoiseGeneratorSettingsMixin {
	@Unique
	private SurfaceRules.RuleSource sousei$cachedMergedRule = null;
	@Unique
	private SurfaceRules.RuleSource[] sousei$lastCustomRules = null;
	@Unique
	private SurfaceRules.RuleSource sousei$lastVanillaRule = null;

	@Inject(method = "surfaceRule", at = @At("RETURN"), cancellable = true)
	private void sousei$injectSurfaceRules(CallbackInfoReturnable<SurfaceRules.RuleSource> cir) {
		SurfaceRules.RuleSource[] customRules = SurfaceRuleRegistry.getRulesArray(); //
		if (customRules.length == 0) {
			return;
		}
		SurfaceRules.RuleSource vanillaRule = cir.getReturnValue();
		if (this.sousei$cachedMergedRule != null && this.sousei$lastVanillaRule == vanillaRule && this.sousei$lastCustomRules == customRules) {
			cir.setReturnValue(this.sousei$cachedMergedRule);
			return;
		}
		SurfaceRules.RuleSource[] combined = new SurfaceRules.RuleSource[customRules.length + 1];
		System.arraycopy(customRules, 0, combined, 0, customRules.length);
		combined[customRules.length] = vanillaRule;
		this.sousei$lastVanillaRule = vanillaRule;
		this.sousei$lastCustomRules = customRules;
		this.sousei$cachedMergedRule = SurfaceRules.sequence(combined);
		cir.setReturnValue(this.sousei$cachedMergedRule);
	}
}