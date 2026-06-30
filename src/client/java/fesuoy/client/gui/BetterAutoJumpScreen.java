package fesuoy.client.gui;

import fesuoy.config.BetterAutoJumpConfig;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

import java.util.List;
import java.util.function.IntConsumer;

public class BetterAutoJumpScreen extends Screen {

    private final Screen parent;
    private BetterAutoJumpConfig config;

    private Button masterToggle;
    private Button obstacleToggle;
    private Button edgeToggle;
    private Button presetButton;
    private Button edgeSettingsButton;
    private Button obstacleVarianceButton;
    private Button obstacleCooldownButton;
    private GridLayout layout;

    protected BetterAutoJumpScreen(Screen parent) {
        super(Component.literal("Better Auto-Jump Config"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        this.config = BetterAutoJumpConfig.getInstance();

        this.layout = new GridLayout().columnSpacing(10).rowSpacing(6);
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
        ).width(410).tooltip(Tooltip.create(Component.literal("Master toggle for all Better Auto-Jump features"))).build();
        grid.addChild(masterToggle, 2);

        obstacleToggle = Button.builder(
                toggleLabel("Obstacle Jump", config.obstacleJumpEnabled),
                btn -> {
                    config.obstacleJumpEnabled = !config.obstacleJumpEnabled;
                    btn.setMessage(toggleLabel("Obstacle Jump", config.obstacleJumpEnabled));
                    updateButtons();
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

        grid.addChild(new StringWidget(Component.literal("\u00a7lTiming"), this.font), 2);

        addIntSlider(grid, "Variance", config.varianceTicks, 0, 5, 1,
                v -> config.varianceTicks = v);

        addIntSlider(grid, "Cooldown", config.cooldownTicks, 1, 10, 1,
                v -> config.cooldownTicks = v);

        obstacleVarianceButton = Button.builder(
                toggleLabel("Obstacle Variance", config.obstacleVarianceEnabled),
                btn -> {
                    config.obstacleVarianceEnabled = !config.obstacleVarianceEnabled;
                    btn.setMessage(toggleLabel("Obstacle Variance", config.obstacleVarianceEnabled));
                }
        ).width(200).tooltip(Tooltip.create(Component.literal("Apply random delay before obstacle jumps"))).build();
        grid.addChild(obstacleVarianceButton);

        obstacleCooldownButton = Button.builder(
                toggleLabel("Obstacle Cooldown", config.obstacleCooldownEnabled),
                btn -> {
                    config.obstacleCooldownEnabled = !config.obstacleCooldownEnabled;
                    btn.setMessage(toggleLabel("Obstacle Cooldown", config.obstacleCooldownEnabled));
                }
        ).width(200).tooltip(Tooltip.create(Component.literal("Apply cooldown between consecutive obstacle jumps"))).build();
        grid.addChild(obstacleCooldownButton);

        grid.addChild(new StringWidget(Component.literal("\u00a7lDetection"), this.font), 2);

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
        ).width(200).tooltip(Tooltip.create(Component.literal("Fine-tune detection parameters, save custom presets"))).build();
        grid.addChild(edgeSettingsButton);

        grid.addChild(Button.builder(
                Component.literal("Save & Close"),
                btn -> {
                    config.save();
                    this.minecraft.gui.setScreen(parent);
                }
        ).width(200).tooltip(Tooltip.create(Component.literal("Save settings and close"))).build());

        grid.addChild(Button.builder(
                CommonComponents.GUI_CANCEL,
                btn -> {
                    BetterAutoJumpConfig.reload();
                    this.minecraft.gui.setScreen(parent);
                }
        ).width(200).tooltip(Tooltip.create(Component.literal("Discard changes and close"))).build());

        layout.arrangeElements();
        layout.setPosition((width - layout.getWidth()) / 2, (height - layout.getHeight()) / 2);
        layout.visitWidgets(this::addRenderableWidget);

        updateButtons();
    }

    private void updateButtons() {
        boolean anyEnabled = config.enabled;
        boolean obstacleActive = anyEnabled && config.obstacleJumpEnabled;
        obstacleToggle.active = anyEnabled;
        obstacleVarianceButton.active = obstacleActive;
        obstacleCooldownButton.active = obstacleActive;
        edgeToggle.active = anyEnabled;
        presetButton.active = anyEnabled;
        edgeSettingsButton.active = anyEnabled;
    }

    private void addIntSlider(GridLayout.RowHelper grid, String label, int current,
                              int min, int max, int step, IntConsumer setter) {
        double range = max - min;
        double initial = range > 0 ? (current - min) / range : 0.0;
        grid.addChild(new AbstractSliderButton(0, 0, 410, 20,
                Component.literal(label + ": " + current + " tick" + (current == 1 ? "" : "s")), initial) {
            {
                updateMessage();
            }

            @Override
            protected void updateMessage() {
                int val = (int) Math.round(min + Mth.clamp(this.value, 0.0, 1.0) * range);
                this.setMessage(Component.literal(label + ": " + val + " tick" + (val == 1 ? "" : "s")));
            }

            @Override
            protected void applyValue() {
                int val = (int) Math.round(min + Mth.clamp(this.value, 0.0, 1.0) * range);
                setter.accept(val);
            }
        }, 2);
    }

    private static Component toggleLabel(String name, boolean value) {
        return Component.literal(name + ": " + (value ? "ON" : "OFF"));
    }

    @Override
    public void onClose() {
        BetterAutoJumpConfig.reload();
        this.minecraft.gui.setScreen(parent);
    }
}
