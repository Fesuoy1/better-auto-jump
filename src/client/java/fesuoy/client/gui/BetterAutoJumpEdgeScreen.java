package fesuoy.client.gui;

import fesuoy.config.BetterAutoJumpConfig;
import fesuoy.config.BetterAutoJumpConfig.CustomPreset;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

import java.util.List;
import java.util.function.DoubleConsumer;

public class BetterAutoJumpEdgeScreen extends Screen {

    private final Screen parent;
    private BetterAutoJumpConfig config;
    private Button presetButton;
    private EditBox presetNameBox;
    private Button saveCloseButton;

    protected BetterAutoJumpEdgeScreen(Screen parent) {
        super(Component.literal("Detection Settings"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        this.config = BetterAutoJumpConfig.getInstance();

        GridLayout content = new GridLayout().columnSpacing(10).rowSpacing(4);
        content.defaultCellSetting().alignHorizontallyCenter();
        var grid = content.createRowHelper(1);

        // Title
        grid.addChild(new StringWidget(this.title, this.font));

        // Preset selector
        presetButton = Button.builder(
                Component.literal("Preset: " + config.edgePreset),
                btn -> cyclePreset()
        ).width(280).tooltip(Tooltip.create(Component.literal("Cycle through built-in and saved custom presets"))).build();
        grid.addChild(presetButton);

        // --- Edge Detection ---
        grid.addChild(new StringWidget(Component.literal("§lEdge Detection"), this.font));

        grid.addChild(new StringWidget(Component.literal("§l  Scan Range"), this.font));
        addSlider(grid, "Sprint Distance", config.edgeScanMaxDistSprint, 0.5, 5.0, 0.1,
                v -> config.edgeScanMaxDistSprint = v);
        addSlider(grid, "Walk Distance", config.edgeScanMaxDistWalk, 0.5, 5.0, 0.1,
                v -> config.edgeScanMaxDistWalk = v);

        grid.addChild(new StringWidget(Component.literal("§l  Resolution"), this.font));
        addSlider(grid, "Step Distance", config.edgeScanStep, 0.05, 1.0, 0.05,
                v -> config.edgeScanStep = v);
        addSlider(grid, "Start Distance", config.edgeScanStart, 0.1, 1.0, 0.05,
                v -> config.edgeScanStart = v);

        grid.addChild(new StringWidget(Component.literal("§l  Hitbox"), this.font));
        addSlider(grid, "Scan Radius", config.edgeScanRadius, 0.1, 1.0, 0.05,
                v -> config.edgeScanRadius = v);
        addSlider(grid, "V.Offset", config.edgeVerticalOffset, 0.01, 0.3, 0.01,
                v -> config.edgeVerticalOffset = v);

        grid.addChild(new StringWidget(Component.literal("§l  Thresholds"), this.font));
        addSlider(grid, "Min Velocity", config.minVelocityThreshold, 0.01, 0.5, 0.01,
                v -> config.minVelocityThreshold = v);
        addSlider(grid, "Solid Min", config.solidAtFeetMin, 0.001, 0.1, 0.001,
                v -> config.solidAtFeetMin = v);
        addSlider(grid, "Solid Max", config.solidAtFeetMax, 0.1, 1.0, 0.1,
                v -> config.solidAtFeetMax = v);

        // --- Obstacle Detection ---
        grid.addChild(new StringWidget(Component.literal("§lObstacle Detection"), this.font));

        addSlider(grid, "Jump Height", config.obstacleJumpHeight, 0.5, 3.0, 0.05,
                v -> config.obstacleJumpHeight = v);
        addSlider(grid, "Prediction Mult", config.obstaclePredictionMult, 1.0, 20.0, 0.5,
                v -> config.obstaclePredictionMult = v);
        addSlider(grid, "BPT Clamp Max", config.obstacleBptClampMax, 0.2, 2.0, 0.05,
                v -> config.obstacleBptClampMax = v);
        addSlider(grid, "BPT Slow Thresh", config.obstacleBptSlowThreshold, 0.0, 0.8, 0.05,
                v -> config.obstacleBptSlowThreshold = v);
        addSlider(grid, "BPT Slow Mult", config.obstacleBptSlowMultiplier, 0.1, 2.0, 0.05,
                v -> config.obstacleBptSlowMultiplier = v);
        addSlider(grid, "Angle Threshold", config.obstacleAngleThreshold, 5.0, 90.0, 1.0,
                v -> config.obstacleAngleThreshold = v);

        // --- Save Custom Preset ---
        grid.addChild(new StringWidget(Component.literal("§lSave Custom Preset"), this.font));

        presetNameBox = new EditBox(this.font, 280, 20, Component.literal("Preset name"));
        presetNameBox.setMaxLength(64);
        presetNameBox.setResponder(text -> updateSaveCloseButton());
        grid.addChild(presetNameBox);

        grid.addChild(Button.builder(
                Component.literal("Save Current as Preset"),
                btn -> saveCustomPreset()
        ).width(280).tooltip(Tooltip.create(Component.literal("Save all current detection values as a named preset"))).build());

        // Bottom buttons
        saveCloseButton = Button.builder(
                Component.literal("Save & Close"),
                btn -> {
                    config.save();
                    this.minecraft.gui.setScreen(parent);
                }
        ).width(280).tooltip(Tooltip.create(Component.literal("Save & Close cannot be used while a preset name is entered. Save or clear it first."))).build();
        grid.addChild(saveCloseButton);

        grid.addChild(Button.builder(
                CommonComponents.GUI_CANCEL,
                btn -> {
                    BetterAutoJumpConfig.reload();
                    this.minecraft.gui.setScreen(parent);
                }
        ).width(280).build());

        updateSaveCloseButton();

        content.arrangeElements();

        // Center the content grid
        int contentWidth = content.getWidth();

        // Wrap in scrollable layout
        var scrollable = new net.minecraft.client.gui.components.ScrollableLayout(
                this.minecraft, content, this.height - 20
        );
        scrollable.arrangeElements();
        scrollable.setPosition((this.width - contentWidth) / 2, 10);
        scrollable.visitWidgets(this::addRenderableWidget);
    }

    private void cyclePreset() {
        List<String> allNames = config.allPresetNames();
        if (allNames.isEmpty()) return;

        int currentIdx = allNames.indexOf(config.edgePreset);
        int nextIdx;
        if (currentIdx < 0 || currentIdx >= allNames.size() - 1) {
            nextIdx = 0;
        } else {
            nextIdx = currentIdx + 1;
        }
        String nextName = allNames.get(nextIdx);
        config.applyPresetByName(nextName);
        rebuild();
    }

    private void saveCustomPreset() {
        String name = presetNameBox.getValue().trim();
        if (name.isEmpty()) return;

        // Remove existing preset with same name (overwrite)
        config.customPresets.removeIf(p -> p.name.equals(name));
        // Add new preset from current config values
        config.customPresets.add(new CustomPreset(name, config));
        config.edgePreset = name;
        presetNameBox.setValue("");
        updateSaveCloseButton();
        rebuild();
    }

    private void updateSaveCloseButton() {
        boolean hasText = !presetNameBox.getValue().trim().isEmpty();
        saveCloseButton.active = !hasText;
        saveCloseButton.setTooltip(hasText
                ? Tooltip.create(Component.literal("Save or clear the preset name first"))
                : Tooltip.create(Component.literal("Save config and close")));
    }

    private void addSlider(GridLayout.RowHelper grid, String label, double current,
                           double min, double max, double step, DoubleConsumer setter) {
        double initial = Mth.clamp((current - min) / (max - min), 0.0, 1.0);
        grid.addChild(new AbstractSliderButton(0, 0, 280, 20,
                Component.literal(label + ": " + formatValue(current, step)), initial) {
            {
                updateMessage();
            }
            @Override
            protected void updateMessage() {
                double val = snap(Mth.lerp(Mth.clamp(this.value, 0.0, 1.0), min, max), step);
                this.setMessage(Component.literal(label + ": " + formatValue(val, step)));
            }
            @Override
            protected void applyValue() {
                double val = snap(Mth.lerp(Mth.clamp(this.value, 0.0, 1.0), min, max), step);
                setter.accept(val);
                config.detachFromPreset();
                presetButton.setMessage(Component.literal("Preset: " + config.edgePreset));
            }
        });
    }

    private void rebuild() {
        this.rebuildWidgets();
    }

    private static double snap(double value, double step) {
        return Math.round(value / step) * step;
    }

    @SuppressWarnings("MalformedFormatString")
    private static String formatValue(double value, double step) {
        int decimals = Math.max(0, (int) Math.ceil(-Math.log10(step)));
        return String.format("%." + decimals + "f", value);
    }

    @Override
    public void onClose() {
        BetterAutoJumpConfig.reload();
        this.minecraft.gui.setScreen(parent);
    }
}
