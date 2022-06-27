package com.example.accessibilityplay;


import android.accessibilityservice.AccessibilityGestureEvent;
import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.accessibilityservice.GestureDescription;
import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.os.Binder;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import java.io.IOException;
import java.sql.Array;
import java.util.Arrays;

public class MyAccessibilityService extends AccessibilityService {
    public static MyAccessibilityService myAccessibilityService;
    public static boolean revertDirection = false;
    public static long tapDelay = 0;
    final static String TAG = "LALALA";
    private Window window;
    private int statusBarHeight;

    @Override
    public void onAccessibilityEvent(AccessibilityEvent accessibilityEvent) {
        AccessibilityNodeInfo accessibilityNodeInfo = accessibilityEvent.getSource();
        if (AccessibilityEvent.eventTypeToString(accessibilityEvent.getEventType()).equals("TYPE_WINDOW_CONTENT_CHANGED")) {
            return;
        }
//        Log.d(TAG, "onAccessibilityEvent: \n" + accessibilityEvent);
//        AccessibilityNodeInfo rootWindow = getRootInActiveWindow();
//        Log.d(TAG, "onServiceConnected: \n" + rootWindow);
    }

    public MyAccessibilityService() {
        super();
    }

    @Override
    public void onInterrupt() {
        Log.d(TAG, "Interrupted");
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.d(TAG, "onUnbind: unbind");
        try {
            window.close();
        } catch (IllegalArgumentException ignore){

        }

        return super.onUnbind(intent);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        int result = getStatusBarWidth();
        Configurations.configuration.setStatusBarHeight(result);
        Log.d(TAG, "onServiceConnected: " + result);
        statusBarHeight = result;

        AccessibilityServiceInfo info = new AccessibilityServiceInfo();
        info.eventTypes = AccessibilityEvent.TYPES_ALL_MASK;
        info.feedbackType = AccessibilityServiceInfo.FEEDBACK_HAPTIC;
        info.notificationTimeout = 10;
        this.setServiceInfo(info);
        Log.d(TAG, "onServiceConnected: Service connected");
        myAccessibilityService = this;
        window = new Window(this);
        window.open();
//        performPinch(0, 1000);
    }
    public void performSingleTap(float x, float y, long delay, long duration) {
        Path path = new Path();
        Log.d(TAG, "performClick: " + statusBarHeight);
        path.moveTo(x, y);
        GestureDescription.StrokeDescription clickStroke = new GestureDescription.StrokeDescription(path, delay, duration);
        GestureDescription.Builder clickBuilder = new GestureDescription.Builder();
        clickBuilder.addStroke(clickStroke);
        boolean res = this.dispatchGesture(clickBuilder.build(), null, null);
        Log.d(TAG, "performClick: result is " + res);
    }

    @Override
    public boolean onGesture(@NonNull AccessibilityGestureEvent gestureEvent) {
        Log.d(TAG, "onGesture: \n" + gestureEvent);
        return super.onGesture(gestureEvent);
    }

    public void performSwipe(int numFigure, float[] x1, float[] y1, float[] x2, float[] y2, long delay, long duration) {
        Log.d(TAG, "performSwipe: \n" + numFigure);
        Path[] path = new Path[numFigure];
        GestureDescription.StrokeDescription[] swipeStroke = new GestureDescription.StrokeDescription[numFigure];
        GestureDescription.Builder swipeBuilder = new GestureDescription.Builder();
        for (int i = 0; i < numFigure; i++) {
            path[i] = new Path();
            if (revertDirection) {
                path[i].moveTo(x2[i], y2[i]);
                path[i].lineTo(x1[i], y1[i]);
            } else {
                path[i].moveTo(x1[i], y1[i]);
                path[i].lineTo(x2[i], y2[i]);
            }
            Log.d(TAG, "performSwipe: \n" + x1[i] + ' ' + y1[i] + ' ' + x2[i] + ' ' + y2[i]);
            swipeStroke[i] = new GestureDescription.StrokeDescription(path[i], delay, duration);
            swipeBuilder.addStroke(swipeStroke[i]);
        }
        boolean res = this.dispatchGesture(swipeBuilder.build(), null, null);
        Log.d(TAG, "performSwipe: " + res);
    }

    public void performDoubleTap(float x, float y, long delay, long duration, long interval) {
        Path path = new Path();
        path.moveTo(x, y);
//        path.lineTo(x + 1, y + 2 * statusBarHeight);
        GestureDescription.StrokeDescription clickStroke = new GestureDescription.StrokeDescription(path, delay, duration);
        GestureDescription.StrokeDescription clickStroke2 = new GestureDescription.StrokeDescription(path, delay + interval, duration);
        GestureDescription.Builder clickBuilder = new GestureDescription.Builder();
        clickBuilder.addStroke(clickStroke);
        clickBuilder.addStroke(clickStroke2);
        boolean res = this.dispatchGesture(clickBuilder.build(), null, null);
        Log.d(TAG, "performDoubleTap: result 1 is " + res);
    }

    public void performPinch(long delay, long duration) {
        Path path1 = new Path();
        path1.moveTo(300, 1000);
        path1.lineTo(300, 500);
        Path path2 = new Path();
        path2.moveTo(400, 1200);
        path2.lineTo(400, 1700);
        GestureDescription.StrokeDescription pinchStroke1 = new GestureDescription.StrokeDescription(path1, delay, duration);
        GestureDescription.StrokeDescription pinchStroke2 = new GestureDescription.StrokeDescription(path2, delay, duration);
        GestureDescription.Builder pinchBuilder = new GestureDescription.Builder();
        pinchBuilder.addStroke(pinchStroke1).addStroke(pinchStroke2);
        boolean res = this.dispatchGesture(pinchBuilder.build(), null, null);
        Log.d(TAG, "performPinch: result is " + res);
    }
    public int getStatusBarWidth() {
        int result = 0;
        int resourceId = this.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = this.getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }
}
