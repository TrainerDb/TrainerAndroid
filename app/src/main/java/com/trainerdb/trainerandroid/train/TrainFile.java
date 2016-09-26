package com.trainerdb.trainerandroid.train;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.content.FileProvider;
import java.io.File;

/**
 * Created by dcotrim on 29/06/2016.
 */
public class TrainFile {
    private File file;
    private boolean selected = false;

    public TrainFile(File file) {
        this.file = file;
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public boolean isSelected() {
        return selected;
    }

    public String getName() {
        return file.getName();
    }

    public void shareFile(Activity context) {
        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.setType("application/xml");

        Uri fileUri = FileProvider.getUriForFile(context, "com.trainerandroid.fileprovider", file);
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        shareIntent.setDataAndType(fileUri, context.getContentResolver().getType(fileUri));

        // Set the result
        context.setResult(Activity.RESULT_OK, shareIntent);

        shareIntent.putExtra(Intent.EXTRA_STREAM, fileUri);
        context.startActivity(Intent.createChooser(shareIntent, "Share"));
    }
}
