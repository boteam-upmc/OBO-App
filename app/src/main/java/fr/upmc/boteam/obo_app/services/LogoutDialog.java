package fr.upmc.boteam.obo_app.services;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;

import fr.upmc.boteam.obo_app.MainActivity;
import fr.upmc.boteam.obo_app.R;


public class LogoutDialog extends DialogFragment{
    public static final String TAG = "Dialog";

    public static LogoutDialog newInstance(String action){
        LogoutDialog f = new LogoutDialog();
        Bundle args = new Bundle();
        args.putSerializable("action", action);
        f.setArguments(args);
        return f;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        getDialog().setCanceledOnTouchOutside(false);//pour ne pouvoir pas cliquer en dehors de la dialogue
        final View view = inflater.inflate(R.layout.dialog, container, false);

        final String action = getArguments().getString("action");

        Button btn_cancel = (Button) view.findViewById(R.id.cancel) ;
        Button btn_confirm = (Button) view.findViewById(R.id.confirm);
        btn_cancel.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                dismiss();//fermer Dialog
            }
        } );
        btn_confirm.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                if("ExitApp".equals(action)){
                    intent.setAction("ExitApp");
                }
                intent.setClass(getActivity(),MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        } );

        return view;
    }


}
