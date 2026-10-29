package org.lavalogged;

/**
 * Flag threaded from {@code tryDrainFluid}/{@code pickupBlock} to the parameterless
 * {@code getBucketFillSound()}/{@code getPickupSound()} vanilla callbacks, which have no way to
 * see the block state that was just drained. Vanilla invokes the pair back-to-back on the server
 * thread, so a simple thread-local marker is enough to swap the water fill sound for the lava one.
 */
public final class LavaDrainSound {

	private static final ThreadLocal<Boolean> DRAINED = ThreadLocal.withInitial(() -> Boolean.FALSE);

	private LavaDrainSound() {
	}

	/**
	 * Marks that a lava drain just happened on this thread.
	 */
	public static void mark() {
		DRAINED.set(Boolean.TRUE);
	}

	/**
	 * {@return whether a lava drain happened since the last call, clearing the marker}
	 */
	public static boolean consume() {
		boolean drained = DRAINED.get();
		DRAINED.set(Boolean.FALSE);
		return drained;
	}

}
