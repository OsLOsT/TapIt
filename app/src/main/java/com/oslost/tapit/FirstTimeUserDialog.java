package com.oslost.tapit;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatDialogFragment;


public class FirstTimeUserDialog extends AppCompatDialogFragment {

    private EditText addressText;
    private Button addAddressButton;
    private FirstTimeUserDialogListener listener;
    private AlertDialog alertDialog;
    private Context context;
    private SharedPreferences showLocation;

    /* #################################################################
        SharedPrefs showLocation contains the following key-value pair:

        - MyLocationName : <Location Name>
        - MyLatitude : <Location Lat>
        - MyLongitude : <Location Lng>
        - FirstTime : <Boolean FirsTime>

       ################################################################# */

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        alertDialog = builder.create();

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.first_time_add_address, null);
        builder.setView(view);

        addressText = view.findViewById(R.id.new_address_text);
        addAddressButton = view.findViewById(R.id.new_address_button);

        addAddressButton.setOnClickListener(addButton);

        return builder.create();
    }

    private View.OnClickListener addButton = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            String address = addressText.getText().toString();
            if(address !=null){



               alertDialog.dismiss();
            }
        }
    };

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            listener = (FirstTimeUserDialogListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + "must implement FirstTimeUserDialogListener");
        }
    }

    public interface FirstTimeUserDialogListener{
        void applyAddress(String address);
    }





}
