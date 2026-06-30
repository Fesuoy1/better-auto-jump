package fesuoy.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class BetterAutoJumpConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve("better-auto-jump.json");
    private static BetterAutoJumpConfig instance;

    public boolean enabled = true;
    public boolean obstacleJumpEnabled = true;
    public boolean edgeJumpEnabled = false;
    public int varianceTicks = 1;
    public int cooldownTicks = 1;
    public boolean obstacleVarianceEnabled = false;
    public boolean obstacleCooldownEnabled = false;

    // Edge detection parameters
    public double edgeScanMaxDistSprint = 2.0;
    public double edgeScanMaxDistWalk = 1.5;
    public double edgeScanStep = 0.35;
    public double edgeScanStart = 0.3;
    public double edgeScanRadius = 0.3;
    public double edgeVerticalOffset = 0.05;
    public double minVelocityThreshold = 0.1;
    public double solidAtFeetMin = 0.001;
    public double solidAtFeetMax = 0.6;
    // Obstacle detection parameters
    public double obstacleJumpHeight = 1.2;
    public double obstaclePredictionMult = 6.0;
    public double obstacleBptClampMax = 0.8;
    public double obstacleBptSlowThreshold = 0.2;
    public double obstacleBptSlowMultiplier = 0.7;
    public double obstacleAngleThreshold = 30.0;

    // Custom saved presets
    public List<CustomPreset> customPresets = new ArrayList<>();

    // Current preset name ("Default", "Precision", "Custom", or a saved custom name)
    public String edgePreset = "Default";

    public static class CustomPreset {
        public String name;
        public double edgeScanMaxDistSprint;
        public double edgeScanMaxDistWalk;
        public double edgeScanStep;
        public double edgeScanStart;
        public double edgeScanRadius;
        public double edgeVerticalOffset;
        public double minVelocityThreshold;
        public double solidAtFeetMin;
        public double solidAtFeetMax;
        public double obstacleJumpHeight;
        public double obstaclePredictionMult;
        public double obstacleBptClampMax;
        public double obstacleBptSlowThreshold;
        public double obstacleBptSlowMultiplier;
        public double obstacleAngleThreshold;

        public CustomPreset() {}

        public CustomPreset(String name, BetterAutoJumpConfig cfg) {
            this.name = name;
            this.edgeScanMaxDistSprint = cfg.edgeScanMaxDistSprint;
            this.edgeScanMaxDistWalk = cfg.edgeScanMaxDistWalk;
            this.edgeScanStep = cfg.edgeScanStep;
            this.edgeScanStart = cfg.edgeScanStart;
            this.edgeScanRadius = cfg.edgeScanRadius;
            this.edgeVerticalOffset = cfg.edgeVerticalOffset;
            this.minVelocityThreshold = cfg.minVelocityThreshold;
            this.solidAtFeetMin = cfg.solidAtFeetMin;
            this.solidAtFeetMax = cfg.solidAtFeetMax;
            this.obstacleJumpHeight = cfg.obstacleJumpHeight;
            this.obstaclePredictionMult = cfg.obstaclePredictionMult;
            this.obstacleBptClampMax = cfg.obstacleBptClampMax;
            this.obstacleBptSlowThreshold = cfg.obstacleBptSlowThreshold;
            this.obstacleBptSlowMultiplier = cfg.obstacleBptSlowMultiplier;
            this.obstacleAngleThreshold = cfg.obstacleAngleThreshold;
        }

        public void applyTo(BetterAutoJumpConfig cfg) {
            cfg.edgeScanMaxDistSprint = this.edgeScanMaxDistSprint;
            cfg.edgeScanMaxDistWalk = this.edgeScanMaxDistWalk;
            cfg.edgeScanStep = this.edgeScanStep;
            cfg.edgeScanStart = this.edgeScanStart;
            cfg.edgeScanRadius = this.edgeScanRadius;
            cfg.edgeVerticalOffset = this.edgeVerticalOffset;
            cfg.minVelocityThreshold = this.minVelocityThreshold;
            cfg.solidAtFeetMin = this.solidAtFeetMin;
            cfg.solidAtFeetMax = this.solidAtFeetMax;
            cfg.obstacleJumpHeight = this.obstacleJumpHeight;
            cfg.obstaclePredictionMult = this.obstaclePredictionMult;
            cfg.obstacleBptClampMax = this.obstacleBptClampMax;
            cfg.obstacleBptSlowThreshold = this.obstacleBptSlowThreshold;
            cfg.obstacleBptSlowMultiplier = this.obstacleBptSlowMultiplier;
            cfg.obstacleAngleThreshold = this.obstacleAngleThreshold;
        }
    }

    public List<String> allPresetNames() {
        List<String> names = new ArrayList<>();
        for (EdgePreset p : EdgePreset.values()) {
            names.add(p.displayName);
        }
        for (CustomPreset p : customPresets) {
            names.add(p.name);
        }
        return names;
    }

    public void applyPresetByName(String name) {
        for (EdgePreset p : EdgePreset.values()) {
            if (p.displayName.equals(name)) {
                applyEdgePreset(p);
                return;
            }
        }
        for (CustomPreset p : customPresets) {
            if (p.name.equals(name)) {
                p.applyTo(this);
                edgePreset = name;
                return;
            }
        }
    }

    public enum EdgePreset {
        DEFAULT("Default", 2.0, 1.5, 0.35, 0.3, 0.3, 0.05, 0.1, 0.001, 0.6),
        PRECISION("Precision", 1.2, 1.0, 0.2, 0.2, 0.25, 0.03, 0.08, 0.001, 0.5),
        LONG_RANGE("Long Range", 3.5, 2.5, 0.4, 0.3, 0.35, 0.05, 0.1, 0.001, 0.6),
        AGGRESSIVE("Aggressive", 2.5, 2.0, 0.5, 0.3, 0.4, 0.08, 0.1, 0.001, 0.6);

        public final String displayName;
        public final double scanMaxDistSprint;
        public final double scanMaxDistWalk;
        public final double scanStep;
        public final double scanStart;
        public final double scanRadius;
        public final double verticalOffset;
        public final double minVelocity;
        public final double solidFeetMin;
        public final double solidFeetMax;

        EdgePreset(String displayName, double scanMaxDistSprint, double scanMaxDistWalk,
                   double scanStep, double scanStart, double scanRadius,
                   double verticalOffset, double minVelocity,
                   double solidFeetMin, double solidFeetMax) {
            this.displayName = displayName;
            this.scanMaxDistSprint = scanMaxDistSprint;
            this.scanMaxDistWalk = scanMaxDistWalk;
            this.scanStep = scanStep;
            this.scanStart = scanStart;
            this.scanRadius = scanRadius;
            this.verticalOffset = verticalOffset;
            this.minVelocity = minVelocity;
            this.solidFeetMin = solidFeetMin;
            this.solidFeetMax = solidFeetMax;
        }
    }

    public void applyEdgePreset(EdgePreset preset) {
        edgeScanMaxDistSprint = preset.scanMaxDistSprint;
        edgeScanMaxDistWalk = preset.scanMaxDistWalk;
        edgeScanStep = preset.scanStep;
        edgeScanStart = preset.scanStart;
        edgeScanRadius = preset.scanRadius;
        edgeVerticalOffset = preset.verticalOffset;
        minVelocityThreshold = preset.minVelocity;
        solidAtFeetMin = preset.solidFeetMin;
        solidAtFeetMax = preset.solidFeetMax;
        edgePreset = preset.displayName;
    }

    public void detachFromPreset() {
        this.edgePreset = "Custom";
    }

    public static BetterAutoJumpConfig getInstance() {
        if (instance == null) {
            instance = load();
        }
        return instance;
    }

    public static void reload() {
        instance = load();
    }

    private static BetterAutoJumpConfig load() {
        if (Files.exists(CONFIG_PATH)) {
            try {
                String json = Files.readString(CONFIG_PATH);
                return GSON.fromJson(json, BetterAutoJumpConfig.class);
            } catch (IOException e) {
                BetterAutoJumpConfig cfg = new BetterAutoJumpConfig();
                cfg.save();
                return cfg;
            }
        }
        BetterAutoJumpConfig cfg = new BetterAutoJumpConfig();
        cfg.save();
        return cfg;
    }

    public void save() {
        try {
            Files.writeString(CONFIG_PATH, GSON.toJson(this));
        } catch (IOException e) {
            // silently fail
        }
    }
}
