package net.fengg.app.activity;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import net.fengg.app.model.History;
import net.fengg.app.R;

import java.util.List;

/**
 * @author zhangfeng_2017
 * @date 2018/1/5
 */

public class HistoryAdapter extends BaseAdapter {
    private List<History> datas;
    Context context;

    HistoryAdapter(Context context, List<History> list) {
        this.context = context;
        this.datas = list;
    }

    @Override
    public int getCount() {
        return datas.size();
    }

    @Override
    public Object getItem(int i) {
        return datas.get(i);
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    public void setDatas(List<History> datas) {
        this.datas = datas;
        notifyDataSetChanged();
    }

    @Override
    public View getView(int i, View convertView, ViewGroup viewGroup) {
        ViewHolder holder = null;
        if (convertView == null) {
            holder=new ViewHolder();
            convertView = LayoutInflater.from(context).inflate(R.layout.item_eat, null);
            holder.txt_start = convertView.findViewById(R.id.txt_start);
            holder.txt_end = convertView.findViewById(R.id.txt_end);
            holder.txt_status = convertView.findViewById(R.id.txt_status);
            convertView.setTag(holder);
        }else {
            holder = (ViewHolder)convertView.getTag();
        }
        History history = datas.get(i);
        holder.txt_start.setText(TextUtils.isEmpty(history.getStr1()) ? "" : history.getStr1());
        holder.txt_end.setText(TextUtils.isEmpty(history.getStr2()) ? "" : history.getStr2());
        holder.txt_status.setText(TextUtils.isEmpty(history.getStr3()) ? "" : history.getStr3());

        return convertView;
    }

    class ViewHolder{
        TextView txt_start;
        TextView txt_end;
        TextView txt_status;
    }
}
