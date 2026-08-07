package pie.rusty.sousei.mixin;

import pie.rusty.sousei.api.VoronoiBiomeRegistry;
import pie.rusty.sousei.api.FastVoronoiSampler;
import pie.rusty.sousei.api.BiomeSourceDimensionHolder;
import pie.rusty.sousei.api.BakedVoronoiEntry;

import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Mixin;

import net.minecraft.world.level.biome.MultiNoiseBiomeSource;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.resources.RegistryOps;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.Holder;

import java.util.stream.Stream;
import java.util.Set;
import java.util.HashSet;
import java.util.Collections;

import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mojang.serialization.MapCodec;

@Mixin(value = MultiNoiseBiomeSource.class, priority = 1500)
public abstract class MultiNoiseBiomeSourceMixin implements BiomeSourceDimensionHolder {
	@Shadow
	@Mutable
	public static MapCodec<MultiNoiseBiomeSource> CODEC;
	@Unique
	private static final ThreadLocal<HolderGetter<Biome>> SOUSEI$BIOME_GETTER = new ThreadLocal<>();
	@Unique
	private static final ResourceLocation SOUSEI$OVERWORLD = ResourceLocation.fromNamespaceAndPath("minecraft", "overworld");
	@Unique
	private static final BakedVoronoiEntry[] SOUSEI$EMPTY_ENTRIES = new BakedVoronoiEntry[0];
	@Unique
	private ResourceLocation sousei$dimension = SOUSEI$OVERWORLD;
	@Unique
	private HolderGetter<Biome> sousei$localGetter = null;
	@Unique
	private BakedVoronoiEntry[] sousei$cachedEntries = null;
	@Unique
	private Set<Holder<Biome>> sousei$cachedPossibleBiomes = null;
	@Unique
	private final FastVoronoiSampler sousei$fastSampler = new FastVoronoiSampler();

	@Override
	public void sousei$setDimension(ResourceLocation dimension) {
		if (dimension != null) {
			this.sousei$dimension = dimension;
			this.sousei$cachedEntries = null;
			this.sousei$cachedPossibleBiomes = null;
		}
	}

	@Override
	public ResourceLocation sousei$getDimension() {
		return this.sousei$dimension;
	}

	@Inject(method = "<clinit>", at = @At("TAIL"))
	private static void sousei$wrapCodec(CallbackInfo ci) {
		MapCodec<MultiNoiseBiomeSource> originalCodec = CODEC;
		CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(RegistryOps.retrieveGetter(Registries.BIOME), originalCodec.forGetter(source -> source)).apply(instance, (getter, source) -> {
			MultiNoiseBiomeSourceMixin mixin = (MultiNoiseBiomeSourceMixin) (Object) source;
			mixin.sousei$localGetter = getter;
			return source;
		}));
	}

	@Unique
	private BakedVoronoiEntry[] sousei$getOrUpdateEntries() {
		if (this.sousei$cachedEntries == null) {
			HolderGetter<Biome> getter = this.sousei$localGetter != null ? this.sousei$localGetter : SOUSEI$BIOME_GETTER.get();
			if (getter != null) {
				VoronoiBiomeRegistry.bakeAll(getter);
			}
			BakedVoronoiEntry[] fetched = VoronoiBiomeRegistry.getBakedForDimension(this.sousei$dimension);
			this.sousei$cachedEntries = (fetched != null && fetched.length > 0) ? fetched : SOUSEI$EMPTY_ENTRIES;
		}
		return this.sousei$cachedEntries;
	}

	@Inject(method = "collectPossibleBiomes", at = @At("RETURN"), cancellable = true)
	private void sousei$registerPossibleBiomes(CallbackInfoReturnable<Stream<Holder<Biome>>> cir) {
		if (this.sousei$cachedPossibleBiomes != null) {
			cir.setReturnValue(this.sousei$cachedPossibleBiomes.stream());
			return;
		}
		Stream<Holder<Biome>> currentStream = cir.getReturnValue();
		if (currentStream == null)
			return;
		BakedVoronoiEntry[] entries = sousei$getOrUpdateEntries();
		Set<Holder<Biome>> expanded = new HashSet<>();
		currentStream.forEach(expanded::add);
		if (entries.length > 0) {
			for (BakedVoronoiEntry entry : entries) {
				if (entry != null && entry.biomeHolder != null) {
					expanded.add(entry.biomeHolder);
				}
			}
		}
		this.sousei$cachedPossibleBiomes = Collections.unmodifiableSet(expanded);
		cir.setReturnValue(this.sousei$cachedPossibleBiomes.stream());
	}

	@Inject(method = "getNoiseBiome(IIILnet/minecraft/world/level/biome/Climate$Sampler;)Lnet/minecraft/core/Holder;", at = @At("HEAD"), cancellable = true)
	private void sousei$overrideBiome(int x, int y, int z, Climate.Sampler sampler, CallbackInfoReturnable<Holder<Biome>> cir) {
		BakedVoronoiEntry[] entries = sousei$getOrUpdateEntries();
		if (entries.length == 0) {
			return;
		}
		int blockX = net.minecraft.core.QuartPos.toBlock(x);
		int blockY = net.minecraft.core.QuartPos.toBlock(y);
		int blockZ = net.minecraft.core.QuartPos.toBlock(z);
		Climate.TargetPoint target = sampler.sample(x, y, z);
		Holder<Biome> winningBiome = this.sousei$fastSampler.sample(entries, blockX, blockY, blockZ, Climate.unquantizeCoord(target.temperature()), Climate.unquantizeCoord(target.humidity()), Climate.unquantizeCoord(target.continentalness()),
				Climate.unquantizeCoord(target.erosion()), Climate.unquantizeCoord(target.depth()), Climate.unquantizeCoord(target.weirdness()), null);
		if (winningBiome != null && winningBiome.isBound()) {
			cir.setReturnValue(winningBiome);
		}
	}
}