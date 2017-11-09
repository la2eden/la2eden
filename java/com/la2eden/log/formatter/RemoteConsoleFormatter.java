/*
 * This file is part of the La2Eden project.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.la2eden.log.formatter;

import com.la2eden.Server;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.LogRecord;

/**
 * @author Enkel
 */
public class RemoteConsoleFormatter extends AbstractFormatter {
    public String format(LogRecord record) {
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
}
