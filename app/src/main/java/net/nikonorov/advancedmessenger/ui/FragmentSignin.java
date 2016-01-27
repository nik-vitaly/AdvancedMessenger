package net.nikonorov.advancedmessenger.ui;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import net.nikonorov.advancedmessenger.R;
import net.nikonorov.advancedmessenger.User;
import net.nikonorov.advancedmessenger.utils.TaskType;
import net.nikonorov.advancedmessenger.utils.Utils;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by vitaly on 25.01.16.
 */
public class FragmentSignin extends CallableFragment {

    public FragmentSignin(){}

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_signin, null);

        View signupButton = view.findViewById(R.id.sign_bar_button);

        signupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((ActivitySign)getActivity()).changeFragment(FragmentSet.SIGNIN, FragmentSet.SIGNUP);
            }
        });

        Button signinButton = (Button)  view.findViewById(R.id.signin_auth_action);

        signinButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                EditText loginET = (EditText) view.findViewById(R.id.signin_login);
                EditText passET = (EditText) view.findViewById(R.id.signin_pass);

                StringBuilder sb = new StringBuilder();

                User.setLogin(loginET.getText().toString());
                User.setPass(Utils.getHashMD5(passET.getText().toString()));

                sb.append("{\"action\":\"auth\", \"data\":{\"login\":\"");
                sb.append(loginET.getText()).append("\", ");
                sb.append("\"pass\": \"").append(Utils.getHashMD5(passET.getText().toString())).append("\"}} ");

                String reqObject = sb.toString();

                serviceHelper.executeCommand(TaskType.REGISTRATE, reqObject, getActivity());
            }
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        SharedPreferences sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
        String sid = sharedPref.getString("sid", null);
        String cid = sharedPref.getString("cid", null);
        String login = sharedPref.getString("login", null);

        if (sid != null && cid != null && login != null){
            User.setSid(sid);
            User.setCid(cid);
            User.setLogin(login);

            Toast.makeText(getActivity(), "Hello "+login, Toast.LENGTH_SHORT).show();

            Intent intent = new Intent(getActivity(), ActivityMain.class);
            startActivity(intent);
        }
    }

    @Override
    public void correctCodeHandle(int taskType, String data) {
        if (taskType == TaskType.AUTH){

            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getActivity(), "Welcome "+User.getLogin(), Toast.LENGTH_SHORT).show();
                }
            });

            try {
                JSONObject jsonObject = new JSONObject(data);
                User.setSid(jsonObject.getJSONObject("data").getString("sid"));
                User.setCid(jsonObject.getJSONObject("data").getString("cid"));

                SharedPreferences sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPref.edit();

                editor.putString("sid", User.getSid());
                editor.putString("uid", User.getCid());

                editor.putString("login", User.getLogin());

                editor.commit();

            } catch (JSONException e) {
                e.printStackTrace();
            }

            Intent intent = new Intent(getActivity(), ActivityMain.class);
            startActivity(intent);
        }
    }
}
