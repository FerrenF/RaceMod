package core.gfx;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import necesse.engine.GameLoadingScreen;
import necesse.engine.localization.Localization;
import necesse.gfx.gameTexture.GameTexture;

public class GamePartsLoader {
	private static final Object LOCK = new Object();
	private static final int LOADING_THREADS = 10;
	private ThreadPoolExecutor loadingExecutor;
	private LinkedList<Task<?>> tasks = new LinkedList();
	private boolean triggerFirstTimeSetup;
	private boolean firstTimeSetupTriggered;

	private static ThreadFactory defaultThreadFactory() {
		AtomicInteger threadNum = new AtomicInteger(0);
		return (r) -> {
			return new Thread((ThreadGroup) null, r, "parts-loader-" + threadNum.incrementAndGet());
		};
	}

	public synchronized void startLoaderThreads() {
		if (this.loadingExecutor == null) {
			this.loadingExecutor = new ThreadPoolExecutor(10, 10, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingDeque(),
					defaultThreadFactory());
		}
	}

	public synchronized void waitForCurrentTasks() {
		while (!this.tasks.isEmpty()) {
			Task<?> task = (Task) this.tasks.removeFirst();

			try {
				task.waitForComplete();
			} catch (ExecutionException | InterruptedException var3) {
				throw new RuntimeException(var3);
			}
		}

	}

	public synchronized void endLoaderThreads() {
		this.waitForCurrentTasks();
		this.loadingExecutor.shutdown();

		try {
			boolean terminated = this.loadingExecutor.awaitTermination(10L, TimeUnit.MINUTES);
			if (!terminated) {
				System.err.println("SKIN LOADER COULD NOT TERMINATE WITHIN 10 MINUTES");
			}
		} catch (InterruptedException var2) {
			throw new RuntimeException(var2);
		}

		this.loadingExecutor = null;
	}

	public void triggerFirstTimeSetup() {
		this.triggerFirstTimeSetup = true;
	}

	public synchronized TextureTask submitTask(String loadingName, Callable<GameTexture> task, boolean makeFinal,
			Consumer<GameTexture> onDone) {
		Future<GameTexture> future = this.loadingExecutor.submit(task);
		TextureTask textureTask = new TextureTask(loadingName, future, makeFinal, onDone);
		this.tasks.addLast(textureTask);
		return textureTask;
	}

	public synchronized TextureTask submitTask(String loadingName, Callable<GameTexture> task, boolean makeFinal) {
		return this.submitTask(loadingName, task, makeFinal, (Consumer) null);
	}

	public synchronized void submitTaskAddToList(ArrayList<GameTexture> textures, int index, String loadingName,
			Callable<GameTexture> task, boolean makeFinal) {
		this.submitTask(loadingName, task, makeFinal, (texture) -> {
			this.addToList(textures, index, texture);
		});
	}

	public synchronized void addToList(ArrayList<GameTexture> textures, int index, GameTexture texture) {
		synchronized (textures) {
			textures.add(Math.min(index, textures.size()), texture);
		}
	}

	public synchronized void submitTask(String loadingName, Runnable task) {
		Future<Object> future = this.loadingExecutor.submit(task, (Object) null);
		EmptyTask emptyTask = new EmptyTask(loadingName, future);
		this.tasks.addLast(emptyTask);
	}

	public interface Task<T> {
		T waitForComplete() throws InterruptedException, ExecutionException;
	}

	public class TextureTask implements Task<GameTexture> {
		public String loadingName;
		public Future<GameTexture> future;
		private boolean isDone;
		public GameTexture texture;
		public boolean makeFinal;
		public Consumer<GameTexture> onDone;

		public TextureTask(String loadingName, Future<GameTexture> future, boolean makeFinal,
				Consumer<GameTexture> onDone) {
			this.loadingName = loadingName;
			this.future = future;
			this.makeFinal = makeFinal;
			this.onDone = onDone;
		}

		public synchronized GameTexture waitForComplete() throws InterruptedException, ExecutionException {
			if (this.isDone) {
				return this.texture;
			} else {
				synchronized (GamePartsLoader.LOCK) {
					if (GamePartsLoader.this.triggerFirstTimeSetup && !GamePartsLoader.this.firstTimeSetupTriggered) {
						GameLoadingScreen.drawLoadingString(Localization.translate("loading", "firstsetup"));
						GamePartsLoader.this.firstTimeSetupTriggered = true;
					}

					if (this.loadingName != null) {
						GameLoadingScreen.drawLoadingSub(this.loadingName);
					}

					this.texture = (GameTexture) this.future.get();
					if (this.makeFinal) {
						this.texture.makeFinal();
					}

					if (this.onDone != null) {
						this.onDone.accept(this.texture);
					}

					this.isDone = true;
					return this.texture;
				}
			}
		}
	}

	public class EmptyTask implements Task<Object> {
		public String loadingName;
		public Future<Object> task;
		private boolean isDone;

		public EmptyTask(String loadingName, Future<Object> task) {
			this.loadingName = loadingName;
			this.task = task;
		}

		public synchronized Object waitForComplete() throws InterruptedException, ExecutionException {
			if (this.isDone) {
				return null;
			} else {
				synchronized (GamePartsLoader.LOCK) {
					if (GamePartsLoader.this.triggerFirstTimeSetup && !GamePartsLoader.this.firstTimeSetupTriggered) {
						GameLoadingScreen.drawLoadingString(Localization.translate("loading", "firstsetup"));
						GamePartsLoader.this.firstTimeSetupTriggered = true;
					}

					if (this.loadingName != null) {
						GameLoadingScreen.drawLoadingSub(this.loadingName);
					}

					this.task.get();
					this.isDone = true;
					return null;
				}
			}
		}
	}
}