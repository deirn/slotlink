package badasintended.slotlink.mixin.pseudo;

import badasintended.slotlink.compat.invsort.InventorySortButton;
import net.minecraft.client.gui.components.ImageButton;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;

@Pseudo
@Mixin(targets = "net.kyrptonaught.inventorysorter.client.SortButtonWidget")
public abstract class SortButtonWidgetMixin extends ImageButton implements InventorySortButton {

    @Unique
    private boolean initialized = false;

    public SortButtonWidgetMixin() {
        super(0, 0, 0, 0, 0, 0, 0, null, null);
    }

    @Override
    public boolean slotlink$isInitialized() {
        return initialized;
    }

    @Override
    public void slotlink$setInitialized(boolean initialized) {
        this.initialized = initialized;
    }

}
