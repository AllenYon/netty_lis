package cn.link.lis;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

/**
 * Created with IntelliJ IDEA.
 * User: Link
 * Date: 13-6-5
 * Time: PM4:43
 * To change this template use File | Settings | File Templates.
 */
public abstract class AbsBaseAdapter<T> extends BaseAdapter {

    protected abstract int getCellRid();

    protected abstract T getViewHolder(View view);

    protected abstract void onGetView(int position, T holder);


    protected Context mContext;

    public AbsBaseAdapter(Context mContext) {
        this.mContext = mContext;
    }



    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = null;
        if (convertView == null) {
            view = View.inflate(mContext, getCellRid(), null);
            T holder = getViewHolder(view);
            view.setTag(holder);
        } else {
            view = convertView;
        }
        T holder = (T) view.getTag();

        onGetView(position, holder);


        return view;  //ToDo
    }

}
