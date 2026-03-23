package resources;

import engine.EventBus;
import engine.World;
import engine_interfaces.objects.EntityID;
import engine_interfaces.objects.Point;
import engine_interfaces.objects.System;
import engine_interfaces.objects.events.MovementEvent;
import resources.components.ChunkableComponent;

import java.util.HashMap;
import java.util.HashSet;

// splits the world into spatial chunks and tracks which entities are in which chunk.
// makes querying nearby entities more efficient.
public class ChunkSystem extends System {
    private final int chunkSize;
    private HashMap<Point, HashSet<EntityID>> chunkEntities = new HashMap<>();

    public ChunkSystem(EventBus Bus, World world, int chunkSize, HashMap<Point, HashSet<EntityID>> chunkMap) {
        this.chunkSize = chunkSize;
        this.chunkEntities = chunkMap;

        Bus.subscribe(MovementEvent.class, "ChunkSystem", event -> {
            var movementEvent = (MovementEvent) event;
            var chunkComponent = (ChunkableComponent) world.Entities.get(movementEvent.entityID).get(ChunkableComponent.class);

            if (chunkComponent == null) {
                return;
            }

            chunkComponent.chunk = getChunkForPosition(movementEvent.newOrigin);

            var previousChunk = getChunkForPosition(movementEvent.previousOrigin);
            var newChunk = getChunkForPosition(movementEvent.newOrigin);

            if (previousChunk != newChunk) {
                // Remove from previous chunk
                chunkEntities.computeIfPresent(previousChunk, (chunk, entities) -> {
                    entities.remove(movementEvent.entityID);
                    return entities.isEmpty() ? null : entities;
                });

                // Add to new chunk
                chunkEntities.computeIfAbsent(newChunk, chunk -> new HashSet<>()).add(movementEvent.entityID);
            }
        });
    }

    public Point getChunkForPosition(Point position) {
        int chunkX = (int) Math.floor((double) position.x() / chunkSize);
        int chunkY = (int) Math.floor((double) position.y() / chunkSize);
        return new Point(chunkX, chunkY);
    }


    @Override
    public void update(World world, int tickCount) {
    }
}
