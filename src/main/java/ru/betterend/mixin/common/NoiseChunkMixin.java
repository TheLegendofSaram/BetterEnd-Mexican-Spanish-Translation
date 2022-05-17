package ru.betterend.mixin.common;

import net.minecraft.world.level.levelgen.*;
import net.minecraft.world.level.levelgen.blending.Blender;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.bclib.BCLib;
import ru.betterend.interfaces.BETargetChecker;
import ru.betterend.world.generator.TerrainGenerator;

import java.util.List;

@Mixin(NoiseChunk.class)
public class NoiseChunkMixin implements BETargetChecker {
	private boolean be_isEndGenerator;

	@Inject(method = "<init>*", at = @At("TAIL"))
	private void be_onNoiseChunkInit(int i,
									 RandomState randomState,
									 int j,
									 int k,
									 NoiseSettings noiseSettings,
									 DensityFunctions.BeardifierOrMarker beardifierOrMarker,
									 NoiseGeneratorSettings noiseGeneratorSettings,
									 Aquifer.FluidPicker fluidPicker,
									 Blender blender,
									 CallbackInfo ci) {
		var o = BETargetChecker.class.cast(noiseGeneratorSettings);
		if (o!= null) be_isEndGenerator = o.be_isTarget();
		else BCLib.LOGGER.warning(noiseGeneratorSettings + " has unknown implementation.");
	}
	
	@Override
	public boolean be_isTarget() {
		return be_isEndGenerator;
	}
	
	@Override
	public void be_setTarget(boolean target) {
		be_isEndGenerator = target;
	}

	@Shadow @Final private List<NoiseChunk.NoiseInterpolator> interpolators;

	@Inject(method = "fillSlice", at = @At("HEAD"), cancellable = true)
	private void be_fillSlice(boolean primarySlice, int x, CallbackInfo info) {
		if (!be_isTarget()) return;
		
		info.cancel();

		NoiseChunkAccessor accessor = NoiseChunkAccessor.class.cast(this);
		NoiseSettings noiseSettings = accessor.bnv_getNoiseSettings();

		final int sizeY = noiseSettings.getCellHeight();
		final int sizeXZ = noiseSettings.getCellWidth();
		final int cellSizeXZ = accessor.bnv_getCellCountXZ() + 1;
		final int firstCellZ = accessor.bnv_getFirstCellZ();
		
		x *= sizeXZ;
		for (int cellXZ = 0; cellXZ < cellSizeXZ; ++cellXZ) {
			int z = (firstCellZ + cellXZ) * sizeXZ;
			for (NoiseChunk.NoiseInterpolator noiseInterpolator : this.interpolators) {
				if (noiseInterpolator instanceof NoiseInterpolatorAccessor interpolator) {
					final double[] ds = (primarySlice
							? interpolator.be_getSlice0()
							: interpolator.be_getSlice1())[cellXZ];
					TerrainGenerator.fillTerrainDensity(ds, x, z, sizeXZ, sizeY);
				}
			}
		}
	}
}
