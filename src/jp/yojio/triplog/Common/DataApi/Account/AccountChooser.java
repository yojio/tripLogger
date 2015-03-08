package jp.yojio.triplog.Common.DataApi.Account;

import jp.yojio.triplog.R;
import jp.yojio.triplog.Common.Common.Const;
import jp.yojio.triplog.Common.Misc.Misc;

import com.google.android.accounts.Account;
import com.google.android.accounts.AccountManager;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;

public class AccountChooser {

  private int selectedAccountIndex = -1;
  private Account selectedAccount = null;

  public interface AccountHandler {
    public void handleAccountSelected(Account account);
  }

  public void chooseAccount(final Activity activity, final AccountHandler handler) {
    final Account[] accounts = AccountManager.get(activity).getAccountsByType(Const.ACCOUNT_TYPE);
    if (accounts.length < 1) {
      alertNoAccounts(activity, handler);
      return;
    }
    if (accounts.length == 1) {
      handler.handleAccountSelected(accounts[0]);
      return;
    }

    if (selectedAccount != null) {
      handler.handleAccountSelected(selectedAccount);
      return;
    }

    // 過去に指定したものを引き継ぐ（消す仕組みが必要）
    String curaddr = Misc.getEnvValueStr(activity, Const.KEY_CURRENT_ADDR);
    if ((curaddr != null) && (!curaddr.equals(""))){
      for (int i=0;i<accounts.length;i++){
        if (curaddr.equals(accounts[i].name)){
          selectedAccount = accounts[i];
          handler.handleAccountSelected(selectedAccount);
          return;
        }
      }
    }

    // Let the user choose.
    final AlertDialog.Builder builder = new AlertDialog.Builder(activity);
    builder.setTitle(R.string.choose_account_title);
    builder.setCancelable(false);
    builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
      public void onClick(DialogInterface dialog, int which) {
        selectedAccount = accounts[selectedAccountIndex];
        Misc.setEnvValue(activity, Const.KEY_CURRENT_ADDR, selectedAccount.name);
        handler.handleAccountSelected(selectedAccount);
      }
    });
    builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
      public void onClick(DialogInterface dialog, int which) {
        handler.handleAccountSelected(null);
      }
    });
    String[] choices = new String[accounts.length];
    for (int i = 0; i < accounts.length; i++) {
      choices[i] = accounts[i].name;
    }
    builder.setSingleChoiceItems(choices, selectedAccountIndex, new DialogInterface.OnClickListener() {
      public void onClick(DialogInterface dialog, int which) {
        selectedAccountIndex = which;
      }
    });
    builder.show();
  }

  private void alertNoAccounts(final Activity activity, final AccountHandler handler) {
    final AlertDialog.Builder builder = new AlertDialog.Builder(activity);
    builder.setTitle(R.string.no_account_found_title);
    builder.setMessage(R.string.no_account_found);
    builder.setCancelable(true);
    builder.setNegativeButton(R.string.ok, new DialogInterface.OnClickListener() {
      public void onClick(DialogInterface dialog, int which) {
        handler.handleAccountSelected(null);
      }
    });
    builder.show();
  }
}
