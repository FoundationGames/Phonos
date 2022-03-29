package io.github.foundationgames.phonos.util;

import net.minecraft.block.Block;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;

import java.util.ArrayList;
import java.util.List;

public class ShapeDefinition {
    private final List<Box> boxes;

    public ShapeDefinition() {
        this.boxes = new ArrayList<>();
    }

    private ShapeDefinition(List<Box> boxes) {
        this.boxes = boxes;
    }

    public ShapeDefinition cuboid(double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
        this.boxes.add(new Box(minX, minY, minZ, maxX, maxY, maxZ));

        return this;
    }

    public ShapeDefinition copy() {
        return new ShapeDefinition(new ArrayList<>(this.boxes));
    }

    public VoxelShape toShape(Direction direction) {
        var shapes = new VoxelShape[boxes.size()];

        for (int i = 0; i < boxes.size(); i++) {
            var box = boxes.get(i);

            shapes[i] = switch (direction) {
                case SOUTH -> Block.createCuboidShape(16 - box.maxX, box.minY, 16 - box.maxZ, 16 - box.minX, box.maxY, 16 - box.minZ);
                case WEST -> Block.createCuboidShape(box.minZ, box.minY, box.minX, box.maxZ, box.maxY, box.maxX);
                case EAST -> Block.createCuboidShape(16 - box.maxZ, box.minY, 16 - box.maxX, 16 - box.minZ, box.maxY, 16 - box.minX);
                default -> Block.createCuboidShape(box.minX, box.minY, box.minZ, box.maxX, box.maxY, box.maxZ);
            };
        }

        return VoxelShapes.union(VoxelShapes.empty(), shapes);
    }
}
