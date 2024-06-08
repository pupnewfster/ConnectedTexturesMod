package team.chisel.ctm.client.util;

import javax.annotation.ParametersAreNonnullByDefault;

import lombok.experimental.UtilityClass;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
@UtilityClass
public class FaceOffset {
    public static BlockPos getBlockPosOffsetFromFaceOffset(Direction facing, int xOffset, int yOffset) {
        return switch (facing) {
            // UP
            default -> new BlockPos(xOffset, 0, -yOffset);
            case DOWN -> new BlockPos(xOffset, 0, yOffset);
            case NORTH -> new BlockPos(-xOffset, yOffset, 0);
            case SOUTH -> new BlockPos(xOffset, yOffset, 0);
            case WEST -> new BlockPos(0, yOffset, xOffset);
            case EAST -> new BlockPos(0, yOffset, -xOffset);
        };
    }
}