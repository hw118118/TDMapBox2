package com.example.hgis.tdmapbox2;


import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.LayoutAnimationController;

/**
 * Created by HGIS on 2017/8/28.
 */

public class CustomRecyclerView extends RecyclerView{
    public CustomRecyclerView(Context context) {
        super(context);
    }

    public CustomRecyclerView(Context context, AttributeSet attrs){
        super(context, attrs);
    }
    public CustomRecyclerView(Context context,AttributeSet attrs,int defStyle){
        super(context,attrs,defStyle);
    }

    @Override
    public void setLayoutManager(LayoutManager layout) {
        super.setLayoutManager(layout);
    }

    @Override
    protected void attachLayoutAnimationParameters(View child, ViewGroup.LayoutParams params, int index, int count) {
        if (getAdapter()!=null){
            LayoutAnimationController.AnimationParameters animationParameters=params.layoutAnimationParameters;
            if(animationParameters==null){
                AlphaAnimation alphaAnimation=new AlphaAnimation(0,1);
                alphaAnimation.setDuration(1000);
                animationParameters=new LayoutAnimationController.AnimationParameters();
                params.layoutAnimationParameters=animationParameters;
            }
            animationParameters.count=count;
            animationParameters.index=index;
        }else {
            super.attachLayoutAnimationParameters(child, params, index, count);
        }
    }
}
