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

package OAT.trading;

import OAT.util.SystemUtil;
import OAT.util.Waiting;
import OAT.util.GeneralUtil;
import OAT.util.DateUtil;
import OAT.util.FileUtil;
import OAT.util.TextUtil;
import OAT.ui.BarChartFrame;
import OAT.ui.DebugFrame;
import OAT.ui.BacktestFrame;
import OAT.ui.MainFrame;
import OAT.ui.PreferencesFrame;
import OAT.ui.GetDataFrame;
import OAT.trading.thread.HybridStrategy;
import OAT.trading.thread.BaseThread;
import OAT.trading.thread.BacktestThread;
import OAT.trading.thread.HistDataCollector;
import OAT.trading.thread.TradingThread;
import OAT.trading.thread.MainThread;
import OAT.trading.client.AccountClient;
import OAT.trading.client.IbDataClient;
import OAT.trading.client.HistDataClient;
import OAT.trading.client.IbAccountClient;
import OAT.trading.client.IbTradingClient;
import OAT.trading.client.DataClient;
import OAT.trading.client.Connectable;
import OAT.trading.client.IbHistDataClient;
import OAT.trading.client.TradingClient;
import OAT.sql.SqlConnect;
import OAT.sql.DataSchema;
import OAT.sql.TradingSchema;
import OAT.sql.BacktestSchema;
import OAT.sql.WebSchema;
import com.apple.eawt.AppEvent.PreferencesEvent;
import com.apple.eawt.AppEvent.QuitEvent;
import com.apple.eawt.Application;
import com.apple.eawt.PreferencesHandler;
import com.apple.eawt.QuitHandler;
import com.apple.eawt.QuitResponse;
import com.ib.client.Contract;
import java.awt.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.sql.SQLException;
import java.util.List;
import java.util.*;
import java.util.logging.Level;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import javax.swing.JOptionPane;
import javax.swing.text.DefaultCaret;
import org.hyperic.sigar.Sigar;
import OAT.data.ChartDataset;
import OAT.event.AccountPortfolioChangeEvent;
import OAT.event.GenericListener;
import OAT.event.State;
import OAT.event.StateChangeEvent;
import OAT.trading.client.IbClient.Platform;
import OAT.trading.thread.BacktestThread.BacktestData;
import OAT.ui.util.DefaultTheme;
import OAT.ui.util.TimeOutOptionPane;
import OAT.ui.util.UiUtil;

/**
 *
 * @author Antonio Yip
 */
public final class Main extends MainThread implements GenericListener, QuitHandler, PreferencesHandler {

    //Parameters
    public static String p_Application = "Tou.app";
    public static String p_Broker_Login_Script_File = "connectIB.scptd";
    public static String p_email_Receipient = "antoniokly@gmail.com";
    public static String p_email_Sender = "kuma.macminicolo.net";
    public static String p_Default_Host = "kuma.local";
    public static String p_Base_Currency = "";
    //
    public static boolean p_Send_Mail = true;
    public static boolean p_Debug_Mode = false;
    public static boolean p_Portfolio_Market_Data = false;
    public static Level p_Log_Level = Level.CONFIG;
    public static int p_Backtest_Max_Thread = 500;
    //Time
    public static int p_Push_Log_Every_Minute = 5;
    public static long p_Task_Interval = DateUtil.SECOND_TIME * 30;
    public static long p_Wake_up_before_Open = DateUtil.FIVE_MINUTE_TIME;
    public static long p_SQL_Time_Out = DateUtil.HOUR_TIME * 12;
    public static long p_Market_Data_Time_Out = DateUtil.MINUTE_TIME;
    public static long p_Init_Time_Out = DateUtil.MINUTE_TIME * 2;
    public static long p_Exit_Time_Out = DateUtil.SECOND_TIME * 30;
    public static long p_Cancel_Order_Time_Out = DateUtil.SECOND_TIME * 10;
    public static long p_Send_Order_Time_Out = DateUtil.SECOND_TIME * 10;
    public static long p_Request_Time_Out = DateUtil.SECOND_TIME * 15;
    public static long p_Contract_Details_Time_Out = DateUtil.SECOND_TIME * 30;
    public static long p_Position_Close_Time_Out = DateUtil.SECOND_TIME * 30;
    public static long p_Restart_Client_Wait = DateUtil.SECOND_TIME * 2;
    public static long p_Restart_Broker_Wait = DateUtil.SECOND_TIME * 20;
    public static long p_Backtest_Time_Out = DateUtil.MINUTE_TIME * 10;
    public static long p_Wait_Interval = 50;
    public static long p_Backtest_Wait_Interval = 50;
    public static long p_Position_Complete_Time_Out = DateUtil.SECOND_TIME * 10;
    public static long p_Account_Download_Wait = DateUtil.SECOND_TIME * 5;
    public static long p_Account_Subscription_Time_Out = DateUtil.MINUTE_TIME * 5;
    public static long p_Post_Connection_Wait = DateUtil.SECOND_TIME * 2;
    public static long p_Submitted_Order_Wait = DateUtil.SECOND_TIME * 5;
    public static long p_Client_Connection_Wait = DateUtil.MINUTE_TIME * 3;
    public static String p_Email_Summary_Time = "05:15";
    public static String p_Auto_Restart_Time = "06:05";
    public static String p_Main_Sleep_Time = "06:15";
    public static String p_Clear_Balance_Time = "06:15";
    //GUI
    public static String p_Theme = "Standard";
    public static String p_Font = "Lucida Grande";
    public static int p_Smaller_Font_Size = 11;
    public static int p_Normal_Font_Size = 12;
    public static int p_Bigger_Font_Size = 13;
    public static String p_Chart_Up_Color = UiUtil.getColorRGB(Color.GREEN);
    public static String p_Chart_Down_Color = UiUtil.getColorRGB(Color.RED);
    public static String p_Chart_Flat_Color = UiUtil.getColorRGB(Color.YELLOW);
    public static String p_Chart_Long_Trade_Color = UiUtil.getColorRGB(Color.BLUE);
    public static String p_Chart_Short_Trade_Color = UiUtil.getColorRGB(Color.MAGENTA);
    public static Dimension p_Chart_Min_Size = new Dimension(320, 200);
    public static Dimension p_Chart_Max_Size = new Dimension(460, 320);
    //
    public static int p_Server_Port = 8888;
    public static long p_Last_Start_Time;
    public static long p_Last_Exit_Time;
    //
    public static Account ibAccount = Account.CASH;
    public static Platform ibPlatform = Platform.GATEWAY;
    public static boolean ibRestart = true;
    public static double p_Prediction_Win_Margin = 0.0;
    //Files
    public static String appFolder;
    public static String appContentsFolder;
    public static String appResourcesFolder;
    public static String userFolder;
    public static String tradingFolder;
    public static String dataFolder;
//    public static String logFolder;
    public static String modelFolder;
    public static String javaFolder;
    public static String sqlScriptsFolder;
    public static String resourcesFolder;
//    public static String logFile;
    public static File jarFile;
    //GUI
    public static DefaultTheme theme;
    public static Font smallerFont;
    public static Font normalFont;
    public static Font biggerFont;
    public static Color upColor;
    public static Color downColor;
    public static Color flatColor;
    public static Color longColor;
    public static Color shortColor;
    //
    public static Shape defaultShape = new DefaultCaret();
    public static int[] defaultShade = new int[]{100, 150, 255};
    public static Paint[][] defaultPaints = new Paint[][]{
        new Paint[]{new Color(100, 100, 100), new Color(150, 150, 150), new Color(250, 250, 250)},
        new Paint[]{new Color(128, 0, 0), new Color(200, 0, 0), new Color(255, 0, 0)},
        new Paint[]{new Color(0, 0, 128), new Color(0, 0, 200), new Color(0, 0, 255)},
        new Paint[]{new Color(0, 128, 0), new Color(0, 200, 0), new Color(0, 255, 0)}
    };
    public static Paint[][] greyPaints = new Paint[][]{
        new Paint[]{new Color(100, 80, 100), new Color(150, 120, 150), new Color(250, 200, 250)},
        new Paint[]{new Color(100, 100, 80), new Color(150, 150, 120), new Color(250, 250, 200)},
        new Paint[]{new Color(80, 100, 80), new Color(120, 150, 120), new Color(200, 250, 200)},
        new Paint[]{new Color(140, 140, 140), new Color(160, 160, 160), new Color(180, 180, 180)}
    };
    public static BasicStroke defaultStoke =
            new BasicStroke(1, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER);
    public static BasicStroke dottedStoke =
            new BasicStroke(1, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER, 10, new float[]{5, 5}, 0);
    public static Shape enterShape =
            new Polygon(new int[]{0, -6, -6}, new int[]{0, 4, -4}, 3);
    public static Shape exitShape =
            new Polygon(new int[]{0, 6, 6}, new int[]{0, 4, -4}, 3);
    //Lists
    public static Map<String, HybridStrategy> strategyMap = Collections.synchronizedMap(new HashMap<String, HybridStrategy>());
    public static List<HybridStrategy> savedStrategies = Collections.synchronizedList(new ArrayList<HybridStrategy>());
    public static List<HybridStrategy> currentStrategies = Collections.synchronizedList(new ArrayList<HybridStrategy>());
    public static List<Contract> contracts = Collections.synchronizedList(new ArrayList<Contract>());
    public static List<BarChartFrame> chartFrames = Collections.synchronizedList(new ArrayList<BarChartFrame>()); //all visible charts
    //
    public static BacktestThread backtestThread;
    public static BacktestData backtestData;
    public static HistDataCollector dataCollector;
    public static int selectedStrategyId;
    //
    public static TradingSchema tradingSchema = new TradingSchema();
    public static DataSchema dataSchema = new DataSchema();
    public static WebSchema webSchema = new WebSchema();
//    public static ModelSchema modelSchema;
    public static BacktestSchema backtestSchema;
    public static DataSchema backtestDataSchema;
    //
    public static AccountClient accountClient;
    public static DataClient dataClient;
    public static TradingClient tradingClient;
    public static HistDataClient histDataClient;
    //
    public static final int CLIENT_ID_BASE = 10;
    //
    public static MainFrame frame;
    public static Rectangle[][] chartFramesGrid;
    public static PreferencesFrame preferencesFrame;
    public static DebugFrame debugFrame;
    public static BacktestFrame backtestFrame;
    public static GetDataFrame dataFrame;
    public static int lastChartFrameGridId;
    //
    private static Set<BaseThread> initializedTradingThreads = Collections.synchronizedSet(new HashSet<BaseThread>());
    private static long startTime;
    private static long exitTime;
    private static boolean pendingRestart;
    private static Main mainThread;
    private static ServerSocket servo;
    private static String versionNumber;
    private static String versionDate;
    private static boolean savingDataToSql;
    private static Sigar sigar = new Sigar();

    public static void main(String[] args) throws Exception {
        startTime = DateUtil.getTimeNow();

        mainThread = new Main();

        loadArguments(args);
        loadPaths();

        versionNumber = SystemUtil.getVersionNum();
        versionDate = SystemUtil.getVersionDate();

        System.out.println(p_Application + " started at "
                + DateUtil.getCalendarDate(startTime).getTime()
                + "\nVersion: " + versionNumber + " (" + versionDate + ")"
                + "\nArgs: " + TextUtil.toString(args, " "));

        //Listeners
        mainThread.addChangeListener(mainThread);

        //Apple
        System.setProperty("apple.laf.useScreenMenuBar", "true");
        Application.getApplication().setQuitHandler(mainThread);
        Application.getApplication().setPreferencesHandler(mainThread);

        //Main frame
        frame = new MainFrame(
                ibAccount.toString(),
                DateUtil.getTimeStamp(startTime, DateUtil.DATETIME_TZ_FORMAT),
                versionNumber);

        chartFramesGrid = UiUtil.getChartFrameGrid();

        //Start main client
        mainThread.init();
    }

    private static void loadArguments(String[] args) throws IOException {
        if (args != null && args.length > 0) {
            if (args.length > 0) {
                if (!isDefaultHost()) {
                    args[0] = args[0].toUpperCase().
                            replace("CASH", "PAPER");
                }

                ibAccount = Account.valueOf(args[0].toUpperCase());
            }

            if (args.length > 1) {
                ibPlatform = Platform.valueOf(args[1].toUpperCase());
            }

            if (args.length > 2) {
                ibRestart = Boolean.parseBoolean(args[2]);
            }
        }
    }

    private static void loadPaths() throws IOException, URISyntaxException {
//        ClassLoader loader = ClassLoader.getSystemClassLoader();
//        System.out.println(loader.getResource("tou/sql/scripts").getPath());
//        System.out.println(loader.getResource("tou").getPath());

        userFolder = FileUtil.getUserFolder();
        tradingFolder = FileUtil.getFolder(userFolder, "Trading");
        dataFolder = FileUtil.getFolder(tradingFolder, "Data");
        modelFolder = FileUtil.getFolder(tradingFolder, "Model");

        jarFile = new File(Main.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath());

        appFolder = new File(".").getCanonicalPath() + File.separator
                + p_Application + File.separator;

//        System.out.println(appFolder);

        if (!new File(appFolder).exists()) {
            appFolder = FileUtil.getAppFolder() + p_Application + File.separator;
        }

        appContentsFolder = FileUtil.getFolder(appFolder, "Contents");
        appResourcesFolder = FileUtil.getFolder(appContentsFolder, "Resources");

//        System.out.println(ClassLoader.getSystemClassLoader().getResource("tou/").getPath());

//        sqlScriptsFolder = FileUtil.getFolder(appResourcesFolder, "Scripts");
//        modelFolder = FileUtil.getFolder(appResourcesFolder, "Model");

        sqlScriptsFolder = "tou/sql/scripts/";
//        resourcesFolder = ClassLoader.getSystemClassLoader().getResource("tou/").toExternalForm();


//        System.out.println(resourcesFolder);

//        System.out.println(sqlScriptsFolder);

//        javaFolder = FileUtil.getFolder(appResourcesFolder, "Java");
//        jarFile = new File(javaFolder + "TouTrading.jar");


//        System.out.println(jarFile);
    }

    private void initUI() {
        theme = new DefaultTheme(p_Theme);
        smallerFont = new Font(p_Font, Font.PLAIN, p_Smaller_Font_Size);
        normalFont = new Font(p_Font, Font.PLAIN, p_Normal_Font_Size);
        biggerFont = new Font(p_Font, Font.PLAIN, p_Bigger_Font_Size);
        upColor = UiUtil.getColor(p_Chart_Up_Color);
        downColor = UiUtil.getColor(p_Chart_Down_Color);
        flatColor = UiUtil.getColor(p_Chart_Flat_Color);
        longColor = UiUtil.getColor(p_Chart_Long_Trade_Color);
        shortColor = UiUtil.getColor(p_Chart_Short_Trade_Color);
    }

    private static void connect(Connectable... connectables) {
        for (Connectable connectable : connectables) {
            connectable.connect();
        }
    }

    private static void disconnect(Connectable... connectables) {
        for (Connectable connectable : connectables) {
            connectable.disconnect();
        }
    }

    @Override
    public void init() {
        super.init();

        connectToSQL();

        Preferences preferences = Preferences.userNodeForPackage(Main.class).node(Main.class.getSimpleName());

        log(Level.INFO, "Version: " + versionNumber + " (" + versionDate + ")");
        log(Level.INFO, "Jar path: " + jarFile.getPath());
        log(Level.INFO, "Resources path: " + resourcesFolder);
        log(Level.INFO, "Preferences node: " + preferences.absolutePath());

        setPreferences(preferences);
        loadPreferences();
        logParameters();

        if (Main.getAccount() == Account.DEMO) {
            setLogLevel(Level.FINE);
        } else {
            setLogLevel(p_Log_Level);
        }

        initUI();

        //SQL trading tables
        try {
            initSchema(tradingSchema);
            initSchema(dataSchema);
            initSchema(webSchema);
//            initSchema(modelSchema);

        } catch (Exception ex) {
            log(Level.SEVERE, null, ex);
        }

        savingDataToSql = isDefaultHost() && getAccount() != Account.DEMO;

        if (savingDataToSql) {
            log(Level.INFO, "Price data is saving to SQL.");
        } else {
            log(Level.WARNING, "Price data is not saving to SQL.");
        }

        accountClient = new IbAccountClient(this);
        dataClient = new IbDataClient(this);
        tradingClient = new IbTradingClient(this);
        histDataClient = new IbHistDataClient(this);

        //init strategies
        loadStrategies();

        //init frames
        frame.setupTables();
        preferencesFrame = new PreferencesFrame(frame);
        debugFrame = new DebugFrame(frame);
        backtestFrame = new BacktestFrame(frame);
        dataFrame = new GetDataFrame(frame);

        //Login IB
        if (ibPlatform == Platform.GATEWAY || ibPlatform == Platform.TWS) {
            try {
                connectToBroker(ibAccount.toString(), ibPlatform.toString(), String.valueOf(ibRestart));
            } catch (Exception ex) {
                log(Level.SEVERE, null, ex);
            }
        }

        new Thread(accountClient).start();
        new Thread(dataClient).start();
        new Thread(tradingClient).start();

        new Waiting(p_Client_Connection_Wait, p_Wait_Interval, logger) {

            @Override
            public boolean waitWhile() {
                return !accountClient.isConnected()
                        || !dataClient.isConnected()
                        || !tradingClient.isConnected();
            }

            @Override
            public void retry() {
            }

            @Override
            public String message() {
                return "Waiting clients to connect.";
            }

            @Override
            public void timeout() {
                if (backtestThread == null) {
                    log(Level.SEVERE, "Cannot start clients. Exiting system.");
                    exitSystem();
                }
            }
        };

        GeneralUtil.pause(
                p_Post_Connection_Wait,
                getLogger(),
                "Pause "
                + DateUtil.getDurationStr(p_Post_Connection_Wait)
                + " after connection...");

        log(Level.INFO, "Starting main and trading threads.");

        mainThread.postConnection();
        new Thread(mainThread).start();

        //Start trading strategies
        for (final HybridStrategy strategy : currentStrategies) {
            new Thread(new Runnable() {

                @Override
                public void run() {
                    strategy.postConnection();

                    new Waiting(p_Contract_Details_Time_Out, p_Wait_Interval, strategy.getLogger()) {

                        @Override
                        public boolean waitWhile() {
                            return !strategy.isInitialized();
                        }

                        @Override
                        public void retry() {
                        }

                        @Override
                        public String message() {
                            return "Waiting contract details to download...";
                        }

                        @Override
                        public void timeout() {
                        }
                    };

                    new Thread(strategy).start();
                }
            }).start();
        }
    }

    @Override
    protected void intervalTask() {
        if (!isInitialized()
                && ibAccount != Account.DEMO
                && DateUtil.getTimeNow() - startTime >= p_Init_Time_Out) {
//            System.out.println(DateUtil.getTimeNow() - startTime);
//            System.out.println(p_Init_Time_Out);
//            System.out.println("kkk " + DateUtil.getDateTimeString(startTime));
//            System.out.println("p_kkk " + DateUtil.getDateTimeString(p_Last_Start_Time));
            log(Level.WARNING, "Start time out, exiting.");
            System.out.println("Exiting due to initialization time out.");
            exitSystem();
        }

        if (isExiting()
                && DateUtil.getTimeNow() - exitTime >= p_Exit_Time_Out) {
            log(Level.WARNING, "Exit time out, exit anyway.");
            exitSystem();
        }

        if (!isSleeping()) {
            connect(accountClient, dataClient, tradingClient);
        }

        if (isAllStrategiesLoaded()) {
            Calendar now = DateUtil.getCalendarDate();

            for (HybridStrategy strategy : currentStrategies) {
                if (strategy.getNextScheduleTime() > 0
                        && strategy.getNextScheduleTime() + p_Task_Interval
                        < now.getTimeInMillis()) {
                    log(Level.SEVERE,
                            "{0} is dead. Last scheduled task was {1}",
                            new Object[]{
                                strategy.getName(),
                                DateUtil.getDateTimeString(strategy.getNextScheduleTime())
                            });

//                    new Thread(strategy).start();
                }
            }

            AccountPortfolio mainAccountPortfolio = null;
            if (!accountPortfolios.isEmpty()) {
                mainAccountPortfolio = accountPortfolios.get(0);
            }

            //Auto restart if server delays reported
            if (pendingRestart
                    && DateUtil.isSameTimeOfDay(now, p_Auto_Restart_Time)) {
                log(Level.INFO, "Exiting due to server delay.");
                exit(true);
            }

            //email trades
            if (!isSleeping() && !DateUtil.isSunday()
                    && DateUtil.isSameTimeOfDay(now, p_Email_Summary_Time)) {
                String summary = getAccountSummary(mainAccountPortfolio);

                log(Level.INFO, summary);
                mailer.activity(summary);
            }

            if (getAwakeStrategiesCount() > 0) {
                if (isSleeping()) {
                    wakeUpNow();
                } else {
                    if (now.getTimeInMillis() - getLastAccountUpdate() >= p_Account_Subscription_Time_Out) {
                        setState(false, State.SUBSCRIBED_ACCOUNT_UPDATE);
//                        reqAccountUpdates();

                        for (HybridStrategy strategy : currentStrategies) {
                            if (strategy.getCurrentPosition() == 0) {
                                if (strategy.getAccountPosition() != null
                                        && strategy.getAccountPosition().getPosition() != 0) {
                                    strategy.resetAccountPosition();
                                }
                            }
                        }

                    } else if (now.getTimeInMillis() - getLastAccountUpdate() >= p_Account_Download_Wait) {
                        if (mainAccountPortfolio != null
                                && mainAccountPortfolio == frame.getSelectedAccount()) {
                            for (AccountPosition position : mainAccountPortfolio.accountPositions.values()) {
                                if (!position.isFired()) {
                                    position.firePositionChanged();
                                }
                            }
                        }

                    } else {
                        frame.changeToPrimaryAccount();
                    }

                    reqAccountUpdates();
                }

            } else {
                //Sleep
                long wakeUpTime = getEarliestWakeUpTime() - DateUtil.FIVE_MINUTE_TIME;

//                if (DateUtil.isSameTimeOfDay(
//                        now,
//                        p_Main_Sleep_Time + "-"
//                        + DateUtil.getTimeStamp(wakeUpTime, "HH:mm"))) {
//                }

                wakeUpUntil(wakeUpTime);
            }
        }

        log(Level.FINE, GeneralUtil.getCpuUsage(sigar));

        super.intervalTask();
    }

    @Override
    public void postWakeUp() {
        connectToSQL();

        connect(accountClient, dataClient, tradingClient);
        reqAccountUpdates();
    }

    @Override
    public void preSleep() {
        cancelAccountUpdates();
        disconnect(histDataClient, tradingClient, dataClient, accountClient);

        pause(3000, "Pause for disconnecting clients");
        disconnectFromSQL();
    }

    @Override
    protected void preExit() {
        //save
        if (getAccount() != Account.DEMO) {
            try {
                savePreferences();
            } catch (Exception ex) {
                log(Level.SEVERE, null, ex);
            }
        } else {
            log(Level.INFO, "Demo account, preferences are not save.");
        }

        disconnectFromSQL();
    }

    @Override
    public void exit() {
        exit(false);
    }

    public void exit(boolean force) {
//        if (!force && !isInitialized()) {
//            log(Level.INFO, "Exit not allowed during initialization.");
//            return;
//        }

        boolean isExiting;
        int openStrategiesCount = getOpenStrategiesCount();

        if (force || getAccount() == Account.DEMO) {
            isExiting = true;
        } else {
            if (openStrategiesCount > 0) {
                isExiting = JOptionPane.YES_OPTION
                        == TimeOutOptionPane.showConfirmDialog(
                        frame,
                        "Clients are running, OK to exit?",
                        null,
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.PLAIN_MESSAGE,
                        new String[]{"Yes", "No"},
                        "No",
                        10,
                        false);
            } else {
                isExiting = JOptionPane.YES_OPTION
                        == TimeOutOptionPane.showConfirmDialog(
                        frame,
                        "Program is closing in ",
                        null,
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.PLAIN_MESSAGE,
                        null,
                        0,
                        10,
                        true);
            }
        }

        if (isExiting) {
            setState(true, State.EXITING);
            p_Last_Start_Time = startTime;
            p_Last_Exit_Time = exitTime = DateUtil.getTimeNow();

            try {
                getPreferences().sync();
            } catch (BackingStoreException ex) {
            }

            //hide all frames
            for (int i = chartFrames.size() - 1; i >= 0; i--) {
                chartFrames.get(i).setVisible(false);
            }

//            for (BarChartFrame chartFrame : chartFrames) {
//                chartFrame.setVisible(false);
//            }

            debugFrame.setVisible(false);

            if (backtestThread != null) {
                new Thread(new Runnable() {

                    @Override
                    public void run() {
                        backtestThread.exit();
                    }
                }).start();
            }



            if (openStrategiesCount == 0) {
                exitSystem();
                return;
            }

            //exit currentStrategies
            for (final HybridStrategy strategy : currentStrategies) {
                new Thread(new Runnable() {

                    @Override
                    public void run() {
                        strategy.exit();
                    }
                }).start();
            }

        } else {
            setState(false, State.EXITING);
            log(Level.INFO, "Exit canceled.");
        }
    }

    private void exitSystem() {
        super.exit();
        exited();

        frame.setVisible(false);

        Calendar now = DateUtil.getCalendarDate();
        mailer.activity("Program exited at " + now.getTime() + ".");
//        getPreferences().put("Last Exit", "" + now.getTimeInMillis());

        System.exit(0);
    }

    private void loadStrategies() {
        log(Level.INFO, "Loading strategies...");

        for (Plugin plugin : getChildren()) {
//            System.out.println("plugin = " + plugin.getDefaultNodeName());

            savedStrategies.add((HybridStrategy) plugin);
        }

        if (savedStrategies.isEmpty()) {
            log(Level.WARNING, "No saved strategy found.");


            try {
                savedStrategies = new LinkedList<HybridStrategy>(Arrays.asList(
                        new HybridStrategy("HSI", "FUT", "HKFE", "HKD"),
                        new HybridStrategy("HHI.HK", "FUT", "HKFE", "HKD"),
                        new HybridStrategy("YM", "FUT", "ECBOT", "USD"),
                        new HybridStrategy("NQ", "FUT", "GLOBEX", "USD"),
                        new HybridStrategy("ES", "FUT", "GLOBEX", "USD"),
                        new HybridStrategy("K200", "FUT", "KSE", "KRW"),
                        new HybridStrategy("CAC40", "FUT", "", "EUR"),
                        new HybridStrategy("DAX", "FUT", "", "EUR")));
            } catch (Exception ex) {
                log(Level.SEVERE, null, ex);
            }
        }

        for (final HybridStrategy strategy : savedStrategies) {
            if (strategy == null) {
                continue;
            }

            strategyMap.put(strategy.getSymbol(), strategy);
            strategyMap.put(strategy.getTradingSymbol(), strategy);

            boolean repeatedContract = false;

            if (!strategy.isBacktestOnly()) {
                strategy.addChangeListener(this);
                strategy.setDataClient(dataClient);
                strategy.setTradingClient(tradingClient);

                for (HybridStrategy otherStrategy : currentStrategies) {
                    if (strategy.isSameContract(otherStrategy)) {
                        strategy.setContractId(otherStrategy.getContractId());
                        repeatedContract = true;
                        break;
                    }
                }

                if (!repeatedContract) {
                    contracts.add(strategy.getContract());
                    strategy.setContractId(contracts.size() - 1);
                    strategy.setUpdatingQuote(true);
                }

                strategy.setThreadId(CLIENT_ID_BASE * currentStrategies.size());
                currentStrategies.add(strategy);

                frame.addLogTextPane(strategy.getDefaultLoggerName());

                new Thread(new Runnable() {

                    @Override
                    public void run() {
                        strategy.init();
                    }
                }).start();
            }
        }
    }

    private void connectToBroker(String... args) throws FileNotFoundException, IOException {
        log(Level.INFO, "Connecting to broker {0}...", ibPlatform.name());

        SystemUtil.runAppleScript(appResourcesFolder + p_Broker_Login_Script_File, args);

        if (ibRestart) {
            GeneralUtil.pause(
                    p_Restart_Broker_Wait,
                    getLogger(),
                    "Waiting broker connection to restart.");
        }
    }

    public void toggleDebugMode() {
        p_Debug_Mode = debugFrame.isVisible();
        log(Level.CONFIG, "Debug Mode={0}", p_Debug_Mode);
    }

//    private void initServer() {
//        if (ibAccount == Account.BACKTEST) {
//            return;
//        }
//
//        try {
//            servo = new ServerSocket(p_Server_Port);
//        } catch (IOException ex) {
//            logger.log(Level.SEVERE, null, ex);
//        }
//
//        logger.log(Level.INFO, "Server started, listening to port {0}", p_Server_Port);
//
//        Socket socket;
//
//        while (true) {
//            if (servo == null) {
//                return;
//            }
//
//            try {
//                socket = servo.accept();
//
//                logger.info("Client connected.");
//
//                BufferedOutputStream bos = new BufferedOutputStream(
//                        socket.getOutputStream());
//                PrintWriter pw = new PrintWriter(bos, false);
//                pw.println(DateUtil.getTimeStamp(
//                        loginTime, DateUtil.DATETIME_TZ_FORMAT));
//                pw.flush();
//                pw.close();
//                socket.close();
//            } catch (IOException ex) {
//                logger.log(Level.SEVERE, null, ex);
//            }
//        }
//    }
    //
    //Getters
    //
    @Override
    public String getChildrenNodeName() {
        return "Strategies";
    }

    @Override
    public String getDefaultNodeName() {
        return "Main";
    }

    @Override
    public String getDefaultLoggerName() {
        return this.getClass().getSimpleName();
    }

    public static boolean isDefaultHost() {
        String host;
        try {
            host = InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException ex) {
            return false;
        }

        return host != null && p_Default_Host != null
                && host.equalsIgnoreCase(p_Default_Host);
    }

    public static boolean isPendingRestart() {
        return pendingRestart;
    }

    public static boolean isAdvisorAccount() {
        return mainThread.getAdvisorAccount() != null;
    }

    public static boolean isAllStrategiesLoaded() {
        if (initializedTradingThreads.size() > currentStrategies.size()) {
            mainThread.log(Level.WARNING, "initializedClients > currentStrategies");
        }

        return initializedTradingThreads.size() == currentStrategies.size();
    }

    public static Account getAccount() {
        return ibAccount;
    }

    public static Main getMainThread() {
        return mainThread;
    }

    public static AccountClient getAccountClient() {
        return accountClient;
    }

    public static DataClient getDataClient() {
        return dataClient;
    }

    public static TradingClient getTradingClient() {
        return tradingClient;
    }

    public static HistDataClient getHistDataClient() {
        return histDataClient;
    }

    public static BarChartFrame getChartFrame(ChartDataset chartData) {
        if (chartData == null) {
            return null;
        }

        for (BarChartFrame chartFrame : chartFrames) {
//            boolean isSameChart;

//            if (chartData.isBacktest()) {
//                isSameChart = chartData == chartFrame.getDataset(0);
//            } else {
//                isSameChart = chartData.equals(chartFrame.getDataset(0));
//            }

//            if (isSameChart) {
            if (chartData.equals(chartFrame.getDataset(0))) {
//                chartFrame.toFront();
                return chartFrame;
            }
        }

        return new BarChartFrame(chartData);
    }

    public static List<HybridStrategy> getCurrentStrategies() {
        return currentStrategies;
    }

    public static HybridStrategy getStrategy(int index) {
        if (index < 0 || index >= currentStrategies.size()) {
            return null;
        }

        selectedStrategyId = index;
        return currentStrategies.get(index);
    }

    public static HybridStrategy getStrategy(Contract contract) {
        if (contract == null) {
            return null;
        }

//        for (int i = 0; i < currentStrategies.getItemCount(); i++) {
//            HybridStrategy strategy = currentStrategies.get(i);
////        }
////        
////        for (HybridStrategy strategy : currentStrategies) {
//            if (strategy.getContract() != null
//                    && strategy.getContract().m_localSymbol != null
//                    && strategy.getContract().m_localSymbol.equals(contract.m_localSymbol)) {
//                selectedStrategyId = i;
//                return strategy;
//            }
//        }

        return strategyMap.get(contract.m_symbol);
    }

    public static int getAwakeStrategiesCount() {
        int count = 0;

        for (HybridStrategy strategy : currentStrategies) {
            if (!strategy.isSleeping()) {
                count++;
            }
        }

        return count;
    }

    public static int getOpenStrategiesCount() {
        int count = 0;

        for (HybridStrategy strategy : currentStrategies) {
            if (strategy.getTradingHours() != null
                    && strategy.getTradingHours().isOpen()) {
                count++;
            }
        }

        return count;
    }

    public static long getEarliestWakeUpTime() {
        long wakeUpTime = Long.MAX_VALUE;

        for (HybridStrategy strategy : currentStrategies) {
            if (strategy.getWakeUpTime() < wakeUpTime) {
                wakeUpTime = strategy.getWakeUpTime();
            }
        }

        return wakeUpTime;
    }

    public static boolean isSavingDataToSql() {
        return savingDataToSql;
    }

    public static Level getLogLevel() {
        return p_Log_Level;
    }

    //
    //Setters
    //
    public void setLogLevel(Level level) {
        if (level == null) { // || level == p_Log_Level) {
            return;
        }

        p_Log_Level = level;

        getLogger().setLevel(level);

        for (HybridStrategy strategy : currentStrategies) {
            if (strategy.getLogger() != null) {
                strategy.getLogger().setLevel(level);
            }
        }
    }

    //
    //Listeners
    //
    @Override
    public void eventHandler(EventObject event) {
        if (event instanceof StateChangeEvent) {
            stateChanged((StateChangeEvent) event);

        } else if (event instanceof AccountPortfolioChangeEvent) {
            accountPortfolioChanged((AccountPortfolioChangeEvent) event);
        }
    }

    private void stateChanged(StateChangeEvent event) {
        switch (event.getState()) {
            case INITIALIZED:
                if (event.isActive()) {
                    if (event.getSource() instanceof TradingThread) {
                        boolean added = initializedTradingThreads.add((TradingThread) event.getSource());
                        int size = initializedTradingThreads.size();
                        log(Level.FINE, "{0} initialized.", (TradingThread) event.getSource());
                        log(Level.FINE, "Initialized trading threads = {0}", size);

//                    if (added && isAllStrategiesLoaded()) {
                        if (added && size == currentStrategies.size()) {
                            log(Level.INFO, "All trading threads were loaded successfully");

                            //mail
                            mailer.activity("Program started successfully at "
                                    + DateUtil.getCalendarDate().getTime()
                                    + ". Version " + versionNumber);

                            setState(true, State.INITIALIZED);

                            reqAccountUpdates();
                        }
                    }

                }
                break;

            case EXITED:
                if (event.isActive() && event.getSource() instanceof TradingThread) {
                    initializedTradingThreads.remove((TradingThread) event.getSource());
                    int size = initializedTradingThreads.size();
                    log(Level.FINE, "{0} exited.", (TradingThread) event.getSource());
                    log(Level.FINE, "Initialized trading threads = {0}", size);

                    if (isExiting() && size == 0) {
                        exitSystem();
                    }
                }
                break;

//            case CONNECTING:
//                if (event.isActive()) {
//
//                    connectedClients.add((IbThread) event.getSource());
//
////                    if (logger.isLoggable(Level.FINE)) {
//                    logger.log(Level.FINE, "Connection count = {0}", connectedClients.getItemCount());
////                    }
//
//                } else {
//                    connectedClients.remove((IbThread) event.getSource());
//
////                    if (logger.isLoggable(Level.FINE)) {
//                    logger.log(Level.FINE, "Connection count = {0}", connectedClients.getItemCount());
////                    }
//                }
//                break;

            case SERVER_DELAYED:
                if (event.isActive() && !pendingRestart && event.getSource() instanceof TradingThread) {
                    log(Level.WARNING, "Server delay reported. Pending for restart.");
                    pendingRestart = true;
                }

                break;

            default:
        }
    }

    private void accountPortfolioChanged(AccountPortfolioChangeEvent accountPortfolioChangeEvent) {
    }

    @Override
    public void handlePreferences(final PreferencesEvent pe) {
        new Thread(new Runnable() {

            @Override
            public void run() {
                preferencesFrame.setVisible(true);
            }
        }).start();
    }

    @Override
    public void handleQuitRequestWith(final QuitEvent qe, final QuitResponse qr) {
        new Thread(new Runnable() {

            @Override
            public void run() {
                exit();
                qr.cancelQuit();
            }
        }).start();
    }

    //
    //SQL
    //
    private void connectToSQL() {
        try {
            tradingSchema.connect(getAccount().tradingSchema);
            dataSchema.connect(getAccount().dataSchema);
//            modelSchema.connect(getAccount().modelSchema);
            webSchema.connect(getAccount().webSchema);
        } catch (Exception ex) {
            log(Level.SEVERE, null, ex);
        }
    }

    private void disconnectFromSQL() {
        disconnectSchema(webSchema);
//        disconnectSchema(modelSchema);
        disconnectSchema(dataSchema);
        disconnectSchema(tradingSchema);
    }

    private void disconnectSchema(SqlConnect sqlConnect) {
        if (sqlConnect == null) {
            return;
        }

        try {
            if (sqlConnect.isConnectionClosed()) {
                return;
            }

            sqlConnect.disconnect();
        } catch (SQLException ex) {
            log(Level.SEVERE, null, ex);
        }

        log(Level.INFO, "{0} is disconnected.",
                sqlConnect.getClass().getSimpleName());
    }

    private void initSchema(SqlConnect sqlConnect) throws Exception {
        log(Level.INFO, "Initializing {0} {1}...",
                new Object[]{
                    sqlConnect.getClass().getSimpleName(),
                    sqlConnect.getSchema()
                });

        sqlConnect.init();
    }
}
