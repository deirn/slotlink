package badasintended.slotlink.datagen.provider

import badasintended.slotlink.init.Blocks
import badasintended.slotlink.init.Items
import badasintended.slotlink.util.modId
import java.util.function.Consumer
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput
import net.fabricmc.fabric.api.datagen.v1.provider.FabricRecipeProvider
import net.minecraft.data.recipes.RecipeBuilder
import net.minecraft.data.recipes.FinishedRecipe
import net.minecraft.world.item.Item
import net.minecraft.world.level.ItemLike
import net.minecraft.data.recipes.RecipeCategory
import net.minecraft.core.registries.Registries
import net.minecraft.tags.TagKey
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.level.block.Blocks as McBlocks
import net.minecraft.data.recipes.ShapedRecipeBuilder.shaped as shaped
import net.minecraft.data.recipes.ShapelessRecipeBuilder.shapeless as shapeless
import net.minecraft.world.item.Items as McItems

class RecipeProvider(dataGenerator: FabricDataOutput) : FabricRecipeProvider(dataGenerator) {

    override fun buildRecipes(exporter: Consumer<FinishedRecipe>) {
        shaped(RecipeCategory.MISC, Blocks.CABLE, 8)
            .pattern("SSS")
            .pattern("I I")
            .pattern("SSS")
            .define('I', tag("c:iron_ingots"))
            .define('S', McBlocks.STONE_SLAB)
            .criterion(McItems.IRON_INGOT, tag("c:iron_ingots"))
            .save(exporter)

        shaped(RecipeCategory.MISC, Blocks.IMPORT_CABLE, 4)
            .pattern(" C ")
            .pattern("CHC")
            .pattern(" C ")
            .define('H', tag("c:hoppers"))
            .define('C', Blocks.CABLE)
            .criterion(Blocks.CABLE)
            .save(exporter)

        shapeless(RecipeCategory.MISC, Blocks.IMPORT_CABLE)
            .requires(Blocks.EXPORT_CABLE)
            .criterion(Blocks.CABLE)
            .criterion(Blocks.EXPORT_CABLE)
            .save(exporter, modId("export_to_import_cable"))

        shapeless(RecipeCategory.MISC, Blocks.EXPORT_CABLE)
            .requires(Blocks.IMPORT_CABLE)
            .criterion(Blocks.CABLE)
            .criterion(Blocks.IMPORT_CABLE)
            .save(exporter, modId("import_to_export_cable"))

        shaped(RecipeCategory.MISC, Blocks.LINK_CABLE, 4)
            .pattern(" C ")
            .pattern("CHC")
            .pattern(" C ")
            .define('H', tag("c:wooden_chests"))
            .define('C', Blocks.CABLE)
            .criterion(Blocks.CABLE)
            .save(exporter)

        shaped(RecipeCategory.MISC, Blocks.MASTER)
            .pattern("QCQ")
            .pattern("CDC")
            .pattern("QCQ")
            .define('D', tag("c:diamonds"))
            .define('Q', tag("c:quartz_blocks"))
            .define('C', Blocks.CABLE)
            .criterion(Blocks.CABLE)
            .save(exporter)

        shaped(RecipeCategory.MISC, Blocks.REQUEST)
            .pattern("TCT")
            .pattern("CGC")
            .pattern("TCT")
            .define('G', tag("c:gold_ingots"))
            .define('T', McBlocks.CRAFTING_TABLE)
            .define('C', Blocks.LINK_CABLE)
            .criterion(Blocks.LINK_CABLE)
            .save(exporter)

        shaped(RecipeCategory.MISC, Blocks.INTERFACE)
            .pattern("SIS")
            .pattern("IRE")
            .pattern("SES")
            .define('R', tag("c:iron_ingots"))
            .define('S', McBlocks.SMOOTH_STONE)
            .define('I', Blocks.IMPORT_CABLE)
            .define('E', Blocks.EXPORT_CABLE)
            .criterion(Blocks.IMPORT_CABLE)
            .criterion(Blocks.EXPORT_CABLE)
            .save(exporter)

        shaped(RecipeCategory.MISC, Items.LIMITED_REMOTE)
            .pattern("SDS")
            .pattern("GRG")
            .pattern("SDS")
            .define('S', tag("c:redstone_dusts"))
            .define('D', tag("c:diamonds"))
            .define('G', tag("c:gold_ingots"))
            .define('R', Blocks.REQUEST)
            .criterion(Blocks.REQUEST)
            .save(exporter)

        shaped(RecipeCategory.MISC, Items.UNLIMITED_REMOTE)
            .pattern("GEC")
            .pattern("PRP")
            .pattern("CEG")
            .define('E', McItems.ENDER_EYE)
            .define('P', McItems.PHANTOM_MEMBRANE)
            .define('G', McItems.GHAST_TEAR)
            .define('C', McItems.PRISMARINE_CRYSTALS)
            .define('R', Items.LIMITED_REMOTE)
            .criterion(Items.LIMITED_REMOTE)
            .save(exporter)

        shapeless(RecipeCategory.MISC, Items.MULTI_DIM_REMOTE)
            .requires(McItems.DRAGON_BREATH)
            .requires(McItems.TOTEM_OF_UNDYING)
            .requires(McItems.NETHER_STAR)
            .requires(Items.UNLIMITED_REMOTE)
            .criterion(Items.UNLIMITED_REMOTE)
            .save(exporter)
    }

    private fun tag(id: String): TagKey<Item> {
        return TagKey.create(Registries.ITEM, ResourceLocation(id))
    }

    private fun <T : RecipeBuilder> T.criterion(item: ItemLike): T {
        unlockedBy(getHasName(item), has(item))
        return this
    }

    private fun <T : RecipeBuilder> T.criterion(item: ItemLike, tag: TagKey<Item>): T {
        unlockedBy(getHasName(item), has(tag))
        return this
    }

    override fun getRecipeIdentifier(identifier: ResourceLocation): ResourceLocation {
        return identifier
    }

}