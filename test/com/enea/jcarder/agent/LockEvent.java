/*
 * JCarder -- cards Java programs to keep threads disentangled
 *
 * Copyright (C) 2006-2007 Enea AB
 * Copyright (C) 2007 Ulrik Svensson
 * Copyright (C) 2007 Joel Rosdahl
 *
 * This program is made available under the GNU GPL version 2, with a special
 * exception for linking with JUnit. See the accompanying file LICENSE.txt for
 * details.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.
 */

package com.enea.jcarder.agent;

import com.enea.jcarder.common.Lock;
import com.enea.jcarder.common.LockingContext;
import com.enea.jcarder.common.events.LockEventListenerIfc.LockEventType;

public final class LockEvent {
    /** true for lock, false for unlock */
    private final LockEventType mType;

    private final Lock mLock;
    private final LockingContext mLockingContext;

    public LockEvent(final LockEventType type,
                     final Lock lock,
                     final LockingContext lockingContext) {
        mType = type;
        mLock = lock;
        mLockingContext = lockingContext;
    }

    public int hashCode() {
        return mLock.hashCode();
    }

    /**
     * This method is auto-generated by Eclipse.
     */
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        final LockEvent other = (LockEvent) obj;

        if (this.mType != other.mType)
            return false;

        if (this.mLock == null) {
            if (other.mLock != null)
                return false;
        } else if (!this.mLock.equals(other.mLock))
            return false;
        if (this.mLockingContext == null) {
            if (other.mLockingContext != null)
                return false;
        } else if (!this.mLockingContext.equals(other.mLockingContext))
            return false;
        return true;
    }

    public String toString() {
        return mType.toString() + ":" +
            mLock.toString() + " in ctx:" + mLockingContext;
    }
}
