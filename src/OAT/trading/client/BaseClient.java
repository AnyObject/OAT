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

package OAT.trading.client;

import OAT.event.Listenable;
import OAT.event.State;
import OAT.event.StateChangeEvent;
import OAT.trading.thread.BaseThread;
import OAT.util.Loggable;

/**
 *
 * @author Antonio Yip
 */
public abstract class BaseClient extends Listenable implements  Connectable, Loggable {

    protected BaseThread baseThread;

    public BaseClient(BaseThread baseThread) {
        this.baseThread = baseThread;
    }

    @Override
    public void run() {
        connect();
    }

    public abstract int getClientId();

    //
    //Events
    //
    protected void fireStateChanged(State state, boolean active) {
        notifyListeners(new StateChangeEvent(this, state, active));
    }
}
