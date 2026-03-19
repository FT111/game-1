package engine_interfaces.objects;

public record LayerID(String Id) {
    @Override
    public String toString() {
        return "layer-" + Id;
    }
}