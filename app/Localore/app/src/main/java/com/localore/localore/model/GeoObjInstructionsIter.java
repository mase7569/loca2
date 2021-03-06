package com.localore.localore.model;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.localore.localore.LocaUtils;
import com.localore.localore.R;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Helper for building geo-objects.
 * Iterator that fetches geo-object data and iterates over
 * instructions for building the geo-objects.
 */
public class GeoObjInstructionsIter {
    private static final String[] OVERPASS_ENDPOINTS = new String[]{
            "https://overpass-api.de/api/interpreter",
            "http://overpass.openstreetmap.ru/cgi/interpreter",
            "https://overpass.kumi.systems/api/interpreter"};

    private URL url;
    private Scanner scanner = null;

    public GeoObjInstructionsIter(NodeShape area, Context context) {
        try {
            this.url = getQueryURL(area, context);
            Log.i("<ME>", "Query: " + url.toString());
        }
        catch(MalformedURLException e) {
            throw new RuntimeException(e.toString());
        }
    }

    /**
     * @return URL that provides geo-objects-data.
     */
    private URL getQueryURL(NodeShape area, Context context) throws MalformedURLException {
        double[] bs = area.getBounds();
        String bs_str = String.format("%s,%s,%s,%s", bs[1], bs[0], bs[3], bs[2]);
        String poly_str = area.toRawString();

        String query = LocaUtils.readTextFile(R.raw.ql_query, context);
        query = query.replace("{{bbox}}", bs_str);
        query = query.replace("{{poly}}", poly_str);
        query = URLEncoder.encode(query);

        String endpoint = OVERPASS_ENDPOINTS[0];
        return new URL(endpoint + "?data=" + query);
    }

    /**
     * Call before next().
     */
    public void open() throws IOException {
        try {
            URLConnection con = this.url.openConnection();
            con.setConnectTimeout(0);
            con.setReadTimeout(0);
            InputStream in = con.getInputStream();
            this.scanner = new Scanner(in);
        }
        catch (IOException e) {
            Log.d("<ME>", e.toString() + ". URL: " + url.toString());
            throw e;
        }
    }

    /**
     * Returns instructions to build geo-objects. Closes connection
     * if called when no more instructions.
     *
     * e.g:
     * ["lat_lon 59.0206802 18.5453333",
     *  "lat_lon ... ...",
     *  "id NODE/43664464",
     *  "version 1",
     *  "name Ängsön-Marskär",
     *  "tag natural=coastline",
     *  "tag place=island"]
     *
     * @return Next instruction, or NULL if no more.
     */
    public List<String> next() {
        List<String> instr = new ArrayList<>();

        while (this.scanner.hasNextLine()) {
            String line = scanner.nextLine().trim();

            if (line.equals("</NODE>") ||
                    line.equals("</WAY>") ||
                    line.equals("</REL>")) {

                return instr;
            }

            String res;
            if ((res=extractID(line)) != null) {
                instr.add("id " + res);
            }
            if ((res=extractName(line)) != null) {
                instr.add("name " + res);
            }
            else if ((res=extractPoint(line)) != null) {
                instr.add("lat_lon " + res);
            }
            else if ((res=extractVersion(line)) != null) {
                instr.add("version " + res);
            }
            else if ((res=extractTag(line)) != null) {
                instr.add("tag " + res);
            }
        }

        scanner.close();
        return null;
    }

    /**
     * @return "type/number" or null.
     * type = NODE/WAY/REL
     */
    private String extractID(String line) {
        Pattern p = Pattern.compile("<(.*?) id=\"(.*?)\">");
        Matcher m = p.matcher(line);
        if (m.find()) return m.group(1) + "/" + m.group(2);
        else return null;
    }

    /**
     * @return "Name" or NULL.
     */
    private String extractName(String line) {
        Pattern p = Pattern.compile("k=\"name\" v=\"(.*?)\"");
        Matcher m = p.matcher(line);
        if (m.find()) return m.group(1);
        else return null;
    }

    /**
     * @return "lat lon" or NULL.
     */
    private String extractPoint(String line) {
        Pattern p = Pattern.compile("lat=\"(.*?)\" lon=\"(.*?)\"");
        Matcher m = p.matcher(line);
        if (m.find()) return m.group(1) + " " + m.group(2);
        else return null;
    }

    /**
     * @return "ver" or NULL.
     */
    private String extractVersion(String line) {
        Pattern p = Pattern.compile("k=\"version\" v=\"(.*?)\"");
        Matcher m = p.matcher(line);
        if (m.find()) return m.group(1);
        else return null;
    }

    /**
     * @return "key=value" or NULL.
     */
    private String extractTag(String line) {
        Pattern p = Pattern.compile("k=\"(.*?)\" v=\"(.*?)\"");
        Matcher m = p.matcher(line);

        if (m.find()) return m.group(1) + "=" + m.group(2);
        else return null;
    }
}