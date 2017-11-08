package com.la2eden.log.handler;

import com.la2eden.Server;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.LogRecord;

public class SocketHandler extends Handler
{
    private static Boolean ENABLED;
    private static Integer SERVER_PORT;
    private static String TOKEN;

    private static OutputStreamWriter writer;
    private static DataOutputStream stream;
    private static ServerSocket socket;
    private static Socket client;
    private static Thread thread;

    private void configure() {
        LogManager manager = LogManager.getLogManager();
        String cname = getClass().getName();

        ENABLED = Boolean.valueOf(manager.getProperty(cname + ".enabled"));
        SERVER_PORT = Integer.valueOf(manager.getProperty(cname + ".port"));
        TOKEN = manager.getProperty(cname + ".token");

        setLevel(Level.parse(manager.getProperty(cname + ".level")));
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

    private static String format(LogRecord record) {
        String type = (Server.serverMode == Server.MODE_GAMESERVER) ? "game;" : "login;";
        StringBuilder output = new StringBuilder();
        String date = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date(record.getMillis())) + ";";
        String level = record.getLevel().getName() + ";";
        String message = record.getMessage();

        if (message != null) {
            output.append(type);
            output.append(date);
            output.append(level);
            output.append(message);
        }

        return output.toString();
    }

    private void connect() {
        if (ENABLED) {
            Runnable listener = () -> {
                try {
                    socket = new ServerSocket(SERVER_PORT);

                    while (client == null) {
                        client = socket.accept();
                    }

                    stream = new DataOutputStream(client.getOutputStream());
                    //OutputStreamWriter outWriter = new OutputStreamWriter(stream, "UTF-8");
                    //writer = new PrintWriter(outWriter);
                    writer = new OutputStreamWriter(stream, "UTF-8");

                    InputStreamReader isr = new InputStreamReader(client.getInputStream());
                    BufferedReader br = new BufferedReader(isr);
                    String readTkn = br.readLine();

                    if ((readTkn.startsWith("tkn:"))) {
                        String token = readTkn.replaceAll("tkn:", "");

                        if (!token.equals(TOKEN)) {
                            send("ERR");
                            close();
                        } else {
                            // Connected
                            send("EHLO");
                        }
                    }
                } catch (IOException e) {
                    // code
                }
            };

            thread = new Thread(listener);
            thread.start();
        }
    }

    private void send(String msg) {
        if (client != null) {
            try {
                writer.write(msg);
            } catch (Exception e) {
                System.out.println("Error while sending data: " + e.getMessage());
            }
        }
    }

    public void publish(LogRecord record)
    {
        if (!isLoggable(record)) {
            return;
        }

        if (ENABLED) {
            String msg = format(record);

            send(msg);
        }
    }

    public void flush() {
        if (writer != null) {
            try {
                writer.flush();
                //stream.flush();
            } catch (Exception e) {
                // code
            }
        }
    }

    public void close() {
        if ((socket != null) && (writer != null) && (!thread.isInterrupted())) {
            try {
                flush();

                writer.close();
                stream.close();
                socket.close();

                thread.interrupt();
            } catch (IOException ix) {
                // drop through.
            }

            writer = null;
            stream = null;
            socket = null;
            thread = null;

            // Start again
            connect();
        }
    }
}
