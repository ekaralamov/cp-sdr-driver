package sdr.driver.cp

import kotlinx.coroutines.runBlocking
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.locks.ReentrantLock
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.concurrent.withLock

class TunerAccessToken<Address, S : TunerAccessToken.Session> private constructor(
    private val deviceAddress: Address,
    private val callingPackage: String,
    private val registry: Registry<Address, S>
) {

    interface Session {

        suspend fun close()
    }

    @Singleton
    class Registry<Address, S : Session> @Inject constructor(
        private val permissionRepository: ClientPermissionRepository
    ) {

        private val map = ConcurrentHashMap<Address, TunerAccessToken<Address, S>>()

        fun acquireToken(
            deviceAddress: Address,
            callingPackage: String,
            sessionFactory: (Address) -> S
        ): TunerAccessToken<Address, S> {
            var oldToken: TunerAccessToken<Address, S>?
            while (true) {
                val freshToken = TunerAccessToken(
                    deviceAddress,
                    callingPackage,
                    this
                )
                freshToken.lock.withLock {
                    oldToken = map.putIfAbsent(deviceAddress, freshToken)
                    if (oldToken == null) {
                        try {
                            runBlocking {
                                if (permissionRepository.retrieveResolutionFor(callingPackage) != ClientPermissionResolution.Permanent.Granted)
                                    throw SecurityException("access denied")
                            }
                            freshToken._session = sessionFactory(deviceAddress)
                            return freshToken
                        } catch (throwable: Throwable) {
                            map.remove(deviceAddress)
                            throw throwable
                        }
                    }
                }
                @Suppress("NAME_SHADOWING") val oldToken = oldToken!!
                oldToken.lock.lockInterruptibly()
                try {
                    if (oldToken._session == null) continue
                    if (oldToken.refCount == 0) {
                        oldToken.closed.await()
                        continue
                    }
                    if (oldToken.callingPackage != callingPackage)
                        throw DeviceBusyException()
                    oldToken.refCount++
                    return oldToken
                } finally {
                    oldToken.lock.unlock()
                }
            }
        }

        suspend fun revokeTokensFor(packageName: String) {
            val i = map.values.iterator()
            while (i.hasNext()) with(i.next()) {
                if (callingPackage == packageName)
                    revoke(unregister = { i.remove() })
            }
        }

        internal fun remove(token: TunerAccessToken<Address, S>) {
            map.remove(token.deviceAddress)
        }
    }

    private var refCount = 1

    private val lock = ReentrantLock()
    private val closed = lock.newCondition()

    private var _session: S? = null
    val session: S
        get() = lock.withLock { _session ?: throw PermissionRevokedException() }

    suspend fun release() {
        lock.withLock {
            if (refCount == 0 || --refCount != 0)
                return
        }
        close(unregister = { registry.remove(this) })
    }

    private suspend inline fun revoke(unregister: () -> Unit) {
        lock.withLock {
            if (refCount == 0)
                return
            refCount = 0
        }
        close(unregister)
    }

    private suspend inline fun close(unregister: () -> Unit) {
        _session!!.close()
        lock.withLock {
            _session = null
            unregister()
            closed.signalAll()
        }
    }
}
