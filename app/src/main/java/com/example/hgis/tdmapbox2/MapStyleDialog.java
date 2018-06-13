package com.example.hgis.tdmapbox2;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

/**
 * Created by HGIS on 2017/8/11.
 */

public class MapStyleDialog extends Dialog implements OnClickListener {
    private Context context;
    private RelativeLayout rl_satellite;
    private RelativeLayout rl_streets;
    private LeaveMapStyleDialogListener listener;
    public interface LeaveMapStyleDialogListener{
        public void onClick(View view);
    }
    public MapStyleDialog(Context context){
        super(context);
        this.context=context;
    }
    public MapStyleDialog(Context context,int theme,LeaveMapStyleDialogListener listener){
        super(context,theme);
        // TODO Auto-generated constructor stub
        this.context = context;
        this.listener = listener;
    }
    @Override
    public void onClick(View view) {
        listener.onClick(view);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.switch_mapstyle);
        rl_streets=findViewById(R.id.rl_vector);
        rl_satellite=findViewById(R.id.rl_satellite);
        rl_satellite.setOnClickListener(this);
        rl_streets.setOnClickListener(this);
    }
}
