package fesuoy.client;

import fesuoy.client.autojump.EdgeDetector;
import fesuoy.autojump.JumpTrigger;
import fesuoy.autojump.SprintManager;
import fesuoy.config.BetterAutoJumpConfig;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;

public class BetterAutoJumpClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        BetterAutoJumpConfig config = BetterAutoJumpConfig.getInstance();
        JumpTrigger.setCooldownMax(config.cooldownTicks);

        ClientTickEvents.START_CLIENT_TICK.register(client -> {
            if (!config.enabled || !config.edgeJumpEnabled) return;
            if (client.player == null || client.level == null) return;
            if (JumpTrigger.isPending()) return;

            if (EdgeDetector.detect(client.player, client.level)) {
                SprintManager.record(client.player.isSprinting());
                int variance = client.isLocalServer() ? config.varianceTicks : Math.max(1, config.varianceTicks);
                JumpTrigger.set(variance);
            }
        });
    }
}
