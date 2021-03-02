package com.sapher.youtubedl.utils;

import com.sapher.youtubedl.DownloadProgressCallback;

import java.io.IOException;
import java.io.InputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StreamProcessExtractor extends Thread {
    private static final String GROUP_PERCENT = "percent";
    private static final String GROUP_MINUTES = "minutes";
    private static final String GROUP_SECONDS = "seconds";

    private String destination="";

    private InputStream stream;
    private StringBuffer buffer;
    private final DownloadProgressCallback callback;

    private Pattern p = Pattern.compile("\\[download\\]\\s+(?<percent>\\d+\\.\\d)% .* ETA (?<minutes>\\d+):(?<seconds>\\d+)");
    //[download]   0.1% of 2.96MiB at  1.29MiB/s ETA 00:02



    public StreamProcessExtractor(StringBuffer buffer, InputStream stream, DownloadProgressCallback callback) {
        this.stream = stream;
        this.buffer = buffer;
        this.callback = callback;
        this.start();
    }

    public void run() {
        try {
            StringBuilder currentLine = new StringBuilder();
            int nextChar;
            while ((nextChar = stream.read()) != -1) {
                buffer.append((char) nextChar);
                if (nextChar == '\r' && callback != null) {
                    processOutputLine(currentLine.toString());
                    currentLine.setLength(0);
                    continue;
                }
                currentLine.append((char) nextChar);
            }
        } catch (IOException ignored) {

        }
    }

    private void processOutputLine(String line) {


        String pattern = "\n[download] Destination: ";

        if (line.contains(pattern)) {
            int startPattern = line.indexOf(pattern);
            int start = startPattern + pattern.length();
            destination = line.substring(start, line.length()-1).trim();
        }

        Matcher m = p.matcher(line);
        if (m.matches()) {
            float progress = Float.parseFloat(m.group(GROUP_PERCENT));
            long eta = convertToSeconds(m.group(GROUP_MINUTES), m.group(GROUP_SECONDS));
            callback.onProgressUpdate(destination, progress, eta);
        }


    }

    private int convertToSeconds(String minutes, String seconds) {
        return Integer.parseInt(minutes) * 60 + Integer.parseInt(seconds);
    }
}
