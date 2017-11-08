package com.la2eden.log.handler;

import com.la2eden.log.formatter.SocketFormatter;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.*;

public class SocketHandler extends StreamHandler
{
    private static Boolean ENABLED;
    private static Integer SERVER_PORT;
    private static String TOKEN;

    private static DataOutputStream stream;
    private static ServerSocket socket;
    private static Thread thrd;
    private static Writer writer;

    private void configure() {
        LogManager manager = LogManager.getLogManager();
        String cname = getClass().getName();

        ENABLED = Boolean.valueOf(manager.getProperty(cname + ".enabled"));
        SERVER_PORT = Integer.valueOf(manager.getProperty(cname + ".port"));
        TOKEN = manager.getProperty(cname + ".token");

        setLevel(Level.parse(manager.getProperty(cname + ".level")));

        try {
            setEncoding(manager.getProperty(cname +".encoding"));
        } catch (Exception ex) {
            try {
                setEncoding(null);
            } catch (Exception e) {
                // code
            }
        }
    }

    public SocketHandler() throws IOException {
        // We are going to use the logging defaults.
        configure();

        connect();
    }

    public SocketHandler(int port) throws IOException {
        configure();
        SERVER_PORT = port;

        connect();
    }

    public void connect() {
        if (ENABLED) {
            Runnable listener = () -> {
                try {
                    socket = new ServerSocket(SERVER_PORT);
                    Socket client = null;

                    while (client == null) {
                        client = socket.accept();
                    }

                    stream = new DataOutputStream(client.getOutputStream());
                    writer = new OutputStreamWriter(stream);

                    InputStreamReader isr = new InputStreamReader(client.getInputStream());
                    BufferedReader br = new BufferedReader(isr);
                    String readTkn = br.readLine();
                    String token;
                    if (readTkn.startsWith("tkn:")) {
                        token = readTkn.replaceAll("tkn:", "");

                        if ((token == null) || (!token.equals(TOKEN))) {
                            writer.write("Invalid access token!");
                            close();
                        }

                        // Connected
                        writer.write("EHLO");
                        flush();
                    }

                    //setOutputStream(stream);
                } catch (IOException e) {
                    // code
                }
            };

            thrd = new Thread(listener);
            thrd.start();
        }
    }

    @Override
    public void publish(LogRecord record)
    {
        if (!isLoggable(record)) {
            return;
        }

        if (ENABLED) {
            String msg = SocketFormatter.format(record);

            try {
                writer.write(msg);
                flush();
            } catch (IOException e) {
                close();
            }
        }
    }

    @Override
    public void flush() {
        if (ENABLED && writer != null) {
            try {
                writer.flush();
                stream.flush();
            } catch (IOException e) {
                // code
            }
        }
    }

    @Override
    public void close() throws SecurityException {
        if ((socket != null) && (writer != null) && (!thrd.isInterrupted())) {
            try {
                flush();

                writer.close();
                socket.close();

                thrd.interrupt();
            } catch (IOException ix) {
                // drop through.
            }
        }

        writer = null;
        socket = null;
        thrd = null;

        // Start again
        connect();
    }
}
