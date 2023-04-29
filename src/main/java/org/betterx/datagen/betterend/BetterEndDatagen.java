package org.betterx.datagen.betterend;

import org.betterx.datagen.betterend.advancement.EndAdvancementDataProvider;
import org.betterx.datagen.betterend.recipes.EndRecipeDataProvider;
import org.betterx.datagen.betterend.worldgen.EndBiomesDataProvider;
import org.betterx.datagen.betterend.worldgen.EndRegistriesDataProvider;

import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.Registries;

import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;

public class BetterEndDatagen implements DataGeneratorEntrypoint {
    @Override
    public void onInitializeDataGenerator(FabricDataGenerator dataGenerator) {
        EndBiomesDataProvider.ensureStaticallyLoaded();
        EndRecipeDataProvider.buildRecipes();

        final FabricDataGenerator.Pack pack = dataGenerator.createPack();
        pack.addProvider(EndBiomesDataProvider::new);

        pack.addProvider(EndRecipeDataProvider::new);
        pack.addProvider(EndRegistriesDataProvider::new);
        pack.addProvider(EndAdvancementDataProvider::new);
    }

    @Override
    public void buildRegistry(RegistrySetBuilder registryBuilder) {
        EndRegistrySupplier.INSTANCE.bootstrapRegistries(registryBuilder);
        registryBuilder.add(Registries.BIOME, EndBiomesDataProvider::bootstrap);
    }
}
