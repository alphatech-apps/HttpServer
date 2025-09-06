package com.jakir.httpserver;

//
// Created by JAKIR HOSSAIN on 9/7/2025.
//

import android.os.Environment;
import android.util.Log;


import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import fi.iki.elonen.NanoHTTPD;

public class MyHttpServer extends NanoHTTPD {

    private static final String TAG = "MyHttpServer";
    private final File rootDir;

    public MyHttpServer(int port) {
        super(port);
        // Root directory = /storage/emulated/0/
        rootDir = Environment.getExternalStorageDirectory();
    }

    @Override
    public Response serve(IHTTPSession session) {
        String uri = session.getUri();
        Log.d(TAG, "Request: " + uri);

        File file = new File(rootDir, uri);

        if (!file.exists()) {
            return newFixedLengthResponse(Response.Status.NOT_FOUND, "text/plain", "404 Not Found");
        }

        if (file.isDirectory()) {
            // List directory
            StringBuilder sb = new StringBuilder("<h1>Index of " + uri + "</h1><ul>");
            File[] files = file.listFiles();
            if (files != null) {
                for (File f : files) {
                    sb.append("<li><a href=\"")
                            .append(uri.endsWith("/") ? uri : uri + "/")
                            .append(f.getName())
                            .append("\">")
                            .append(f.getName())
                            .append("</a></li>");
                }
            }
            sb.append("</ul>");
            return newFixedLengthResponse(Response.Status.OK, "text/html", sb.toString());
        } else {
            try {
                FileInputStream fis = new FileInputStream(file);
                String mime = getMimeTypeForFile(file.getName());
                return newChunkedResponse(Response.Status.OK, mime, fis);
            } catch (Exception e) {
                return newFixedLengthResponse(Response.Status.INTERNAL_ERROR, "text/plain", "Error: " + e.getMessage());
            }
        }
    }

    // Basic MIME type resolver
    public static String getMimeTypeForFile(String filename) {
        if (filename.endsWith(".html") || filename.endsWith(".htm")) return "text/html";
        if (filename.endsWith(".jpg") || filename.endsWith(".jpeg")) return "image/jpeg";
        if (filename.endsWith(".png")) return "image/png";
        if (filename.endsWith(".svg")) return "image/svg+xml";
        if (filename.endsWith(".txt")) return "text/plain";
        return "application/octet-stream";
    }
}
