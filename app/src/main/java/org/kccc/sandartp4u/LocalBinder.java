package org.kccc.sandartp4u;

import android.os.Binder;

import java.lang.ref.WeakReference;

public class LocalBinder<S> extends Binder {
	public LocalBinder(S service) {
		mService = new WeakReference<S>(service);
	}
	
	private WeakReference<S> mService;
	
	public S getService() {
		return mService.get();
	}
}
