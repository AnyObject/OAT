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

import java.io.File;
import java.io.IOException;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 *
 * @author Antonio Yip
 */
public class SystemUtil {

    public static void runShellCommand(String command) throws IOException {
        new ProcessBuilder("bash", "-c", command).start();
    }

    public static void runJava(String jar, String className, String[] args) throws IOException {
        if (new File(jar).exists()) {
            runShellCommand("java -classpath '" + jar + "' "
                    + className + " "
                    + TextUtil.toString(args, " "));
        }
    }

    public static void runAppleScript(String path, String[] args) throws IOException {
        runAppleScript(new File(path), args);
    }

    public static void runAppleScript(File file, String[] args) throws IOException {
        if (file.exists()) {
            runShellCommand("osascript '" + file.getPath() + "' " + TextUtil.toString(args, " "));
        }
    }

    public static String getVersionNum() {
        String version = "";
        String build = "";
        ResourceBundle bundle = ResourceBundle.getBundle("version");

        try {
            version = bundle.getString("VERSION");
            build = bundle.getString("BUILD");
        } catch (MissingResourceException e) {
        }


        return version + "." + build;
    }

    public static String getVersionDate() {
        String date = "";
        ResourceBundle bundle = ResourceBundle.getBundle("version");

        try {
            date = bundle.getString("DATE");
        } catch (MissingResourceException e) {
        }


        return date;
    }
}
