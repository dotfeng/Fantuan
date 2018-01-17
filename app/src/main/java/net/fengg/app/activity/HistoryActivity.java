package net.fengg.app.activity;

import android.content.DialogInterface;
import android.graphics.Color;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.ColorTemplate;

import net.fengg.app.AppApplication;
import net.fengg.app.model.Body;
import net.fengg.app.tool.Util;
import net.fengg.app.model.EatMilk;
import net.fengg.app.model.History;
import net.fengg.app.model.Shit;
import net.fengg.app.model.Sleep;
import net.fengg.app.R;

import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.objectbox.Box;
import io.objectbox.BoxStore;

public class HistoryActivity extends AppCompatActivity implements OnChartValueSelectedListener {

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.lv_history)
    ListView lv_history;
    @BindView(R.id.chart1)
    LineChart mChart;

    Box<EatMilk> milkBox;
    Box<Shit> shitBox;
    Box<Sleep> sleepBox;
    Box<Body> bodyBox;

    List<History> datas = new ArrayList<>();
    HistoryAdapter adapter;

    Menu menu;

    int itemSelect = 2;
    boolean istatistics = false;

    String dateTimeFormat = "yyyy/MM/dd\nHH:mm:ss";
    String dateFormat = "yyyy/MM/dd";
    String timeFormat = "HH:mm:ss";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);
        ActionBar ab = getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);

        BoxStore boxStore = ((AppApplication) getApplication()).getBoxStore();
        milkBox = boxStore.boxFor(EatMilk.class);
        shitBox = boxStore.boxFor(Shit.class);
        sleepBox = boxStore.boxFor(Sleep.class);
        bodyBox = boxStore.boxFor(Body.class);
        mChart.setOnChartValueSelectedListener(this);

        // no description text
        mChart.getDescription().setEnabled(false);

        // enable touch gestures
        mChart.setTouchEnabled(true);

        mChart.setDragDecelerationFrictionCoef(0.9f);

        // enable scaling and dragging
        mChart.setDragEnabled(true);
        mChart.setScaleEnabled(true);
        mChart.setDrawGridBackground(false);
        mChart.setHighlightPerDragEnabled(true);
//        mChart.setMaxVisibleValueCount(3);
        // if disabled, scaling can be done on x- and y-axis separately
        mChart.setPinchZoom(true);

        // set an alternative background color
//        mChart.setBackgroundColor(Color.LTGRAY);
        mChart.setNoDataText(getString(R.string.no_data));
        mChart.animateX(1500);
        getMilk();
        adapter = new HistoryAdapter(this, datas);
        lv_history.setAdapter(adapter);
        lv_history.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(final AdapterView<?> adapterView, View view, final int position, long id) {
                if(istatistics) {
                    return false;
                }
                new AlertDialog.Builder(HistoryActivity.this)
                        .setTitle(R.string.tip)
                        .setMessage(R.string.bedelete)
                        .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                        .setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                History history = datas.get(position);
                                switch (itemSelect) {
                                    case 0:
                                        shitBox.remove(history.getId());
                                        getShit();
                                        adapter.notifyDataSetChanged();
                                        break;
                                    case 1:
                                        sleepBox.remove(history.getId());
                                        getSleep();
                                        adapter.notifyDataSetChanged();
                                        break;
                                    case 2:
                                        milkBox.remove(history.getId());
                                        getMilk();
                                        adapter.notifyDataSetChanged();
                                        break;
//                                    case 3:
//                                        bodyBox.remove(history.getId());
//                                        getWeight();
//                                        adapter.notifyDataSetChanged();
//                                        break;
                                    default:
                                        break;
                                }

                            }
                        })
                        .create().show();
                return false;
            }
        });
    }

    private void setData() {
        List<Body> bodyList = bodyBox.getAll();
        Collections.sort(bodyList);
        Collections.reverse(bodyList);
        LinkedHashMap<String, List<Body>> bodyMap = new LinkedHashMap<>();
        SimpleDateFormat df = new SimpleDateFormat(dateFormat);
        List<String> dateList = new ArrayList<>();
        for(int i = 0; i < bodyList.size();) {
            List<Body> bodies = new ArrayList<>();
            Body body = bodyList.get(i);
            bodies.add(body);
            String date1 = df.format(body.getTime());
            boolean inFor = false;
            for(int j = i + 1; j < bodyList.size(); j++) {
                inFor = true;
                Body body1 = bodyList.get(j);
                String date2 = df.format(body1.getTime());
                if(date1.equals(date2)) {
                    bodies.add(body1);
                    i = j + 1;
                } else {
                    i++;
                    break;
                }
            }
            if(!dateList.contains(date1)) {
                dateList.add(date1);
            }
            bodyMap.put(date1, bodies);
            if(!inFor) {
                break;
            }
        }
        if(null == dateList || 0 == dateList.size()) {
            return;
        }
        ArrayList<Entry> yVals1 = new ArrayList<>();
        ArrayList<Entry> yVals2 = new ArrayList<>();
        ArrayList<Entry> yVals3 = new ArrayList<>();
        float maxWeight = 0;
        float maxHeight = 0;

        for(String date : dateList) {
            List<Body> bodies = bodyMap.get(date);
            if(null != bodies && 0 != bodies.size()) {
                float weight = 0;
                float height = 0;
                float temperature = 0;
                for (Body body : bodies) {
                    weight += body.getWeight();
                    height += body.getHeight();
                    temperature += body.getTemperature();
                    if(maxWeight < body.getWeight()) {
                        maxWeight = body.getWeight();
                    }
                    if(maxHeight < body.getHeight()) {
                        maxHeight = body.getHeight();
                    }
                }

                float x = TimeUnit.MILLISECONDS.toDays(bodies.get(0).getTime());

                Entry entry1 = new Entry(x, height / bodies.size());
                Entry entry2 = new Entry(x, weight / bodies.size());
                Entry entry3 = new Entry(x, temperature / bodies.size());
                entry1.setData(bodies.get(0));
                entry2.setData(bodies.get(0));
                entry3.setData(bodies.get(0));
                yVals1.add(entry1);
                yVals2.add(entry2);
                yVals3.add(entry3);
            }
        }
//        Collections.sort(yVals1, new EntryXComparator());
//        Collections.sort(yVals2, new EntryXComparator());

        // get the legend (only possible after setting data)
        Legend l = mChart.getLegend();

        // modify the legend ...
        l.setForm(Legend.LegendForm.LINE);
        l.setTextSize(14f);
        l.setTextColor(Color.BLACK);
        l.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        l.setHorizontalAlignment(Legend.LegendHorizontalAlignment.LEFT);
        l.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        l.setDrawInside(false);
//        l.setYOffset(11f);

        XAxis xAxis = mChart.getXAxis();
        xAxis.setTextSize(11f);
        xAxis.setTextColor(Color.BLACK);
        xAxis.setDrawGridLines(true);
//        xAxis.setDrawAxisLine(true);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
//        xAxis.setValueFormatter(custom);
        xAxis.setGranularity(1);
//        xAxis.setGranularityEnabled(true);
        xAxis.setAvoidFirstLastClipping(true);
//        xAxis.setAxisMinimum(0);
        xAxis.setValueFormatter(new IAxisValueFormatter() {

            private SimpleDateFormat mFormat = new SimpleDateFormat("MM/dd");

            @Override
            public String getFormattedValue(float value, AxisBase axis) {

                long millis = TimeUnit.DAYS.toMillis((long) value+1);
                return mFormat.format(new Date(millis));
            }
        });
//        Calendar calendar = Calendar.getInstance();
//        calendar.setTimeInMillis(weightHeightList.get(0).getTime());
//        int day = calendar.get(Calendar.DATE);
//        calendar.set(Calendar.DATE, day - 1);
//
//        xAxis.setAxisMinimum(calendar.getTimeInMillis());

        YAxis leftAxis = mChart.getAxisLeft();
        leftAxis.setTextColor(Color.BLACK);
        leftAxis.setAxisMaximum(maxHeight+10);
        leftAxis.setAxisMinimum(1f);
//        leftAxis.setDrawAxisLine(true);
        leftAxis.setDrawGridLines(false);
        leftAxis.setGranularityEnabled(true);
        leftAxis.setXOffset(10);

        YAxis rightAxis = mChart.getAxisRight();
        rightAxis.setTextColor(Color.RED);
        rightAxis.setAxisMaximum(maxWeight+10);
        rightAxis.setAxisMinimum(1);
//        rightAxis.setDrawAxisLine(true);
        rightAxis.setDrawGridLines(false);
//        rightAxis.setDrawZeroLine(true);
        rightAxis.setGranularityEnabled(false);
        rightAxis.setXOffset(10);

        LineDataSet set1, set2, set3;

        if (mChart.getData() != null &&
                mChart.getData().getDataSetCount() > 0) {
            set1 = (LineDataSet) mChart.getData().getDataSetByIndex(0);
            set2 = (LineDataSet) mChart.getData().getDataSetByIndex(1);
            set3 = (LineDataSet) mChart.getData().getDataSetByIndex(2);
            set1.setValues(yVals1);
            set2.setValues(yVals2);
            set3.setValues(yVals3);
            mChart.getData().notifyDataChanged();
            mChart.notifyDataSetChanged();
        } else {
            // create a dataset and give it a type
            set1 = new LineDataSet(yVals1, getString(R.string.height));

            set1.setAxisDependency(YAxis.AxisDependency.LEFT);
            set1.setColor(ColorTemplate.getHoloBlue());
            set1.setCircleColor(Color.BLACK);
            set1.setLineWidth(2f);
            set1.setCircleRadius(3f);
            set1.setFillAlpha(65);
            set1.setFillColor(ColorTemplate.getHoloBlue());
            set1.setHighLightColor(Color.rgb(244, 117, 117));
            set1.setDrawCircleHole(true);
            //set1.setFillFormatter(new MyFillFormatter(0f));
            //set1.setDrawHorizontalHighlightIndicator(false);
            //set1.setVisible(false);
            //set1.setCircleHoleColor(Color.WHITE);

            // create a dataset and give it a type
            set2 = new LineDataSet(yVals2, getString(R.string.weight));
            set2.setAxisDependency(YAxis.AxisDependency.RIGHT);
            set2.setColor(Color.GREEN);
            set2.setCircleColor(Color.BLACK);
            set2.setLineWidth(2f);
            set2.setCircleRadius(3f);
            set2.setFillAlpha(65);
            set2.setFillColor(Color.GREEN);
            set2.setDrawCircleHole(true);
            set2.setHighLightColor(Color.rgb(244, 117, 117));
            //set2.setFillFormatter(new MyFillFormatter(900f));

            set3 = new LineDataSet(yVals3, getString(R.string.temperature));
            set3.setAxisDependency(YAxis.AxisDependency.LEFT);
            set3.setColor(Color.RED);
            set3.setCircleColor(Color.BLACK);
            set3.setLineWidth(2f);
            set3.setCircleRadius(3f);
            set3.setFillAlpha(65);
            set3.setFillColor(Color.RED);
            set3.setDrawCircleHole(true);
            set3.setHighLightColor(Color.rgb(244, 117, 117));

            // create a data object with the datasets
            LineData data = new LineData(set1, set2, set3);
            data.setValueTextColor(Color.BLACK);
            data.setValueTextSize(11f);

            // set data
            mChart.setData(data);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_history, menu);
        this.menu = menu;
        return true;
    }

    @Override
    public boolean onMenuOpened(int featureId, Menu menu) {
        if (menu != null) {
            if (menu.getClass().getSimpleName().equalsIgnoreCase("MenuBuilder")) {
                try {
                    Method method = menu.getClass().getDeclaredMethod("setOptionalIconsVisible", Boolean.TYPE);
                    method.setAccessible(true);
                    method.invoke(menu, true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return super.onMenuOpened(featureId, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        MenuItem eat = menu.findItem(R.id.action_eat);
        MenuItem sleep = menu.findItem(R.id.action_sleep);
        MenuItem shit = menu.findItem(R.id.action_shit);
        MenuItem body = menu.findItem(R.id.action_body);
        MenuItem statistics = menu.findItem(R.id.action_statistics);
        switch (item.getItemId()) {
            case R.id.action_shit:
                itemSelect = 0;
                istatistics = false;
                item.setChecked(true);
                sleep.setChecked(false);
                eat.setChecked(false);
                body.setChecked(false);
                break;
            case R.id.action_sleep:
                itemSelect = 1;
                istatistics = false;
                item.setChecked(true);
                shit.setChecked(false);
                eat.setChecked(false);
                body.setChecked(false);
                break;
            case R.id.action_eat:
                itemSelect = 2;
                istatistics = false;
                item.setChecked(true);
                shit.setChecked(false);
                sleep.setChecked(false);
                body.setChecked(false);
                break;
            case R.id.action_body:
                itemSelect = 3;
                istatistics = false;
                item.setChecked(true);
                shit.setChecked(false);
                sleep.setChecked(false);
                eat.setChecked(false);
                break;
            case R.id.action_statistics:
                istatistics = !istatistics;
                shit.setChecked(false);
                sleep.setChecked(false);
                body.setChecked(false);
                if(istatistics) {
                    eat.setChecked(false);
                } else {
                    eat.setChecked(true);
                }
            default:
                break;
        }
        statistics.setChecked(istatistics);
        eat.setIcon(eat.isChecked() ? R.drawable.ic_emoticon_tongue_black_48dp :
                R.drawable.ic_emoticon_tongue_grey600_48dp);
        shit.setIcon(shit.isChecked() ? R.drawable.icons8_poo_black_96 :
                R.drawable.icons8_poo_grey_96);
        sleep.setIcon(sleep.isChecked() ? R.drawable.ic_sleep_black_48dp :
                R.drawable.ic_sleep_grey600_48dp);
        body.setIcon(body.isChecked() ? R.drawable.icons8_weightlifting_black :
                R.drawable.icons8_weightlifting_grey);
        lv_history.setVisibility(View.VISIBLE);
        mChart.setVisibility(View.GONE);
        if(statistics.isChecked()) {
            statistics.setIcon(R.drawable.ic_chart_bar_black_48dp);
            showStatistics();
        } else {
            statistics.setIcon(R.drawable.ic_chart_bar_white_48dp);
            if(eat.isChecked()) {
                getMilk();
                adapter.notifyDataSetChanged();
            }
            if(sleep.isChecked()) {
                getSleep();
                adapter.notifyDataSetChanged();
            }
            if(shit.isChecked()) {
                getShit();
                adapter.notifyDataSetChanged();
            }
            if(body.isChecked()) {
//                getWeight();
//                adapter.notifyDataSetChanged();
                lv_history.setVisibility(View.GONE);
                mChart.setVisibility(View.VISIBLE);
                setData();
            }
        }

        return super.onOptionsItemSelected(item);
    }

    private void showStatistics() {
        datas.clear();
        List<EatMilk> eatMilkList = milkBox.getAll();
        List<Sleep> sleepList = sleepBox.getAll();
        List<Shit> shitList = shitBox.getAll();
        List<Body> bodyList = bodyBox.getAll();
        Collections.sort(eatMilkList);
        Collections.sort(sleepList);
        Collections.sort(shitList);
        Collections.sort(bodyList);
        SimpleDateFormat df = new SimpleDateFormat(dateFormat);

        List<String> dateList = new ArrayList<>();

        LinkedHashMap<String, List<EatMilk>> milkMap = new LinkedHashMap<>();
        for(int i = 0; i < eatMilkList.size();) {
            List<EatMilk> eatL = new ArrayList<>();
            List<EatMilk> eatR = new ArrayList<>();
            List<EatMilk> eatML = new ArrayList<>();
            EatMilk eatMilk = eatMilkList.get(i);
            if(0 != eatMilk.getMl()) {
                eatML.add(eatMilk);
            } else {
                if(eatMilk.isLeft()) {
                    eatL.add(eatMilk);
                } else {
                    eatR.add(eatMilk);
                }
            }
            String date1 = df.format(eatMilk.getStart());
            boolean inFor = false;
            for(int j = i + 1; j < eatMilkList.size(); j++) {
                inFor = true;
                EatMilk milk = eatMilkList.get(j);
                String date2 = df.format(milk.getStart());
                if(date1.equals(date2)) {
                    if(0 != milk.getMl()) {
                        eatML.add(milk);
                    } else {
                        if(milk.isLeft()) {
                            eatL.add(milk);
                        } else {
                            eatR.add(milk);
                        }
                    }
                    i = j + 1;
                } else {
                    i++;
                    break;
                }
            }
            if(!dateList.contains(date1)) {
                dateList.add(date1);
            }
            milkMap.put(date1 + "l", eatL);
            milkMap.put(date1 + "r", eatR);
            milkMap.put(date1 + "ml", eatML);
            if(!inFor) {
                break;
            }
        }

        LinkedHashMap<String, List<Sleep>> sleepMap = new LinkedHashMap<>();
        for(int i = 0; i < sleepList.size();) {
            List<Sleep> sleepL = new ArrayList<>();
            Sleep sleep = sleepList.get(i);
            if(sleep.isSleep()) {
                sleepL.add(sleep);
            }
            String date1 = df.format(sleep.getTime());
            boolean inFor = false;
            for(int j = i + 1; j < sleepList.size(); j++) {
                inFor = true;
                Sleep sleep1 = sleepList.get(j);
                String date2 = df.format(sleep1.getTime());
                if(date1.equals(date2)) {
                    if(sleep1.isSleep()) {
                        sleepL.add(sleep1);
                    }
                    i = j + 1;
                } else {
                    i++;
                    break;
                }
            }
            if(!dateList.contains(date1)) {
                dateList.add(date1);
            }
            sleepMap.put(date1, sleepL);
            if(!inFor) {
                break;
            }
        }

        LinkedHashMap<String, List<Shit>> shitMap = new LinkedHashMap<>();
        for(int i = 0; i < shitList.size();) {
            List<Shit> shitL = new ArrayList<>();
            Shit shit = shitList.get(i);
            shitL.add(shit);
            String date1 = df.format(shit.getTime());
            boolean inFor = false;
            for(int j = i + 1; j < shitList.size(); j++) {
                inFor = true;
                Shit shit1 = shitList.get(j);
                String date2 = df.format(shit1.getTime());
                if(date1.equals(date2)) {
                    shitL.add(shit1);
                    i = j + 1;
                } else {
                    i++;
                    break;
                }
            }
            if(!dateList.contains(date1)) {
                dateList.add(date1);
            }
            shitMap.put(date1, shitL);
            if(!inFor) {
                break;
            }
        }

        LinkedHashMap<String, List<Body>> bodyMap = new LinkedHashMap<>();
        for(int i = 0; i < bodyList.size();) {
            List<Body> bodies = new ArrayList<>();
            Body body = bodyList.get(i);
            bodies.add(body);
            String date1 = df.format(body.getTime());
            boolean inFor = false;
            for(int j = i + 1; j < bodyList.size(); j++) {
                inFor = true;
                Body body1 = bodyList.get(j);
                String date2 = df.format(body1.getTime());
                if(date1.equals(date2)) {
                    bodies.add(body1);
                    i = j + 1;
                } else {
                    i++;
                    break;
                }
            }
            if(!dateList.contains(date1)) {
                dateList.add(date1);
            }
            bodyMap.put(date1, bodies);
            if(!inFor) {
                break;
            }
        }

        for(String date : dateList) {
            History history = new History();
            List<EatMilk> eatL = milkMap.get(date + "l");
            List<EatMilk> eatR = milkMap.get(date + "r");
            List<EatMilk> eatML = milkMap.get(date + "ml");
            long sum = 0;

            if(null != eatL && 0 != eatL.size()) {
                for (EatMilk eatMilk : eatL) {
                    sum += eatMilk.getDuration();
                }

                history.setStr1(date);
                history.setStr2(Util.formatTime(sum / eatL.size()));
                history.setStr3(getString(R.string.left));
                datas.add(history);
            }

            if(null != eatR && 0 != eatR.size()) {
                sum = 0;
                for (EatMilk eatMilk : eatR) {
                    sum += eatMilk.getDuration();
                }
                history = new History();
                history.setStr1(date);
                history.setStr2(Util.formatTime(sum / eatR.size()));
                history.setStr3(getString(R.string.right));
                datas.add(history);
            }

            if(null != eatML && 0 != eatML.size()) {
                sum = 0;
                for (EatMilk eatMilk : eatML) {
                    sum += eatMilk.getMl();
                }
                history = new History();
                history.setStr1(date);
                history.setStr2(eatML.size() + "次");
                history.setStr3(sum / eatML.size() + "ml");
                datas.add(history);
            }

            List<Sleep> sleeps = sleepMap.get(date);
            if(null != sleeps && 0 != sleeps.size()) {
                sum = 0;
                for (Sleep sleep : sleeps) {
                    sum += sleep.getDuration();
                }
                history = new History();
                history.setStr1(date);
                history.setStr2(Util.formatTime(sum / sleeps.size()));
                history.setStr3(sleeps.size() + "睡");
                datas.add(history);
            }

            List<Shit> shits = shitMap.get(date);
            if(null != shits && 0 != shits.size()) {
                sum = 0;
                for (Shit shit : shits) {
                    sum += shit.getDuration();
                }
                history = new History();
                history.setStr1(date);
                history.setStr2(Util.formatTime(sum / shits.size()));
                history.setStr3(shits.size() + "拉");
                datas.add(history);
            }

            List<Body> bodies = bodyMap.get(date);
            if(null != bodies && 0 != bodies.size()) {
                float weight = 0;
                float height = 0;
                float temperature = 0;
                for (Body body : bodies) {
                    weight += body.getWeight();
                    height += body.getHeight();
                    temperature += body.getTemperature();
                }
                history = new History();
                history.setStr1(date);
                history.setStr2(weight / bodies.size() + "kg," + height / bodies.size() + "cm");
                history.setStr3(temperature / bodies.size() + getString(R.string.celsius));
                datas.add(history);
            }
        }
        adapter.notifyDataSetChanged();
    }

    private void getMilk() {
        List<EatMilk> list = milkBox.getAll();
        Collections.sort(list);
        datas.clear();
        SimpleDateFormat df = new SimpleDateFormat(dateTimeFormat);
        for(EatMilk milk : list) {
            History history = new History();
            history.setId(milk.getId());
            history.setStr1(df.format(milk.getStart()));
            if(milk.getMl() != 0) {
                history.setStr3(milk.getMl() + "ml");
            } else {
                history.setStr2(Util.formatTime(milk.getDuration()));
                history.setStr3(getString(milk.isLeft() ? R.string.left : R.string.right));
            }
            datas.add(history);
        }
    }

    private void getShit() {
        List<Shit> list = shitBox.getAll();
        Collections.sort(list);
        datas.clear();
        SimpleDateFormat df = new SimpleDateFormat(dateTimeFormat);
        for(Shit shit : list) {
            History history = new History();
            history.setId(shit.getId());
            history.setStr1(df.format(shit.getTime()));
            history.setStr2(Util.formatTime(shit.getDuration()));
            history.setStr3(shit.getStatus());
            datas.add(history);
        }
    }

    private void getSleep() {
        List<Sleep> list = sleepBox.getAll();
        Collections.sort(list);
        datas.clear();
        SimpleDateFormat df = new SimpleDateFormat(dateTimeFormat);
        for(Sleep sleep : list) {
            History history = new History();
            history.setId(sleep.getId());
            history.setStr1(df.format(sleep.getTime()));
            history.setStr2(Util.formatTime(sleep.getDuration()));
            history.setStr3(getString(sleep.isSleep() ? R.string.sleep : R.string.wake));
            datas.add(history);
        }
    }

    private void getWeight() {
        List<Body> list = bodyBox.getAll();
        Collections.sort(list);
        datas.clear();
        SimpleDateFormat df = new SimpleDateFormat(dateTimeFormat);
        for(Body weightHeight : list) {
            History history = new History();
            history.setId(weightHeight.getId());
            history.setStr1(df.format(weightHeight.getTime()));
            history.setStr2(weightHeight.getHeight() + "cm");
            history.setStr3(weightHeight.getWeight() + "kg");
            datas.add(history);
        }
    }

    @Override
    public void onValueSelected(Entry e, Highlight h) {
        SimpleDateFormat df = new SimpleDateFormat(dateTimeFormat, Locale.CHINA);
        Body weightHeight = (Body) e.getData();
        Toast.makeText(this, df.format(weightHeight.getTime()) + "\n" + e.getY(), Toast.LENGTH_LONG).show();
    }

    @Override
    public void onNothingSelected() {

    }
}
