package com.rawchen.feishubot.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;


public class TaskPool {
	private static final Map<String, BlockingQueue<Task>> taskPool = new HashMap<>();

	public static void init(List<String> accounts) {
		for (String account : accounts) {
			taskPool.put(account, new LinkedBlockingQueue<>());
		}
	}

	public static void addTask(Task task) throws InterruptedException {
		BlockingQueue<Task> queue = taskPool.get(task.getAccount());
		queue.put(task);
	}

	public static void runTask() {
		Set<String> accounts = taskPool.keySet();
		for (String account : accounts) {
			new Thread(() -> {
				BlockingQueue<Task> queue = taskPool.get(account);
				while (true) {
					try {
						Task task = queue.take();
						task.run();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}).start();
		}
	}


}
