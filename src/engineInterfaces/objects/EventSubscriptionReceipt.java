package engineInterfaces.objects;

public class EventSubscriptionReceipt {
    public Runnable cancel;

    public EventSubscriptionReceipt(Runnable cancel) {
        this.cancel = cancel;
    }
}
