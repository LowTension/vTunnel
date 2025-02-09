package com.netbyte.vtunnel.activity;


import static android.app.Activity.RESULT_OK;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.net.VpnService;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import com.google.android.material.switchmaterial.SwitchMaterial;
import com.netbyte.vtunnel.R;
import com.netbyte.vtunnel.model.AppConst;
import com.netbyte.vtunnel.service.SimpleVPNService;
import com.netbyte.vtunnel.thread.WsThread;


public class HomeTab extends Fragment {
    SharedPreferences preferences;
    SwitchMaterial switchMaterial;
    OnFragmentInteractionListener mListener;

    public HomeTab() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_tab_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        FragmentActivity activity = this.getActivity();
        assert activity != null;
        preferences = activity.getSharedPreferences(AppConst.APP_NAME, Activity.MODE_PRIVATE);
        switchMaterial = getView().findViewById(R.id.connButton);
        switchMaterial.setChecked(WsThread.RUNNING);
        switchMaterial.setOnCheckedChangeListener((buttonView, isChecked) -> {
            Intent intent = VpnService.prepare(this.getActivity());
            if (intent != null) {
                startActivityForResult(intent, 0);
            } else {
                Intent data = new Intent();
                data.putExtra("isChecked", isChecked);
                onActivityResult(0, RESULT_OK, data);
            }
            Toast.makeText(activity, isChecked ? "STARTED ！" : "STOPPED !", Toast.LENGTH_LONG).show();
        });
        {

        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Uri uri);
    }

    @Override
    public void onActivityResult(int request, int result, Intent data) {
        super.onActivityResult(request, result, data);
        if (result != RESULT_OK) {
            return;
        }
        boolean isChecked = true;
        if (data != null) {
            isChecked = data.getBooleanExtra("isChecked", false);
        }
        String server = preferences.getString("server", AppConst.DEFAULT_SERVER_ADDRESS);
        String dns = preferences.getString("dns", AppConst.DEFAULT_DNS);
        String key = preferences.getString("key", AppConst.DEFAULT_KEY);
        String bypassApps = preferences.getString("bypass_apps", "");
        boolean obfuscate = preferences.getBoolean("obfuscate", false);

        Intent intent = new Intent(this.getActivity(), SimpleVPNService.class);
        intent.setAction(isChecked ? AppConst.BTN_ACTION_CONNECT : AppConst.BTN_ACTION_DISCONNECT);
        intent.putExtra("server", server);
        intent.putExtra("dns", dns);
        intent.putExtra("key", key);
        intent.putExtra("bypass_apps", bypassApps);
        intent.putExtra("obfuscate", obfuscate);
        getActivity().startService(intent);
    }
}
