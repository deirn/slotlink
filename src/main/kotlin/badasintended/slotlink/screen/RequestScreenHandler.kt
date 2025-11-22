package badasintended.slotlink.screen

import badasintended.slotlink.block.entity.BlockEntityWatcher
import badasintended.slotlink.block.entity.MasterBlockEntity
import badasintended.slotlink.block.entity.RequestBlockEntity
import badasintended.slotlink.init.Packets.UPDATE_CURSOR
import badasintended.slotlink.init.Packets.UPDATE_MAX_SCROLL
import badasintended.slotlink.init.Packets.UPDATE_SLOT_NUMBERS
import badasintended.slotlink.init.Packets.UPDATE_VIEWED_STACK
import badasintended.slotlink.init.Screens
import badasintended.slotlink.recipe.fastRecipeManager
import badasintended.slotlink.screen.slot.LockedSlot
import badasintended.slotlink.screen.view.ItemView
import badasintended.slotlink.screen.view.toView
import badasintended.slotlink.storage.FilteredItemStorage
import badasintended.slotlink.storage.NetworkStorage
import badasintended.slotlink.util.actionBar
import badasintended.slotlink.util.allEmpty
import badasintended.slotlink.util.cursorStorage
import badasintended.slotlink.util.input
import badasintended.slotlink.util.int
import badasintended.slotlink.util.isEmpty
import badasintended.slotlink.util.item
import badasintended.slotlink.util.merge
import badasintended.slotlink.util.nbt
import badasintended.slotlink.util.result
import badasintended.slotlink.util.s2c
import badasintended.slotlink.util.stack
import badasintended.slotlink.util.storage
import java.util.*
import kotlin.collections.set
import kotlin.math.ceil
import kotlin.math.min
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction
import net.minecraft.world.entity.player.Player
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.inventory.CraftingContainer
import net.minecraft.world.inventory.ResultContainer
import net.minecraft.world.Container
import net.minecraft.world.item.ItemStack
import net.minecraft.network.protocol.game.ClientboundContainerClosePacket
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket
import net.minecraft.world.item.crafting.CraftingRecipe
import net.minecraft.world.item.crafting.Ingredient
import net.minecraft.world.item.crafting.Recipe
import net.minecraft.recipebook.PlaceRecipe
import net.minecraft.world.item.crafting.RecipeType
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.world.inventory.CraftingMenu
import net.minecraft.world.inventory.AbstractContainerMenu
import net.minecraft.world.inventory.ContainerListener
import net.minecraft.world.inventory.MenuType
import net.minecraft.world.inventory.ResultSlot
import net.minecraft.world.inventory.Slot
import net.minecraft.world.inventory.ClickType
import net.minecraft.world.inventory.ClickType.CLONE
import net.minecraft.world.inventory.ClickType.PICKUP
import net.minecraft.world.inventory.ClickType.QUICK_MOVE
import net.minecraft.world.inventory.ClickType.SWAP
import net.minecraft.world.inventory.ClickType.THROW
import net.minecraft.server.level.ServerPlayer

@Suppress("UnstableApiUsage")
open class RequestScreenHandler(
    syncId: Int,
    val playerInventory: Inventory,
    private val storage: NetworkStorage,
) : CraftingMenu(syncId, playerInventory),
    MasterBlockEntity.Watcher,
    BlockEntityWatcher<RequestBlockEntity>,
    PlaceRecipe<Ingredient> {

    companion object {

        private val whitespaceRegex = Regex("\\s+")

    }

    val player: Player = playerInventory.player

    private val filledViews = arrayListOf<ItemView>()

    private val trackedViews = ArrayList<ItemView>(54)
    val itemViews = ArrayList<ItemView>(54)

    private var lastSortData = SortData(SortMode.NAME, "")
    private var scheduledSortData: SortData? = null

    var viewedHeight = 0

    var maxScroll = 0
    private var lastScroll = 0

    private var request: RequestBlockEntity? = null
    private var master: MasterBlockEntity? = null

    private val visualStorages = hashMapOf<FilteredItemStorage, List<ItemView>>()

    var totalSlotSize = 0
    var filledSlotSize = 0

    init {
        for (i in 0 until 54) {
            itemViews.add(ItemStack.EMPTY.toView())
        }
    }

    /** Client side **/
    constructor(syncId: Int, playerInventory: Inventory) : this(
        syncId, playerInventory, NetworkStorage(arrayListOf())
    )

    /** Server side **/
    @Suppress("LeakingThis")
    constructor(
        syncId: Int,
        playerInventory: Inventory,
        storage: NetworkStorage,
        request: RequestBlockEntity?,
        master: MasterBlockEntity
    ) : this(syncId, playerInventory, storage) {
        this.request = request
        this.master = master

        val uniqueDifferentiators = HashSet<Any>()
        storage.parts.forEach { part ->
            if (uniqueDifferentiators.add(part.differentiator)) {
                visualStorages[part] = part
                    .map { it.toView() }
            }
        }

        for (i in 0 until 54) {
            trackedViews.add(ItemStack.EMPTY.toView())
        }

        addSlotListener(object : ContainerListener {
            override fun dataChanged(handler: AbstractContainerMenu, property: Int, value: Int) {}

            override fun slotChanged(handler: AbstractContainerMenu, slotId: Int, stack: ItemStack) {
                s2c(player, ClientboundContainerSetSlotPacket(syncId, incrementStateId(), slotId, stack))
            }
        })
    }

    fun scheduleSort(mode: SortMode, filter: String) {
        scheduledSortData = SortData(mode, filter)
    }

    fun scroll(amount: Int) {
        val scroll = amount.coerceIn(0, maxScroll)

        for (i in 0 until viewedHeight * 9) {
            val stack = filledViews.getOrElse(i + 9 * scroll) { ItemView.EMPTY }
            itemViews[i].update(stack)
        }

        lastScroll = scroll
    }

    /** server only **/
    fun multiSlotAction(i: Int, data: Int, type: ClickType) {
        val view = itemViews[i]
        val variant = view.toVariant()
        var cursor = carried

        if (cursor.isEmpty) {
            if (type == CLONE) {
                if (player.abilities.instabuild && cursor.isEmpty) cursor = view.toStack(view.item.maxStackSize)
            } else {
                if (type == THROW) {
                    val all = data == 1
                    Transaction.openOuter().use { transaction ->
                        val extracted = storage
                            .extract(variant, if (all) variant.item.maxStackSize.toLong() else 1, transaction)
                        if (extracted > 0) {
                            player.storage.drop(variant, extracted, transaction)
                        }
                        transaction.commit()
                    }
                } else if (type != SWAP || !view.isItemAndTagEqual(playerInventory.getItem(data))) {
                    when (type) {
                        SWAP -> Transaction.openOuter().use { transaction ->
                            val slot = player.storage.getSlot(data)
                            val extracted = storage.extract(variant, slot.capacity - slot.amount, transaction)
                            slot.insert(variant, extracted, transaction)
                            transaction.commit()
                        }

                        QUICK_MOVE -> Transaction.openOuter().use { transaction ->
                            val stock = storage.simulateExtract(variant, variant.item.maxStackSize.toLong(), transaction)
                            val inserted = player.storage.offer(variant, stock, transaction)
                            storage.extract(variant, inserted, transaction)
                            transaction.commit()
                        }

                        else -> if (!variant.isBlank) Transaction.openOuter().use { transaction ->
                            val max = min(view.count.toLong(), variant.item.maxStackSize.toLong())
                            val request = if (data == 1) (max + 1) / 2 else max
                            val extracted = storage.extract(variant, request, transaction)
                            cursorStorage.insert(variant, extracted, transaction)
                            cursor = cursorStorage.resource.toStack(cursorStorage.amount.toInt())
                            transaction.commit()
                        }
                    }
                }
            }
        } else if (type == PICKUP) {
            cursor = moveStackToNetwork(cursor)
        }

        updateCursor(cursor)
    }

    fun craftingResultSlotClick(button: Int, quickMove: Boolean) {
        if (button !in 0..2) return

        var cursor = carried
        val resultStack = result.getItem(0)

        if (resultStack.isEmpty) return

        if (button == 2) {
            if (player.abilities.instabuild && cursor.isEmpty) cursor = resultStack.copy().apply { count =
                maxStackSize
            }
        } else {
            while (true) {
                val merged = cursor.merge(resultStack)
                if (!merged.second.isEmpty || merged.allEmpty()) break

                cursor = merged.first
                resultStack.onCraftedBy(player.level, player, resultStack.count)

                val remainingStacks = player.level.fastRecipeManager
                    .getRemainingItemsFor(RecipeType.CRAFTING, input, player.level)

                var finished = false
                for (i in remainingStacks.indices) {
                    val remainingStack = remainingStacks[i]
                    val inputStack = input.getItem(i)
                    if (!inputStack.isEmpty) {
                        if (inputStack.count != 1) {
                            inputStack.shrink(1)
                            player.moveOrNetworkOrDrop(remainingStack, true)
                        } else {
                            val variant = ItemVariant.of(inputStack)
                            var extracted = false
                            Transaction.openOuter().use { transaction ->
                                if (storage.extract(variant, 1, transaction) == 1L) {
                                    extracted = true
                                }
                                transaction.commit()
                            }
                            if (extracted) player.moveOrNetworkOrDrop(remainingStack, true)
                            else {
                                inputStack.shrink(1)
                                if (inputStack.isEmpty) input.setItem(i, remainingStack)
                                else player.moveOrNetworkOrDrop(remainingStack, true)
                                finished = true
                                continue
                            }
                        }
                    }
                }
                result.awardUsedRecipes(player)
                result.setItem(0, ItemStack.EMPTY)
                slotsChanged(input)
                if (!quickMove || finished) break
            }
            if (quickMove) {
                player.moveOrNetworkOrDrop(cursor, true)
            }
        }
        updateCursor(cursor)
    }

    fun applyRecipe(recipe: Recipe<*>) {
        if (recipe.type == RecipeType.CRAFTING) {
            clearCraftingGrid()
            broadcastChanges()
            placeRecipe(3, 3, -1, recipe, recipe.ingredients.iterator(), 0)
        }
    }

    fun clearCraftingGrid(toPlayerInventory: Boolean = false) {
        for (i in 1..9) slots[i].apply {
            if (toPlayerInventory) moveItemStackTo(item, 10, 46, false)
            setByPlayer(moveStackToNetwork(item))
        }
    }

    fun move() {
        var cursor = carried
        slots.forEach {
            if (it.container is Inventory
                && it.containerSlot >= 9
                && it.mayPickup(player)
                && (cursor.isEmpty || cursor.sameItem(it.item))
            ) {
                it.setByPlayer(moveStackToNetwork(it.item))
            }
        }

        if (!cursor.isEmpty) {
            cursor = moveStackToNetwork(cursor)
        }

        updateCursor(cursor)
    }

    fun restock() {
        var cursor = carried
        if (cursor.isEmpty) slots.filter { it.container is Inventory }.forEach {
            it.setByPlayer(it.item.restock())
        } else {
            cursor = cursor.restock()
        }

        updateCursor(cursor)
    }

    open fun resize(viewedHeight: Int, craft: Boolean) {
        val coerced = viewedHeight.coerceIn(3, 6)
        val h = coerced * 18 + 23

        val craftH = if (craft) 67 else 0

        slots.clear()

        addSlotOnly(ResultSlot(playerInventory.player, this.input, this.result, 0, -999999, -999999 + h))

        for (m in 0 until 3) for (l in 0 until 3) addSlotOnly(
            if (craft) Slot(input, l + m * 3, 30 + l * 18, 8 + m * 18 + h)
            else LockedSlot(input, l + m * 3)
        )

        for (m in 0 until 3) for (l in 0 until 9) addSlotOnly(
            Slot(playerInventory, l + m * 9 + 9, 8 + l * 18, 9 + craftH + m * 18 + h)
        )

        for (m in 0 until 9) addSlotOnly(
            Slot(playerInventory, m, 8 + m * 18, 67 + craftH + h)
        )

        this.viewedHeight = coerced
    }

    private fun addSlotOnly(slot: Slot): Slot {
        slot.index = slots.size
        slots.add(slot)
        return slot
    }

    private fun updateCursor(stack: ItemStack) {
        setCarried(stack)
        s2c(player, UPDATE_CURSOR) {
            stack(stack)
        }
    }

    private fun moveStackToNetwork(stack: ItemStack): ItemStack {
        if (stack.isEmpty) return stack
        val variant = ItemVariant.of(stack)
        var count = stack.count.toLong()

        Transaction.openOuter().use { transaction ->
            count -= storage.insert(variant, count, transaction)
            transaction.commit()
        }

        return variant.toStack(count.toInt())
    }

    private fun moveStackToPlayerOrNetwork(player: Player, stack: ItemStack): ItemStack {
        if (stack.isEmpty) return stack

        Transaction.openOuter().use { transaction ->
            val variant = ItemVariant.of(stack)
            val count = stack.count.toLong()
            val offered = player.storage.offer(variant, count, transaction)
            if (offered > 0L) {
                stack.shrink(offered.toInt())
                transaction.commit()
            }
        }

        return moveStackToNetwork(stack)
    }

    private fun Player.moveOrNetworkOrDrop(stack: ItemStack, retainOwnership: Boolean) {
        drop(moveStackToPlayerOrNetwork(this, stack), retainOwnership)
        stack.count = 0
    }

    private fun ItemStack.restock(max: Int = 64): ItemStack {
        if (isEmpty) return ItemStack.EMPTY

        val stack = copy()
        val variant = ItemVariant.of(stack)
        val space = (min(stack.maxStackSize, max) - stack.count).toLong()

        Transaction.openOuter().use { transaction ->
            val extracted = storage.extract(variant, space, transaction).toInt()
            stack.count += extracted
            transaction.commit()
        }

        return stack
    }

    private fun onRemoved(key: String) {
        if (player is ServerPlayer) {
            s2c(player, ClientboundContainerClosePacket(containerId))
            player.actionBar("container.slotlink.request.$key")
        }
    }

    private fun sort(sortData: SortData) {
        sortData.mode.sort(filledViews)

        if (lastSortData != sortData) scroll(0) else scroll(lastScroll)
        lastSortData = sortData

        s2c(player, UPDATE_SLOT_NUMBERS) {
            int(containerId)
            int(totalSlotSize)
            int(filledSlotSize)
        }
    }

    override fun clicked(i: Int, j: Int, actionType: ClickType, playerEntity: Player) {
        if (playerEntity !is ServerPlayer) return
        super.clicked(i, j, actionType, playerEntity)
        s2c(playerEntity, UPDATE_CURSOR) {
            stack(carried)
        }
    }

    override fun quickMoveStack(player: Player, index: Int): ItemStack {
        val inventory = slots[index].container
        var stack = ItemStack.EMPTY
        when (inventory) {
            is ResultContainer -> {
                stack = super.quickMoveStack(player, index)
            }

            is CraftingContainer -> {
                super.quickMoveStack(player, index)
                stack = moveStackToNetwork(slots[index].item)
            }

            is Inventory -> {
                stack = moveStackToNetwork(slots[index].item)
                slots[index].setByPlayer(stack)
                stack = super.quickMoveStack(player, index)
            }
        }
        return stack
    }

    override fun addItemToSlot(inputs: Iterator<Ingredient>, slot: Int, amount: Int, gridX: Int, gridY: Int) {
        val ingredient = inputs.next()
        if (ingredient.isEmpty) return

        val matchingVariant = ingredient.items.map { ItemVariant.of(it) }

        Transaction.openOuter().use { transaction ->
            for (variant in matchingVariant) {
                val extracted = storage.extract(variant, 1L, transaction)
                if (extracted > 0L) {
                    input.setItem(slot, variant.toStack())
                    transaction.commit()
                    return
                }
            }
        }

        val stack = slots
            .firstOrNull { it.container is Inventory && it.mayPickup(player) && ingredient.test(it.item) }
            ?.remove(1) ?: return

        input.setItem(slot, stack)
    }

    override fun slotsChanged(inventory: Container) {
        if (inventory is CraftingContainer) if (player is ServerPlayer) {
            var stack = ItemStack.EMPTY
            val optional: Optional<CraftingRecipe> =
                player.server.fastRecipeManager.getRecipeFor(RecipeType.CRAFTING, input, player.level)
            if (optional.isPresent) {
                val craftingRecipe = optional.get()
                if (result.setRecipeUsed(player.level, player, craftingRecipe)) {
                    stack = craftingRecipe.assemble(input, player.level.registryAccess())
                }
            }
            result.setItem(0, stack)
            s2c(player, ClientboundContainerSetSlotPacket(containerId, incrementStateId(), 0, stack))
        }
    }

    override fun broadcastChanges() {
        super.broadcastChanges()

        if (player !is ServerPlayer) return

        var resort = false

        visualStorages.forEach { entry ->
            val storage = entry.key
            val caches = entry.value

            for ((i, view) in storage.withIndex()) {
                val cacheView = caches[i]
                if (!cacheView.isItemAndTagEqual(view)) {
                    if (cacheView.isEmpty && !view.isEmpty) {
                        filledSlotSize++
                    } else if (!cacheView.isEmpty && view.isEmpty) {
                        filledSlotSize--
                    }

                    val beforeId = filledViews.indexOfFirst { cacheView.isItemAndTagEqual(it) }
                    if (beforeId >= 0) {
                        val beforeMatch = filledViews[beforeId]
                        beforeMatch.count -= cacheView.count
                        if (beforeMatch.isEmpty) {
                            filledViews.removeAt(beforeId)
                        }
                    }

                    if (!view.isEmpty && lastSortData.filters.all { it.match(view) }) {
                        val afterMatch = filledViews.firstOrNull { it.isItemAndTagEqual(view) }
                        if (afterMatch == null) {
                            filledViews.add(view.toView())
                        } else {
                            afterMatch.count += view.amount.toInt()
                        }
                    }

                    resort = true
                    cacheView.update(view.resource.item, view.resource.nbt?.copy(), view.amount.toInt())
                } else if (cacheView.count != view.amount.toInt()) {
                    val filled = filledViews.firstOrNull { cacheView.isItemAndTagEqual(it) }
                    if (filled != null) {
                        filled.count -= cacheView.count - view.amount.toInt()
                        cacheView.update(view.resource.item, view.resource.nbt?.copy(), view.amount.toInt())
                        resort = true
                    }
                }

            }
        }


        if (resort) sort(lastSortData)

        scheduledSortData?.let { sortData ->
            scheduledSortData = null
            totalSlotSize = 0
            filledSlotSize = 0
            filledViews.clear()

            visualStorages.keys.forEach { storage ->
                storage.forEach { view ->
                    totalSlotSize++
                    if (!view.isEmpty) {
                        filledSlotSize++

                        if (sortData.filters.all { it.match(view) }) {
                            val match = filledViews.firstOrNull { it.isItemAndTagEqual(view) }
                            if (match == null) {
                                filledViews.add(view.toView())
                            } else {
                                match.count += view.amount.toInt()
                            }
                        }
                    }
                }
            }

            sort(sortData)
        }

        itemViews.forEachIndexed { i, after ->
            val before = trackedViews[i]
            if (before != after) {
                s2c(player, UPDATE_VIEWED_STACK) {
                    int(containerId)
                    int(i)
                    item(after.item)
                    nbt(after.nbt)
                    int(after.count)
                }
                before.update(after)
            }
        }

        val max = ceil((filledViews.size / 9f) - viewedHeight).toInt().coerceAtLeast(0)
        if (maxScroll != max) {
            s2c(player, UPDATE_MAX_SCROLL) {
                int(containerId)
                int(max)
            }
            maxScroll = max
            scroll(0)
        }
    }

    override fun stillValid(player: Player?) = true

    override fun getType(): MenuType<*> = Screens.REQUEST

    override fun removed(player: Player?) {
        if (player !is ServerPlayer) return

        if (!carried.isEmpty) if (player.isAlive && !player.hasDisconnected()) {
            player.moveOrNetworkOrDrop(carried, false)
        } else {
            player.drop(moveStackToNetwork(carried), false)
        }

        // try to move crafting input to network first
        for (i in 0 until input.containerSize) {
            input.setItem(i, moveStackToNetwork(input.getItem(i)))
        }
        // then move to player inventory, and drop if fail
        clearContainer(player, input)

        request?.watchers?.remove(this)
        request?.setChanged()
        master?.watchers?.remove(this)
        master?.unmarkForcedChunks()
    }

    override fun onMasterRemoved() = onRemoved("brokenMaster")

    override fun onRemoved() = onRemoved("brokenSelf")

    private inner class SortData(
        val mode: SortMode,
        filter: String,
    ) {

        val filters by lazy { filter.trim().split(whitespaceRegex).map { Filter(it) } }

    }

    private inner class Filter(string: String) {

        val first = string.getOrElse(0) { 'w' }
        val term = when (first) {
            '@', '#' -> string.drop(1)
            else -> string
        }

        @Suppress("DEPRECATION")
        fun match(view: StorageView<ItemVariant>): Boolean = term.isBlank() || when (first) {
            '@' -> BuiltInRegistries.ITEM.getKey(view.resource.item).toString().contains(term, true)
            '#' -> BuiltInRegistries.ITEM
                .tagNames
                .anyMatch { it.location.toString().contains(term, true) && view.resource.item.builtInRegistryHolder()
                    .`is`(it) }

            else -> view.resource.toStack().hoverName.string.contains(term, true)
        }

    }

    @Suppress("unused")
    enum class SortMode(
        private val id: String,
        val sort: (ArrayList<ItemView>) -> Any
    ) {

        NAME("name", { it -> it.sortBy { it.singleStack.hoverName.string } }),
        NAME_DESC("name_desc", { it -> it.sortByDescending { it.singleStack.item.description.string } }),

        ID("id", { it -> it.sortBy { BuiltInRegistries.ITEM.getKey(it.item).toString() } }),
        ID_DESC("id_desc", { it -> it.sortByDescending { BuiltInRegistries.ITEM.getKey(it.item).toString() } }),

        COUNT("count", { it -> it.sortBy { it.count } }),
        COUNT_DESC("count_desc", { it -> it.sortByDescending { it.count } });

        companion object {

            val values = values()

        }

        fun next(): SortMode {
            return values[(ordinal + 1) % values.size]
        }

        override fun toString() = id

    }

}
