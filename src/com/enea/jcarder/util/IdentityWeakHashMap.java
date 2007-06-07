package com.enea.jcarder.util;

import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import net.jcip.annotations.NotThreadSafe;

/**
 * This class is similar to the java.util.WeakHashMap but compairs objects with
 * the == operator instead of with the Object.equals method.
 *
 * TODO Add basic tests for this class.
 */
@NotThreadSafe
public final class IdentityWeakHashMap<V> {
    private final HashMap<IdentityComparableKey, V> mHashMap;
    private final ReferenceQueue<Object> mReferenceQueue;
    private final StrongKey mStrongKey = new StrongKey();
    private Object mLastKey = null; // Cache to improve performance.
    private V mLastValue = null; // Cache to improve performance.
    private int mPutCounter = 0;

    public IdentityWeakHashMap() {
        mHashMap = new HashMap<IdentityComparableKey, V>();
        mReferenceQueue = new ReferenceQueue<Object>();
    }

    public V get(Object key) {
        // Avoid calls to removeGarbageCollectedKeys in this method in order
        // to improve performance.
        if (key == mLastKey) {
            return mLastValue;
        }
        mLastKey = key;
        mStrongKey.setReferent(key);
        mLastValue = mHashMap.get(mStrongKey);
        return mLastValue;
    }

    public void put(Object key, V value) {
        assert value != null;
        mLastKey = key;
        mLastValue = value;
        if (mPutCounter > 1000) {
            // Don't call to often in order to improve performance.
            removeGarbageCollectedKeys();
            mPutCounter = 0;
        } else {
            mPutCounter++;
        }
        mHashMap.put((new WeakKey(key, mReferenceQueue)), value);
    }

    private void removeGarbageCollectedKeys() {
        Reference e;
        int noOfCollectedLocks = 0;
        while ((e = mReferenceQueue.poll()) != null) {
            noOfCollectedLocks++;
            mHashMap.remove(e);
        }
    }

    private static interface IdentityComparableKey {
        Object get();
        boolean equals(Object obj);
        int hashCode();
    }

    private static class StrongKey implements IdentityComparableKey {
        private Object mReferent;

        void setReferent(Object referent) {
            assert referent != null;
            mReferent = referent;
        }

        public Object get() {
            return mReferent;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            try {
                IdentityComparableKey reference = (IdentityComparableKey) obj;
                return (reference.get() == get());
            } catch (ClassCastException e) {
                return false;
            }
        }

        @Override
        public int hashCode() {
            return System.identityHashCode(mReferent);
        }
    }

    private static class WeakKey extends WeakReference<Object>
    implements IdentityComparableKey {
        private final int mHash;

        WeakKey(Object referent, ReferenceQueue<Object> queue) {
            super(referent, queue);
            assert referent != null;
            mHash = System.identityHashCode(referent);
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            try {
                IdentityComparableKey reference = (IdentityComparableKey) obj;
                Object referent = reference.get();
                return (referent != null) &&  (referent == get());
            } catch (ClassCastException e) {
                return false;
            }
        }

        @Override
        public int hashCode() {
            return mHash;
        }
    }
}