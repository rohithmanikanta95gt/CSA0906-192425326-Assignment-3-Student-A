
import java.util.concurrent.CountDownLatch;

public class SeatBooking {

    private final boolean[] booked = new boolean[10];
    private int bookingsMade = 0;

    private final CountDownLatch ready =
        new CountDownLatch(100);

    private final CountDownLatch start =
        new CountDownLatch(1);

    // Without synchronization
    public void bookUnsafe(int seat) {
        try {
            boolean available = !booked[seat];

            ready.countDown();
            start.await();

            if (available) {
                booked[seat] = true;
                bookingsMade++;
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    // With synchronization
    public synchronized void bookSafe(int seat) {
        if (!booked[seat]) {
            booked[seat] = true;
            bookingsMade++;
        }
    }

    public static void main(String[] args)
            throws InterruptedException {

        SeatBooking unsafe = new SeatBooking();

        Thread[] users = new Thread[100];

        for (int i = 0; i < 100; i++) {
            final int seat = i % 10;

            users[i] = new Thread(() ->
                unsafe.bookUnsafe(seat));

            users[i].start();
        }

        unsafe.ready.await();
        unsafe.start.countDown();

        for (Thread t : users) {
            t.join();
        }

        System.out.println("Without synchronization:");
        System.out.println("Seats sold: " +
                           unsafe.bookingsMade);

        SeatBooking safe = new SeatBooking();

        for (int i = 0; i < 100; i++) {
            final int seat = i % 10;

            users[i] = new Thread(() ->
                safe.bookSafe(seat));

            users[i].start();
        }

        for (Thread t : users) {
            t.join();
        }

        System.out.println("\nWith synchronization:");
        System.out.println("Seats sold: " +
                           safe.bookingsMade);
    }
}
