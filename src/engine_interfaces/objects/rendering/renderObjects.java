package engine_interfaces.objects.rendering;

import engine.Resources;
import engine.World;
import engine_interfaces.objects.CameraView;

public record renderObjects(World world, Resources resources, CameraView camera) {
}