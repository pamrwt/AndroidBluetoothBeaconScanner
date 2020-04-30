package com.example.biaconscanner;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.AlertDialog.Builder;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothAdapter.LeScanCallback;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build.VERSION;
import android.os.Bundle;

import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.biaconscanner.model.BleScanResult;
import com.example.biaconscanner.view.BleResultView;
import com.google.android.gms.analytics.HitBuilders.EventBuilder;
import com.google.android.gms.analytics.HitBuilders.ScreenViewBuilder;
import com.google.android.gms.analytics.Tracker;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_CODE_BLUETOOTH_ADMIN_PERMISSION = 1;
    private static final String TAG = "BleScanner";
    private Button btnScan;
    private LeScanCallback callbackJellyBean;
    private ScanCallback callbackLollipop;
    private RecyclerView listScannedDevices;
    BroadcastReceiver mBluetoothStateReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if ("android.bluetooth.adapter.action.STATE_CHANGED".equals(intent.getAction())) {
                int state = intent.getIntExtra("android.bluetooth.adapter.extra.STATE", -1);
                if (state == 10) {
                    MainActivity.this.stopScan();
                } else if (state == 12) {
                    MainActivity.this.startScan();
                }
            }
        }
    };
    /* access modifiers changed from: private */
    public boolean mScanActive = false;
    private Tracker mTracker;
    private ProgressBar pgbScan;
    private TextView txtNoDevicesFound;

    class BleResultViewHolder extends RecyclerView.ViewHolder {
        public BleResultViewHolder(View itemView) {
            super(itemView);
        }

        public void bind(BleScanResult bleScanResult) {
            ((BleResultView) this.itemView).bind(bleScanResult);
        }
    }

    class BleResultsAdapter extends RecyclerView.Adapter<BleResultViewHolder> {
        private ArrayList<BleScanResult> mResults;

        BleResultsAdapter() {
        }

        public void addScanResult(BleScanResult bleScanResult) {
            if (this.mResults == null) {
                this.mResults = new ArrayList<>();
            }
            for (int i = View.VISIBLE; i < this.mResults.size(); i++) {
                if (((BleScanResult) this.mResults.get(i)).getDevice().equals(bleScanResult.getDevice())) {
                    this.mResults.set(i, bleScanResult);
                    notifyItemChanged(i);
                    return;
                }
            }
            this.mResults.add(bleScanResult);
            notifyItemInserted(this.mResults.size() - 1);
        }

        public BleResultViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new BleResultViewHolder(new BleResultView(parent.getContext()));
        }

        public void onBindViewHolder(BleResultViewHolder holder, int position) {
            holder.bind((BleScanResult) this.mResults.get(position));
        }

        public int getItemCount() {
            if (this.mResults != null) {
                return this.mResults.size();
            }
            return View.VISIBLE;
        }
    }

    /* access modifiers changed from: protected */
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.mTracker = ((AnalyticsApplication) getApplication()).getDefaultTracker();
        this.mTracker.setScreenName("Main Screen");
        this.mTracker.send(new ScreenViewBuilder().build());
        this.listScannedDevices = (RecyclerView) findViewById(R.id.listScannedDevices);
        this.listScannedDevices.setLayoutManager(new LinearLayoutManager(this));
        this.txtNoDevicesFound = (TextView) findViewById(R.id.txtNoDevicesFound);
        this.txtNoDevicesFound.setVisibility(View.VISIBLE);
        this.pgbScan = (ProgressBar) findViewById(R.id.pgbScan);
        this.pgbScan.setVisibility(View.INVISIBLE);
        this.btnScan = (Button) findViewById(R.id.btnScan);
        this.btnScan.setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                if (MainActivity.this.mScanActive) {
                    MainActivity.this.stopScan();
                } else {
                    MainActivity.this.startScan();
                }
            }
        });
        if (VERSION.SDK_INT >= 21) {
            this.callbackLollipop = new ScanCallback() {
                @TargetApi(21)
                public void onScanResult(int callbackType, ScanResult result) {
                    MainActivity.this.handleScanResult(result.getDevice(), result.getScanRecord() != null ? result.getScanRecord().getBytes() : null, result.getRssi());
                }

                public void onBatchScanResults(List<ScanResult> results) {
                    super.onBatchScanResults(results);
                }

                public void onScanFailed(int errorCode) {
                    super.onScanFailed(errorCode);
                }
            };
        } else if (VERSION.SDK_INT >= 18) {
            this.callbackJellyBean = new LeScanCallback() {
                public void onLeScan(BluetoothDevice bluetoothDevice, int rssi, byte[] scanRecord) {
                    MainActivity.this.handleScanResult(bluetoothDevice, scanRecord, rssi);
                }
            };
        }
    }

    /* access modifiers changed from: private */
    public void handleScanResult(BluetoothDevice device, byte[] scanResponse, int rssi) {
        this.txtNoDevicesFound.setVisibility(View.GONE);
        BleScanResult response = new BleScanResult(device, scanResponse, rssi);
        Log.d(TAG, response.toString());
        ((BleResultsAdapter) this.listScannedDevices.getAdapter()).addScanResult(response);
    }

    /* access modifiers changed from: protected */
    public void onStart() {
        super.onStart();
        startScan();
        registerReceiver(this.mBluetoothStateReceiver, new IntentFilter("android.bluetooth.adapter.action.STATE_CHANGED"));
    }

    /* access modifiers changed from: private */
    public void startScan() {
        if (hasPermission(new String[]{"android.permission.BLUETOOTH", "android.permission.BLUETOOTH_ADMIN", "android.permission.ACCESS_FINE_LOCATION"}, 1, true)) {
            BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            if (bluetoothAdapter.isEnabled()) {
                this.mTracker.send(new EventBuilder().setCategory("Scan").setAction("Start Scan").build());
                this.mScanActive = true;
                this.listScannedDevices.setAdapter(new BleResultsAdapter());
                this.txtNoDevicesFound.setVisibility(View.VISIBLE);
                this.pgbScan = (ProgressBar) findViewById(R.id.pgbScan);
                this.pgbScan.setVisibility(View.INVISIBLE);

                this.btnScan.setText("Stop Scan");
                this.btnScan.setBackgroundResource(R.drawable.button_background_stop_scan_pressed);

                if (VERSION.SDK_INT >= 21) {
                    bluetoothAdapter.getBluetoothLeScanner().startScan(this.callbackLollipop);
                } else if (VERSION.SDK_INT >= 18) {
                    bluetoothAdapter.startLeScan(this.callbackJellyBean);
                }
            } else {
                new Builder(this).setTitle(R.string.app_name).setMessage("Blutooth").setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialogInterface, int i) {
                        BluetoothAdapter.getDefaultAdapter().enable();
                    }
                }).setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialogInterface, int i) {
                    }
                }).show();
            }
        }
    }

    @SuppressLint("WrongConstant")
    private boolean hasPermission(String[] permissions, int requestCode, boolean requestIfDoesntHavePermission) {
        if (VERSION.SDK_INT < 23 || permissions == null || permissions.length == View.VISIBLE) {
            return true;
        }
        int permissionCheckResult = View.VISIBLE;
        for (int i = View.VISIBLE; i < permissions.length && permissionCheckResult == View.VISIBLE; i++) {
            permissionCheckResult = checkSelfPermission(permissions[i]);
        }
        if (permissionCheckResult == View.VISIBLE) {
            return true;
        }
        if (requestIfDoesntHavePermission) {
            requestPermissions(permissions, requestCode);
        }
        return false;
    }

    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (permissions != null && grantResults != null) {
            boolean allRequestedPermissionsGranted = true;
            for (int i = View.VISIBLE; i < permissions.length && allRequestedPermissionsGranted; i++) {
                if (grantResults[i] != View.VISIBLE) {
                    allRequestedPermissionsGranted = false;
                }
            }
            if (!allRequestedPermissionsGranted || 1 != requestCode) {
                stopScan();
            } else {
                startScan();
            }
        }
    }

    /* access modifiers changed from: protected */
    public void onStop() {
        super.onStop();
        stopScan();
        unregisterReceiver(this.mBluetoothStateReceiver);
    }

    /* access modifiers changed from: private */
    public void stopScan() {
        this.mTracker.send(new EventBuilder().setCategory("Scan").setAction("Stop Scan").build());
        this.mScanActive = false;
        this.pgbScan.setVisibility(View.INVISIBLE);
        this.btnScan.setText("Start");
        this.btnScan.setBackgroundResource(R.drawable.button_background_start_scan_pressed);
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (VERSION.SDK_INT >= 21) {
            if (bluetoothAdapter.getBluetoothLeScanner() != null) {
                bluetoothAdapter.getBluetoothLeScanner().stopScan(this.callbackLollipop);
            }
        } else if (VERSION.SDK_INT >= 18) {
            bluetoothAdapter.stopLeScan(this.callbackJellyBean);
        }
    }
}
