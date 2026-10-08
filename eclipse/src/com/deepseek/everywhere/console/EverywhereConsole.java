package com.deepseek.everywhere.console;

import java.text.SimpleDateFormat;
import java.util.Date;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleManager;
import org.eclipse.ui.console.MessageConsole;
import org.eclipse.ui.console.MessageConsoleStream;

/**
 * Canal de console integrado para o Everywhere (DeepSeek Harness) no Eclipse.
 */
public class EverywhereConsole {

    private static final String CONSOLE_NAME = "Everywhere - DeepSeek Harness";
    private static MessageConsole console;
    private static MessageConsoleStream infoStream;
    private static MessageConsoleStream errStream;
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("HH:mm:ss");

    private static synchronized MessageConsole getConsole() {
        if (console == null) {
            IConsoleManager consoleManager = ConsolePlugin.getDefault().getConsoleManager();
            IConsole[] existing = consoleManager.getConsoles();
            for (IConsole c : existing) {
                if (CONSOLE_NAME.equals(c.getName()) && c instanceof MessageConsole) {
                    console = (MessageConsole) c;
                    break;
                }
            }
            if (console == null) {
                console = new MessageConsole(CONSOLE_NAME, null);
                consoleManager.addConsoles(new IConsole[] { console });
            }

            infoStream = console.newMessageStream();
            errStream = console.newMessageStream();

            Display display = Display.getDefault();
            if (display != null && !display.isDisposed()) {
                display.syncExec(() -> {
                    try {
                        errStream.setColor(display.getSystemColor(SWT.COLOR_RED));
                    } catch (Exception ignored) {
                    }
                });
            }
        }
        return console;
    }

    public static void log(String message) {
        try {
            getConsole();
            String timestamp = DATE_FORMAT.format(new Date());
            infoStream.println("[" + timestamp + "] [INFO] " + message);
        } catch (Exception e) {
            System.out.println("[Everywhere] " + message);
        }
    }

    public static void error(String message) {
        try {
            getConsole();
            String timestamp = DATE_FORMAT.format(new Date());
            errStream.println("[" + timestamp + "] [ERROR] " + message);
        } catch (Exception e) {
            System.err.println("[Everywhere ERROR] " + message);
        }
    }

    public static void show() {
        try {
            MessageConsole c = getConsole();
            ConsolePlugin.getDefault().getConsoleManager().showConsoleView(c);
        } catch (Exception ignored) {
        }
    }

    public static void clear() {
        try {
            if (console != null) {
                console.clearConsole();
            }
        } catch (Exception ignored) {
        }
    }
}
