package badasintended.slotlink.mixin;

import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.inventory.CraftingMenu;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(CraftingMenu.class)
public interface CraftingScreenHandlerAccessor {

    @NotNull
    @Accessor("craftSlots")
    CraftingContainer getInput();

    @NotNull
    @Accessor("resultSlots")
    ResultContainer getResult();

}
