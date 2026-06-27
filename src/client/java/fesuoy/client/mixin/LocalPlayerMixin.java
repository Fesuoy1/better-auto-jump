package fesuoy.client.mixin;

import fesuoy.client.autojump.ImprovedAutoJump;
import fesuoy.autojump.SprintManager;
import fesuoy.config.BetterAutoJumpConfig;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(LocalPlayer.class)
public abstract class LocalPlayerMixin {

    @Shadow
    private int autoJumpTime;

    @Shadow
    protected abstract boolean canAutoJump();

    /**
     * @author Fesuoy
     * @reason Replaced with ImprovedAutoJump
     */
    @Overwrite
    public void updateAutoJump(float dx, float dz) {
        if (canAutoJump() && BetterAutoJumpConfig.getInstance().enabled && BetterAutoJumpConfig.getInstance().obstacleJumpEnabled) {
            LocalPlayer player = (LocalPlayer) (Object) this;
            if (ImprovedAutoJump.autojumpPlayer(player, dx, dz)) {
                SprintManager.record(player.isSprinting());
                autoJumpTime = 1;
            } else {
                autoJumpTime = 0;
            }
        }
    }
}
