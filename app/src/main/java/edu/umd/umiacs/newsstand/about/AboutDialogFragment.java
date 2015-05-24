package edu.umd.umiacs.newsstand.about;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

import edu.umd.umiacs.newsstand.MainActivity;
import edu.umd.umiacs.newsstand.R;
import edu.umd.umiacs.newsstand.source.Source;
import edu.umd.umiacs.newsstand.webview.WebViewActivity;

/**
 * Created by Brendan on 9/27/13.
 */
public class AboutDialogFragment extends DialogFragment {

    DialogInterface.OnClickListener moreInfoListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            Intent intent = new Intent(getActivity(), WebViewActivity.class);
            intent.putExtra("articleURL", "http://www.cs.umd.edu/~hjs/newsstand-first-page.html");
            intent.putExtra(MainActivity.TITLE, "Map");
            intent.putExtra("webViewTitle", "More Info");
            startActivity(intent);
        }
    };

    public Dialog onCreateDialog(Bundle savedInstanceState) {

        /** Getting the arguments passed to this fragment */
        //Bundle bundle = getArguments();

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_about, null);

        String version = "Version 1.0";
        try {
            PackageInfo pInfo = getActivity().getPackageManager().getPackageInfo(getActivity().getPackageName(), 0);
            version = "Version " + pInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {

        }

        TextView versionText = (TextView) view.findViewById(R.id.aboutDialogVersion);
        versionText.setText(version);

        /** Creating a builder for the alert dialog window */
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("About");
        builder.setView(view);
        builder.setPositiveButton("OK", (DialogInterface.OnClickListener)null);
        builder.setNegativeButton("More Info", moreInfoListener);

        /** Creating the alert dialog window using the builder class */
        AlertDialog dialog = builder.create();

        /** Return the alert dialog window */
        return dialog;
    }
}
