package com.trainerdb.trainerandroid.data;

import com.trainerdb.trainerandroid.Compression;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by dcotrim on 20/09/2016.
 */
public class WorkoutData implements Serializable{
    public String key;
    public String description;
    public String goals;
    public String name;
    public String source;
    public String pointsDeflate;

    public float intensityFactor;
    public long totalTicks;
    public long createdAt;
    public long tss;

    private transient ErgFile erg;

    public ErgFile getErg() {
        if (this.erg == null) {
            List<ErgPoint> ergPoints = new ArrayList<>();
            String inflated = Compression.decompress(this.pointsDeflate);
            try {
                JSONArray array = new JSONArray(inflated);
                for (int i = 0; i < array.length(); i++) {
                    JSONObject jObject = array.getJSONObject(i);
                    ergPoints.add(new ErgPoint(jObject.getDouble("t") * 1000, jObject.getDouble("w")));
                }
            } catch (Exception ex) {
            }

            this.erg = new ErgFile(ergPoints, null, null);
        }
        return this.erg;
    }


}
