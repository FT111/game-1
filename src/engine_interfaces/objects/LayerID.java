package engine_interfaces.objects;

public record LayerID(long Id) {
    @Override
    public String toString() {
        return "layer-" + Id;
    }
}