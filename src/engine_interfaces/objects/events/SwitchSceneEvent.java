package engine_interfaces.objects.events;

import engine_interfaces.objects.Event;

public class SwitchSceneEvent extends Event {
    public String sceneName;

    public SwitchSceneEvent(String sceneName) {
        this.sceneName = sceneName;
    }
}
