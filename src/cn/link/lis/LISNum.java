package cn.link.lis;

import android.content.Context;
import android.graphics.Color;
import android.view.Gravity;
import android.widget.CheckBox;

public class LISNum extends CheckBox implements Comparable<LISNum> {
    public LISNum(Context context) {
        super(context);
        setTextColor(Color.BLACK);
        setButtonDrawable(R.drawable.translate);
        setBackgroundResource(R.drawable.select_cb_bg);
        setGravity(Gravity.CENTER_VERTICAL);
        setPadding(8, 8, 8, 8);

    }

    int index;
    int num;

    public void setIndex(int i) {
        this.index = i;
    }

    public void setNum(int s) {
        this.num = s;
        setText(String.valueOf(s));
    }

    @Override
    public int compareTo(LISNum another) {
        if (index < another.index) {
            return -1;
        } else if (index == another.index) {
            return 0;
        } else {
            return 1;
        }
    }
}