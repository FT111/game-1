package resources.components;

import engine_interfaces.objects.Component;
import engine_interfaces.objects.EntityID;
import engine_interfaces.objects.LayerID;
import engine_interfaces.objects.Point;
import engine_interfaces.objects.rendering.Colour;

import java.util.HashSet;
import java.util.List;

public class VisionEmitterComponent extends Component {
    public int visionRange;
    public int fieldOfViewAngle;
    public int visionTickFrequency;

    public LayerID visionLayer;
    public HashSet<EntityID> currentlyVisibleEntities = new HashSet<>();

    public Colour visionColour;

    public VisionEmitterComponent(int visionRange, int fieldOfViewAngle, int visionTickFrequency, LayerID visionLayer) {
        this(visionRange, fieldOfViewAngle, visionTickFrequency, visionLayer, null);
    }

    public VisionEmitterComponent(int visionRange, int fieldOfViewAngle, int visionTickFrequency, LayerID visionLayer, Colour visionColour) {
        this.visionRange = visionRange;
        this.fieldOfViewAngle = fieldOfViewAngle;
        this.visionTickFrequency = visionTickFrequency;

        this.visionLayer = visionLayer;
        this.visionColour = visionColour;
    }
}
