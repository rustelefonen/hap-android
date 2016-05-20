package no.rustelefonen.hap.util;

import android.app.Activity;
import android.content.DialogInterface;
import android.graphics.Color;
import android.support.v7.app.AlertDialog;

import no.rustelefonen.hap.R;


/**
 * Created by martinnikolaisorlie on 13/04/16.
 */
public class DialogHelper {

    public static void showInfoDialog(Activity activity, String title, String displayedText) {
        showConfirmFinishDialog(activity, title, displayedText, false);
    }

    public static void showDiscardChangesAndFinishDialog(final Activity activity, String title, String displayedText) {
        showConfirmFinishDialog(activity, title, displayedText, true);
    }

    public static void showConfirmDialogWithAction(final Activity activity, String title, String displayedText, String destructiveButtonText, DialogInterface.OnClickListener listener){
        showDialog(activity, title, displayedText, true, destructiveButtonText, listener);
    }

    public static void showConfirmFinishDialog(final Activity activity, String title, String displayedText, boolean destructive) {
        showDialog(activity, title, displayedText, destructive, activity.getString(R.string.dialog_destructive_button), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                activity.finish();
            }
        });
    }

    public static void showConfirmExitDialog(final Activity activity, String title, String displayedText) {
        showDialog(activity, title, displayedText, true, activity.getString(R.string.dialog_confirm_exit_button), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                activity.finish();
            }
        });
    }

    private static void showDialog(final Activity activity, String title, String displayedText, boolean destructive, String destructiveButtonText, DialogInterface.OnClickListener listener){
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle(title);
        builder.setMessage(displayedText);
        builder.setNegativeButton(!destructive ? activity.getString(R.string.dialog_ok_button) : activity.getString(R.string.dialog_destructive_abort_button), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        if (destructive) builder.setPositiveButton(destructiveButtonText, listener);

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
        alertDialog.getButton(DialogInterface.BUTTON_POSITIVE).setTextColor(Color.RED);
    }
}
