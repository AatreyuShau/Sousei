package pie.rusty.sousei.api;

import org.checkerframework.checker.units.qual.min;

import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mojang.serialization.Codec;

public record FloatRange(float min, float max) {
	public static final Codec<FloatRange> CODEC = RecordCodecBuilder
			.create(instance -> instance.group(Codec.FLOAT.optionalFieldOf("min", -1.0f).forGetter(FloatRange::min), Codec.FLOAT.optionalFieldOf("max", 1.0f).forGetter(FloatRange::max)).apply(instance, FloatRange::new));

	public boolean contains(float value) {
		return value >= min && value <= max;
	}
}