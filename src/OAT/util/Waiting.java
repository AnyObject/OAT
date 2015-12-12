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

import java.util.logging.Level;

/**
 *
 * @author Antonio Yip
 */
public abstract class Waiting {

    private long maxWait;
    private long pause;
    private long waitCount = 0;
    private boolean timeout = false;
    private java.util.logging.Logger logger;

    /**
     * Implement waitWhile() for boolean test. <br> Implement retry() for retry
     * after boolean test is false.
     *
     * @param maxWait in milliseconds
     * @param pause in milliseconds
     * @param logger
     */
    public Waiting(long maxWait, long pause, java.util.logging.Logger logger) {
        this.maxWait = maxWait;
        if (pause > 0) {
            this.pause = pause;
        } else {
            this.pause = maxWait;
        }
        this.logger = logger;

        run();
    }

    public abstract boolean waitWhile();

    public abstract void retry();

    public abstract String message();

    public abstract void timeout();

    public long getMaxWait() {
        return maxWait;
    }

    public long getPause() {
        return pause;
    }

    public long getWaitCount() {
        return waitCount;
    }

    public boolean isTimeout() {
        return timeout;
    }
    
   

    private void run() {
        String msg = message();
        boolean wait;

        while (wait = waitWhile()) {
            if (pause * waitCount < maxWait) {

                if (waitCount == 0 && logger != null && msg != null) {
                    logger.info(msg);
                }

                retry();

                try {
                    Thread.sleep(pause);
                } catch (InterruptedException ex) {
                    if (logger != null) {
                        logger.log(Level.SEVERE, msg, ex);
                    } else {
                        ex.printStackTrace();
                    }
                }
                waitCount++;

            } else {
                timeout = true;
                
                if (logger != null && maxWait > pause && msg != null) {
                    logger.log(Level.WARNING, "Time out ({0}s) - {1}",
                            new Object[]{
                                (int) (maxWait / 1000),
                                msg
                            });
                }

                timeout();
                return;
            }
        }

        if (!wait) {
            logger.log(Level.FINE, "{0} finished.", msg);
        }
    }
}
