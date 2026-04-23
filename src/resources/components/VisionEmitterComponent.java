package resources.components;

import engine_interfaces.objects.Component;
import engine_interfaces.objects.EntityID;
import engine_interfaces.objects.LayerID;
import engine_interfaces.objects.Point;

import java.util.HashSet;
import java.util.List;

public class VisionEmitterComponent extends Component {
    public int visionRange;
    public int fieldOfViewAngle;
    public int visionTickFrequency;

    public LayerID visionLayer;
    public HashSet<EntityID> currentlyVisibleEntities = new HashSet<>();

    public VisionEmitterComponent(int visionRange, int fieldOfViewAngle, int visionTickFrequency, LayerID visionLayer) {
        this.visionRange = visionRange;
        this.fieldOfViewAngle = fieldOfViewAngle;
        this.visionTickFrequency = visionTickFrequency;

        this.visionLayer = visionLayer;
    }
}
