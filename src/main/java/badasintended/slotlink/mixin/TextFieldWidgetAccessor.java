package badasintended.slotlink.mixin;

import net.minecraft.client.gui.components.EditBox;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(EditBox.class)
public interface TextFieldWidgetAccessor {

    @Accessor("frame")
    int getFocusedTicks();

    @Accessor("frame")
    void setFocusedTicks(int value);

}
