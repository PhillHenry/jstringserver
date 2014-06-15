package com.google.code.jstringserver.stats;

public class SynchronizedStatsDecorator implements Stats {
	
	private final Stats stats;
	
	public SynchronizedStatsDecorator(Stats stats) {
		this.stats = stats;
	}

	@Override
	public void start(long time) {
		synchronized (stats) {
			stats.start(time);
		}
	}

	@Override
	public void stop(long duration) {
		synchronized (stats) {
			stats.stop(duration);
		}
	}

	@Override
	public long getMaxTime() {
		synchronized (stats) {
			return stats.getMaxTime();
		}
	}

	@Override
	public long getTotalCallsServiced() {
		synchronized (stats) {
			return stats.getTotalCallsServiced();
		}
	}

	@Override
	public String toString() {
		synchronized (stats) {
			return stats.toString();
		}
	}

}
