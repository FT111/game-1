package engine_interfaces.objects.scene;

import java.util.List;

/// Scene Graph Tree
public class SceneNode<T> {
    public T objectId;
    public SceneNode<T> parent;
    public List<SceneNode<T>> children;

    public SceneNode(T objectId, SceneNode<T> parent) {
        this.objectId = objectId;
        this.parent = parent;
    }

    public SceneNode(T objectId) {
        this.objectId = objectId;
    }

    public void attach(SceneNode<T> node) {
        if (node.parent != null) { node.parent.children.remove(node); }
        node.parent = this;
        this.children.add(node);
    }

    public void detach(SceneNode<T> node) {
        if (node.parent == this) {
            node.parent = null;
            this.children.remove(node);
        }
    }

    // DFS search for node with given id
    public SceneNode<T> findNode(T objectId) {
        if (this.objectId.equals(objectId)) return this;
        for (SceneNode<T> child : children) {
            SceneNode<T> result = child.findNode(objectId);
            if (result != null) return result;
        }
        return null;
    }
}
