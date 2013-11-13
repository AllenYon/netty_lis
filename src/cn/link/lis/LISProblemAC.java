package cn.link.lis;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

public class LISProblemAC extends Activity {
    ViewGroup mVGContain;
    SortedSet<LISNum> ss = new TreeSet<LISNum>();
    TextView mTvTips;
    TextView mTvDifficulty;
    TextView mTvLevel;

    Button mBtnNext;

    int mBestSolve;
    Level mCurrentLevel;

    boolean firstPlay;
    public static final String KEY_FIRSTPLAY = "first_play";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        if (!ConfigUtils.readBoolean(this, KEY_FIRSTPLAY)) {
            new AlertDialog.Builder(this)
                    .setMessage("需要查看游戏帮助吗?")
                    .setNegativeButton("已经会了", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            ConfigUtils.writeBoolean(LISProblemAC.this, KEY_FIRSTPLAY, true);
                        }
                    }).setPositiveButton("游戏帮助", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    //ToDo
                    Intent intent = new Intent(LISProblemAC.this, How2play.class);
                    startActivity(intent);
                    ConfigUtils.writeBoolean(LISProblemAC.this, KEY_FIRSTPLAY, true);
                }
            }).create().show();
        }

        mVGContain = (ViewGroup) findViewById(R.id.layout_contain);
        mTvDifficulty = (TextView) findViewById(R.id.tv_difficulty);
        mTvTips = (TextView) findViewById(R.id.tv_max);
        mTvLevel = (TextView) findViewById(R.id.tv_level);

//        findViewById(R.id.btn_refresh).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                //ToDo
//                update();
//            }
//        });
        mBtnNext = (Button) findViewById(R.id.btn_next);
        mBtnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //ToDo
                mCurrentLevel = mCurrentLevel.nextLevel();
                update();
            }
        });


        mCurrentLevel = Level.firstLevel();
        update();
    }

    public void update() {
        mVGContain.removeAllViews();
        ss.clear();
        int difficulty = mCurrentLevel.count_num;
        int numMax = mCurrentLevel.max_num;
        Random random = new Random();
        int[] r = new int[difficulty];
        TextView h = new TextView(this);
        h.setTextColor(Color.BLACK);
        h.setBackgroundResource(R.drawable.select_cb_bg);
        h.setGravity(Gravity.CENTER_VERTICAL);
        h.setPadding(8, 8, 8, 8);
        h.setTextSize(mCurrentLevel.text_size);
        h.setText("{");
        mVGContain.addView(h);
        for (int i = 0; i < difficulty; i++) {
            r[i] = random.nextInt(numMax + 1) + 1;
            LISNum num = new LISNum(this);
            num.setTextSize(mCurrentLevel.text_size);
            num.setIndex(i);
            num.setNum(r[i]);
            num.setOnCheckedChangeListener(listener);
            mVGContain.addView(num);
        }
        TextView f = new TextView(this);
        f.setTextColor(Color.BLACK);
        f.setBackgroundResource(R.drawable.select_cb_bg);
        f.setGravity(Gravity.CENTER_VERTICAL);
        f.setPadding(8, 8, 8, 8);
        f.setTextSize(mCurrentLevel.text_size);
        f.setText("}");
        mVGContain.addView(f);

        mBestSolve = lis(r);

        StringBuffer sb = new StringBuffer();
        for (Integer i : suq) {
            sb.append(i.intValue()).append(" ");
        }
        mTvTips.setText(mBestSolve + "");
        mTvDifficulty.setText("难度:" + mCurrentLevel.name);
        mTvTips.setText(sb.toString());
    }

    int[] suq;

    int lis(int A[]) {
        int[] d = new int[A.length];
        int len = 1;
        for (int i = 0; i < A.length; ++i) {
            d[i] = 1;
            for (int j = 0; j < i; ++j) {
//                if (A[j] <= A[i] && d[j] + 1 > d[i])
                if (A[j] < A[i] && d[j] + 1 > d[i]) {
                    d[i] = d[j] + 1;
                }
            }
            if (d[i] > len) {
                len = d[i];
            }
        }
        int tempMax = len;
        suq = new int[len];
        int j = 0;
        for (int i = d.length - 1; i >= 0; i--) {
            if (d[i] == tempMax) {
                suq[j] = A[i];
                tempMax--;
                j++;
            }
        }
        return len;
    }



    public CompoundButton.OnCheckedChangeListener listener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            //ToDo
            LISNum num = (LISNum) buttonView;
            if (isChecked) {
                ss.add(num);
            } else {
                ss.remove(num);

            }
            tvPre();
        }
    };

    private void tvPre() {
        if (ss.size() == mBestSolve) {
            boolean asc = true;
            Iterator<LISNum> it2 = ss.iterator();
            int preNum = it2.next().num;
            while (it2.hasNext() && asc) {
                int cNum = it2.next().num;
                if (preNum >= cNum) {
                    asc = false;
                    break;
                }
                preNum = cNum;
            }

            if (asc) {
                Toast.makeText(this, "win", Toast.LENGTH_LONG).show();
            }
        } else {

        }
    }
}
