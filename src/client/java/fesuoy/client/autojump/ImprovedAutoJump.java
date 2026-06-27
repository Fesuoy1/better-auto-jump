package fesuoy.client.autojump;

import fesuoy.config.BetterAutoJumpConfig;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.shapes.VoxelShape;

public class ImprovedAutoJump {

    private ImprovedAutoJump() { }

    public static boolean autojumpPlayer(LocalPlayer player, float dx, float dz) {
        BetterAutoJumpConfig cfg = BetterAutoJumpConfig.getInstance();

        if (!player.input.hasForwardImpulse()) return false;

        if (dx * dx + dz * dz < 0.001F) {
            float speed = player.getSpeed();
            Vec2 move = player.input.getMoveVector();
            float f = Mth.sin(player.getYRot() * Mth.DEG_TO_RAD);
            float g = Mth.cos(player.getYRot() * Mth.DEG_TO_RAD);
            dx = move.x * speed * g - move.y * speed * f;
            dz = move.y * speed * g + move.x * speed * f;
        }

        float jumpHeight = (float) cfg.obstacleJumpHeight;
        if (player.hasEffect(MobEffects.JUMP_BOOST)) {
            //noinspection DataFlowIssue
            jumpHeight += (float)(player.getEffect(MobEffects.JUMP_BOOST).getAmplifier() + 1) * 0.75F;
        }
        Level world = player.level();
        double value = Math.sqrt((dx * dx) + (dz * dz));
        double bpt = Mth.clamp(value, 0.001, cfg.obstacleBptClampMax);
        if (bpt < cfg.obstacleBptSlowThreshold) bpt *= cfg.obstacleBptSlowMultiplier;
        AABB currentBox = player.getBoundingBox();

        double dirX, dirZ, moveAngle;
        if (value > 0.001) {
            dirX = dx / value;
            dirZ = dz / value;
            moveAngle = calcAngle(player.getX(), player.getZ(), player.getX() + dirX, player.getZ() + dirZ);
        } else {
            float yawRad = -player.getViewYRot(0) * (float)(Math.PI / 180);
            dirX = Mth.sin(yawRad);
            dirZ = Mth.cos(yawRad);
            moveAngle = mcDeg2NormalDeg(-player.getViewYRot(0));
        }

        double predictionX = dirX * bpt * cfg.obstaclePredictionMult;
        double predictionZ = dirZ * bpt * cfg.obstaclePredictionMult;
        AABB predictionBox = currentBox.move(predictionX, 0, predictionZ);
        int minX = Mth.floor(predictionBox.minX);
        int minY = Mth.floor(predictionBox.minY);
        int minZ = Mth.floor(predictionBox.minZ);
        int maxX = Mth.floor(predictionBox.maxX);
        int maxY = Mth.floor(predictionBox.maxY);
        int maxZ = Mth.floor(predictionBox.maxZ);

        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        float playerHeight = player.getBbHeight();

        for (int i = minX; i <= maxX; i++) {
            for (int j = minY; j <= maxY; j++) {
                for (int k = minZ; k <= maxZ; k++) {
                    pos.set(i, j, k);
                    VoxelShape jumpTargetShape = world.getBlockState(pos).getCollisionShape(world, pos).move(i, j, k);
                    if (jumpTargetShape.isEmpty()) continue;
                    double ydiff = getCollisionY(angleToDirection(moveAngle).getOpposite(), jumpTargetShape) - player.getY();
                    if (ydiff > player.maxUpStep() + 0.001 && ydiff < jumpHeight) {
                        if (!hasHeadSpace(player, currentBox, jumpHeight, playerHeight, pos)) continue;
                        double playerToBlockAngle = calcAngle(player.getX(), player.getZ(), i + 0.5, k + 0.5);
                        if (Math.abs(angleDiff(playerToBlockAngle, moveAngle)) < cfg.obstacleAngleThreshold) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    public static boolean hasHeadSpace(LocalPlayer player, AABB playerBox, float jumpHeight, float playerHeight, BlockPos target) {
        int minX = Mth.floor(Math.min(playerBox.minX, target.getX()));
        int minY = Mth.floor(player.getY() + jumpHeight);
        int minZ = Mth.floor(Math.min(playerBox.minZ, target.getZ()));
        int maxX = Mth.floor(Math.max(playerBox.maxX, target.getX()));
        int maxY = Mth.floor(player.getY() + playerHeight + jumpHeight);
        int maxZ = Mth.floor(Math.max(playerBox.maxZ, target.getZ()));

        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

        for (int i = minX; i <= maxX; i++) {
            for (int j = minY; j <= maxY; j++) {
                for (int k = minZ; k <= maxZ; k++) {
                    pos.set(i, j, k);
                    VoxelShape blockingShape = player.level().getBlockState(pos).getCollisionShape(player.level(), pos).move(i, j, k);
                    if (blockingShape.min(Axis.Y) - player.getY() < jumpHeight + playerHeight) return false;
                }
            }
        }

        return true;
    }

    public static double getCollisionY(Direction side, VoxelShape shape) {
        boolean positiveDirection = side.getStepX() + side.getStepZ() > 0;
        double maxDir = Double.NaN;
        double maxY = Double.NaN;
        for (AABB box : shape.toAabbs()) {
            if (box.maxY > maxY || Double.isNaN(maxDir)) {
                if (positiveDirection) {
                    if (Double.isNaN(maxDir) || box.max(side.getAxis()) >= maxDir) {
                        maxDir = box.max(side.getAxis());
                        maxY = box.maxY;
                    }
                } else {
                    if (Double.isNaN(maxDir) || box.min(side.getAxis()) <= maxDir) {
                        maxDir = box.min(side.getAxis());
                        maxY = box.maxY;
                    }
                }
            }
        }
        return maxY;
    }

    public static Direction angleToDirection(double deg) {
        if (deg > 0 && deg < 45) {
            return Direction.NORTH;
        } else if (deg >= 45 && deg < 135) {
            return Direction.EAST;
        } else if (deg >= 135 && deg < 225) {
            return Direction.SOUTH;
        } else if (deg >= 225 && deg < 315) {
            return Direction.WEST;
        } else {
            return Direction.NORTH;
        }
    }

    public static double mcDeg2NormalDeg(double a) {
        a += 180;
        while (a < 0) a += 360;
        while (a > 360) a -= 360;
        return a;
    }

    public static double calcAngle(double x, double y, double x1, double y1) {
        return Mth.atan2(x - x1, y1 - y) * 180 / Math.PI + 180;
    }

    public static double angleDiff(double a, double b) {
        double difference = a - b;
        while (difference < -180) difference += 360;
        while (difference > 180) difference -= 360;
        return difference;
    }
}
