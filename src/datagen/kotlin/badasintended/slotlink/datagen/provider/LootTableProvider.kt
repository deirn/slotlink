package badasintended.slotlink.datagen.provider

import badasintended.slotlink.init.Blocks
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput
import net.fabricmc.fabric.api.datagen.v1.provider.FabricBlockLootTableProvider

class LootTableProvider(dataGenerator: FabricDataOutput) : FabricBlockLootTableProvider(dataGenerator) {

    override fun generate() {
        dropSelf(Blocks.CABLE)
        dropSelf(Blocks.EXPORT_CABLE)
        dropSelf(Blocks.IMPORT_CABLE)
        dropSelf(Blocks.LINK_CABLE)
        dropSelf(Blocks.MASTER)
        dropSelf(Blocks.REQUEST)
        dropSelf(Blocks.INTERFACE)
    }

}