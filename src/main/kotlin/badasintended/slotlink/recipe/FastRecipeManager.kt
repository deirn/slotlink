package badasintended.slotlink.recipe

import badasintended.slotlink.util.callGetAllOfType
import badasintended.slotlink.util.recipesAccess
import java.util.*
import net.minecraft.world.Container
import net.minecraft.world.item.crafting.Recipe
import net.minecraft.world.item.crafting.RecipeManager
import net.minecraft.world.item.crafting.RecipeType
import net.minecraft.server.MinecraftServer
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.level.Level

private val holder = WeakHashMap<RecipeManager, FastRecipeManager>()

val Level.fastRecipeManager get() = holder.getOrPut(recipeManager) { FastRecipeManager(recipeManager) }!!
val MinecraftServer.fastRecipeManager get() = holder.getOrPut(recipeManager) { FastRecipeManager(recipeManager) }!!

/**
 * An attempt to optimize [RecipeManager]'s unnecessarily expensive filtering method
 * by caching previous results and using saner way to filter.
 */
class FastRecipeManager(
    private val delegate: RecipeManager
) : RecipeManager() {

    private val firstMatchCache = WeakHashMap<Container, Recipe<Container>>()
    private val getCache = WeakHashMap<ResourceLocation, Recipe<*>>()

    @Synchronized
    @Suppress("UNCHECKED_CAST")
    override fun <C : Container, T : Recipe<C>> getRecipeFor(
        type: RecipeType<T>,
        inventory: C,
        world: Level
    ): Optional<T> {
        val cache = firstMatchCache[inventory]
        if (cache != null && cache.type == type && cache.matches(inventory, world)) {
            return Optional.of(cache as T)
        } else {
            firstMatchCache.remove(inventory)
        }

        val result = delegate.callGetAllOfType(type).values.firstOrNull { it.matches(inventory, world) }
        if (result != null) {
            firstMatchCache[inventory] = result as Recipe<Container>
            return Optional.of(result as T)
        }

        return Optional.empty()
    }

    @Synchronized
    override fun byKey(id: ResourceLocation): Optional<out Recipe<*>> {
        return Optional.ofNullable(getCache.getOrPut(id) { delegate.recipesAccess.values.firstNotNullOfOrNull { it[id] } })
    }

}