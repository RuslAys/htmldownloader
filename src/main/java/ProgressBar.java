import java.util.concurrent.atomic.AtomicInteger;

public class ProgressBar {
    private final AtomicInteger state = new AtomicInteger(0);
    private final int capacity;

    public ProgressBar(int capacity) {
        this.capacity = capacity;
    }

    public void up() {
        int currentState = state.incrementAndGet();
        System.out.printf("%d/%d%%\n", (int) (((float) currentState) / capacity * 100), 100);
    }
}
