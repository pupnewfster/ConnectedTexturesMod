package team.chisel.ctm.client.util;

import static net.minecraft.core.Direction.DOWN;
import static net.minecraft.core.Direction.EAST;
import static net.minecraft.core.Direction.NORTH;
import static net.minecraft.core.Direction.SOUTH;
import static net.minecraft.core.Direction.UP;
import static net.minecraft.core.Direction.WEST;

import lombok.experimental.UtilityClass;
import net.minecraft.core.Direction;

/**
 * A bunch of methods that got stripped out of Direction in 1.15
 * 
 * @author Mojang
 */
@UtilityClass
public class DirectionHelper {

	public static Direction rotateAround(Direction dir, Direction.Axis axis) {
        return switch (axis) {
            case X -> {
                if (dir != WEST && dir != EAST) {
                    yield rotateX(dir);
                }
                yield dir;
            }
            case Y -> {
                if (dir != UP && dir != DOWN) {
                    yield dir.getClockWise();
                }
                yield dir;
            }
            case Z -> {
                if (dir != NORTH && dir != SOUTH) {
                    yield rotateZ(dir);
                }
                yield dir;
            }
        };
	}

	public static Direction rotateX(Direction dir) {
        return switch (dir) {
            case NORTH -> DOWN;
            case SOUTH -> UP;
            case UP -> NORTH;
            case DOWN -> SOUTH;
			default -> throw new IllegalStateException("Unable to get X-rotated facing of " + dir);
        };
	}

	public static Direction rotateZ(Direction dir) {
        return switch (dir) {
            case EAST -> DOWN;
            case WEST -> UP;
            case UP -> EAST;
            case DOWN -> WEST;
			default -> throw new IllegalStateException("Unable to get Z-rotated facing of " + dir);
        };
	}
}