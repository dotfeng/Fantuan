package net.fengg.app.activity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.TimePicker;

import net.fengg.app.AppApplication;
import net.fengg.app.model.Body;
import net.fengg.app.model.Shit_;
import net.fengg.app.model.Sleep_;
import net.fengg.app.tool.Util;
import net.fengg.app.model.EatMilk;
import net.fengg.app.model.History;
import net.fengg.app.model.Shit;
import net.fengg.app.model.Sleep;
import net.fengg.app.model.EatMilk_;
import net.fengg.app.R;
import net.fengg.app.tool.UtilLog;
import net.fengg.tag.FlowTagLayout;
import net.fengg.tag.OnTagSelectListener;

import java.lang.ref.WeakReference;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.objectbox.Box;
import io.objectbox.BoxStore;
import io.objectbox.query.QueryBuilder;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.txt_count_time)
    TextView txt_count_time;
    @BindView(R.id.txt_last_count)
    TextView txt_last_count;
    @BindView(R.id.txt_last_time)
    TextView txt_last_time;
    @BindView(R.id.btn_eat)
    Button btn_eat;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.lv_history)
    ListView lv_history;

    Box<EatMilk> milkBox;
    Box<Sleep> sleepBox;
    Box<Shit> shitBox;
    Box<Body> bodyBox;

    List<History> datas = new ArrayList<>();
    HistoryAdapter adapter;

    private static final String APP = "APP";
    private static final String EAT_START = "EAT_START";
    private static final String EAT_END = "EAT_END";
    private static final String MILK = "MILK";
    private static final String EATING = "EATING";
    private static final String LEFT = "LEFT";

    String dateTimeFormat = "yyyy/MM/dd\nHH:mm:ss";
    String dateFormat = "yyyy/MM/dd";
    String timeFormat = "HH:mm:ss";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);

        BoxStore boxStore = ((AppApplication) getApplication()).getBoxStore();
        milkBox = boxStore.boxFor(EatMilk.class);
        sleepBox = boxStore.boxFor(Sleep.class);
        shitBox = boxStore.boxFor(Shit.class);
        bodyBox = boxStore.boxFor(Body.class);

        adapter = new HistoryAdapter(this, datas);
        lv_history.setAdapter(adapter);
    }

    private void getMilk() {
        SharedPreferences sp = getSharedPreferences(APP, Context.MODE_PRIVATE);
        boolean eating = sp.getBoolean(EATING, false);
        boolean left = sp.getBoolean(LEFT, true);

        QueryBuilder<EatMilk> builder = milkBox.query();
        EatMilk milk = builder.orderDesc(EatMilk_.end).build().findFirst();
        if(null != milk) {
            int ml = milk.getMl();
            if(ml != 0) {
                txt_last_count.setText(ml + "ml");
            } else {
                txt_last_count.setText(milk.isLeft() ? R.string.left : R.string.right);
            }
            SimpleDateFormat df = new SimpleDateFormat(timeFormat, Locale.CHINA);
            txt_last_time.setText(df.format(milk.getEnd()));
        } else {
            txt_last_count.setText("");
            txt_last_time.setText("00:00:00");
            txt_count_time.setText("00:00:00");
        }
        if(eating) {
            btn_eat.setTextSize(32);
            if(left) {
                btn_eat.setText(R.string.now_left);
            } else {
                btn_eat.setText(R.string.now_right);
            }
            btn_eat.setCompoundDrawablesWithIntrinsicBounds(
                    getResources().getDrawable(R.mipmap.ic_pause_grey600_48dp)
                    ,null,null,null);
        } else {
            btn_eat.setText(R.string.eat);
            btn_eat.setTextSize(48);
            btn_eat.setCompoundDrawablesWithIntrinsicBounds(
                    getResources().getDrawable(R.mipmap.ic_play_grey600_48dp)
                    ,null,null,null);
        }
        eatHandler.sendEmptyMessage(0);
//        List<EatMilk> list = milkBox.getAll();
        builder = milkBox.query();
        List<EatMilk> list = builder.orderDesc(EatMilk_.end).build().find(0, 6);

//        Collections.sort(list);
        datas.clear();
        SimpleDateFormat df = new SimpleDateFormat(dateTimeFormat, Locale.CHINA);
        for(EatMilk ml : list) {
            History history = new History();
            history.setId(ml.getId());
            history.setStr1(df.format(ml.getStart()));
            if(ml.getMl() != 0) {
                history.setStr3(ml.getMl() + "ml");
            } else {
                history.setStr2(Util.formatTime(ml.getDuration()));
                history.setStr3(getString(ml.isLeft() ? R.string.left : R.string.right));
            }
            datas.add(history);
        }
        adapter.notifyDataSetChanged();
    }

    @Override
    protected void onResume() {
        super.onResume();
        getMilk();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
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
        switch (item.getItemId()) {
            case R.id.action_add_sleep:
                onMenuAddSleepClick();
                break;
            case R.id.action_add_eat:
                onMenuAddEatClick();
                break;
            case R.id.action_add_shit:
                onMenuAddShitClick();
                break;
            case R.id.action_add_body:
                onMenuAddBodyClick();
                break;
            case R.id.action_shit:
                onMenuShitClick();
                break;
            case R.id.action_sleep:
                onMenuSleepClick();
                break;
            case R.id.action_body:
                onMenuBodyClick();
                break;
            case R.id.action_history:
                startActivity(new Intent(MainActivity.this, HistoryActivity.class));
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void onMenuAddSleepClick() {
        AlertDialog.Builder addSleepDialog =
                new AlertDialog.Builder(MainActivity.this);
        final View addSleepView = LayoutInflater.from(MainActivity.this)
                .inflate(R.layout.dialog_add_sleep, null);
        addSleepDialog.setTitle(R.string.add_sleep);
        addSleepDialog.setView(addSleepView);
        final RadioGroup rg_sleep = addSleepView.findViewById(R.id.rg_sleep);
        RadioButton rb_sleep = addSleepView.findViewById(R.id.rb_sleep);
        RadioButton rb_wake = addSleepView.findViewById(R.id.rb_wake);
        final TextView txt_count_time = addSleepView.findViewById(R.id.txt_count_time);
        final TimePicker tp_start = addSleepView.findViewById(R.id.tp_start);
        final TimePicker tp_end = addSleepView.findViewById(R.id.tp_end);
        tp_start.setIs24HourView(true);
        tp_end.setIs24HourView(true);

        tp_start.setOnTimeChangedListener(new TimePicker.OnTimeChangedListener() {
            @Override
            public void onTimeChanged(TimePicker timePicker, int hourOfDay, int minute) {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                    int endHour = tp_end.getHour();
                    int endMinute = tp_end.getMinute();
                    StringBuilder sb = new StringBuilder();
                    int m = endMinute - minute;
                    int h = endHour - hourOfDay;
                    if(m < 0) {
                        m += 60;
                        h--;
                    }
                    if(h >= 0 && h < 10) {
                        sb.append("0");
                    }
                    sb.append(h);
                    sb.append(":");
                    if(m >= 0 && m < 10) {
                        sb.append("0");
                    }
                    sb.append(m);
                    txt_count_time.setText(sb.toString());
                }
            }
        });

        tp_end.setOnTimeChangedListener(new TimePicker.OnTimeChangedListener() {
            @Override
            public void onTimeChanged(TimePicker timePicker, int hourOfDay, int minute) {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                    int startHour = tp_start.getHour();
                    int startMinute = tp_start.getMinute();
                    StringBuilder sb = new StringBuilder();
                    int m = minute - startMinute;
                    int h = hourOfDay - startHour;
                    if(m < 0) {
                        m += 60;
                        h--;
                    }
                    if(h >= 0 && h < 10) {
                        sb.append("0");
                    }
                    sb.append(h);
                    sb.append(":");
                    if(m >= 0 && m < 10) {
                        sb.append("0");
                    }
                    sb.append(m);
                    txt_count_time.setText(sb.toString());
                }
            }
        });

        addSleepDialog.setPositiveButton(R.string.add, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                    int startHour = tp_start.getHour();
                    int startMinute = tp_start.getMinute();
                    int endHour = tp_end.getHour();
                    int endMinute = tp_end.getMinute();
                    Calendar calendar = Calendar.getInstance();
                    calendar.set(Calendar.HOUR_OF_DAY, endHour);
                    calendar.set(Calendar.MINUTE, endMinute);
                    Sleep sleep =  new Sleep();
                    if(R.id.rb_sleep == rg_sleep.getCheckedRadioButtonId()) {
                        sleep.setSleep(true);
                    }  else {
                        sleep.setSleep(false);
                    }
                    sleep.setTime(calendar.getTimeInMillis());

                    SimpleDateFormat format = new SimpleDateFormat("yyyy:MM:dd HH:mm:ss");
                    UtilLog.debug(format.format(calendar.getTimeInMillis()));
                    long duration = (endMinute - startMinute) * 60 * 1000 +
                            (endHour - startHour) * 60 * 60 *1000;
                    sleep.setDuration(duration);

                    sleepBox.put(sleep);
                }
            }
        });
        addSleepDialog.setNegativeButton(R.string.close, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        addSleepDialog.show();
    }

    private void onMenuAddEatClick() {
        AlertDialog.Builder addEatDialog =
                new AlertDialog.Builder(MainActivity.this);
        final View addEatView = LayoutInflater.from(MainActivity.this)
                .inflate(R.layout.dialog_add_eat, null);
        addEatDialog.setTitle(R.string.add_eat);
        addEatDialog.setView(addEatView);
        final RadioGroup rg_eat = addEatView.findViewById(R.id.rg_eat);
        RadioButton rb_left = addEatView.findViewById(R.id.rb_left);
        RadioButton rb_right = addEatView.findViewById(R.id.rb_right);
        final TextView txt_count_time = addEatView.findViewById(R.id.txt_count_time);
        final TextView txt_to = addEatView.findViewById(R.id.txt_to);
        final EditText et_ml = addEatView.findViewById(R.id.et_ml);
        et_ml.setSelection(et_ml.getText().length());
        final TimePicker tp_start = addEatView.findViewById(R.id.tp_start);
        final TimePicker tp_end = addEatView.findViewById(R.id.tp_end);

        tp_start.setIs24HourView(true);
        tp_end.setIs24HourView(true);

        rg_eat.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                switch (i) {
                    case R.id.rb_left:
                    case R.id.rb_right:
                        txt_count_time.setVisibility(View.VISIBLE);
                        et_ml.setVisibility(View.GONE);
                        tp_start.setVisibility(View.VISIBLE);
                        txt_to.setVisibility(View.VISIBLE);
                        break;
                    case R.id.rb_milk:
                        txt_count_time.setVisibility(View.GONE);
                        et_ml.setVisibility(View.VISIBLE);
                        tp_start.setVisibility(View.GONE);
                        txt_to.setVisibility(View.GONE);
                        break;
                    default:
                        break;
                }
            }
        });

        tp_start.setOnTimeChangedListener(new TimePicker.OnTimeChangedListener() {
            @Override
            public void onTimeChanged(TimePicker timePicker, int hourOfDay, int minute) {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                    int endHour = tp_end.getHour();
                    int endMinute = tp_end.getMinute();
                    StringBuilder sb = new StringBuilder();
                    int m = endMinute - minute;
                    int h = endHour - hourOfDay;
                    if(m < 0) {
                        m += 60;
                        h--;
                    }
                    if(h >= 0 && h < 10) {
                        sb.append("0");
                    }
                    sb.append(h);
                    sb.append(":");
                    if(m >= 0 && m < 10) {
                        sb.append("0");
                    }
                    sb.append(m);
                    txt_count_time.setText(sb.toString());
                }
            }
        });

        tp_end.setOnTimeChangedListener(new TimePicker.OnTimeChangedListener() {
            @Override
            public void onTimeChanged(TimePicker timePicker, int hourOfDay, int minute) {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                    int startHour = tp_start.getHour();
                    int startMinute = tp_start.getMinute();
                    StringBuilder sb = new StringBuilder();
                    int m = minute - startMinute;
                    int h = hourOfDay - startHour;
                    if(m < 0) {
                        m += 60;
                        h--;
                    }
                    if(h >= 0 && h < 10) {
                        sb.append("0");
                    }
                    sb.append(h);
                    sb.append(":");
                    if(m >= 0 && m < 10) {
                        sb.append("0");
                    }
                    sb.append(m);
                    txt_count_time.setText(sb.toString());
                }
            }
        });

        addEatDialog.setPositiveButton(R.string.add, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                    int startHour = tp_start.getHour();
                    int startMinute = tp_start.getMinute();
                    int endHour = tp_end.getHour();
                    int endMinute = tp_end.getMinute();

                    EatMilk eatMilk = new EatMilk();
                    Calendar cal = Calendar.getInstance();
                    cal.set(Calendar.HOUR_OF_DAY, startHour);
                    cal.set(Calendar.MINUTE, startMinute);
                    eatMilk.setStart(cal.getTimeInMillis());

                    if(R.id.rb_milk == rg_eat.getCheckedRadioButtonId()) {
                        String ml = et_ml.getEditableText().toString();
                        if(!TextUtils.isEmpty(ml)) {
                            eatMilk.setMl(Integer.parseInt(ml));
                        }
                        eatMilk.setEnd(cal.getTimeInMillis());
                    } else {
                        if (R.id.rb_left == rg_eat.getCheckedRadioButtonId()) {
                            eatMilk.setLeft(true);
                        } else if (R.id.rb_right == rg_eat.getCheckedRadioButtonId()) {
                            eatMilk.setLeft(false);
                        }
                        cal.set(Calendar.HOUR_OF_DAY, endHour);
                        cal.set(Calendar.MINUTE, endMinute);
                        eatMilk.setEnd(cal.getTimeInMillis());
                        SimpleDateFormat format = new SimpleDateFormat("yyyy:MM:dd HH:mm:ss");
                        UtilLog.debug(format.format(cal.getTimeInMillis()));
                        long duration = (endMinute - startMinute) * 60 * 1000 +
                                (endHour - startHour) * 60 * 60 *1000;
                        eatMilk.setDuration(duration);
                    }
                    milkBox.put(eatMilk);
                    getMilk();
                }
            }
        });
        addEatDialog.setNegativeButton(R.string.close, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        addEatDialog.show();
    }

    private void onMenuAddShitClick() {
        AlertDialog.Builder addShitDialog =
                new AlertDialog.Builder(MainActivity.this);
        final View addShitView = LayoutInflater.from(MainActivity.this)
                .inflate(R.layout.dialog_add_shit, null);
        addShitDialog.setTitle(R.string.add_shit);
        addShitDialog.setView(addShitView);
        final String[] status = {""};
        final TimePicker tp_time = addShitView.findViewById(R.id.tp_time);
        tp_time.setIs24HourView(true);
        FlowTagLayout flowTagLayout = addShitView.findViewById(R.id.color_flow_layout);
        TagAdapter tagAdapter = new TagAdapter<>(MainActivity.this);
        flowTagLayout.setTagCheckedMode(FlowTagLayout.FLOW_TAG_CHECKED_MULTI);
        flowTagLayout.setAdapter(tagAdapter);
        flowTagLayout.setOnTagSelectListener(new OnTagSelectListener() {
            @Override
            public void onItemSelect(FlowTagLayout parent, List<Integer> selectedList) {
                if (selectedList != null && selectedList.size() > 0) {
                    StringBuilder sb = new StringBuilder();

                    for (int i : selectedList) {
                        sb.append(parent.getAdapter().getItem(i));
                        sb.append(",");
                    }
                    status[0] = sb.toString();
                }
            }
        });
        QueryBuilder<Shit> shitBuilder = shitBox.query();
        final Shit shit = shitBuilder.orderDesc(Shit_.time).build().findFirst();
        long time = System.currentTimeMillis();
        if(null != shit) {
        }
        if(null != shit) {
            time = shit.getTime();
            status[0] = shit.getStatus();
        }
        final Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(time);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            tp_time.setHour(calendar.get(Calendar.HOUR_OF_DAY));
            tp_time.setMinute(calendar.get(Calendar.MINUTE));
        }

        List<String> dataSource = new ArrayList<>();
        dataSource.add("黄色");
        dataSource.add("绿色");
        dataSource.add("奶瓣");
        dataSource.add("稀");
        tagAdapter.onlyAddAll(dataSource);
        List<Integer> select = new ArrayList<>();
        if(status[0].contains("黄色")) {
            select.add(0);
        }
        if(status[0].contains("绿色")) {
            select.add(1);
        }
        if(status[0].contains("奶瓣")) {
            select.add(2);
        }
        if(status[0].contains("稀")) {
            select.add(3);
        }
        tagAdapter.setSelect(select);

        addShitDialog.setPositiveButton(R.string.ok,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        long time = System.currentTimeMillis();
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            calendar.set(Calendar.HOUR_OF_DAY, tp_time.getHour());
                            calendar.set(Calendar.MINUTE, tp_time.getMinute());
                            time = calendar.getTimeInMillis();
                        }
                        Shit st = new Shit();
                        st.setTime(time);
                        if(null != shit) {
                            st.setDuration(time - shit.getTime());
                        }
                        st.setStatus(status[0]);
                        shitBox.put(st);
                    }
                });
        addShitDialog.setNegativeButton(R.string.close, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });

        addShitDialog.show();
    }

    private void onMenuAddBodyClick() {
        AlertDialog.Builder addBodyDialog =
                new AlertDialog.Builder(MainActivity.this);
        final View addBodyView = LayoutInflater.from(MainActivity.this)
                .inflate(R.layout.dialog_add_body, null);
        addBodyDialog.setTitle(R.string.add_body);
        addBodyDialog.setView(addBodyView);

        final EditText et_weight = addBodyView.findViewById(R.id.et_weight);
        final EditText et_height = addBodyView.findViewById(R.id.et_height);
        final EditText et_temperature = addBodyView.findViewById(R.id.et_temperature);
        final DatePicker dp_date = addBodyView.findViewById(R.id.dp_date);
        Calendar calendar = Calendar.getInstance();
        dp_date.init(calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH), null);
        addBodyDialog.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                long time = System.currentTimeMillis();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    Calendar calendar = Calendar.getInstance();
                    calendar.set(Calendar.YEAR, dp_date.getYear());
                    calendar.set(Calendar.MONTH, dp_date.getMonth());
                    calendar.set(Calendar.DAY_OF_MONTH, dp_date.getDayOfMonth());
                    time = calendar.getTimeInMillis();
                }
                Body body = new Body();
                body.setTime(time);
                String weight = et_weight.getEditableText().toString();
                String height = et_height.getEditableText().toString();
                String temperature = et_temperature.getEditableText().toString();
                if(!TextUtils.isEmpty(weight)) {
                    body.setWeight(Float.parseFloat(weight));
                }
                if(!TextUtils.isEmpty(height)) {
                    body.setHeight(Float.parseFloat(height));
                }
                if(!TextUtils.isEmpty(temperature)) {
                    body.setTemperature(Float.parseFloat(temperature));
                }
                bodyBox.put(body);
            }
        });
        addBodyDialog.setNegativeButton(R.string.close, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        addBodyDialog.show();
    }

    private void onMenuShitClick() {
        AlertDialog.Builder shitDialog =
                new AlertDialog.Builder(MainActivity.this);
        final View shitView = LayoutInflater.from(MainActivity.this)
                .inflate(R.layout.dialog_shit, null);
        shitDialog.setTitle(R.string.last);
        shitDialog.setView(shitView);
        final String[] status = {""};

        FlowTagLayout flowTagLayout = shitView.findViewById(R.id.color_flow_layout);
        TagAdapter tagAdapter = new TagAdapter<>(MainActivity.this);
        flowTagLayout.setTagCheckedMode(FlowTagLayout.FLOW_TAG_CHECKED_MULTI);
        flowTagLayout.setAdapter(tagAdapter);
        flowTagLayout.setOnTagSelectListener(new OnTagSelectListener() {
            @Override
            public void onItemSelect(FlowTagLayout parent, List<Integer> selectedList) {
                if (selectedList != null && selectedList.size() > 0) {
                    StringBuilder sb = new StringBuilder();

                    for (int i : selectedList) {
                        sb.append(parent.getAdapter().getItem(i));
                        sb.append(",");
                    }
                    status[0] = sb.toString();
                }
            }
        });
        QueryBuilder<Shit> shitBuilder = shitBox.query();
        final Shit shit = shitBuilder.orderDesc(Shit_.time).build().findFirst();

        final Handler shitHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                TextView txt_count_time = shitView.findViewById(R.id.txt_count_time);
                long current = System.currentTimeMillis();
                if(null != shit) {
                    long time = shit.getTime();
                    txt_count_time.setText(Util.formatTime(current - time));
                    sendEmptyMessageDelayed(0, 1000);
                }
            }
        };
        shitHandler.sendEmptyMessage(0);

        if(null != shit) {
            status[0] = shit.getStatus();
        }
        List<String> dataSource = new ArrayList<>();
        dataSource.add("黄色");
        dataSource.add("绿色");
        dataSource.add("奶瓣");
        dataSource.add("稀");
        tagAdapter.onlyAddAll(dataSource);
        List<Integer> select = new ArrayList<>();
        if(status[0].contains("黄色")) {
            select.add(0);
        }
        if(status[0].contains("绿色")) {
            select.add(1);
        }
        if(status[0].contains("奶瓣")) {
            select.add(2);
        }
        if(status[0].contains("稀")) {
            select.add(3);
        }
        tagAdapter.setSelect(select);

        shitDialog.setPositiveButton(R.string.ok,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        long time = System.currentTimeMillis();
                        Shit st = new Shit();
                        st.setTime(time);
                        if(null != shit) {
                            st.setDuration(time - shit.getTime());
                        }
                        st.setStatus(status[0]);
                        shitBox.put(st);
                    }
                });
        shitDialog.setNegativeButton(R.string.close, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });

        shitDialog.show();
    }

    private void onMenuSleepClick() {
        AlertDialog.Builder sleepDialog =
                new AlertDialog.Builder(MainActivity.this);
        final View sleepView = LayoutInflater.from(MainActivity.this)
                .inflate(R.layout.dialog_sleep, null);

        QueryBuilder<Sleep> sleepBuilder = sleepBox.query();
        final Sleep sleep = sleepBuilder.orderDesc(Sleep_.time).build().findFirst();

        final Handler sleepHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                TextView txt_count_time = sleepView.findViewById(R.id.txt_count_time);
                long current = System.currentTimeMillis();
                if(null != sleep) {
                    long time = sleep.getTime();
                    txt_count_time.setText(Util.formatTime(current - time));
                    sendEmptyMessageDelayed(0, 1000);
                }
            }
        };
        sleepHandler.sendEmptyMessage(0);
        final boolean isSleep = sleep != null ? sleep.isSleep() : false;

        sleepDialog.setTitle(isSleep ? R.string.wake_last : R.string.sleep_last);
        sleepDialog.setView(sleepView);
        sleepDialog.setPositiveButton(isSleep ? R.string.sleep : R.string.wake,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Sleep sl = new Sleep();
                        long time = System.currentTimeMillis();
                        sl.setSleep(!isSleep);
                        sl.setTime(time);
                        if(null != sleep) {
                            sl.setDuration(time - sleep.getTime());
                        }
                        sleepBox.put(sl);
                    }
                });
        sleepDialog.setNegativeButton(R.string.close, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        sleepDialog.show();
    }

    private void onMenuBodyClick() {
        AlertDialog.Builder bodyDialog =
                new AlertDialog.Builder(MainActivity.this);
        final View bodyView = LayoutInflater.from(MainActivity.this)
                .inflate(R.layout.dialog_body, null);
        bodyDialog.setTitle(R.string.tip);
        bodyDialog.setView(bodyView);

        final EditText et_weight = bodyView.findViewById(R.id.et_weight);
        final EditText et_height = bodyView.findViewById(R.id.et_height);
        final EditText et_temperature = bodyView.findViewById(R.id.et_temperature);

        bodyDialog.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                long time = System.currentTimeMillis();
                Body body = new Body();
                body.setTime(time);
                String weight = et_weight.getEditableText().toString();
                String height = et_height.getEditableText().toString();
                String temperature = et_temperature.getEditableText().toString();
                if(!TextUtils.isEmpty(weight)) {
                    body.setWeight(Float.parseFloat(weight));
                }
                if(!TextUtils.isEmpty(height)) {
                    body.setHeight(Float.parseFloat(height));
                }
                if(!TextUtils.isEmpty(temperature)) {
                    body.setHeight(Float.parseFloat(temperature));
                }
                bodyBox.put(body);
            }
        });
        bodyDialog.setNegativeButton(R.string.close, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        bodyDialog.show();
    }

    @OnClick(R.id.btn_eat)
    public void onEat() {
        final SharedPreferences sp = getSharedPreferences(APP, Context.MODE_PRIVATE);
        boolean eating = sp.getBoolean(EATING, false);

        if(eating) {
            final long time = System.currentTimeMillis();
            SharedPreferences.Editor editor = sp.edit();
            editor.putLong(EAT_END, time);
            editor.putBoolean(EATING, false);
            editor.commit();

            btn_eat.setText(R.string.eat);
            btn_eat.setTextSize(48);
            btn_eat.setCompoundDrawablesWithIntrinsicBounds(
                    getResources().getDrawable(R.mipmap.ic_play_grey600_48dp)
            ,null,null,null);

            long start = sp.getLong(EAT_START,0);
            boolean left = sp.getBoolean(LEFT, true);

            txt_last_count.setText(left ? R.string.left : R.string.right);
            SimpleDateFormat df = new SimpleDateFormat(timeFormat, Locale.CHINA);
            txt_last_time.setText(df.format(time));

            EatMilk milk = new EatMilk();
            milk.setStart(start);
            milk.setEnd(time);
            milk.setDuration(time - start);
            milk.setLeft(left);
            milkBox.put(milk);

            getMilk();
            adapter.notifyDataSetChanged();
            eatHandler.sendEmptyMessage(0);
        } else {
            final AlertDialog.Builder customizeDialog =
                    new AlertDialog.Builder(MainActivity.this);
            final View dialogView = LayoutInflater.from(MainActivity.this)
                    .inflate(R.layout.dialog_eat, null);
            customizeDialog.setTitle(R.string.tip);
            customizeDialog.setView(dialogView);
            final CheckedTextView ctv_milk = dialogView.findViewById(R.id.ctv_milk);
            final EditText et_ml = dialogView.findViewById(R.id.et_ml);
            et_ml.setSelection(et_ml.getText().length());
            final RadioGroup rg_left = dialogView.findViewById(R.id.rg_left);
            RadioButton rb_left = dialogView.findViewById(R.id.rb_left);
            RadioButton rb_right = dialogView.findViewById(R.id.rb_right);
            boolean left = sp.getBoolean(LEFT, true);
            rg_left.check(left ? R.id.rb_right : R.id.rb_left);

            ctv_milk.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View view) {
                    ctv_milk.toggle();
                    if (ctv_milk.isChecked()) {
                        et_ml.setEnabled(true);
                        rg_left.clearCheck();
                        Util.disableRadioGroup(rg_left);
                    } else {
                        et_ml.setEnabled(false);
                        rg_left.check(R.id.rb_left);
                        Util.enableRadioGroup(rg_left);
                    }
                }
            });

            customizeDialog.setPositiveButton(R.string.ok,
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (ctv_milk.isChecked()) {
                                final long time = System.currentTimeMillis();
                                SharedPreferences.Editor editor = sp.edit();
                                editor.putLong(EAT_END, time);
                                EditText et_ml = dialogView.findViewById(R.id.et_ml);
                                String ml = et_ml.getEditableText().toString();
                                if(!TextUtils.isEmpty(ml)) {
                                    try {
                                        editor.putInt(MILK, Integer.parseInt(ml));
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                                editor.commit();

                                EatMilk milk = new EatMilk();
                                milk.setStart(time);
                                milk.setEnd(time);
                                milk.setMl(Integer.parseInt(ml));
                                milkBox.put(milk);
                                getMilk();
                            } else {
                                final long time = System.currentTimeMillis();
                                SharedPreferences.Editor editor = sp.edit();

                                RadioGroup rg_left = dialogView.findViewById(R.id.rg_left);
                                if (R.id.rb_left == rg_left.getCheckedRadioButtonId()) {
                                    editor.putBoolean(LEFT, true);
                                } else {
                                    editor.putBoolean(LEFT, false);
                                }
                                editor.putLong(EAT_START, time);
                                editor.putInt(MILK, 0);
                                editor.putBoolean(EATING, true);
                                editor.commit();
                                getMilk();
                            }
                        }
                    });
            customizeDialog.setNegativeButton(R.string.close, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.dismiss();
                }
            });
            customizeDialog.show();
        }
    }

    EatHandler eatHandler = new EatHandler(this);

    public static class EatHandler extends Handler {
        private WeakReference<MainActivity> reference;

        public EatHandler(MainActivity activity) {
            reference = new WeakReference<MainActivity>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            MainActivity mainActivity = reference.get();
            if(mainActivity != null) {
                SharedPreferences sp = mainActivity.getSharedPreferences(APP, Context.MODE_PRIVATE);
                boolean eating = sp.getBoolean(EATING, false);
                long current = System.currentTimeMillis();
                if (eating) {
                    long time = sp.getLong(EAT_START, current);
                    mainActivity.txt_count_time.setText(Util.formatTime(current - time));
                    sendEmptyMessageDelayed(0, 1000);
                } else {
                    QueryBuilder<EatMilk> builder = mainActivity.milkBox.query();
                    EatMilk milk = builder.orderDesc(EatMilk_.end).build().findFirst();
                    if (null != milk) {
                        mainActivity.txt_count_time.setText(Util.formatTime(current - milk.getEnd()));
                        sendEmptyMessageDelayed(0, 1000);
                    }
                }
            }
        }
    }
}
