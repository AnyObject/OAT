/*
    Open Auto Trading : A fully automatic equities trading platform with machine learning capabilities
    Copyright (C) 2015 AnyObject Ltd.

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package OAT.util;

import java.awt.Color;
import java.io.IOException;
import java.util.logging.*;
import javax.swing.JTextPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import OAT.trading.Main;

/**
 *
 * @author Antonio Yip
 */
public class ThreadLogger extends Logger {

//    private LogFormatter formatter;
//    private ModifiedFileHandler fileHandler;
    private ModifiedMemoryHandler memoryHandler;
    private WindowHandler windowHandler;
    private MailHandler mailHandler;
    private JTextPane logTextPane;
    private Mailer mailer;
//    private String logFile;
    private String lastMessage;
    private boolean isLogChanged;

    public ThreadLogger() {
        super(Main.class.getName(), null);
        super.setUseParentHandlers(true);
    }

    public ThreadLogger(String name, String resourceBundleName, Level level) {
        super(name, resourceBundleName);

        try {
            setUseParentHandlers(false);
            java.util.logging.Logger.getLogger(name);

            Formatter formatter = new LogFormatter();

            //log file
//            fileHandler = newFileFormatter(formatter, level);
//            logFile = Main.logFolder + DateUtil.getTimeStamp(
//                    DateUtil.SIMPLE_DATE_FORMAT) + "-" + name + ".log";
//            fileHandler = new ModifiedFileHandler(getLogFile());
//            fileHandler.setFormatter(formatter);
//            fileHandler.setLevel(level);

            //memory
            memoryHandler = new ModifiedMemoryHandler(newFileFormatter(formatter));
            memoryHandler.setLevel(level);

            //log window
            windowHandler = new WindowHandler();
            windowHandler.setFormatter(formatter);
            windowHandler.setLevel(level);
            logTextPane = Main.frame.getLogTextPane(name);

            //mail
            mailHandler = new MailHandler();
            mailHandler.setFormatter(formatter);
            mailHandler.setLevel(Level.SEVERE);

            LogManager.getLogManager().addLogger(this);
            addHandler(memoryHandler);
            addHandler(windowHandler);
            addHandler(mailHandler);

            super.setLevel(Level.ALL);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public JTextPane getLogTextPane() {
        return logTextPane;
    }

    public Mailer getMailer() {
        return mailer;
    }

    public void setMailer(Mailer mailer) {
        this.mailer = mailer;
    }

//    private String getLogFile() {
//        return Main.getLogFolder()
//                + DateUtil.getTimeStamp(DateUtil.SIMPLE_DATE_FORMAT)
//                + "-" + getName() + ".log";
//    }
    private FileHandler newFileFormatter(Formatter formatter) throws IOException {
        String logFile = FileUtil.getLogFolder(Main.tradingFolder)
                + DateUtil.getTimeStamp(DateUtil.SIMPLE_DATE_FORMAT)
                + "-" + getName() + ".log";

        FileHandler newFileHandler = new FileHandler(logFile, true);
        newFileHandler.setFormatter(formatter);
        newFileHandler.setLevel(Level.ALL);

        return newFileHandler;
    }

//    public synchronized void renewLogFile() {
//        try {
//            fileHandler = newFileFormatter(fileHandler.getFormatter(), fileHandler.getLevel());
//        } catch (IOException ex) {
//        }
//
//
//        memoryHandler = new ModifiedMemoryHandler(fileHandler);
//    }
    @Override
    public void log(LogRecord record) {
        if (record != null && isLoggable(record.getLevel())) {
            super.log(record);
            lastMessage = record.getMessage();
            isLogChanged = true;
        }
    }

    class LogFormatter extends Formatter {

        @Override
        public String format(LogRecord record) {
            StringBuilder sb = new StringBuilder();
            Throwable throwable = record.getThrown();

//            String dateStamp =
//                    DateUtil.getTimeStamp("E").substring(0, 2).toUpperCase()
//                    + " " + DateUtil.getTimeStamp(record.getMillis())
//                    + "  " + record.getLevel().getName() + ": ";

            sb.append(DateUtil.getTimeStamp("E").substring(0, 2).toUpperCase()).
                    append(" ").append(DateUtil.getTimeStamp(record.getMillis())).
                    append("  ").append(record.getLevel().getName()).append(": ");

            if (record.getMessage() != null) {
                sb.append(formatMessage(record)).append(" ");
            }
            if (record.getLevel() == Level.SEVERE && throwable != null) {
                sb.append(TextUtil.LINE_SEPARATOR).append("\t").append(throwable);
                sb.append(TextUtil.LINE_SEPARATOR).append("\t").append(throwable.getCause());
                for (StackTraceElement e : throwable.getStackTrace()) {
                    sb.append(TextUtil.LINE_SEPARATOR).append("\t").append(e);
                }
            }

//            String message = sb.toString();
//            lastMessage = message;
//            return dateStamp +  message + TextUtil.LINE_SEPARATOR;

            return sb.append(TextUtil.LINE_SEPARATOR).toString();
        }
    }

    class WindowHandler extends Handler {

        @Override
        public synchronized void publish(LogRecord record) {
            if (record == null || !isLoggable(record)) {
                return;
            }

            try {
                Color color;
                if (record.getLevel() == Level.SEVERE) {
                    color = Color.RED;
                    Logger logger = java.util.logging.Logger.getLogger(record.getSourceClassName());

                    if (record.getParameters() != null) {
                        logger.log(record.getLevel(), record.getMessage(), record.getParameters());

                    } else if (record.getThrown() != null) {
                        logger.log(record.getLevel(), record.getMessage(), record.getThrown());

                    } else {
                        logger.log(record.getLevel(), record.getMessage());
                    }

                } else if (record.getLevel() == Level.WARNING) {
                    color = new Color(128, 64, 0);
                } else {
                    color = Color.BLACK;
                }

                Main.frame.appendLogText(logTextPane, getFormatter().format(record), color);

            } catch (Exception e) {
//                System.out.println(e.getMessage());
//                if (!e.getMessage().contains("Interrupted attempt to aquire write lock")) {
                reportError(null, e, ErrorManager.WRITE_FAILURE);
//                }
            }
        }

        @Override
        public void flush() {
        }

        @Override
        public void close() throws SecurityException {
        }

        @Override
        public void setLevel(Level newLevel) {
            if (newLevel.intValue() <= Level.INFO.intValue()) {
                super.setLevel(newLevel);
            } else {
                super.setLevel(Level.INFO);
            }
        }
    }

    class MailHandler extends Handler {

        @Override
        public synchronized void publish(LogRecord record) {
            if (record == null || !isLoggable(record)) {
                return;
            }

            if (mailer != null && !record.getMessage().equals(lastMessage)) {
                mailer.error(getFormatter().format(record));
            }
        }

        @Override
        public void flush() {
        }

        @Override
        public void close() throws SecurityException {
        }
    }

    class ModifiedMemoryHandler extends MemoryHandler {

        public ModifiedMemoryHandler(FileHandler fileHandler) {
            super(fileHandler, 1000000, Level.WARNING);
        }

        @Override
        public void setLevel(Level newLevel) {
            if (newLevel.intValue() <= Level.FINE.intValue()) {
                super.setLevel(newLevel);
            } else {
                super.setLevel(Level.FINE);
            }
        }
    }

//    class ModifiedFileHandler extends FileHandler {
//
//        public ModifiedFileHandler(String logFile) throws IOException, SecurityException {
//            super(logFile, true);
//        }
//
//        @Override
//        public void setLevel(Level newLevel) {
//            if (newLevel.intValue() <= Level.FINE.intValue()) {
//                super.setLevel(newLevel);
//            } else {
//                super.setLevel(Level.FINE);
//            }
//        }
//    }
    public void clearLogTextPane() {
        if (logTextPane == null) {
            return;
        }

        Document logText = logTextPane.getDocument();

        try {
            logText.remove(0, logText.getLength());
        } catch (BadLocationException ex) {
            log(Level.SEVERE, null, ex);
        }
    }

    public void pushLog(boolean pushLogMessage) {
        if (isLogChanged) {
            if (pushLogMessage) {
                fine("Push logs.");
            }

            memoryHandler.push();
            isLogChanged = false;
        }
    }

    public void pushLog() {
        pushLog(true);
    }

    @Override
    public void setLevel(Level newLevel) {
        if (newLevel != null) {
            setAllLevels(newLevel);

            log(Level.INFO, "Log Level: Window={0}, Memory={1}, Mail={2}",//, Mail={3}",
                    new Object[]{
                        windowHandler.getLevel().getName(),
                        memoryHandler.getLevel().getName(),
                        //                        fileHandler.getLevel().getName(),
                        mailHandler.getLevel().getName()
                    });
        }
    }

    @Override
    public boolean isLoggable(Level level) {
        return level.intValue() >= memoryHandler.getLevel().intValue();
    }

    @Override
    public Level getLevel() {
        return memoryHandler.getLevel();
    }

    private void setAllLevels(Level level) {
        windowHandler.setLevel(level);
        memoryHandler.setLevel(level);
//        fileHandler.setLevel(level);
    }
}
