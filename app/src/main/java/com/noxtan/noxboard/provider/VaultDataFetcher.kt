package com.noxtan.noxboard.provider

import android.content.Context
import android.net.Uri
import com.noxtan.noxboard.ui.screens.VaultAccount

object VaultDataFetcher {
    private val PROVIDER_URI = Uri.parse("content://com.noxtan.kee.vaultprovider/items")

    sealed class VaultResult {
        data class Success(val accounts: List<VaultAccount>) : VaultResult()
        object Locked : VaultResult()
        object Error : VaultResult()
    }

    fun fetchAccounts(context: Context): VaultResult {
        return try {
            val cursor = context.contentResolver.query(PROVIDER_URI, null, null, null, null)
                ?: return VaultResult.Error

            cursor.use {
                if (it.columnNames.contains("status") && it.moveToFirst()) {
                    if (it.getString(0) == "LOCKED") {
                        return VaultResult.Locked
                    }
                }

                val accounts = mutableListOf<VaultAccount>()
                val titleIdx = it.getColumnIndex("title")
                val userIdx = it.getColumnIndex("username")
                val passIdx = it.getColumnIndex("password")
                val totpIdx = it.getColumnIndex("totp")
                val urlIdx = it.getColumnIndex("url")
                val noteIdx = it.getColumnIndex("note")

                while (it.moveToNext()) {
                    accounts.add(
                        VaultAccount(
                            name = it.getString(titleIdx) ?: "Unknown",
                            username = it.getString(userIdx) ?: "",
                            password = it.getString(passIdx) ?: "",
                            totp = it.getString(totpIdx) ?: "",
                            url = it.getString(urlIdx) ?: "",
                            note = it.getString(noteIdx) ?: ""
                        )
                    )
                }
                VaultResult.Success(accounts)
            }
        } catch (e: Exception) {
            com.noxtan.noxboard.utils.NoxLogger.logError("VaultDataFetcher", "Failed to fetch accounts", e)
            VaultResult.Error
        }
    }
}