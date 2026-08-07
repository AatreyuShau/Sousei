package pie.rusty.sousei.api;

import net.minecraft.resources.ResourceLocation;

public interface BiomeSourceDimensionHolder {
	void sousei$setDimension(ResourceLocation dimension);

	ResourceLocation sousei$getDimension();
}