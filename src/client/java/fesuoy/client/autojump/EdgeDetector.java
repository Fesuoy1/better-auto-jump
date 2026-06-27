package fesuoy.client.autojump;

import fesuoy.config.BetterAutoJumpConfig;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class EdgeDetector {

    private static final double VEL_EPSILON = 0.001;

    public static boolean detect(LocalPlayer player, Level level) {
        BetterAutoJumpConfig cfg = BetterAutoJumpConfig.getInstance();

        if (!player.onGround()) return false;
        if (!player.input.hasForwardImpulse()) return false;

        double velX;
        double velZ;
        Vec3 vel = player.getDeltaMovement();
        velX = vel.x;
        velZ = vel.z;

        if (velX * velX + velZ * velZ < VEL_EPSILON) {
            float speed = player.getSpeed();
            Vec2 move = player.input.getMoveVector();
            float f = Mth.sin(player.getYRot() * Mth.DEG_TO_RAD);
            float g = Mth.cos(player.getYRot() * Mth.DEG_TO_RAD);
            velX = move.x * speed * g - move.y * speed * f;
            velZ = move.y * speed * g + move.x * speed * f;
        }

        if (velX * velX + velZ * velZ < VEL_EPSILON) return false;

        double px = player.getX();
        double py = player.getY();
        double pz = player.getZ();

        double len = Math.sqrt(velX * velX + velZ * velZ);
        if (len < cfg.minVelocityThreshold) return false;

        double dx = velX / len;
        double dz = velZ / len;

        boolean sprinting = player.isSprinting();
        double maxDist = sprinting ? cfg.edgeScanMaxDistSprint : cfg.edgeScanMaxDistWalk;
        double radius = cfg.edgeScanRadius;
        double vOffset = cfg.edgeVerticalOffset;
        double solidMin = cfg.solidAtFeetMin;
        double solidMax = cfg.solidAtFeetMax;

        for (double dist = cfg.edgeScanStart; dist <= maxDist; dist += cfg.edgeScanStep) {
            double nx = px + dx * dist;
            double nz = pz + dz * dist;

            if (!hasGroundBelow(level, player, nx, py, nz, radius, vOffset) && !hasSolidAtFeet(level, nx, py, nz, solidMin, solidMax)) {
                return true;
            }
        }

        return false;
    }

    private static boolean hasGroundBelow(Level level, LocalPlayer player, double x, double y, double z, double radius, double vOffset) {
        AABB scanBox = new AABB(x - radius, y - vOffset, z - radius, x + radius, y, z + radius);
        for (VoxelShape shape : level.getBlockCollisions(player, scanBox)) {
            if (!shape.isEmpty()) return true;
        }
        return false;
    }

    private static boolean hasSolidAtFeet(Level level, double x, double y, double z, double min, double max) {
        BlockPos pos = BlockPos.containing(x, y, z);
        BlockState state = level.getBlockState(pos);
        VoxelShape shape = state.getCollisionShape(level, pos, CollisionContext.empty());
        if (shape.isEmpty()) return false;
        double shapeTop = shape.bounds().maxY + pos.getY();
        return shapeTop > y + min && shapeTop <= y + max;
    }
}
