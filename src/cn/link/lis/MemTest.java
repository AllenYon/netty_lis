package cn.link.lis;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

public class MemTest extends Activity {

    GridView mGridView;
    MemTestAdapter mAdapter;

    Button mTvLevel;
    Button mBtnPrepare;
    TextView mTvWin;

    enum GameState {
        Prepare, RememberTheNum, HideTheNum
    }

    int winCount;
    int loseCount;
    int currentClick;
    GameState gameState;
    Level mCurrentLevel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ac_memtest);
        mGridView = (GridView) findViewById(R.id.gridview);
        mTvWin = (TextView) findViewById(R.id.tv_win);
        mTvLevel = (Button) findViewById(R.id.btn_level);
        mTvLevel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String[] items = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15"};
                new AlertDialog.Builder(MemTest.this)
                        .setTitle("选择难度")
                        .setIcon(R.drawable.ic_launcher)
                        .setItems(items, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //ToDo
                                mCurrentLevel = Level.selectLevel(which);
                                winCount = 0;
                                loseCount = 0;
                                gameState = GameState.Prepare;
                                start();
                            }
                        })
                        .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //ToDo
                                dialog.dismiss();
                            }
                        })
                        .create().show();
            }
        });
        mBtnPrepare = (Button) findViewById(R.id.btn_prepare);
        mBtnPrepare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //ToDo
                if (gameState == GameState.Prepare) {
                    gameState = GameState.RememberTheNum;
                    start();
                }
            }
        });


        mAdapter = new MemTestAdapter();
        mGridView.setAdapter(mAdapter);
        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //ToDo
                if (gameState == GameState.RememberTheNum) {
                    return;
                }
                int clickValue = mAdapter.getItem(position).intValue();
                if (clickValue != 0) {
                    if (clickValue == mAdapter.sortedSuq[currentClick]) {
                        currentClick++;
                        mAdapter.suq[position] = 0;
                        view.findViewById(R.id.tv).setVisibility(View.INVISIBLE);

                        if (currentClick == mCurrentLevel.count_num) {
                            winCount++;
                            gameState = GameState.Prepare;
                            start();
                        }
                    } else {
                        loseCount++;
                        gameState = GameState.Prepare;
                        start();
                    }
                }

            }
        });

        init();

    }

    public void init() {
        winCount = 0;
        loseCount = 0;
        mCurrentLevel = Level.firstLevel();
        gameState = GameState.Prepare;

        start();

    }

    public void start() {
        switch (gameState) {
            case Prepare: {
                mBtnPrepare.setVisibility(View.VISIBLE);
                int[] suq = new int[35];
                Arrays.fill(suq, 0);
                mAdapter.suq = suq;
                mAdapter.notifyDataSetChanged();
            }
            break;
            case RememberTheNum:
                mBtnPrepare.setVisibility(View.GONE);

                int[] suq = new int[35];
                Arrays.fill(suq, 0);

                int[] aa = randomCommon(mCurrentLevel.min, mCurrentLevel.max, mCurrentLevel.count_num);
                int[] sortedaa = Arrays.copyOf(aa, aa.length);
                Arrays.sort(sortedaa);

                for (int i = 0; i < mCurrentLevel.count_num; i++) {
                    Random random = new Random();
                    int randomIndex = 0;
                    int indexValue = 0;
                    do {
                        randomIndex = random.nextInt(35);
                        indexValue = suq[randomIndex];
                    }
                    while (indexValue != 0);
//                    suq[randomIndex] = i + 1;
                    suq[randomIndex] = aa[i];
                }
                mAdapter.sortedSuq = sortedaa;
                mAdapter.suq = suq;
                mAdapter.notifyDataSetChanged();
                currentClick = 0;
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        //ToDo
                        gameState = GameState.HideTheNum;
                        mAdapter.notifyDataSetChanged();
                    }
                }, mCurrentLevel.delay_hide_the_num);

                break;
            case HideTheNum:

                break;
        }


        //Update Views
        String str = "%d/%d";
        mTvWin.setText(String.format(str, winCount, winCount + loseCount));
        mTvLevel.setText("Level: " + mCurrentLevel.level);

    }


    class MemTestAdapter extends BaseAdapter {
        int[] suq;
        int[] sortedSuq;
        int interference;

        Random mRandom;

        MemTestAdapter() {
            mRandom = new Random();
        }

        @Override
        public int getCount() {
            return suq == null ? 0 : suq.length;  //ToDo
        }

        @Override
        public Integer getItem(int position) {
            return suq[position];  //ToDo
        }

        @Override
        public long getItemId(int position) {
            return 0;  //ToDo
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = View.inflate(MemTest.this, R.layout.cell_memtest, null);
            TextView tv = (TextView) view.findViewById(R.id.tv);
            int value = suq[position];
            if (value == 0) {
                tv.setVisibility(View.INVISIBLE);
            } else {
                if (gameState == GameState.RememberTheNum) {
                    tv.setText(value + "");
                    if (mCurrentLevel.interference && value == interference) {
                        // 添加干扰因素
                        if (value > 1) {
                            int r = mRandom.nextInt(value - 1) + 1;
                            int sub = value - r;
                            tv.setTextSize(30);
                            tv.setText(r + "+" + sub);
                        }
                    }
                } else if (gameState == GameState.HideTheNum) {
                    tv.setText("");
                    tv.setBackgroundColor(Color.BLACK);
                } else if (gameState == GameState.Prepare) {
                    tv.setText(value + "");
                }
            }

            return view;  //ToDo
        }

        @Override
        public void notifyDataSetChanged() {
            super.notifyDataSetChanged();
            interference = mRandom.nextInt(mCurrentLevel.count_num - 2) + 2;
        }
    }

    public static class Level {
        int level;
        int delay_hide_the_num;
        int min;
        int max;
        int count_num;
        int count_round;
        boolean interference;

        public Level(int level, int delay_hide_the_num, int min, int max, int count_num, int count_round) {
            this.level = level;
            this.delay_hide_the_num = delay_hide_the_num;
            this.min = min;
            this.max = max;
            this.count_num = count_num;
            this.count_round = count_round;
        }

        public Level(int level, int delay_hide_the_num, int min, int max, int count_num, int count_round, boolean interference) {
            this(level, delay_hide_the_num, min, max, count_num, count_round);
            this.interference = interference;
        }

        static ArrayList<Level> sLevels;

        static {
            sLevels = gen();
        }

        public static ArrayList<Level> gen() {
            ArrayList<Level> levels = new ArrayList<Level>();
            levels.add(new Level(0, 1000, 1, 6, 5, 4));
            levels.add(new Level(1, 500, 1, 6, 5, 4));
            levels.add(new Level(2, 1000, 1, 8, 7, 4));
            levels.add(new Level(3, 500, 1, 8, 7, 4));
            levels.add(new Level(4, 1000, 1, 9, 8, 4));
            levels.add(new Level(5, 700, 1, 9, 8, 8));
            levels.add(new Level(6, 500, 1, 9, 8, 8));
            levels.add(new Level(7, 350, 1, 9, 8, 8));
            levels.add(new Level(8, 1000, 1, 10, 9, 4));
            levels.add(new Level(9, 700, 1, 10, 9, 4));
            levels.add(new Level(10, 500, 1, 10, 9, 4));
            levels.add(new Level(11, 350, 1, 10, 9, 4));
            levels.add(new Level(12, 1000, 1, 16, 8, 4));
            levels.add(new Level(13, 700, 1, 16, 8, 4));
            levels.add(new Level(14, 500, 1, 16, 8, 4));
            levels.add(new Level(15, 350, 1, 16, 8, 4));
            return levels;
        }


        public static Level firstLevel() {
            return sLevels.get(0);
        }

        public Level nextLevel() {
            return sLevels.get(level + 1);
        }

        public static Level selectLevel(int which) {
            return sLevels.get(which);
        }
    }


    /**
     * 随机指定范围内N个不重复的数
     * 最简单最基本的方法
     *
     * @param min 指定范围最小值
     * @param max 指定范围最大值
     * @param n   随机数个数
     */
    public static int[] randomCommon(int min, int max, int n) {
        if (n > (max - min + 1) || max < min) {
            return null;
        }
        int[] result = new int[n];
        int count = 0;
        while (count < n) {
            int num = (int) (Math.random() * (max - min)) + min;
            boolean flag = true;
            for (int j = 0; j < n; j++) {
                if (num == result[j]) {
                    flag = false;
                    break;
                }
            }
            if (flag) {
                result[count] = num;
                count++;
            }
        }
        return result;
    }
}
