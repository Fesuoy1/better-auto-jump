package fesuoy.client.mixin;

import fesuoy.autojump.JumpTrigger;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.KeyboardInput;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(KeyboardInput.class)
public abstract class KeyboardInputMixin {

    @Inject(method = "tick()V", at = @At("RETURN"))
    private void onTick(CallbackInfo ci) {
        if (JumpTrigger.consumeJump()) {
            LocalPlayer player = Minecraft.getInstance().player;
            if (player != null) {
                ((LocalPlayerAccessor) player).setAutoJumpTime(1);
            }
        }
    }
}
