package pie.rusty.sousei.mixin;

import pie.rusty.sousei.api.BiomeSourceDimensionHolder;

import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.Mixin;

import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.server.MinecraftServer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.resources.ResourceKey;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.Registry;

import java.util.Optional;
import java.util.Map;

@Mixin(MinecraftServer.class)
public abstract class MinecraftServerDimensionMappingMixin {
	@Inject(method = "createLevels", at = @At("HEAD"))
	private void sousei$mapDimensionsToBiomeSources(CallbackInfo ci) {
		MinecraftServer server = (MinecraftServer) (Object) this;
		Optional<Registry<LevelStem>> levelStemRegistryOpt = server.registryAccess().lookup(Registries.LEVEL_STEM);
		if (levelStemRegistryOpt.isPresent()) {
			Registry<LevelStem> levelStemRegistry = levelStemRegistryOpt.get();
			for (Map.Entry<ResourceKey<LevelStem>, LevelStem> entry : levelStemRegistry.entrySet()) {
				ResourceLocation dimLocation = entry.getKey().location();
				LevelStem levelStem = entry.getValue();
				if (levelStem.generator().getBiomeSource() instanceof BiomeSourceDimensionHolder holder) {
					holder.sousei$setDimension(dimLocation);
					System.out.println("[Sousei:] Successfully mapped dimension " + dimLocation + " to its BiomeSource.");
				}
			}
		}
	}
}