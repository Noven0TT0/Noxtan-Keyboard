package com.noxtan.noxboard.provider

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import android.util.Log
import com.noxtan.pass.aidl.INoxtanVaultService
import com.noxtan.pass.aidl.VaultAccountInfo
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

object VaultAidlClient {
    private const val TAG = "NoxVaultAidl"
    private var vaultService: INoxtanVaultService? = null
    private var isBound = false

    sealed class VaultResult {
        data class Success(val accounts: List<VaultAccountInfo>) : VaultResult()
        object Locked : VaultResult()
        object Error : VaultResult()
    }

    private suspend fun ensureServiceBound(context: Context): INoxtanVaultService? = suspendCancellableCoroutine { cont ->
        if (vaultService != null && isBound) {
            cont.resume(vaultService)
            return@suspendCancellableCoroutine
        }

        val intent = Intent().apply {
            component = ComponentName("com.noxtan.pass", "com.noxtan.pass.service.NoxtanVaultService")
        }

        val connection = object : ServiceConnection {
            override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
                Log.d(TAG, "Connected to NoxtanVaultService")
                val iface = INoxtanVaultService.Stub.asInterface(service)
                vaultService = iface
                isBound = true
                if (cont.isActive) cont.resume(iface)
            }

            override fun onServiceDisconnected(name: ComponentName?) {
                Log.w(TAG, "Disconnected from NoxtanVaultService")
                vaultService = null
                isBound = false
            }
        }

        val bound = context.bindService(intent, connection, Context.BIND_AUTO_CREATE)
        if (!bound) {
            Log.e(TAG, "Failed to bind to NoxtanVaultService")
            if (cont.isActive) cont.resume(null)
        }
    }

    suspend fun fetchAccounts(context: Context, packageName: String): VaultResult {
        val service = ensureServiceBound(context) ?: return VaultResult.Error
        return try {
            if (service.isVaultLocked) {
                VaultResult.Locked
            } else {
                val accounts = service.getAccountsForPackage(packageName)
                VaultResult.Success(accounts)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching accounts", e)
            VaultResult.Error
        }
    }

    suspend fun unlockWithPin(context: Context, pin: String): Boolean {
        val service = ensureServiceBound(context) ?: return false
        return try {
            service.unlockWithPin(pin)
        } catch (e: Exception) {
            Log.e(TAG, "Error unlocking with pin", e)
            false
        }
    }

    suspend fun lockVault(context: Context) {
        val service = ensureServiceBound(context) ?: return
        try {
            service.lockVault()
        } catch (e: Exception) {
            Log.e(TAG, "Error locking vault", e)
        }
    }

    suspend fun searchAllAccounts(context: Context, query: String): List<VaultAccountInfo> {
        val service = ensureServiceBound(context) ?: return emptyList()
        return try {
            if (service.isVaultLocked) {
                emptyList()
            } else {
                service.searchAllAccounts(query)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error searching accounts", e)
            emptyList()
        }
    }
}