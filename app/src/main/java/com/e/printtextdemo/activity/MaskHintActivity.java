package com.e.printtextdemo.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;

import com.e.printtextdemo.widget.HintView;
import com.e.printtextdemo.R;
import com.e.printtextdemo.utils.Utility;

import java.util.HashMap;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by weioule
 * on 2020/1/4
 */
public class MaskHintActivity extends AppCompatActivity {

    @BindView(R.id.header_logo)
    ImageView headerLogo;
    @BindView(R.id.fl_sing_in)
    FrameLayout flSingIn;
    @BindView(R.id.iv_1)
    ImageView iv1;
    @BindView(R.id.iv_2)
    ImageView iv2;
    @BindView(R.id.iv_3)
    ImageView iv3;
    @BindView(R.id.iv_4)
    ImageView iv4;
    @BindView(R.id.ll_3)
    LinearLayout ll3;
    @BindView(R.id.ll_speed)
    LinearLayout llSpeed;

    private int raduis5dp;
    private boolean isShowing;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mask_hint);
        ButterKnife.bind(this);

        raduis5dp = Utility.dp2px(this, 5);
    }

    @Override
    protected void onResume() {
        super.onResume();

        headerLogo.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!isShowing) {
                    isShowing = true;
                    showMaskHint1();
                }
            }
        }, 600);
    }

    private void showMaskHint1() {
        View view = getLayoutInflater().inflate(R.layout.main_mask_hint_1, null, false);

        HintView.Builder
                .newInstance(this)
                .setTargetView(headerLogo)
                .setCustomGuideView(view)
                .setOffset(-Utility.dp2px(this, 11), headerLogo.getWidth() / 2)//偏移量
                .setDirction(HintView.Direction.RIGHT)//方向
                .setShape(HintView.MyShape.CIRCULAR)
                .setOutsideShape(HintView.MyShape.CIRCULAR)
                .setRadius(headerLogo.getWidth() / 2 - Utility.dp2px(this, 13))
                .setOnclickListener(new HintView.OnClickCallback() {
                    @Override
                    public void onClickedGuideView() {
                        showMaskHint2();
                    }
                })
                .build().show();

    }

    private void showMaskHint2() {
        View view = getLayoutInflater().inflate(R.layout.main_mask_hint_2, null, false);

        HintView.Builder
                .newInstance(this)
                .setTargetView(flSingIn)
                .setCustomGuideView(view)
                .setOffset(-Utility.dp2px(this, 6), flSingIn.getHeight() + raduis5dp)//偏移量
                .setDirction(HintView.Direction.LEFT)//方向
                .setShape(HintView.MyShape.RECTANGULAR)
                .setOutsideShape(HintView.MyShape.OVAL)
                .setOutsideSpace(Utility.dp2px(this, 10))
                .setDotted(false)
                .setRadius(Utility.dp2px(this, 13))
                .setOnclickListener(new HintView.OnClickCallback() {
                    @Override
                    public void onClickedGuideView() {
                        showMaskHint3();
                    }
                })
                .build().show();

    }

    private void showMaskHint3() {
        View view = getLayoutInflater().inflate(R.layout.main_mask_hint_3, null, false);

        HintView.Builder
                .newInstance(this)
                .setTargetView(iv1)
                .setCustomGuideView(view)
                .setOffset(Utility.dp2px(this, 100), raduis5dp)//偏移量
                .setDirction(HintView.Direction.TOP)//方向
                .setShape(HintView.MyShape.RECTANGULAR)
                .setRadius(raduis5dp)
                .setOnclickListener(new HintView.OnClickCallback() {
                    @Override
                    public void onClickedGuideView() {
                        showMaskHint4();
                    }
                })
                .build().show();

    }

    private void showMaskHint4() {
        View view = getLayoutInflater().inflate(R.layout.main_mask_hint_4, null, false);

        HintView.Builder
                .newInstance(this)
                .setTargetView(iv2)
                .setCustomGuideView(view)
                .setOffset(Utility.dp2px(this, 20), raduis5dp)//偏移量
                .setDirction(HintView.Direction.LEFT_TOP)//方向
                .setShape(HintView.MyShape.RECTANGULAR)
                .setRadius(raduis5dp)
                .setOnclickListener(new HintView.OnClickCallback() {
                    @Override
                    public void onClickedGuideView() {
                        showMaskHint5();
                    }
                })
                .build().show();

    }

    private void showMaskHint5() {
        View view = getLayoutInflater().inflate(R.layout.main_mask_hint_5, null, false);

        HashMap<View, Integer> hashMap = new HashMap<>();
        hashMap.put(iv3, raduis5dp);
        hashMap.put(iv4, raduis5dp);

        HintView.Builder
                .newInstance(this)
                .setTargetView(llSpeed)
                .setMoreTransparentView(hashMap)
                .setCustomGuideView(view)
                .setOffset(Utility.dp2px(this, 36), raduis5dp)//偏移量
                .setDirction(HintView.Direction.TOP)//方向
                .setShape(HintView.MyShape.RECTANGULAR)
                .setRadius(0)
                .setOnclickListener(new HintView.OnClickCallback() {
                    @Override
                    public void onClickedGuideView() {
                        showMaskHint6();
                    }
                })
                .build().show();

    }

    private void showMaskHint6() {
        View view = getLayoutInflater().inflate(R.layout.main_mask_hint_6, null, false);

        HintView.Builder
                .newInstance(this)
                .setTargetView(ll3)
                .setCustomGuideView(view)
                .setOffset(Utility.dp2px(this, 23), raduis5dp)//偏移量
                .setDirction(HintView.Direction.LEFT_TOP)//方向
                .setShape(HintView.MyShape.RECTANGULAR)
                .setRadius(raduis5dp)
                .setOnclickListener(new HintView.OnClickCallback() {
                    @Override
                    public void onClickedGuideView() {
                        isShowing = false;
                    }
                })
                .build().show();
    }
}
