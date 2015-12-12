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

package OAT.event;

import java.util.EventListener;
import java.util.EventObject;
import javax.swing.event.ChangeEvent;
import javax.swing.event.EventListenerList;
import org.jfree.data.general.DatasetChangeEvent;
import org.jfree.data.general.DatasetChangeListener;

/**
 * Listener with generic methods.
 *
 * @author Antonio Yip
 */
public abstract class Listenable {

    private EventListenerList listenerList = new EventListenerList();
//    private LinkedHashSet<WeakReference> weakReferenceListenerList = new LinkedHashSet<WeakReference>();

    /**
     * Add a listener.
     *
     * @param listener generic listener
     */
    public void addChangeListener(EventListener listener) {
        removeChangeListener(listener);

        if (listener != null) {
            listenerList.add(EventListener.class, listener);
//            weakReferenceListenerList.add(new WeakReference<EventListener>(listener));
        }
    }

    /**
     * Remove the specified listener.
     *
     * @param listener
     */
    public void removeChangeListener(EventListener listener) {
        if (listener != null) {
            listenerList.remove(EventListener.class, listener);
//            for (WeakReference weakReference : weakReferenceListenerList) {
//                if (weakReference.get() == listener) {
//                    weakReferenceListenerList.remove(weakReference);
//                }
//            }
        }
    }

    /**
     * Notifies all registered listeners that the items has changed.
     *
     * @param event contains information about the event that triggered the
     * notification.
     *
     */
    protected void notifyListeners(EventObject event) {
        Object[] listeners = listenerList.getListenerList();

        for (int i = listeners.length - 1; i > 0; i -= 2) {
            Object listener = listeners[i];

            if (listener instanceof GenericListener) {
                ((GenericListener) listener).eventHandler(event);

            } else if (listener instanceof DatasetChangeListener
                    && event instanceof DatasetChangeEvent) {
                ((DatasetChangeListener) listener).datasetChanged((DatasetChangeEvent) event);
            }
        }

//        for (WeakReference<EventListener> weakReference : weakReferenceListenerList) {
//            final EventListener listener = weakReference.get();
//            if (listener == null) {
//                this.removeListener(event.getSource());
//            } else {
//                if (listener instanceof GenericListener) {
//                    ((GenericListener) listener).eventHandler(event);
//
//                } else if (listener instanceof DatasetChangeListener
//                        && event instanceof DatasetChangeEvent) {
//                    ((DatasetChangeListener) listener).datasetChanged((DatasetChangeEvent) event);
//                }
//            }
//        }
    }

    /**
     * Notifies all registered listeners that the items has changed.
     *
     */
    public void fireChanged() {
        notifyListeners(new ChangeEvent(this));
    }

//    private void removeListener(Object src) {
//        try {
//            final String methodName = "removeChangeListener";
//            final Method method = ReflectionUtils.findMethod(src.getClass(), methodName, EventListener.class);
//            ReflectionUtils.invokeMethod(method, src, this);
//        } catch (Throwable e) {
//        }
//    }
}
