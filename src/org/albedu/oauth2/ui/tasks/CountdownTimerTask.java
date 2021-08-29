package org.albedu.oauth2.ui.tasks;

import java.util.TimerTask;

public class CountdownTimerTask extends TimerTask {
	private BackgroundTaskListener backgroundTaskListener;
	private int secondsRemaining;
	
	public CountdownTimerTask(BackgroundTaskListener backgroundTaskListener, int secondsRemaining) {
		this.backgroundTaskListener = backgroundTaskListener;
		this.secondsRemaining = secondsRemaining;
	}
	
	@Override
	public void run() {
		this.secondsRemaining = this.secondsRemaining - 1;
		this.backgroundTaskListener.updateUI(this, this.secondsRemaining);
	}
}
