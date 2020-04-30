package com.example.biaconscanner.view;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.widget.PopupMenu;

import com.example.biaconscanner.R;
import com.example.biaconscanner.model.BleScanResult;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

public class BleResultView extends FrameLayout {
    /* access modifiers changed from: private */
    public ImageButton btnShare;
    private ImageView imgDeviceType;
    private LinearLayout layoutMajorMinor;
    private LinearLayout layoutUUIDsContainer;
    /* access modifiers changed from: private */
    public BleScanResult mBleScanResult;
    private int numOfUUIDs = View.VISIBLE;
    private TextView txtDeviceName;
    private TextView txtDistance;
    private TextView txtLastSeen;
    private TextView txtMajor;
    private TextView txtMinor;
    private RssiView viewRssi;

    public BleResultView(Context context) {
        super(context);
        inflate(context, R.layout.view_ble_scan_result, this);
        this.imgDeviceType = (ImageView) findViewById(R.id.imgDeviceType);
        this.txtDeviceName = (TextView) findViewById(R.id.txtDeviceName);
        this.txtDistance = (TextView) findViewById(R.id.txtDistance);
        this.viewRssi = (RssiView) findViewById(R.id.viewRssi);
        this.txtMajor = (TextView) findViewById(R.id.txtMajor);
        this.txtMinor = (TextView) findViewById(R.id.txtMinor);
        this.layoutUUIDsContainer = (LinearLayout) findViewById(R.id.layoutUUIDsContainer);
        this.layoutMajorMinor = (LinearLayout) findViewById(R.id.layoutMajorMinor);
        this.txtLastSeen = (TextView) findViewById(R.id.txtLastSeen);

    }

    public void bind(BleScanResult bleScanResult) {
        this.numOfUUIDs = 0;
              this.layoutUUIDsContainer.removeAllViews();
              this.mBleScanResult = bleScanResult;
              DecimalFormat decimalFormatter = new DecimalFormat("0.00");
        double distance = bleScanResult.calcDistance();
        if (distance <= -1.0d || distance >= 100.0d) {
            this.txtDistance.setVisibility(View.INVISIBLE);
        } else {
            this.txtDistance.setVisibility(View.VISIBLE);
            this.txtDistance.setText(getContext().getString(R.string.ibeacon_distance, new Object[]{decimalFormatter.format(distance)}));
        }
        String name = bleScanResult.getName();
        if (name != null) {
            this.txtDeviceName.setText(name + " (" + bleScanResult.getDevice().getAddress().toUpperCase() + ")");
        } else {
            this.txtDeviceName.setText(bleScanResult.getDevice().getAddress());
        }
        if (bleScanResult.isBeacon()) {
           this.imgDeviceType.setImageResource(R.drawable.ic_device_type_bluetooth);
            this.layoutMajorMinor.setVisibility(VISIBLE);
            this.txtMajor.setText(String.valueOf(bleScanResult.getBeaconData().getMajor()));
            this.txtMinor.setText(String.valueOf(bleScanResult.getBeaconData().getMinor()));
            addUUID(bleScanResult.getBeaconData().getUUID());
        } else {
           this.imgDeviceType.setImageResource(R.drawable.ic_device_type_bluetooth);
            this.layoutMajorMinor.setVisibility(INVISIBLE);
        }
        this.viewRssi.setRssi(bleScanResult.getRSSI());
        if (!(bleScanResult.getUUIDs() == null || bleScanResult.getUUIDs().getUUIDs() == null)) {
            for (UUID curUUID : bleScanResult.getUUIDs().getUUIDs()) {
                addUUID(curUUID);
            }
        }
        if (this.numOfUUIDs == View.VISIBLE) {
            this.layoutUUIDsContainer.setVisibility(View.GONE);
        } else {
            this.layoutUUIDsContainer.setVisibility(View.VISIBLE);
        }
        this.txtLastSeen.setText(getContext().getString(R.string.last_seen_at_desc, new Object[]{SimpleDateFormat.getTimeInstance().format(new Date())}));
    }

    private void addUUID(UUID uuid) {
        TextView txtUUID = new TextView(getContext());
        txtUUID.setText(uuid.toString().toUpperCase());
        this.layoutUUIDsContainer.addView(txtUUID);
        this.numOfUUIDs++;
    }
}
