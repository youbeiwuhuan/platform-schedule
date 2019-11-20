package com.courage.platform.schedule.server.service.timer;

import io.netty.util.*;
import io.netty.util.internal.PlatformDependent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.HashSet;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;

public class ScheduleHashedWheelTimer implements ScheduleTimer {

    private final static Logger logger = LoggerFactory.getLogger(ScheduleHashedWheelTimer.class);

    private static final ResourceLeakDetector<ScheduleHashedWheelTimer> leakDetector = ResourceLeakDetectorFactory.instance().newResourceLeakDetector(ScheduleHashedWheelTimer.class, 1, Runtime.getRuntime().availableProcessors() * 4L);

    private static final AtomicIntegerFieldUpdater<ScheduleHashedWheelTimer> WORKER_STATE_UPDATER;

    static {
        AtomicIntegerFieldUpdater<ScheduleHashedWheelTimer> workerStateUpdater = PlatformDependent.newAtomicIntegerFieldUpdater(ScheduleHashedWheelTimer.class, "workerState");
        if (workerStateUpdater == null) {
            workerStateUpdater = AtomicIntegerFieldUpdater.newUpdater(ScheduleHashedWheelTimer.class, "workerState");
        }
        WORKER_STATE_UPDATER = workerStateUpdater;
    }

    private final ResourceLeak leak;
    private final Worker worker = new Worker();
    private final Thread workerThread;

    public static final int WORKER_STATE_INIT = 0;
    public static final int WORKER_STATE_STARTED = 1;
    public static final int WORKER_STATE_SHUTDOWN = 2;
    @SuppressWarnings({"unused", "FieldMayBeFinal", "RedundantFieldInitialization"})
    private volatile int workerState = WORKER_STATE_INIT; // 0 - init, 1 - started, 2 - shut down

    private final long tickDuration;
    private final HashedWheelBucket[] wheel;
    private final int mask;
    private final CountDownLatch startTimeInitialized = new CountDownLatch(1);
    private final Queue<HashedWheelScheduleTimeout> scheduleTimeouts = PlatformDependent.newMpscQueue();
    private final Queue<HashedWheelScheduleTimeout> cancelledScheduleTimeouts = PlatformDependent.newMpscQueue();

    private volatile long startTime;

    /**
     * Creates a new timer with the default thread factory
     * ({@link Executors#defaultThreadFactory()}), default tick duration, and
     * default number of ticks per wheel.
     */
    public ScheduleHashedWheelTimer() {
        this(Executors.defaultThreadFactory());
    }

    /**
     * Creates a new timer with the default thread factory
     * ({@link Executors#defaultThreadFactory()}) and default number of ticks
     * per wheel.
     *
     * @param tickDuration the duration between tick
     * @param unit         the time unit of the {@code tickDuration}
     * @throws NullPointerException     if {@code unit} is {@code null}
     * @throws IllegalArgumentException if {@code tickDuration} is <= 0
     */
    public ScheduleHashedWheelTimer(long tickDuration, TimeUnit unit) {
        this(Executors.defaultThreadFactory(), tickDuration, unit);
    }

    /**
     * Creates a new timer with the default thread factory
     * ({@link Executors#defaultThreadFactory()}).
     *
     * @param tickDuration  the duration between tick
     * @param unit          the time unit of the {@code tickDuration}
     * @param ticksPerWheel the size of the wheel
     * @throws NullPointerException     if {@code unit} is {@code null}
     * @throws IllegalArgumentException if either of {@code tickDuration} and {@code ticksPerWheel} is <= 0
     */
    public ScheduleHashedWheelTimer(long tickDuration, TimeUnit unit, int ticksPerWheel) {
        this(Executors.defaultThreadFactory(), tickDuration, unit, ticksPerWheel);
    }

    /**
     * Creates a new timer with the default tick duration and default number of
     * ticks per wheel.
     *
     * @param threadFactory a {@link ThreadFactory} that creates a
     *                      background {@link Thread} which is dedicated to
     *                      {@link ScheduleTimerTask} execution.
     * @throws NullPointerException if {@code threadFactory} is {@code null}
     */
    public ScheduleHashedWheelTimer(ThreadFactory threadFactory) {
        this(threadFactory, 100, TimeUnit.MILLISECONDS);
    }

    /**
     * Creates a new timer with the default number of ticks per wheel.
     *
     * @param threadFactory a {@link ThreadFactory} that creates a
     *                      background {@link Thread} which is dedicated to
     *                      {@link ScheduleTimerTask} execution.
     * @param tickDuration  the duration between tick
     * @param unit          the time unit of the {@code tickDuration}
     * @throws NullPointerException     if either of {@code threadFactory} and {@code unit} is {@code null}
     * @throws IllegalArgumentException if {@code tickDuration} is <= 0
     */
    public ScheduleHashedWheelTimer(ThreadFactory threadFactory, long tickDuration, TimeUnit unit) {
        this(threadFactory, tickDuration, unit, 512);
    }

    /**
     * Creates a new timer.
     *
     * @param threadFactory a {@link ThreadFactory} that creates a
     *                      background {@link Thread} which is dedicated to
     *                      {@link ScheduleTimerTask} execution.
     * @param tickDuration  the duration between tick
     * @param unit          the time unit of the {@code tickDuration}
     * @param ticksPerWheel the size of the wheel
     * @throws NullPointerException     if either of {@code threadFactory} and {@code unit} is {@code null}
     * @throws IllegalArgumentException if either of {@code tickDuration} and {@code ticksPerWheel} is <= 0
     */
    public ScheduleHashedWheelTimer(ThreadFactory threadFactory, long tickDuration, TimeUnit unit, int ticksPerWheel) {
        this(threadFactory, tickDuration, unit, ticksPerWheel, true);
    }

    /**
     * Creates a new timer.
     *
     * @param threadFactory a {@link ThreadFactory} that creates a
     *                      background {@link Thread} which is dedicated to
     *                      {@link ScheduleTimerTask} execution.
     * @param tickDuration  the duration between tick
     * @param unit          the time unit of the {@code tickDuration}
     * @param ticksPerWheel the size of the wheel
     * @param leakDetection {@code true} if leak detection should be enabled always, if false it will only be enabled
     *                      if the worker thread is not a daemon thread.
     * @throws NullPointerException     if either of {@code threadFactory} and {@code unit} is {@code null}
     * @throws IllegalArgumentException if either of {@code tickDuration} and {@code ticksPerWheel} is &lt;= 0
     */
    public ScheduleHashedWheelTimer(ThreadFactory threadFactory, long tickDuration, TimeUnit unit, int ticksPerWheel, boolean leakDetection) {

        if (threadFactory == null) {
            throw new NullPointerException("threadFactory");
        }
        if (unit == null) {
            throw new NullPointerException("unit");
        }
        if (tickDuration <= 0) {
            throw new IllegalArgumentException("tickDuration must be greater than 0: " + tickDuration);
        }
        if (ticksPerWheel <= 0) {
            throw new IllegalArgumentException("ticksPerWheel must be greater than 0: " + ticksPerWheel);
        }

        // Normalize ticksPerWheel to power of two and initialize the wheel.
        wheel = createWheel(ticksPerWheel);
        mask = wheel.length - 1;

        // Convert tickDuration to nanos.
        this.tickDuration = unit.toNanos(tickDuration);

        // Prevent overflow.
        if (this.tickDuration >= Long.MAX_VALUE / wheel.length) {
            throw new IllegalArgumentException(String.format("tickDuration: %d (expected: 0 < tickDuration in nanos < %d", tickDuration, Long.MAX_VALUE / wheel.length));
        }
        workerThread = threadFactory.newThread(worker);

        leak = leakDetection || !workerThread.isDaemon() ? leakDetector.open(this) : null;
    }

    private static HashedWheelBucket[] createWheel(int ticksPerWheel) {
        if (ticksPerWheel <= 0) {
            throw new IllegalArgumentException("ticksPerWheel must be greater than 0: " + ticksPerWheel);
        }
        if (ticksPerWheel > 1073741824) {
            throw new IllegalArgumentException("ticksPerWheel may not be greater than 2^30: " + ticksPerWheel);
        }

        ticksPerWheel = normalizeTicksPerWheel(ticksPerWheel);
        HashedWheelBucket[] wheel = new HashedWheelBucket[ticksPerWheel];
        for (int i = 0; i < wheel.length; i++) {
            wheel[i] = new HashedWheelBucket();
        }
        return wheel;
    }

    private static int normalizeTicksPerWheel(int ticksPerWheel) {
        int normalizedTicksPerWheel = 1;
        while (normalizedTicksPerWheel < ticksPerWheel) {
            normalizedTicksPerWheel <<= 1;
        }
        return normalizedTicksPerWheel;
    }

    /**
     * Starts the background thread explicitly.  The background thread will
     * start automatically on demand even if you did not call this method.
     *
     * @throws IllegalStateException if this timer has been
     *                               {@linkplain #stop() stopped} already
     */
    public void start() {
        switch (WORKER_STATE_UPDATER.get(this)) {
            case WORKER_STATE_INIT:
                if (WORKER_STATE_UPDATER.compareAndSet(this, WORKER_STATE_INIT, WORKER_STATE_STARTED)) {
                    workerThread.start();
                }
                break;
            case WORKER_STATE_STARTED:
                break;
            case WORKER_STATE_SHUTDOWN:
                throw new IllegalStateException("cannot be started once stopped");
            default:
                throw new Error("Invalid WorkerState");
        }

        // Wait until the startTime is initialized by the worker.
        while (startTime == 0) {
            try {
                startTimeInitialized.await();
            } catch (InterruptedException ignore) {
                // Ignore - it will be ready very soon.
            }
        }
    }

    @Override
    public Set<ScheduleTimeout> stop() {
        if (Thread.currentThread() == workerThread) {
            throw new IllegalStateException(ScheduleHashedWheelTimer.class.getSimpleName() + ".stop() cannot be called from " + ScheduleTimerTask.class.getSimpleName());
        }

        if (!WORKER_STATE_UPDATER.compareAndSet(this, WORKER_STATE_STARTED, WORKER_STATE_SHUTDOWN)) {
            // workerState can be 0 or 2 at this moment - let it always be 2.
            WORKER_STATE_UPDATER.set(this, WORKER_STATE_SHUTDOWN);

            if (leak != null) {
                leak.close();
            }

            return Collections.emptySet();
        }

        boolean interrupted = false;
        while (workerThread.isAlive()) {
            workerThread.interrupt();
            try {
                workerThread.join(100);
            } catch (InterruptedException ignored) {
                interrupted = true;
            }
        }

        if (interrupted) {
            Thread.currentThread().interrupt();
        }

        if (leak != null) {
            leak.close();
        }
        return worker.unprocessedScheduleTimeouts();
    }

    @Override
    public ScheduleTimeout newTimeout(ScheduleTimerTask task, long delay, TimeUnit unit) {
        if (task == null) {
            throw new NullPointerException("task");
        }
        if (unit == null) {
            throw new NullPointerException("unit");
        }
        start();

        // Add the ScheduleTimeout to the ScheduleTimeout queue which will be processed on the next tick.
        // During processing all the queued HashedWheelScheduleTimeouts will be added to the correct HashedWheelBucket.
        long deadline = System.nanoTime() + unit.toNanos(delay) - startTime;
        HashedWheelScheduleTimeout ScheduleTimeout = new HashedWheelScheduleTimeout(this, task, deadline);
        scheduleTimeouts.add(ScheduleTimeout);
        return ScheduleTimeout;
    }

    private final class Worker implements Runnable {
        private final Set<ScheduleTimeout> unprocessedScheduleTimeouts = new HashSet<ScheduleTimeout>();

        private long tick;

        @Override
        public void run() {
            // Initialize the startTime.
            startTime = System.nanoTime();
            if (startTime == 0) {
                // We use 0 as an indicator for the uninitialized value here, so make sure it's not 0 when initialized.
                startTime = 1;
            }

            // Notify the other threads waiting for the initialization at start().
            startTimeInitialized.countDown();

            do {
                final long deadline = waitForNextTick();
                if (deadline > 0) {
                    int idx = (int) (tick & mask);
                    processCancelledTasks();
                    HashedWheelBucket bucket = wheel[idx];
                    transferScheduleTimeoutsToBuckets();
                    bucket.expireScheduleTimeouts(deadline);
                    tick++;
                }
            } while (WORKER_STATE_UPDATER.get(ScheduleHashedWheelTimer.this) == WORKER_STATE_STARTED);

            // Fill the unprocessedScheduleTimeouts so we can return them from stop() method.
            for (HashedWheelBucket bucket : wheel) {
                bucket.clearScheduleTimeouts(unprocessedScheduleTimeouts);
            }
            for (; ; ) {
                HashedWheelScheduleTimeout ScheduleTimeout = scheduleTimeouts.poll();
                if (ScheduleTimeout == null) {
                    break;
                }
                if (!ScheduleTimeout.isCancelled()) {
                    unprocessedScheduleTimeouts.add(ScheduleTimeout);
                }
            }
            processCancelledTasks();
        }

        private void transferScheduleTimeoutsToBuckets() {
            // transfer only max. 100000 scheduleTimeouts per tick to prevent a thread to stale the workerThread when it just
            // adds new scheduleTimeouts in a loop.
            for (int i = 0; i < 100000; i++) {
                HashedWheelScheduleTimeout scheduleTimeout = scheduleTimeouts.poll();
                if (scheduleTimeout == null) {
                    // all processed
                    break;
                }
                if (scheduleTimeout.state() == HashedWheelScheduleTimeout.ST_CANCELLED) {
                    // Was cancelled in the meantime.
                    continue;
                }

                long calculated = scheduleTimeout.deadline / tickDuration;
                scheduleTimeout.remainingRounds = (calculated - tick) / wheel.length;

                final long ticks = Math.max(calculated, tick); // Ensure we don't schedule for past.
                int stopIndex = (int) (ticks & mask);

                HashedWheelBucket bucket = wheel[stopIndex];
                bucket.addScheduleTimeout(scheduleTimeout);
            }
        }

        private void processCancelledTasks() {
            for (; ; ) {
                HashedWheelScheduleTimeout ScheduleTimeout = cancelledScheduleTimeouts.poll();
                if (ScheduleTimeout == null) {
                    // all processed
                    break;
                }
                try {
                    ScheduleTimeout.remove();
                } catch (Throwable t) {
                    if (logger.isWarnEnabled()) {
                        logger.warn("An exception was thrown while process a cancellation task", t);
                    }
                }
            }
        }

        /**
         * calculate goal nanoTime from startTime and current tick number,
         * then wait until that goal has been reached.
         *
         * @return Long.MIN_VALUE if received a shutdown request,
         * current time otherwise (with Long.MIN_VALUE changed by +1)
         */
        private long waitForNextTick() {
            long deadline = tickDuration * (tick + 1);

            for (; ; ) {
                final long currentTime = System.nanoTime() - startTime;
                long sleepTimeMs = (deadline - currentTime + 999999) / 1000000;

                if (sleepTimeMs <= 0) {
                    if (currentTime == Long.MIN_VALUE) {
                        return -Long.MAX_VALUE;
                    } else {
                        return currentTime;
                    }
                }

                // Check if we run on windows, as if thats the case we will need
                // to round the sleepTime as workaround for a bug that only affect
                // the JVM if it runs on windows.
                //
                // See https://github.com/netty/netty/issues/356
                if (PlatformDependent.isWindows()) {
                    sleepTimeMs = sleepTimeMs / 10 * 10;
                }

                try {
                    Thread.sleep(sleepTimeMs);
                } catch (InterruptedException ignored) {
                    if (WORKER_STATE_UPDATER.get(ScheduleHashedWheelTimer.this) == WORKER_STATE_SHUTDOWN) {
                        return Long.MIN_VALUE;
                    }
                }
            }
        }

        public Set<ScheduleTimeout> unprocessedScheduleTimeouts() {
            return Collections.unmodifiableSet(unprocessedScheduleTimeouts);
        }
    }

    private static final class HashedWheelScheduleTimeout implements ScheduleTimeout {

        private static final int ST_INIT = 0;
        private static final int ST_CANCELLED = 1;
        private static final int ST_EXPIRED = 2;
        private static final AtomicIntegerFieldUpdater<HashedWheelScheduleTimeout> STATE_UPDATER;

        static {
            AtomicIntegerFieldUpdater<HashedWheelScheduleTimeout> updater = PlatformDependent.newAtomicIntegerFieldUpdater(HashedWheelScheduleTimeout.class, "state");
            if (updater == null) {
                updater = AtomicIntegerFieldUpdater.newUpdater(HashedWheelScheduleTimeout.class, "state");
            }
            STATE_UPDATER = updater;
        }

        private final ScheduleHashedWheelTimer timer;
        private final ScheduleTimerTask task;
        private final long deadline;

        private volatile int state = ST_INIT;

        // remainingRounds will be calculated and set by Worker.transferScheduleTimeoutsToBuckets() before the
        // HashedWheelScheduleTimeout will be added to the correct HashedWheelBucket.
        long remainingRounds;

        // This will be used to chain scheduleTimeouts in HashedWheelTimerBucket via a double-linked-list.
        // As only the workerThread will act on it there is no need for synchronization / volatile.
        HashedWheelScheduleTimeout next;
        HashedWheelScheduleTimeout prev;

        // The bucket to which the ScheduleTimeout was added
        HashedWheelBucket bucket;

        HashedWheelScheduleTimeout(ScheduleHashedWheelTimer timer, ScheduleTimerTask task, long deadline) {
            this.timer = timer;
            this.task = task;
            this.deadline = deadline;
        }

        @Override
        public ScheduleTimer timer() {
            return timer;
        }

        @Override
        public ScheduleTimerTask task() {
            return task;
        }

        @Override
        public boolean cancel() {
            // only update the state it will be removed from HashedWheelBucket on next tick.
            if (!compareAndSetState(ST_INIT, ST_CANCELLED)) {
                return false;
            }
            // If a task should be canceled we put this to another queue which will be processed on each tick.
            // So this means that we will have a GC latency of max. 1 tick duration which is good enough. This way
            // we can make again use of our MpscLinkedQueue and so minimize the locking / overhead as much as possible.
            timer.cancelledScheduleTimeouts.add(this);
            return true;
        }

        void remove() {
            HashedWheelBucket bucket = this.bucket;
            if (bucket != null) {
                bucket.remove(this);
            }
        }

        public boolean compareAndSetState(int expected, int state) {
            return STATE_UPDATER.compareAndSet(this, expected, state);
        }

        public int state() {
            return state;
        }

        @Override
        public boolean isCancelled() {
            return state() == ST_CANCELLED;
        }

        @Override
        public boolean isExpired() {
            return state() == ST_EXPIRED;
        }

        public void expire() {
            if (!compareAndSetState(ST_INIT, ST_EXPIRED)) {
                return;
            }

            try {
                task.run(this);
            } catch (Throwable t) {
                if (logger.isWarnEnabled()) {
                    logger.warn("An exception was thrown by " + ScheduleTimerTask.class.getSimpleName() + '.', t);
                }
            }
        }

        @Override
        public String toString() {
            final long currentTime = System.nanoTime();
            long remaining = deadline - currentTime + timer.startTime;

            StringBuilder buf = new StringBuilder(192).append(ScheduleStringUtil.simpleClassName(this)).append('(').append("deadline: ");
            if (remaining > 0) {
                buf.append(remaining).append(" ns later");
            } else if (remaining < 0) {
                buf.append(-remaining).append(" ns ago");
            } else {
                buf.append("now");
            }

            if (isCancelled()) {
                buf.append(", cancelled");
            }

            return buf.append(", task: ").append(task()).append(')').toString();
        }
    }

    /**
     * Bucket that stores HashedWheelScheduleTimeouts. These are stored in a linked-list like datastructure to allow easy
     * removal of HashedWheelScheduleTimeouts in the middle. Also the HashedWheelScheduleTimeout act as nodes themself and so no
     * extra object creation is needed.
     */
    private static final class HashedWheelBucket {
        // Used for the linked-list datastructure
        private HashedWheelScheduleTimeout head;
        private HashedWheelScheduleTimeout tail;

        /**
         * Add {@link HashedWheelScheduleTimeout} to this bucket.
         */
        public void addScheduleTimeout(HashedWheelScheduleTimeout scheduleTimeout) {
            assert scheduleTimeout.bucket == null;
            scheduleTimeout.bucket = this;
            if (head == null) {
                head = tail = scheduleTimeout;
            } else {
                tail.next = scheduleTimeout;
                scheduleTimeout.prev = tail;
                tail = scheduleTimeout;
            }
        }

        /**
         * Expire all {@link HashedWheelScheduleTimeout}s for the given {@code deadline}.
         */
        public void expireScheduleTimeouts(long deadline) {
            HashedWheelScheduleTimeout ScheduleTimeout = head;

            // process all scheduleTimeouts
            while (ScheduleTimeout != null) {
                boolean remove = false;
                if (ScheduleTimeout.remainingRounds <= 0) {
                    if (ScheduleTimeout.deadline <= deadline) {
                        ScheduleTimeout.expire();
                    } else {
                        // The ScheduleTimeout was placed into a wrong slot. This should never happen.
                        throw new IllegalStateException(String.format("ScheduleTimeout.deadline (%d) > deadline (%d)", ScheduleTimeout.deadline, deadline));
                    }
                    remove = true;
                } else if (ScheduleTimeout.isCancelled()) {
                    remove = true;
                } else {
                    ScheduleTimeout.remainingRounds--;
                }
                // store reference to next as we may null out ScheduleTimeout.next in the remove block.
                HashedWheelScheduleTimeout next = ScheduleTimeout.next;
                if (remove) {
                    remove(ScheduleTimeout);
                }
                ScheduleTimeout = next;
            }
        }

        public void remove(HashedWheelScheduleTimeout ScheduleTimeout) {
            HashedWheelScheduleTimeout next = ScheduleTimeout.next;
            // remove ScheduleTimeout that was either processed or cancelled by updating the linked-list
            if (ScheduleTimeout.prev != null) {
                ScheduleTimeout.prev.next = next;
            }
            if (ScheduleTimeout.next != null) {
                ScheduleTimeout.next.prev = ScheduleTimeout.prev;
            }

            if (ScheduleTimeout == head) {
                // if ScheduleTimeout is also the tail we need to adjust the entry too
                if (ScheduleTimeout == tail) {
                    tail = null;
                    head = null;
                } else {
                    head = next;
                }
            } else if (ScheduleTimeout == tail) {
                // if the ScheduleTimeout is the tail modify the tail to be the prev node.
                tail = ScheduleTimeout.prev;
            }
            // null out prev, next and bucket to allow for GC.
            ScheduleTimeout.prev = null;
            ScheduleTimeout.next = null;
            ScheduleTimeout.bucket = null;
        }

        /**
         * Clear this bucket and return all not expired / cancelled {@link ScheduleTimeout}s.
         */
        public void clearScheduleTimeouts(Set<ScheduleTimeout> set) {
            for (; ; ) {
                HashedWheelScheduleTimeout ScheduleTimeout = pollScheduleTimeout();
                if (ScheduleTimeout == null) {
                    return;
                }
                if (ScheduleTimeout.isExpired() || ScheduleTimeout.isCancelled()) {
                    continue;
                }
                set.add(ScheduleTimeout);
            }
        }

        private HashedWheelScheduleTimeout pollScheduleTimeout() {
            HashedWheelScheduleTimeout head = this.head;
            if (head == null) {
                return null;
            }
            HashedWheelScheduleTimeout next = head.next;
            if (next == null) {
                tail = this.head = null;
            } else {
                this.head = next;
                next.prev = null;
            }

            // null out prev and next to allow for GC.
            head.next = null;
            head.prev = null;
            head.bucket = null;
            return head;
        }
    }

}
