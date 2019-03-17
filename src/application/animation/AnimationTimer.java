package application.animation;

import javafx.concurrent.Task;

public class AnimationTimer extends Task<Void> {

	private int time;

	public AnimationTimer(int ms) {
		this.time = ms/5;
	}

	@Override
	public Void call() throws Exception {

		for (int i = 0; i < time; i++) {
			Thread.sleep(5);
			updateProgress(i, time);
		}

		return null;
	}

}
