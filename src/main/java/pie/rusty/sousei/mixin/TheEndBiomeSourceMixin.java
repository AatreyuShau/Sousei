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
import org.spongepowered.asm.mixin.Final;

import net.minecraft.world.level.levelgen.synth.PerlinSimplexNoise;
import net.minecraft.world.level.levelgen.XoroshiroRandomSource;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.biome.TheEndBiomeSource;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.resources.RegistryOps;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.QuartPos;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.Holder;

import java.util.stream.Stream;
import java.util.Set;
import java.util.List;
import java.util.HashSet;
import java.util.Collections;

import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mojang.serialization.MapCodec;

@Mixin(TheEndBiomeSource.class)
public abstract class TheEndBiomeSourceMixin implements BiomeSourceDimensionHolder {
	@Shadow
	@Mutable
	@Final
	private static MapCodec<TheEndBiomeSource> CODEC;
	@Unique
	private static final ThreadLocal<HolderGetter<Biome>> SOUSEI$END_BIOME_REGISTRY = new ThreadLocal<>();
	@Unique
	private static final ResourceLocation SOUSEI$THE_END = ResourceLocation.fromNamespaceAndPath("minecraft", "the_end");
	@Unique
	private static final BakedVoronoiEntry[] SOUSEI$EMPTY_ENTRIES = new BakedVoronoiEntry[0];
	@Unique
	private ResourceLocation sousei$dimension = SOUSEI$THE_END;
	@Unique
	private HolderGetter<Biome> sousei$localGetter;
	@Unique
	private final FastVoronoiSampler sousei$fastSampler = new FastVoronoiSampler();
	@Unique
	private BakedVoronoiEntry[] sousei$cachedEntries = null;
	@Unique
	private Set<Holder<Biome>> sousei$cachedPossibleBiomes = null;
	@Unique
	private PerlinSimplexNoise sousei$terrainNoise;
	@Unique
	private PerlinSimplexNoise sousei$detailNoise;
	@Unique
	private PerlinSimplexNoise sousei$regionNoise;
	@Unique
	private boolean sousei$noiseInitialized = false;
	@Unique
	private long sousei$worldSalt = 0L;

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
	private static void sousei$modifyCodec(CallbackInfo ci) {
		CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(RegistryOps.retrieveGetter(Registries.BIOME)).apply(instance, instance.stable(TheEndBiomeSource::create)));
	}

	@Inject(method = "create", at = @At("HEAD"))
	private static void sousei$rememberRegistry(HolderGetter<Biome> biomes, CallbackInfoReturnable<?> cir) {
		SOUSEI$END_BIOME_REGISTRY.set(biomes);
	}

	@Inject(method = "create", at = @At("TAIL"))
	private static void sousei$clearRegistry(HolderGetter<Biome> biomes, CallbackInfoReturnable<?> cir) {
		SOUSEI$END_BIOME_REGISTRY.remove();
	}

	@Inject(method = "<init>", at = @At("TAIL"))
	private void sousei$init(Holder<Biome> end, Holder<Biome> highlands, Holder<Biome> midlands, Holder<Biome> islands, Holder<Biome> barrens, CallbackInfo ci) {
		HolderGetter<Biome> registry = SOUSEI$END_BIOME_REGISTRY.get();
		if (registry != null) {
			this.sousei$localGetter = registry;
		}
	}

	@Unique
	private BakedVoronoiEntry[] sousei$getOrUpdateEntries() {
		if (this.sousei$cachedEntries == null) {
			HolderGetter<Biome> getter = this.sousei$localGetter != null ? this.sousei$localGetter : SOUSEI$END_BIOME_REGISTRY.get();
			if (getter != null) {
				VoronoiBiomeRegistry.bakeAll(getter);
			}
			BakedVoronoiEntry[] fetched = VoronoiBiomeRegistry.getBakedForDimension(this.sousei$dimension);
			this.sousei$cachedEntries = (fetched != null && fetched.length > 0) ? fetched : SOUSEI$EMPTY_ENTRIES;
		}
		return this.sousei$cachedEntries;
	}

	@Inject(method = "collectPossibleBiomes", at = @At("RETURN"), cancellable = true)
	private void sousei$addBiomes(CallbackInfoReturnable<Stream<Holder<Biome>>> cir) {
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

	@Inject(method = "getNoiseBiome", at = @At("HEAD"), cancellable = true)
	private void sousei$injectBiome(int biomeX, int biomeY, int biomeZ, Climate.Sampler noise, CallbackInfoReturnable<Holder<Biome>> cir) {
		if (!SOUSEI$THE_END.equals(this.sousei$dimension)) {
			return;
		}
		BakedVoronoiEntry[] entries = sousei$getOrUpdateEntries();
		if (entries.length == 0) {
			return;
		}
		int blockX = QuartPos.toBlock(biomeX);
		int blockZ = QuartPos.toBlock(biomeZ);
		if (((long) blockX * blockX + (long) blockZ * blockZ) < 1000000L) {
			return;
		}
		int blockY = QuartPos.toBlock(biomeY);
		sousei$initNoise(noise);
		float cont = 0.0f;
		float erosion = 0.0f;
		if (this.sousei$terrainNoise != null && this.sousei$regionNoise != null) {
			double terrainBase = this.sousei$terrainNoise.getValue(blockX * 0.0015, blockZ * 0.0015, true);
			double detail = this.sousei$detailNoise.getValue(blockX * 0.005, blockZ * 0.005, true);
			double region = this.sousei$regionNoise.getValue(blockX * 0.0025, blockZ * 0.0025, true);
			cont = (float) (terrainBase * 0.85 + detail * 0.15);
			erosion = (float) region;
		}
		Holder<Biome> winningBiome = this.sousei$fastSampler.sample(entries, blockX, blockY, blockZ, 0.0f, 0.0f, cont, erosion, 0.0f, 0.0f, null);
		if (winningBiome != null && winningBiome.isBound()) {
			cir.setReturnValue(winningBiome);
		}
	}

	@Unique
	private void sousei$initNoise(Climate.Sampler sampler) {
		if (sousei$noiseInitialized) {
			return;
		}
		try {
			double e1 = sampler.erosion().compute(new DensityFunction.SinglePointContext(2048, 64, 2048));
			double e2 = sampler.erosion().compute(new DensityFunction.SinglePointContext(-4096, 80, 8192));
			double e3 = sampler.erosion().compute(new DensityFunction.SinglePointContext(12000, 72, -7000));
			long s1 = Double.doubleToLongBits(e1);
			long s2 = Long.rotateLeft(Double.doubleToLongBits(e2), 21);
			long s3 = Long.rotateLeft(Double.doubleToLongBits(e3), 42);
			sousei$worldSalt = s1 ^ s2 ^ s3;
			sousei$terrainNoise = new PerlinSimplexNoise(new XoroshiroRandomSource(1337L ^ sousei$worldSalt), List.of(0));
			sousei$detailNoise = new PerlinSimplexNoise(new XoroshiroRandomSource(42069L ^ sousei$worldSalt), List.of(0));
			sousei$regionNoise = new PerlinSimplexNoise(new XoroshiroRandomSource(99999L ^ sousei$worldSalt), List.of(0));
			sousei$noiseInitialized = true;
		} catch (Exception ignored) {
		}
	}
}