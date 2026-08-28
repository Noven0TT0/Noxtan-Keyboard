package com.noxtan.pass.aidl;

import com.noxtan.pass.aidl.VaultAccountInfo;

interface INoxtanVaultService {
    // ၁။ Vault Lock ကျနေသလား စစ်ဆေးရန်
    boolean isVaultLocked();

    // ၂။ Keyboard ပေါ်မှ PIN ဖြင့် သော့လှမ်းဖွင့်ရန်
    boolean unlockWithPin(String pin);

    // ၃။ Keyboard ပေါ်မှ သော့ချက်ချင်း ပြန်ခတ်ရန်
    void lockVault();

    // ၄။ လက်ရှိဖွင့်ထားသော App နှင့် ကိုက်ညီသည့် အကောင့်များ ယူရန်
    List<VaultAccountInfo> getAccountsForPackage(String packageName);

    // ၅။ အကောင့်စာရင်းအားလုံးကို ရှာဖွေပြီး Scroll ဆွဲကြည့်ရန်
    List<VaultAccountInfo> searchAllAccounts(String query);
}