package badasintended.slotlink.util

import badasintended.slotlink.mixin.CraftingScreenHandlerAccessor
import badasintended.slotlink.mixin.HandledScreenAccessor
import badasintended.slotlink.mixin.RecipeManagerAccessor
import badasintended.slotlink.mixin.TextFieldWidgetAccessor
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen
import net.minecraft.client.gui.components.EditBox
import net.minecraft.world.Container
import net.minecraft.world.item.crafting.Recipe
import net.minecraft.world.item.crafting.RecipeManager
import net.minecraft.world.item.crafting.RecipeType
import net.minecraft.world.inventory.CraftingMenu
import net.minecraft.resources.ResourceLocation

inline val CraftingMenu.input get() = (this as CraftingScreenHandlerAccessor).input
inline val CraftingMenu.result get() = (this as CraftingScreenHandlerAccessor).result

inline var EditBox.focusedTicks
    get() = (this as TextFieldWidgetAccessor).focusedTicks
    set(value) = (this as TextFieldWidgetAccessor).run { focusedTicks = value }

inline val AbstractContainerScreen<*>.x get() = (this as HandledScreenAccessor).x
inline val AbstractContainerScreen<*>.y get() = (this as HandledScreenAccessor).y
inline val AbstractContainerScreen<*>.backgroundWidth get() = (this as HandledScreenAccessor).backgroundWidth
inline val AbstractContainerScreen<*>.backgroundHeight get() = (this as HandledScreenAccessor).backgroundHeight

inline val RecipeManager.recipes: MutableMap<RecipeType<*>, MutableMap<ResourceLocation, Recipe<*>>>
    get() = (this as RecipeManagerAccessor).recipes

fun <C : Container, T : Recipe<C>> RecipeManager.callGetAllOfType(type: RecipeType<T>): MutableMap<ResourceLocation, Recipe<C>> =
    (this as RecipeManagerAccessor).callGetAllOfType(type)
