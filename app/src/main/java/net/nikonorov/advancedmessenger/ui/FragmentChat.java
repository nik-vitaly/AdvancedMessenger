package net.nikonorov.advancedmessenger.ui;

import android.Manifest;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import net.nikonorov.advancedmessenger.R;
import net.nikonorov.advancedmessenger.User;
import net.nikonorov.advancedmessenger.ui.adapters.ChatAdapter;
import net.nikonorov.advancedmessenger.utils.TaskType;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

/**
 * Created by vitaly on 29.01.16.
 */
public class FragmentChat extends CallableFragment implements LoaderManager.LoaderCallbacks<Cursor> {

    public FragmentChat(){}

    private String user = null;
    private String userAva = null;

    private ArrayList<JSONObject> data = new ArrayList<>();

    private final String LOG_TAG = "CHAT";
    private final static String DIALOGS_TABLE = "dialogs";

    private String preparedProto = "";
    private Button sendBtn = null;
    private Button putExtra = null;
    private EditText messageET = null;
    private RecyclerView chatList = null;

    private ChatAdapter adapter = null;

    private static final int URL_LOADER = 4;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chat, null);

        sendBtn = (Button) view.findViewById(R.id.send_msg_btn);
        putExtra = (Button) view.findViewById(R.id.extra_msg_btn);
        messageET = (EditText) view.findViewById(R.id.msg_et);
        chatList = (RecyclerView) view.findViewById(R.id.chat_rv);

        chatList = (RecyclerView) view.findViewById(R.id.chat_rv);
        chatList.setLayoutManager(new LinearLayoutManager(getActivity()));

        putExtra.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int hasReadContactsPermission = ContextCompat.checkSelfPermission(getActivity(),
                        Manifest.permission.CAMERA);

                if (hasReadContactsPermission != PackageManager.PERMISSION_GRANTED) {
                    if (!ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),
                            Manifest.permission.CAMERA)) {
                        showMessageOKCancel("You need to allow access to CAMERA",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        ActivityCompat.requestPermissions(getActivity(),
                                                new String[] {Manifest.permission.CAMERA},
                                                ActivityMain.REQUEST_CODE_PHOTO);
                                    }
                                });
                        return ;
                    }
                    ActivityCompat.requestPermissions(getActivity(),
                            new String[] {Manifest.permission.CAMERA},
                            ActivityMain.REQUEST_CODE_PHOTO);
                    return ;
                }
                takePhoto();
            }
        });

        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                StringBuilder sb = new StringBuilder();

                sb.append("{\"action\":\"message\", \"data\":{\"cid\":\"");
                sb.append(User.getCid()).append("\", \"sid\":\"");
                sb.append(User.getSid()).append("\", \"uid\":\"").append(user).append("\", ");
                sb.append("\"body\": \"").append(messageET.getText()).append("\", ")
                        .append("\"attach\": ")
                        .append("{\"mime\": \"photo\", \"data\":\"")
                        .append(preparedProto)
                        .append("\"}")
                        .append("}}");
                messageET.setText("");
                String reqObject = sb.toString();


                StringBuilder builder = new StringBuilder();

                builder.append("{\"from\":\"")
                        .append(User.getLogin())
                        .append("\", \"nick\":\"")
                        .append("me")
                        .append("\", \"body\":\"")
                        .append(messageET.getText())
                        .append("\", \"time\":\"")
                        .append(Long.valueOf(System.currentTimeMillis()).toString())
                        .append("\", \"attach\": { \"mime\":\"")
                        .append("image/jpeg")
                        .append("\", \"data\":\"")
                        .append(preparedProto)
                        .append("\"}}");


                Log.d(LOG_TAG, reqObject);

                preparedProto = "";

                serviceHelper.executeCommand(TaskType.MESSAGE, reqObject, getActivity());

                HashMap<String, String> dataSetDialogs = new HashMap<>();
                dataSetDialogs.put("to_user", User.getLogin());
                dataSetDialogs.put("from_user", User.getLogin());
                dataSetDialogs.put("data", builder.toString());
                dataSetDialogs.put("time", Long.valueOf(System.currentTimeMillis()).toString());

                saveData(dataSetDialogs, DIALOGS_TABLE);
            }
        });

        adapter = new ChatAdapter(data, (ActivityMain) getActivity());

        chatList.setAdapter(adapter);

        getLoaderManager().initLoader(URL_LOADER, null, this);

        return view;
    }

    @Override
    public void correctCodeHandle(int taskType, String data) {
        Log.i(LOG_TAG, "incoming message");
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        switch (id) {
            case URL_LOADER:
                // Returns a new CursorLoader
                return new CursorLoader(
                        getActivity(),   // Parent activity context
                        Uri.parse("content://net.nikonorov.advancedmessenger.providers.db/dialogs"),        // Table to query
                        null,     // Projection to return
                        "to_user = \'"+ User.getLogin()+"\' AND from_user = \'"+user+"\'",            // selection clause
                        null,            // No selection arguments
                        "time ASC"             // sort order
                );
            default:
                // An invalid id was passed in
                return null;
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        ArrayList<JSONObject> loadData = new ArrayList<JSONObject>();



        if (cursor.moveToFirst()){
            while(!cursor.isAfterLast()){
                try {
                    loadData.add(new JSONObject(cursor.getString(cursor.getColumnIndex("data"))));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                cursor.moveToNext();
            }
        }


        data.clear();

        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                adapter.notifyDataSetChanged();
            }
        });


        for(int i = 0; i < loadData.size(); i++){
            data.add(loadData.get(i));
        }

        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                adapter.notifyDataSetChanged();
            }
        });


    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    public void setUser(String user) {
        this.user = user;
    }

    public void setUserAva(String userAva) {
        this.userAva = userAva;
    }

    public String getUserAva() {
        return userAva;
    }

    private void saveData(HashMap<String, String> data, String table) {
        ContentValues cv = new ContentValues();
        Set<String> set = data.keySet();

        for (String key : set){
            cv.put(key, data.get(key));
        }

        final Uri URI = Uri
                .parse("content://net.nikonorov.advancedmessenger.providers.db/"+table);
        Uri newUri = (getActivity()).getContentResolver().insert(URI, cv);

        Log.i(LOG_TAG, "insert, result Uri : " + newUri.toString());
    }

    private void showMessageOKCancel(String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(getActivity())
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", null)
                .create()
                .show();
    }

    public void takePhoto() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, ActivityMain.REQUEST_CODE_PHOTO);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == ActivityMain.REQUEST_CODE_PHOTO) {
            if (resultCode == getActivity().RESULT_OK) {
                Log.d(LOG_TAG, "Photo uri: " + data.getData());
                Bitmap photo = (Bitmap) data.getExtras().get("data");

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                photo.compress(Bitmap.CompressFormat.JPEG, 100, baos);

                byte[] compressedPhoto = baos.toByteArray();

                String encodedPhoto = Base64.encodeToString(compressedPhoto, Base64.NO_WRAP);

                preparedProto = encodedPhoto;
            }

            Log.i("PHOTO", "photo");
        }
    }

}
