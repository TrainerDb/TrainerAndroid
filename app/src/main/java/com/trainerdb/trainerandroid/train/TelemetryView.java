package com.trainerdb.trainerandroid.train;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.trainerdb.trainerandroid.AutoResizeTextView;
import com.trainerdb.trainerandroid.R;

/**
 * Created by Daniel on 07/07/2016.
 */
public class TelemetryView extends LinearLayout {
    private TextView header;
    private AutoResizeTextView data;
    private TelemetryEnum type;

    public TelemetryView(Context context, AttributeSet attrs) {
        super(context, attrs);

        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        inflater.inflate(R.layout.view_telemetry, this, true);

        header = (TextView) getChildAt(0);

        data = (AutoResizeTextView) getChildAt(1);
    }

    public TelemetryView(Context context) {
        this(context, null);
    }

    public void setType(TelemetryEnum type) {
        this.type = type;
        this.header.setText(type.toString());

        switch (type) {
            case CADENCE:
                data.setTextColor(ContextCompat.getColor(this.getContext(), R.color.trainCadence));
                break;
            case LP_CAD:
                data.setTextColor(ContextCompat.getColor(this.getContext(), R.color.trainCadence));
                break;
            case KM:
                data.setTextColor(ContextCompat.getColor(this.getContext(), R.color.trainDistance));
                break;
            case HR:
                data.setTextColor(ContextCompat.getColor(this.getContext(), R.color.trainHearRate));
                break;
            case IF:
                data.setTextColor(ContextCompat.getColor(this.getContext(), R.color.trainPower));
                break;
            case LAP:
                data.setTextColor(ContextCompat.getColor(this.getContext(), R.color.trainLap));
                break;
            case LP_HR:
                data.setTextColor(ContextCompat.getColor(this.getContext(), R.color.trainLapHr));
                break;
            case LP_POWER:
                data.setTextColor(ContextCompat.getColor(this.getContext(), R.color.trainLapPower));
                break;
            case NP:
                data.setTextColor(ContextCompat.getColor(this.getContext(), R.color.trainPower));
                break;
            case POWER:
                data.setTextColor(ContextCompat.getColor(this.getContext(), R.color.trainPower));
                break;
            case POWER_30S:
                data.setTextColor(ContextCompat.getColor(this.getContext(), R.color.trainPower));
                break;
            case SPEED:
                data.setTextColor(ContextCompat.getColor(this.getContext(), R.color.trainSpeed));
                break;
            case TARGET:
                data.setTextColor(ContextCompat.getColor(this.getContext(), R.color.trainTarget));
                break;
            case TSS:
                data.setTextColor(ContextCompat.getColor(this.getContext(), R.color.trainPower));
                break;
        }
    }

    public void setValue(String text) {
        this.data.setText(text);
    }
}
