package com.trainerdb.trainerandroid.activities;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.FileProvider;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;

import com.trainerdb.trainerandroid.R;
import com.trainerdb.trainerandroid.train.TrainFile;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class TrainFileListActivity extends AppCompatActivity {

    List<TrainFile> files;
    ListView list;
    FileListAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_train_file_list);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(myToolbar);
        this.getSupportActionBar().setTitle("Files");

        // Show the Up button in the action bar.
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        files = getListFiles(this.getFilesDir());
        files.addAll(getListFiles(this.getExternalFilesDir(null)));

        list = (ListView) findViewById(R.id.listFiles);

        adapter = new FileListAdapter(this, files);
        list.setAdapter(adapter);

        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                CheckBox chkSelected = (CheckBox) view.findViewById(R.id.checkBoxSelected);
                if (chkSelected.isChecked())
                    files.get(position).setSelected(false);
                else
                    files.get(position).setSelected(true);

                adapter.notifyDataSetChanged();
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_delete) {
            AlertDialog.Builder abuilder = new AlertDialog.Builder(this);
            abuilder.setMessage("Delete workout(s)?");
            abuilder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    Iterator<TrainFile> i = files.iterator();
                    while (i.hasNext()) {
                        TrainFile file = i.next();
                        if (file.isSelected()) {
                            file.getFile().delete();
                            i.remove();
                        }
                    }
                    adapter.notifyDataSetChanged();
                }
            });
            abuilder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                }
            });
            AlertDialog alert = abuilder.create();
            alert.show();
        } else if (id == R.id.action_share) {
            shareFilesSelected();
            /*
            Iterator<TrainFile> i = files.iterator();
            while (i.hasNext()) {
                TrainFile file = i.next();
                if (file.isSelected()) {

                    final FirebaseDatabase database = FirebaseDatabase.getInstance();
                    final DatabaseReference ref = database.getReference("workouts");
                    ref.child("145788").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            final Workout w = dataSnapshot.getValue(Workout.class);
                            w.downloadData(new CompleteListener() {
                                @Override
                                public void onComplete(boolean success) {
                                    FileController controller = new FileController(w, files.get(0), 1);
                                    controller.exportRecord();
                                }
                            });
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                        }
                    });
                }
            }
            */
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_files, menu);
        return true;
    }

    private void shareFilesSelected() {
        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND_MULTIPLE);
        shareIntent.setType("application/xml");

        ArrayList<Uri> filesUri = new ArrayList<Uri>();
        for (TrainFile file : files) {
            if (file.isSelected()) {
                filesUri.add(FileProvider.getUriForFile(this, "com.trainerandroid.fileprovider", file.getFile()));
            }
        }

        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        //shareIntent.setDataAndType(fileUri, getContentResolver().getType(fileUri));
        // Set the result
        this.setResult(Activity.RESULT_OK, shareIntent);

        shareIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, filesUri);
        startActivity(Intent.createChooser(shareIntent, "Share"));
    }

    private List<TrainFile> getListFiles(File parentDir) {
        ArrayList<TrainFile> inFiles = new ArrayList<>();
        File[] files = parentDir.listFiles();
        for (File file : files) {
            if (file.isDirectory()) {
                inFiles.addAll(getListFiles(file));
            } else {
                if (file.getName().endsWith(".csv")) {
                    inFiles.add(new TrainFile(file));
                }
            }
        }
        return inFiles;
    }

    public class FileListAdapter extends ArrayAdapter<TrainFile> {
        private Activity context;
        private List<TrainFile> list;

        public FileListAdapter(Activity context, List<TrainFile> objects) {
            super((Context) context, R.layout.train_file_list_content, objects);
            this.context = context;
            this.list = objects;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View rowView = convertView;

            if (rowView == null) {
                LayoutInflater inflater = this.context.getLayoutInflater();
                rowView = inflater.inflate(R.layout.train_file_list_content, null, true);

            }
            TextView txtTitle = (TextView) rowView.findViewById(R.id.tvFileName);
            CheckBox chkSelected = (CheckBox) rowView.findViewById(R.id.checkBoxSelected);

            TrainFile file = this.list.get(position);
            txtTitle.setText(file.getName());
            chkSelected.setChecked(file.isSelected());

            return rowView;
        }

        @Override
        public int getCount() {
            return list != null ? list.size() : 0;
        }

        @Override
        public TrainFile getItem(int position) {
            return list.get(position);
        }
    }

}
