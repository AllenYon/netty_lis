package cn.link.lis;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import io.netty.buffer.ByteBuf;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.SortedSet;
import java.util.TreeSet;

public class MainActivity extends Activity {
    public static final int PORT = 8082;

    Button mBtnConnect;
    Button mBtnStart;
    Button mBtnAsHost;
    TextView mTvState;
    EditText mEtIPAddress;
    ViewGroup mVGContain;
    ViewGroup mVGPreContain;
    SortedSet<LISNum> sortedSet = new TreeSet<LISNum>();

    LISProblem mLISProblem;


    LIPClient client;
    LIPServer server;
    boolean isHost;
    boolean isWin;

    long localWinTime = Long.MAX_VALUE;
    boolean localConfirm;


    public static final int CLIENT_CONNECTED = 0;
    public static final int CLIENT_REV_LIS = 2;

    public static final int SERVER_CLIENT_CONNECTED = 5;
    public static final int SERVER_LIP = 6;
    public static final int SERVER_REV_CURRENT_INDEX = 8;

    public static final int CLIENT_WIN = 10;
    public static final int SERVER_WIN = 11;

    public static final int CLIENT_CONFIRM_WIN = 12;
    public static final int SERVER_CONFIRM_WIN = 13;

    public static final int ERROR = 99;

    Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case CLIENT_CONNECTED:
                case 1:
                    String str = (String) msg.obj;
                    mTvState.setText(str);
                    break;
                case CLIENT_REV_LIS:
                    MessageProtocol mp = (MessageProtocol) msg.obj;
                    Log.w("", mp.lisProblem.toString());
                    mLISProblem = mp.lisProblem;
                    update(mLISProblem);
                    updatePre(mLISProblem);
                    break;
                case SERVER_CLIENT_CONNECTED:
                    //Client Connected
                    String ipaddress = (String) msg.obj;
                    mTvState.setText("Client Connected " + ipaddress);
                    break;
                case SERVER_LIP:
                    mLISProblem = (LISProblem) msg.obj;
                    update(mLISProblem);
                    updatePre(mLISProblem);
                    break;
                case SERVER_REV_CURRENT_INDEX:
                    MessageProtocol mpindex = (MessageProtocol) msg.obj;
                    int[] indexs = mpindex.currentIndex;
                    manualLISChecked(indexs);
                    break;
                case SERVER_WIN:
                    MessageProtocol mpsw = (MessageProtocol) msg.obj;
                    Toast.makeText(MainActivity.this, "Server will win", Toast.LENGTH_SHORT).show();
                    if (!localConfirm) {
                        if (localWinTime < mpsw.winTime) {
                            //本地胜利时间比 远程胜利时间近 本地的确胜利
                            Toast.makeText(MainActivity.this, "Client confirm win", Toast.LENGTH_SHORT).show();
                            client.send(MessageProtocol.build(MessageProtocol.K_CLIENT_CONFIRM_WIN));
                        } else {
                            Toast.makeText(MainActivity.this, "Server confirm win", Toast.LENGTH_SHORT).show();
                            client.send(MessageProtocol.build(MessageProtocol.K_CLIENT_CONFIRM_LOST));
                        }
                        localConfirm = true;
                    }

                    break;
                case CLIENT_WIN:
                    MessageProtocol mpcw = (MessageProtocol) msg.obj;
                    Toast.makeText(MainActivity.this, "Client will win", Toast.LENGTH_SHORT).show();
                    if (!localConfirm) {
                        if (localWinTime < mpcw.winTime) {
                            //本地胜利时间比 远程胜利时间近 本地的确胜利
                            Toast.makeText(MainActivity.this, "Server confirm win", Toast.LENGTH_SHORT).show();
                            server.send(MessageProtocol.build(MessageProtocol.K_SERVER_CONFIRM_WIN));
                        } else {
                            Toast.makeText(MainActivity.this, "Client confirm win", Toast.LENGTH_SHORT).show();
                            server.send(MessageProtocol.build(MessageProtocol.K_SERVER_CONFIRM_LOST));
                        }
                        localConfirm = true;
                    }
                    break;

                case SERVER_CONFIRM_WIN:
                    Toast.makeText(MainActivity.this, "Server confirm win", Toast.LENGTH_SHORT).show();
                    break;
                case CLIENT_CONFIRM_WIN:
                    Toast.makeText(MainActivity.this, "Client confirm win", Toast.LENGTH_SHORT).show();
                    break;


                case ERROR:
                    Exception e = (Exception) msg.obj;
                    Toast.makeText(MainActivity.this, "Error :" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ac_main);
        mTvState = (TextView) findViewById(R.id.tv_state);
        mVGContain = (ViewGroup) findViewById(R.id.layout_contain);
        mVGPreContain = (ViewGroup) findViewById(R.id.layout_pre_contain);

        mEtIPAddress = (EditText) findViewById(R.id.et_ip);
        mBtnStart = (Button) findViewById(R.id.btn_start);
        mBtnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //ToDo
                ByteBuf bb = MessageProtocol.build(MessageProtocol.K_START_GAME);
                client.send(bb);
            }
        });
        mBtnConnect = (Button) findViewById(R.id.btn_connect);
        mBtnConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //ToDo
                new AsyncTask<Void, Void, Void>() {
                    @Override
                    protected Void doInBackground(Void... params) {
                        isHost = false;
                        String IPAddress = mEtIPAddress.getText().toString();
                        client = new LIPClient(IPAddress, PORT, mHandler);
                        client.connect();
                        return null;  //ToDo
                    }
                }.execute();
            }
        });
        mBtnAsHost = (Button) findViewById(R.id.btn_as_host);
        mBtnAsHost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //ToDo
                mTvState.setText(getIPAddress());
                new AsyncTask<Void, Void, Void>() {
                    @Override
                    protected Void doInBackground(Void... params) {
                        isHost = true;
                        server = new LIPServer(getIPAddress(), PORT, mHandler);
                        try {
                            server.bind();
                        } catch (Exception e) {
                            e.printStackTrace();
                            Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                        return null;  //ToDo
                    }
                }.execute();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (server != null) {
            server.close();
        }
        if (client != null) {
            client.close();
        }
    }

    public void update(LISProblem lis) {
        mVGContain.removeAllViews();
        sortedSet.clear();
        TextView h = new TextView(this);
        h.setTextColor(Color.BLACK);
        h.setBackgroundResource(R.drawable.select_cb_bg);
        h.setGravity(Gravity.CENTER_VERTICAL);
        h.setPadding(8, 8, 8, 8);
        h.setTextSize(50);
        h.setText("{");
        mVGContain.addView(h);
        for (int i = 0; i < lis.randomIntArray.length; i++) {
            LISNum num = new LISNum(this);
            num.setTextSize(50);
            num.setIndex(i);
            num.setNum(lis.randomIntArray[i]);
            num.setOnCheckedChangeListener(listener);
            mVGContain.addView(num);
        }
        TextView f = new TextView(this);
        f.setTextColor(Color.BLACK);
        f.setBackgroundResource(R.drawable.select_cb_bg);
        f.setGravity(Gravity.CENTER_VERTICAL);
        f.setPadding(8, 8, 8, 8);
        f.setTextSize(50);
        f.setText("}");
        mVGContain.addView(f);
    }

    public void updatePre(LISProblem lis) {
        mVGPreContain.removeAllViews();
        sortedSet.clear();
        TextView h = new TextView(this);
        h.setTextColor(Color.BLACK);
        h.setBackgroundResource(R.drawable.select_cb_bg);
        h.setGravity(Gravity.CENTER_VERTICAL);
        h.setPadding(8, 8, 8, 8);
        h.setTextSize(30);
        h.setText("{");
        mVGPreContain.addView(h);
        for (int i = 0; i < lis.randomIntArray.length; i++) {
            LISNum num = new LISNum(this);
            num.setTextSize(30);
            num.setIndex(i);
            num.setNum(lis.randomIntArray[i]);
            mVGPreContain.addView(num);
        }
        TextView f = new TextView(this);
        f.setTextColor(Color.BLACK);
        f.setBackgroundResource(R.drawable.select_cb_bg);
        f.setGravity(Gravity.CENTER_VERTICAL);
        f.setPadding(8, 8, 8, 8);
        f.setTextSize(30);
        f.setText("}");
        mVGPreContain.addView(f);
    }

    public CompoundButton.OnCheckedChangeListener listener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            //ToDo
            LISNum num = (LISNum) buttonView;
            if (isChecked) {
                sortedSet.add(num);
            } else {
                sortedSet.remove(num);
            }
            tvPre();
        }
    };

    public void manualLISChecked(int[] indexs) {
        for (int i = 1; i < mVGPreContain.getChildCount() - 1; i++) {
            LISNum checkbox = (LISNum) mVGPreContain.getChildAt(i);
            checkbox.setChecked(false);
        }
        for (int i = 0; i < indexs.length; i++) {
            LISNum checkbox = (LISNum) mVGPreContain.getChildAt(indexs[i] + 1);
            checkbox.setChecked(true);
        }
    }

    private void tvPre() {
        {
            Iterator<LISNum> iterator = sortedSet.iterator();
            int[] indexs = new int[sortedSet.size()];
            int i = 0;
            while (iterator.hasNext()) {
                indexs[i] = iterator.next().index;
                i++;
            }
            ByteBuf bb = MessageProtocol.sendCurrentIndex(indexs);
            if (!isHost) {
                client.send(bb);
            } else {
                server.send(bb);
            }
        }

        if (sortedSet.size() == mLISProblem.bestSolve) {
            boolean asc = true;
            Iterator<LISNum> it2 = sortedSet.iterator();
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

                localWinTime = System.currentTimeMillis();
                isWin = true;
                if (isHost) {
                    server.send(MessageProtocol.build(MessageProtocol.K_SERVER_WIN, localWinTime));
                } else {
                    client.send(MessageProtocol.build(MessageProtocol.K_CLIENT_WIN, localWinTime));
                }
            }
        } else {

        }
    }

    public String getLocalIpAddress() {
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces();
                 en.hasMoreElements(); ) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress()) {
                        return inetAddress.getHostAddress().toString();
                    }
                }
            }
        } catch (SocketException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public String getIPAddress() {
        // 或者wifi服务
        WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        if (!wifiManager.isWifiEnabled()) {
            wifiManager.setWifiEnabled(true);
        }
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        int ipAddress = wifiInfo.getIpAddress();
        return intToIp(ipAddress);
    }

    private String intToIp(int i) {
        return (i & 0xFF) + "." +
                ((i >> 8) & 0xFF) + "." +
                ((i >> 16) & 0xFF) + "." +
                (i >> 24 & 0xFF);
    }

}
