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

import java.io.*;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Antonio Yip
 */
public class FileUtil {

    public static List readFileToArrayList(String fileName) throws FileNotFoundException, IOException {
        String line = "";
        List data = new ArrayList();
        BufferedReader br = new BufferedReader(new FileReader(fileName));

        while ((line = br.readLine()) != null) {
            data.add(line);
        }

        return data;
    }

    public static List readFileToArrayList(File file) {
        List list = new ArrayList();
        try {
            list = readFileToArrayList(file.getPath());
        } catch (FileNotFoundException ex) {
            Logger.getLogger(FileUtil.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(FileUtil.class.getName()).log(Level.SEVERE, null, ex);
        }
        return list;
    }

    /**
     * Serialise the object o (and any serialisable objects it refers to) and
     * store its serialised state in File f.
     *
     * @param o
     * @param file
     * @throws IOException
     */
    public static void store(Serializable o, File file) throws IOException {
        ObjectOutputStream out = // The class for serialization
                new ObjectOutputStream(new FileOutputStream(file));

        try {
            out.writeObject(o);
        } catch (IOException ex) {
            throw ex;
        } finally {
            try {
                out.close();
            } catch (IOException ex) {
                throw ex;
            }
        }
    }

    public static void saveToFile(String string, File file, boolean append) throws IOException {
        FileWriter fw = null;

        try {
            fw = new FileWriter(file, append);

            PrintWriter out = new PrintWriter(fw);

            out.print(string);
            out.close();

        } catch (IOException ex) {
            throw ex;
        } finally {
            try {
                fw.close();
            } catch (IOException ex) {
                throw ex;
            }
        }
    }

    public static void saveToFile(String string, File file) throws IOException {
        saveToFile(string, file, false);
    }

    public static void appendToFile(String string, File file) throws IOException {
        saveToFile(string, file, true);
    }

    /**
     * Deserialize the contents of File f and return the resulting object
     *
     * @param f
     * @return
     * @throws IOException
     * @throws ClassNotFoundException
     */
    public static Object load(File f) throws IOException, ClassNotFoundException {
        ObjectInputStream in = // The class for de-serialization
                new ObjectInputStream(new FileInputStream(f));
        return in.readObject();
    }

    public static String saveToFile(Serializable object, File file) throws IOException {
        store(object, file);
        return object.toString() + " saved at " + file.toString();
    }

    public static String readTextFile(File aFile) throws IOException {
        return read(new BufferedReader(new FileReader(aFile)));
    }

    public static String readInputStream(InputStream inputStream) throws IOException {
        return read(new BufferedReader(new InputStreamReader(inputStream)));
    }

    public static String read(BufferedReader bufferedReader) throws IOException {
        StringBuilder contents = new StringBuilder();

        try {
            String line = null;
            while ((line = bufferedReader.readLine()) != null) {
                contents.append(line);
                contents.append(System.getProperty("line.separator"));
            }
        } finally {
            bufferedReader.close();
        }

        return contents.toString();
    }

    public static String readResource(String name) throws IOException {
        return readInputStream(ClassLoader.getSystemClassLoader().getResourceAsStream(name));
    }

    public static String getFolder(String folder, String subFolder) {
        File f = new File(folder);

        try {
            SystemUtil.runShellCommand("mkdir '" + folder + subFolder + "'");
        } catch (IOException ex) {
        }

        return folder + subFolder + File.separator;
    }

    public static String getUserFolder() {
        if (isMac()) {
            return System.getProperty("user.home") + File.separator;
        } else {
            return null;
        }
    }

    public static String getAppFolder() {
        if (isMac()) {
            return File.separator + "Applications" + File.separator;
        } else {
            return null;
        }
    }

    public static String getOsName() {
        return System.getProperty("os.name");
    }

    public static boolean isMac() {
        return getOsName().toLowerCase().contains("mac");
    }

    public static String getModified(File file) {
        return DateUtil.getDateTimeString(file.lastModified());
    }

    public static String getModified(String file) {
        File f = new File(file);

        return getModified(f);
    }
    
    public static String getLogFolder(String folder) {
        Calendar now = DateUtil.getCalendarDate();
        String yearString = DateUtil.getTimeStamp(now, "yyyy");
        String monthString = DateUtil.getTimeStamp(now, "MM");
        String dateString = DateUtil.getTimeStamp(now, "dd");

        return getFolder(
                getFolder(
                getFolder(
                getFolder(
                folder,
                "Logs"),
                yearString),
                yearString + "-" + monthString),
                yearString + "-" + monthString + "-" + dateString);
    }
}
