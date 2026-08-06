# New Mod Framework

A clean Minecraft Forge 1.20.1 project extracted from the old Klux project conceptually, without carrying over its content or BlockEntity architecture.

## Included

- Forge 1.20.1 / Java 17 Gradle project
- Central registry bootstrap
- Registries for blocks, items, creative tabs, block entities, menus, recipe types, recipe serializers, sounds, effects, potions and enchantments
- Datagen entry point
- Blockstate/model generation
- Item model generation
- English and Chinese language generation
- Block and item tags
- Recipe generation
- Block loot table generation
- One minimal example item and block proving the pipeline works

## Rename before development

Replace all of the following together:

1. `mod_id=newmod` in `gradle.properties`
2. `New Mod Framework` and other metadata in `gradle.properties`
3. Java package `org.kdvcs.poiesis`
4. Main class `NewMod`
5. `NewMod.MOD_ID = "newmod"`
6. Resource namespaces:
   - `src/main/resources/assets/newmod`
   - generated `data/newmod` and `assets/newmod`
7. Translation key `itemGroup.newmod.main`

Do not rename only the Gradle property. The Java constant and resource namespace must match it.

## First commands

Windows:

```bat
gradlew genIntellijRuns
gradlew runData
gradlew build
```

Generated resources are written to `src/generated/resources` and are already included in the main resource set.

## Adding content

- Blocks: `registry/ModBlocks.java`
- Items: `registry/ModItems.java`
- Other registries: corresponding classes in `registry`
- Datagen: classes in `datagen`
- Runtime systems should be added as new packages rather than placed in registry classes

The example content can be deleted after the first successful `runData` and `runClient` test.
