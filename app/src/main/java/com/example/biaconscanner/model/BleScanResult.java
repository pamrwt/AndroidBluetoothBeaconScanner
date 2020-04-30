package com.example.biaconscanner.model;

import android.bluetooth.BluetoothDevice;
import android.text.TextUtils;

import com.neovisionaries.bluetooth.ble.advertising.ADStructure;
import com.neovisionaries.bluetooth.ble.advertising.IBeacon;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import com.neovisionaries.bluetooth.ble.advertising.ADPayloadParser;
import com.neovisionaries.bluetooth.ble.advertising.ADStructure;
import com.neovisionaries.bluetooth.ble.advertising.Flags;
import com.neovisionaries.bluetooth.ble.advertising.IBeacon;
import com.neovisionaries.bluetooth.ble.advertising.LocalName;
import com.neovisionaries.bluetooth.ble.advertising.TxPowerLevel;
import com.neovisionaries.bluetooth.ble.advertising.UUIDs;



public class BleScanResult {
    private static final String TAG = "BleScanResult";
    private List<ADStructure> mAdditionalData;
    private IBeacon mBeaconData;
    private final BluetoothDevice mDevice;
    private Flags mFlags;
    private LocalName mLocalName;
    private final int mRssi;
    private TxPowerLevel mTxPowerLevel;
    private UUIDs mUUIDs;

    public BleScanResult(BluetoothDevice device, byte[] scanResponse, int rssi) {
        this.mDevice = device;
        this.mRssi = rssi;
        for (ADStructure currStructure : ADPayloadParser.getInstance().parse(scanResponse)) {
            if (currStructure instanceof IBeacon) {
                this.mBeaconData = (IBeacon) currStructure;
            } else if (currStructure instanceof UUIDs) {
                this.mUUIDs = (UUIDs) currStructure;
            } else if (currStructure instanceof TxPowerLevel) {
                this.mTxPowerLevel = (TxPowerLevel) currStructure;
            } else if (currStructure instanceof LocalName) {
                this.mLocalName = (LocalName) currStructure;
            } else if (currStructure instanceof Flags) {
                this.mFlags = (Flags) currStructure;
            } else {
                if (this.mAdditionalData == null) {
                    this.mAdditionalData = new ArrayList();
                }
                this.mAdditionalData.add(currStructure);
            }
        }
    }

    public IBeacon getBeaconData() {
        return this.mBeaconData;
    }

    public UUIDs getUUIDs() {
        return this.mUUIDs;
    }

    public TxPowerLevel getTxPowerLevel() {
        return this.mTxPowerLevel;
    }

    public LocalName getLocalName() {
        return this.mLocalName;
    }

    public Flags getFlags() {
        return this.mFlags;
    }

    public List<ADStructure> getAdditionalData() {
        return this.mAdditionalData;
    }

    public boolean isBeacon() {
        return this.mBeaconData != null;
    }

    public int getPower() {
        if (this.mBeaconData != null) {
            return this.mBeaconData.getPower();
        }
        if (this.mTxPowerLevel != null) {
            return this.mTxPowerLevel.getLevel();
        }
        return 10;
    }

    public BluetoothDevice getDevice() {
        return this.mDevice;
    }

    public int getRSSI() {
        return this.mRssi;
    }

    public double calcDistance() {
        if (getPower() >= 0 || this.mRssi == 0) {
            return -1.0d;
        }
        double ratio = (((double) this.mRssi) * 1.0d) / ((double) getPower());
        if (ratio < 1.0d) {
            return Math.pow(ratio, 10.0d);
        }
        return (0.89976d * Math.pow(ratio, 7.7095d)) + 0.111d;
    }

    public String toString() {
        String str;
        StringBuilder scanResponseData = new StringBuilder();
        scanResponseData.append(scanResponseData.length() > 0 ? ", " : "").append("[distance=" + calcDistance() + "]");
        scanResponseData.append(scanResponseData.length() > 0 ? ", " : "").append("[rssi=" + this.mRssi + "]");
        if (this.mBeaconData != null) {
            scanResponseData.append(scanResponseData.length() > 0 ? ", " : "").append("[UUID=" + this.mBeaconData.getUUID() + "], [Major=" + this.mBeaconData.getMajor() + "], [Minor=" + this.mBeaconData.getMinor() + "], [Power=" + this.mBeaconData.getPower() + "]");
        }
        if (this.mUUIDs != null) {
            scanResponseData.append(scanResponseData.length() > 0 ? ", " : "").append("[UUIDs=" + Arrays.toString(this.mUUIDs.getUUIDs()) + "]");
        }
        if (this.mTxPowerLevel != null) {
            scanResponseData.append(scanResponseData.length() > 0 ? ", " : "").append("[power=" + this.mTxPowerLevel.getLevel() + "]");
        }
        if (this.mDevice != null) {
            scanResponseData.append(scanResponseData.length() > 0 ? ", " : "").append("[address=" + this.mDevice.getAddress() + "]");
        }
        if (this.mLocalName != null) {
            scanResponseData.append(scanResponseData.length() > 0 ? ", " : "").append("[localName=" + this.mLocalName.getLocalName() + "]");
        }
        if (this.mFlags != null) {
            scanResponseData.append(scanResponseData.length() > 0 ? ", " : "").append("[flags=" + this.mFlags + "]");
        }
        if (this.mAdditionalData != null) {
            for (ADStructure currStructure : this.mAdditionalData) {
                if (scanResponseData.length() > 0) {
                    str = ", ";
                } else {
                    str = "";
                }
                scanResponseData.append(str).append("[" + currStructure.getClass().getSimpleName() + "=" + currStructure + "]");
            }
        }
        return scanResponseData.toString();
    }

    public String getName() {
        if (this.mLocalName != null && !TextUtils.isEmpty(this.mLocalName.getLocalName())) {
            return this.mLocalName.getLocalName();
        }
        if (this.mDevice == null || TextUtils.isEmpty(this.mDevice.getName())) {
            return null;
        }
        return this.mDevice.getName();
    }
}
