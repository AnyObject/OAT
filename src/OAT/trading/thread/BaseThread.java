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

package OAT.trading.thread;

import java.util.*;
import java.util.logging.Level;
import OAT.event.State;
import OAT.event.StateChangeEvent;
import OAT.trading.Main;
import OAT.trading.Plugin;
import OAT.util.DateUtil;
import OAT.util.GeneralUtil;
import OAT.util.Mailer;
import OAT.util.ThreadLogger;

/**
 *
 * @author Antonio Yip
 */
public abstract class BaseThread extends Plugin implements Runnable {

    protected Set<State> currentStates = Collections.synchronizedSet(EnumSet.noneOf(State.class));
    protected long threadStartTime;
    protected ThreadLogger logger;
    protected Mailer mailer;
    private Timer timer;
    private long wakeUpTime, nextScheduleTime;
    private int threadId;

    /**
     * Initialisation.
     */
    public void init() {
        threadStartTime = DateUtil.getTimeNow();

        initMailer();
        initLogger();

        log(Level.INFO, "Thread started at {0}",
                DateUtil.getTimeStamp(threadStartTime, DateUtil.DATETIME_TZ_FORMAT));
    }

    @Override
    public void run() {
        Main.getMainThread().log(Level.INFO, "Starting thread: {0}.", getName());
        timer = new Timer();
        intervalTask();
    }

    public void goSleep() {
        if (isSleeping()) {
            return;
        }

        preSleep();
        setState(true, State.SLEEPING);

        String msg = "Hibernating. Wake up at "
                + DateUtil.getCalendarDate(wakeUpTime).getTime();

        log(Level.INFO, msg);
//        mailer.status(msg);
    }

    private void wakeUp() {
        if (DateUtil.getTimeNow() < wakeUpTime) {
            goSleep();
            return;
        }

        if (!isSleeping()) {
            return;
        }

//        getLogger().renewLogFile();
        initLogger(); //re-init logger

        setState(false, State.SLEEPING);
        setState(true, State.WAKING_UP);

        postWakeUp();

        setState(false, State.WAKING_UP);
    }

    public void wakeUpUntil(long wakeUpTime) {
        this.wakeUpTime = wakeUpTime;

        if (isLoggable(Level.FINER)) {
            log(Level.FINER, "Wake up time: {0}", DateUtil.getDateTimeString(wakeUpTime));
        }

        wakeUp();
    }

    public void wakeUpNow() {
        wakeUpUntil(DateUtil.getTimeNow());
    }

    public abstract void postWakeUp();

    public abstract void postConnection();

    public abstract void preSleep();
//    {
//        mailer.status(getName() + " has woken up.");
//    }

    /**
     * Procedures to run on exit.
     */
    public void exit() {
        setState(true, State.EXITING);

        if (timer != null) {
            timer.cancel();
            timer.purge();
        }

        if (isSleeping() || isBacktesting()) {
            exited();
        } else {
            preExit();
        }
    }

    public final void exited() {
        setState(false, State.LOADED, State.EXITING, State.INITIALIZED);
        log(Level.INFO, "Exited.");
        getLogger().pushLog(false);
        setState(true, State.EXITED);
    }

    /**
     * Clean up.
     */
    public synchronized void cleanup() {
        getLogger().pushLog();
        getLogger().clearLogTextPane();
        log(Level.INFO, "Cleaning...");
    }

    protected synchronized void initMailer() {
        mailer = new Mailer(Main.p_email_Receipient, getName());
    }

    protected synchronized void initLogger() {
        logger = new ThreadLogger(getDefaultLoggerName(), null, Main.getLogLevel());
        logger.setMailer(mailer);

        log(Level.CONFIG, "Logger initialized. Default level = " + Main.getLogLevel());
    }

    /**
     * Interval task launched in every period of defined by {@link Main.p_Task_Interval}.
     */
    protected void intervalTask() {
        nextScheduleTime = DateUtil.nextScheduleTime(Main.p_Task_Interval);
        Date time = DateUtil.getCalendarDate(nextScheduleTime).getTime();

        try {
            timer.schedule(new TimerTask() {

                @Override
                public void run() {
                    intervalTask();
                }
            }, time);
        } catch (Exception ex) {
            log(Level.SEVERE, null, ex);
        }

        if (isLoggable(Level.FINER)) {
            log(Level.FINER, "Next Interval Task: {0}", time.toString());
        }

        if (DateUtil.isMinuteInterval(Main.p_Push_Log_Every_Minute)) {
            getLogger().pushLog();
        }
    }

    /**
     * Pause the thread at the provided time.
     *
     * @param wait milliseconds
     * @param message loggable text
     */
    protected void pause(long wait, String message) {
        GeneralUtil.pause(wait, logger, message);
    }

    /**
     * Procedures to run before exit.
     */
    protected abstract void preExit();

    //
    //Getters
    //
    public boolean isStateActive(State state) {
        return currentStates.contains(state);
    }

    public boolean isBacktesting() {
        return isStateActive(State.BACKTESTING);
    }

    public boolean isExited() {
        return isStateActive(State.EXITED);
    }

    public boolean isExiting() {
        return isStateActive(State.EXITING);
    }

    public boolean isInitialized() {
        return isStateActive(State.INITIALIZED);
    }

    public boolean isWakingUp() {
        return isStateActive(State.WAKING_UP);
    }

    public boolean isSleeping() {
        return isStateActive(State.SLEEPING);
    }

    public boolean isServerDelayed() {
        return isStateActive(State.SERVER_DELAYED);
    }

    /**
     * Test if the client is closing a position.
     *
     * @return true or false
     */
//    public boolean isClosingPosition() {
//        return isStateActive(State.CLOSING_POSITION);
//    }

    /**
     * Test if the client is requesting execution.
     *
     * @return true or false
     */
    public boolean isRequestingExecutions() {
        return isStateActive(State.REQUESTING_EXECUTIONS);
    }

    /**
     * Test if the client is requesting contract details.
     *
     * @return true or false
     */
    public boolean isRequestingContractDetails() {
        return isStateActive(State.REQUESTING_CONTRACT_DETAILS);
    }

    /**
     * Test if the client is requesting trading contract details.
     *
     * @return true or false
     */
    public boolean isRequestingTradingContractDetails() {
        return isStateActive(State.REQUESTING_TRADING_CONTRACT_DETAILS);
    }

    /**
     * Test if the client is pending to request executions.
     *
     * @return true or false
     */
    public boolean isPendingReqExecutions() {
        return isStateActive(State.PENDING_REQUEST_EXECUTIONS);
    }

    /**
     * Test if the client is pending to check position.
     *
     * @return true or false
     */
    public boolean isPendingCheckPosition() {
        return isStateActive(State.PENDING_CHECK_POSITION);
    }

    /**
     * Test if trading is suspended.
     *
     * @return
     */
    public boolean isTradingSuspended() {
        return isStateActive(State.TRADING_SUSPENDED);
    }

    public int getThreadId() {
        return threadId;
    }

    public long getWakeUpTime() {
        return wakeUpTime;
    }

    public long getNextScheduleTime() {
        return nextScheduleTime;
    }

    /**
     * Get the logger Name.
     *
     * @return logger name
     */
    public abstract String getDefaultLoggerName();

    /**
     * Get displayed name.
     *
     * @return simple class name
     */
    public String getName() {
        return this.getClass().getSimpleName();
    }

    public long getClientStartTime() {
        return threadStartTime;
    }

    @Override
    public ThreadLogger getLogger() {
        return logger;
    }

    @Override
    public String getLogPrefix() {
        return "";
    }

    //
    //Setters
    //
    /**
     *
     * @param threadId
     */
    public void setThreadId(int threadId) {
        this.threadId = threadId;
    }

    public void setLogger(ThreadLogger logger) {
        this.logger = logger;
    }

    /**
     * Set client state. A message will be logged if the state is changed to
     * true from false.
     *
     * @param active true or false
     * @param states one or more states to be changed
     */
    public void setState(boolean active, State... states) {
        if (states == null) {
            return;
        }

        boolean loggable = !isBacktesting() && logger != null;
        boolean anyChanged = false;

        for (State state : states) {
            boolean changed;

            if (active) {
                if (changed = currentStates.add(state)) {
                    if (loggable && state.message != null) {
                        log(state.level, state.message);
                    }

                    fireStateChanged(state, active);
                }

            } else {
                if (changed = currentStates.remove(state)) {
                    fireStateChanged(state, active);
                }
            }

            if (loggable && changed) {
                if (state.level.intValue() >= logger.getLevel().intValue()) {
                    logger.log(Level.FINE, "{0} = {1}", new Object[]{state.name(), active});
                    anyChanged |= changed;
                }
            }
        }

        //list all current states
        if (anyChanged) {
            logCurrentStates(Level.FINE);
        }
    }

    public void logCurrentStates(Level level) {
        StringBuilder sb = new StringBuilder();

        for (State s : currentStates) {
            if (sb.length() > 0) {
                sb.append(", ");
            }
            sb.append(s.name());
        }

        log(level, "Current states: {0}", sb.toString());
    }

    //
    //Events
    //
    protected void fireStateChanged(State state, boolean active) {
        notifyListeners(new StateChangeEvent(this, state, active));
    }
}
