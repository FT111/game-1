package engine_interfaces.objects;

/// Light ID wrapper for type safety and to avoid confusion with other IDs
public record EntityID(long Id) {
    @Override
    public String toString() {
        return "entity-" + Id;
    }
}
