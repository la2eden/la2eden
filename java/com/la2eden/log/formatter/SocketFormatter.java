package com.la2eden.log.formatter;

import com.la2eden.Server;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.LogRecord;

public class SocketFormatter extends AbstractFormatter {
    public String format(LogRecord record) {
        String type = (Server.serverMode == Server.MODE_GAMESERVER) ? "game;" : "login;";
        StringBuilder output = new StringBuilder();
        String date = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date(record.getMillis())) + ";";
        String level = record.getLevel().getName() + ";";
        String message = record.getMessage();

        if ((message != null) && (!message.isEmpty())) {
            output.append(type);
            output.append(date);
            output.append(level);
            output.append(message);
        }

        return output.toString();
    }
}
