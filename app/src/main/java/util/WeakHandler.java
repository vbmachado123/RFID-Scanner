package util;

import android.os.Handler;
import android.os.Message;

import java.lang.ref.WeakReference;

public abstract class WeakHandler<T> extends Handler {

    private final WeakReference<T> weakRef;

    /**
     * Construct a Handler that  has a weak reference to the object t of type T
     *
     * @param t the object which this Handler will retain a reference to
     */
    public WeakHandler(T t) {
        super();
        weakRef = new WeakReference<T>(t);
    }

    /**
     * If the reference is valid, it invokes {@link #handleMessage(Message, T)}.
     */
    @Override
    public final void handleMessage(Message msg) {
        final T strongRef = weakRef.get();
        if (strongRef != null) {
            handleMessage(msg, strongRef);
        }
    }

    /**
     * Must be implemented by subclasses in order to handle messages.
     *
     * @param msg The message.
     * @param t A strong reference to the object (usually an activity) which is guaranteed not to be <code>null</code>.
     * @see Handler#handleMessage(Message)
     */
    public abstract void handleMessage(Message msg, T t);

}