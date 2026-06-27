package fesuoy.client.gui;

import fesuoy.autojump.JumpTrigger;
import fesuoy.config.BetterAutoJumpConfig;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

import java.util.List;

public class BetterAutoJumpScreen extends Screen {

    private final Screen parent;
    private BetterAutoJumpConfig config;

    private Button masterToggle;
    private Button obstacleToggle;
    private Button edgeToggle;
    private Button presetButton;
    private Button edgeSettingsButton;
    private Button varianceButton;
    private Button cooldownButton;
    private GridLayout layout;

    protected BetterAutoJumpScreen(Screen parent) {
        super(Component.literal("Better Auto-Jump Config"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        this.config = BetterAutoJumpConfig.getInstance();

        this.layout = new GridLayout().columnSpacing(10).rowSpacing(8);
        layout.defaultCellSetting().alignHorizontallyCenter();
        var grid = layout.createRowHelper(2);

        grid.addChild(new StringWidget(this.title, this.font), 2);

        masterToggle = Button.builder(
                toggleLabel("Enabled", config.enabled),
                btn -> {
                    config.enabled = !config.enabled;
                    btn.setMessage(toggleLabel("Enabled", config.enabled));
                    updateButtons();
                }
        ).width(200).tooltip(Tooltip.create(Component.literal("Master toggle for all Better Auto-Jump features"))).build();
        grid.addChild(masterToggle);

        obstacleToggle = Button.builder(
                toggleLabel("Obstacle Jump", config.obstacleJumpEnabled),
                btn -> {
                    config.obstacleJumpEnabled = !config.obstacleJumpEnabled;
                    btn.setMessage(toggleLabel("Obstacle Jump", config.obstacleJumpEnabled));
                }
        ).width(200).tooltip(Tooltip.create(Component.literal("Automatically jumps over blocks up to jump height"))).build();
        grid.addChild(obstacleToggle);

        edgeToggle = Button.builder(
                toggleLabel("Edge Jump", config.edgeJumpEnabled),
                btn -> {
                    config.edgeJumpEnabled = !config.edgeJumpEnabled;
                    btn.setMessage(toggleLabel("Edge Jump", config.edgeJumpEnabled));
                }
        ).width(200).tooltip(Tooltip.create(Component.literal("Automatically jumps when approaching a block edge while moving"))).build();
        grid.addChild(edgeToggle);

        presetButton = Button.builder(
                Component.literal("Preset: " + config.edgePreset),
                btn -> {
                    List<String> allNames = config.allPresetNames();
                    if (allNames.isEmpty()) return;
                    int idx = allNames.indexOf(config.edgePreset);
                    String next = idx < 0 || idx >= allNames.size() - 1 ? allNames.getFirst() : allNames.get(idx + 1);
                    config.applyPresetByName(next);
                    btn.setMessage(Component.literal("Preset: " + next));
                }
        ).width(200).tooltip(Tooltip.create(Component.literal("Cycle through built-in and saved custom presets"))).build();
        grid.addChild(presetButton);

        edgeSettingsButton = Button.builder(
                Component.literal("Advanced Settings..."),
                btn -> this.minecraft.gui.setScreen(new BetterAutoJumpEdgeScreen(this))
        ).width(200).tooltip(Tooltip.create(Component.literal("Fine-tune edge and obstacle detection parameters, save custom presets"))).build();
        grid.addChild(edgeSettingsButton, 2);

        varianceButton = Button.builder(
                varianceLabel(config.varianceTicks),
                btn -> {
                    config.varianceTicks = (config.varianceTicks + 1) % 6;
                    btn.setMessage(varianceLabel(config.varianceTicks));
                }
        ).width(200).tooltip(Tooltip.create(Component.literal("Random delay before edge jump (0-5 ticks) to vary timing"))).build();
        grid.addChild(varianceButton);

        cooldownButton = Button.builder(
                cooldownLabel(config.cooldownTicks),
                btn -> {
                    config.cooldownTicks = config.cooldownTicks >= 10 ? 1 : config.cooldownTicks + 1;
                    btn.setMessage(cooldownLabel(config.cooldownTicks));
                    JumpTrigger.setCooldownMax(config.cooldownTicks);
                }
        ).width(200).tooltip(Tooltip.create(Component.literal("Minimum ticks between consecutive edge jumps"))).build();
        grid.addChild(cooldownButton);

        grid.addChild(Button.builder(
                Component.literal("Save & Close"),
                btn -> {
                    config.save();
                    this.minecraft.gui.setScreen(parent);
                }
        ).width(200).tooltip(Tooltip.create(Component.literal("Save settings and close"))).build());

        grid.addChild(Button.builder(
                CommonComponents.GUI_CANCEL,
                btn -> this.minecraft.gui.setScreen(parent)
        ).width(200).tooltip(Tooltip.create(Component.literal("Discard changes and close"))).build());

        layout.arrangeElements();
        layout.setPosition((width - layout.getWidth()) / 2, (height - layout.getHeight()) / 2);
        layout.visitWidgets(this::addRenderableWidget);

        updateButtons();
    }

    @Override
    protected void repositionElements() {
        layout.arrangeElements();
        layout.setPosition((width - layout.getWidth()) / 2, (height - layout.getHeight()) / 2);
    }

    private void updateButtons() {
        boolean anyEnabled = config.enabled;
        obstacleToggle.active = anyEnabled;
        edgeToggle.active = anyEnabled;
        presetButton.active = anyEnabled;
        edgeSettingsButton.active = anyEnabled;
        varianceButton.active = anyEnabled;
        cooldownButton.active = anyEnabled;
    }

    private static Component toggleLabel(String name, boolean value) {
        return Component.literal(name + ": " + (value ? "ON" : "OFF"));
    }

    private static Component varianceLabel(int ticks) {
        return Component.literal("Variance: " + ticks + " tick" + (ticks == 1 ? "" : "s"));
    }

    private static Component cooldownLabel(int ticks) {
        return Component.literal("Cooldown: " + ticks + " tick" + (ticks == 1 ? "" : "s"));
    }

    @Override
    public void onClose() {
        this.minecraft.gui.setScreen(parent);
    }
}
