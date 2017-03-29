package com.kunstudy.administrator.bluetoothdemo;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Created by Administrator on 2017/3/25.
 */
public class MainActivity extends AppCompatActivity {
    private static final int MESSAGE_READ = 1;
    /**
     * 创建handle来接受消息
     */
    private Handler mHandler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
        }
    };
    private boolean isSearch = false;
    private List<BluetoothDevice> list = new ArrayList<>();
    private List<BluetoothDevice> listSerch = new ArrayList<>();
    ListViewForScrollView  deviceList;
    public static final int REQUESTCODE_OPEN = 1;
    private BluetoothController mController = new BluetoothController();
    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, 1);
            switch (state) {
                case BluetoothAdapter.STATE_OFF:
                    Toast.makeText(MainActivity.this, "蓝牙已关闭", Toast.LENGTH_SHORT).show();
                    break;
                case BluetoothAdapter.STATE_ON:
                    Toast.makeText(MainActivity.this, "蓝牙已打开", Toast.LENGTH_SHORT).show();
                    break;
                case BluetoothAdapter.STATE_TURNING_ON:
                    Toast.makeText(MainActivity.this, "正在打开蓝牙", Toast.LENGTH_SHORT).show();
                    break;
                case BluetoothAdapter.STATE_TURNING_OFF:
                    Toast.makeText(MainActivity.this, "正在关闭蓝牙", Toast.LENGTH_SHORT).show();
                    break;
                default:
                    Toast.makeText(MainActivity.this, "未知状态", Toast.LENGTH_SHORT).show();
            }
        }
    };
    //搜索可见的蓝牙设备的广播接受者,动态注册
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            //发现了设备
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                Toast.makeText(MainActivity.this, "发现设备", Toast.LENGTH_SHORT).show();
                //从Intent中获取设备的BluetoothDevice对象
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                listSerch.add(device);
                //设置展示
                list_serch.setAdapter(new MyListAdapter(listSerch));

            }

        }
    };
    private MyListAdapter adapter;
    private ListViewForScrollView  list_serch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //初始化控件
        initView();

        IntentFilter filter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(receiver, filter);
        //第一步：注册广播
        IntentFilter filter2 = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(mReceiver, filter2);
        //不要忘了在onDestory中unregister
        //第二部：开始发现(在点击事件里面实现)
        //// TODO: 2017/3/26
        if (isSearch) {
            //开始搜索可发现的设备
            mController.searchDevice();
            //结束,获取信息  保存到 listSearch中

        }


    }


    private void initView() {
        //获取连接的莱斯特view的初始化
        deviceList = (ListViewForScrollView ) findViewById(R.id.listview_device);
        //获取搜索出来的listview
        list_serch = (ListViewForScrollView ) findViewById(R.id.listview_serchDevice);
    }

    public void click(View view) {
        switch (view.getId()) {
            case R.id.frist1:
                //点击打开蓝牙页面
                Intent intent = new Intent();
                intent.setAction(Settings.ACTION_BLUETOOTH_SETTINGS);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);

            case R.id.btnIsSupport:
                boolean flag = mController.isSupportBluetooth();
                Toast.makeText(this, "flag = " + flag, Toast.LENGTH_SHORT).show();
                break;
            case R.id.btnIsTurnOn:
                boolean isTurnOn = mController.getBluetoothStatus();
                Toast.makeText(this, "isTurnOn" + isTurnOn, Toast.LENGTH_SHORT).show();
                break;
            case R.id.btnTurnOn:
                mController.turnOnBluetooth(this, REQUESTCODE_OPEN);
                break;
            case R.id.btnTrunOff:
                mController.turnOffBluetooth();
                break;
            case R.id.btnGetBlueToothDeviceLIst:
                Toast.makeText(getBaseContext(), "展示标识的就是点击事件执行了", Toast.LENGTH_SHORT).show();
                //点击就会调用获取已经配对的设备集合
                Set<BluetoothDevice> connectedDeviceList = mController.getConnectedDevice();
                // 遍历展示在listview上面
                for (BluetoothDevice device : connectedDeviceList) {
                    //创建一个集合传入:
                    list.add(device);
                }

                //设置适配器
                adapter = new MyListAdapter(list);
                deviceList.setAdapter(adapter);
                break;
            case R.id.btn_opneDiscoverAbility:
                //调用打开可发现性的方法
                mController.openDiscoverAblity();
                break;
            case R.id.btn_serchDevice:
                //执行搜索的设备的方法
                mController.changeIsSearch(isSearch);

        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            Toast.makeText(this, "终于打开了", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 蓝牙管理类
     */
    public class BluetoothController {
        private BluetoothAdapter mAdapter;

        public BluetoothController() {
            mAdapter = BluetoothAdapter.getDefaultAdapter();
        }

        /**
         * 判断当前设备是否支持蓝牙
         *
         * @return
         */
        public boolean isSupportBluetooth() {
            if (mAdapter != null) {
                return true;
            }
            return false;
        }

        /**
         * 获取蓝牙的状态
         *
         * @return
         */
        public boolean getBluetoothStatus() {
            if (mAdapter != null) {
                return mAdapter.isEnabled();
            }
            return false;
        }

        /**
         * 打开蓝牙
         *
         * @param activity
         * @param requestCode
         */
        public void turnOnBluetooth(MainActivity activity, int requestCode) {
            if (mAdapter != null && !mAdapter.isEnabled()) {
                Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                activity.startActivityForResult(intent, requestCode);
            }
        }

        /**
         * 关闭蓝牙
         */
        public void turnOffBluetooth() {
            if (mAdapter != null && mAdapter.isEnabled()) {
                mAdapter.disable();
            }
        }

        /**
         * 获取已经配对的设备
         * 前提条件是,打开蓝牙,获得授权
         */
        public Set<BluetoothDevice> getConnectedDevice() {
            //判断是否打开和授权
            if ((mAdapter != null) && mAdapter.isEnabled()) {
                return mAdapter.getBondedDevices();

            }
            return null;

        }


        public void openDiscoverAblity() {
            Intent discoverableIntent = new
                    Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            //定义持续时间
            discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
            startActivity(discoverableIntent);
        }

        /**
         * 此方法是用来扫面周围可见的蓝牙设备
         * 时间是十二秒左右,可以设置为三十秒
         */

        public void searchDevice() {
            mAdapter.startDiscovery();
        }

        /**
         * 主要是点击之后才开始搜索周围可见的蓝牙设备
         */
        private boolean changeIsSearch(boolean b) {


            return !b;
        }

        public BluetoothAdapter getmAdapter() {
            return mAdapter;
        }
    }


    @Override
    protected void onDestroy() {
        unregisterReceiver(receiver);
        unregisterReceiver(mReceiver);
        super.onDestroy();
    }
    //开启一个线程来模仿蓝牙服务端
    public class AcceptThread extends Thread {
        private BluetoothServerSocket mServerSocket;

        public AcceptThread() {
            BluetoothServerSocket tmp = null;
            try {
                tmp = mController.getmAdapter().listenUsingRfcommWithServiceRecord
                        ("BluetoothServer", UUID.fromString(getPackageName()));
            } catch (IOException e) {
                e.printStackTrace();
            }
            mServerSocket = tmp;
        }

        @Override
        public void run() {
            super.run();
            BluetoothSocket socket = null;
            //不断监听直到返回连接或者发生异常
            while (true) {
                try {
                    //启连接请求，这是一个阻塞方法，必须放在子线程
                    socket = mServerSocket.accept();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                //建立了连接
                if (socket != null) {
                    //管理连接(在一个独立的线程里进行)
                    manageConnectedSocket(socket);
                    try {
                        mServerSocket.close();//关闭连接
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        /**
         * 取消正在监听的接口
         */
        public void cancel() {
            try {
                mServerSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void manageConnectedSocket(BluetoothSocket socket) {
            //此时已经是连接成功了!  这个方法是用来传递数据的
        }
    }
    /**
     * 创建一个线程来模拟实现蓝牙客户端的实现
     *
     */
    public class ConnectThread extends Thread{
        private BluetoothDevice mDevice;
        private BluetoothSocket mSocket;
        private InputStream mInputStream;
        private OutputStream mOutputSteam;
        public ConnectThread(BluetoothDevice device) throws IOException {
            BluetoothSocket temp = null;
            mDevice = device;
            try {
                temp = mDevice.createRfcommSocketToServiceRecord(UUID.fromString(getPackageName()));
            } catch (IOException e) {
                e.printStackTrace();
            }
            mSocket = temp;
            //获取传递的数据
            //输出流
            mOutputSteam= mSocket.getOutputStream();
            //输入流
            mInputStream=mSocket.getInputStream();
        }

        @Override
        public void run() {
            super.run();
            //取消搜索因为搜索会让连接变慢
            mController.getmAdapter().cancelDiscovery();
            try {
                //通过socket连接设备，这是一个阻塞操作，知道连接成功或发生异常
                mSocket.connect();
                //
                byte[] buffer = new byte[2014];//存储流的缓存
                int bytes;//从read()返回的字节
                //不断监听输入流直到异常发生
                while(true){
                    try {
                        bytes = mInputStream.read(buffer);//从输入流读
                        mHandler.obtainMessage(MESSAGE_READ,bytes,-1,buffer).sendToTarget();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            } catch (IOException e) {
                //无法连接，关闭socket并且退出
                try {
                    mSocket.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
            //管理连接(在独立的线程)
            // manageConnectedSocket(mmSocket);
            /**
             * 发送数据给远程设备
             * @param bytes
             */

        }
        public void write(byte[] bytes){
            try {
                mOutputSteam.write(bytes);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        /**
         * 取消正在进行的链接，关闭socket
         */
        public void cancel() {
            try {
                mSocket.close();
            } catch (IOException e) { }
        }
    }
}
