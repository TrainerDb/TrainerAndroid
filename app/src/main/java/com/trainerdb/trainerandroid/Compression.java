package com.trainerdb.trainerandroid;

import android.util.Base64;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterInputStream;

/**
 * Created by dcotrim on 01/08/2016.
 */
public class Compression {
    public static String compress(String s) {
        DeflaterOutputStream def = null;
        String compressed = null;
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            // create deflater without header
            def = new DeflaterOutputStream(out, new Deflater(Deflater.DEFAULT_COMPRESSION, false));
            def.write(s.getBytes());
            def.close();
            compressed = Base64.encodeToString(out.toByteArray(), Base64.DEFAULT);
            System.out.println(compressed);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return compressed;
    }

    public static String decompress(String s) {
        InflaterInputStream inf = null;
        StringBuilder out = new StringBuilder();
        try {
            inf = new InflaterInputStream(new ByteArrayInputStream(Base64.decode(s, Base64.DEFAULT)));

            final int bufferSize = 1024;
            final char[] buffer = new char[bufferSize];

            Reader in =  new InputStreamReader(inf);

            for (; ; ) {
                int rsz = in.read(buffer, 0, buffer.length);
                if (rsz < 0)
                    break;
                out.append(buffer, 0, rsz);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return out.toString();
    }
}
