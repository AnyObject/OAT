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

package OAT.sql;

import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import OAT.trading.Main;
import OAT.util.DateUtil;
import OAT.util.FileUtil;

/**
 *
 * @author Antonio Yip
 */
public abstract class SqlConnect {

    protected String schema;
    protected Connection connection;
    private transient long connectedSince;
    private transient boolean connectionClosed;

    public void connect(String schema) throws SQLException, ClassNotFoundException {
        this.schema = schema;

        Class.forName("com.mysql.jdbc.Driver");

        connection = DriverManager.getConnection(
                "jdbc:mysql://localhost/" + schema,
                schema,
                null);

        connectedSince = DateUtil.getTimeNow();
        connectionClosed = false;
    }

    public void disconnect() throws SQLException {
        if (!isConnectionClosed()) {
            connection.close();
            connectionClosed = true;
        }
    }

    public boolean isConnectionClosed() throws SQLException {
        if (connection == null || connection.isClosed()) {
            connectionClosed = true;

            return true;
        } else {
            return false;
        }
    }

    public void checkConnection() throws SQLException, ClassNotFoundException {
        if (connectionClosed
                || connection.isClosed()
                || DateUtil.getTimeNow() - connectedSince > Main.p_SQL_Time_Out) {
            connect(schema);
        }
    }

    public String getSchema() {
        return schema;
    }

    public void init() throws SQLException, IOException, ClassNotFoundException {
        createTables();
        createProcedures();
    }

    public abstract void createTables() throws SQLException, IOException, ClassNotFoundException;

    public abstract void createProcedures() throws SQLException, IOException, ClassNotFoundException;

    protected static Calendar getCalendar(ResultSet resultSet, String column) throws SQLException {
        return DateUtil.getCalendarDate(
                resultSet.getLong(column),
                DateUtil.getTimeZone(resultSet.getString("timeZone")));
    }

    protected static Object[][] getTable(ResultSet resultSet, String[] columnNames) throws SQLException {
        List<Object[]> table = new ArrayList();
        table.add(columnNames);

        while (resultSet.next()) {
            Object[] row = new Object[columnNames.length];

            for (int i = 0; i < columnNames.length; i++) {
                row[i] = resultSet.getObject(columnNames[i]);
            }

            table.add(row);
        }

        return table.toArray(new Object[0][0]);
    }

    protected ResultSet callProcedure(String procedure) throws SQLException, ClassNotFoundException {
        return callProcedure(procedure, new Object[]{});
    }

    protected ResultSet callProcedure(String procedure, Object... parameters) throws SQLException, ClassNotFoundException {
        String statement = "CALL `" + procedure + "`("
                + (parameters.length == 0 ? "" : SqlUtil.formatValues(parameters))
                + ");";

        try {
            checkConnection();
            return connection.createStatement().executeQuery(statement);

        } catch (SQLException e) {
            throw new SQLException(statement, e);
        } catch (ClassNotFoundException e) {
            throw e;
        }
    }

    protected boolean execute(String st) throws SQLException, ClassNotFoundException {
        checkConnection();

        return connection.createStatement().execute(st);
    }

    protected int[] executeBatch(String... st) throws SQLException, ClassNotFoundException {
        checkConnection();

        Statement statement = connection.createStatement();

        for (String s : st) {
            if (!s.isEmpty() && !s.matches("\\s{1,}")) {
//                System.out.println(s + ";\n");
                statement.addBatch(s);
            }
        }

        return statement.executeBatch();
    }

    protected int executeUpdate(String st) throws SQLException, ClassNotFoundException {
        checkConnection();

        return connection.createStatement().executeUpdate(st);
    }

    protected ResultSet executeQuery(String st) throws SQLException, ClassNotFoundException {
        checkConnection();

        return connection.createStatement().executeQuery(st);
    }

    protected ResultSet executeQueryFromScript(File scriptFile, String[] variables) throws IOException, SQLException, ClassNotFoundException {
        checkConnection();

        String script = SqlUtil.substitute(FileUtil.readTextFile(scriptFile), variables);

        return connection.createStatement().executeQuery(script);
    }

    protected ResultSet executeQueryFromScript(File scriptFile) throws IOException, SQLException, ClassNotFoundException {
        return executeQueryFromScript(scriptFile, null);
    }

    protected void runScript(String script, String delimiter) throws IOException, SQLException, ClassNotFoundException {
        executeBatch(script.split(delimiter));
    }
    
    protected void runScript(String script, String[] variables) throws IOException, SQLException, ClassNotFoundException {
        final String DELIMITER = "DELIMITER ";

        String s1 = SqlUtil.substitute(script, variables);

        if (!s1.substring(0, 10).equalsIgnoreCase(DELIMITER)) {
            s1 = DELIMITER + ";\n" + s1;
        }

        for (String s2 : s1.split(DELIMITER)) {

            String dl = s2.split("\n", 2)[0]; //delimiter

//            System.out.println(GeneralUtil.toString(ss));
            runScript(s2, dl);
        }
    }

    protected void runScriptFile(File scriptFile, String[] variables) throws IOException, SQLException, ClassNotFoundException {
        runScript(FileUtil.readTextFile(scriptFile), variables);
    }

    protected void runScriptFile(File scriptFile) throws IOException, SQLException, ClassNotFoundException {
        runScriptFile(scriptFile, null);
    }

    protected void runScriptFromResources(String name, String[] variables) throws IOException, SQLException, ClassNotFoundException {
        runScript(FileUtil.readResource(Main.sqlScriptsFolder + name), variables);
    }

    protected void runScriptFromResources(String name) throws IOException, SQLException, ClassNotFoundException {
        runScriptFromResources(name, null);
    }
}
