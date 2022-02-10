package cc.w0rm.douban.model;

import java.util.Objects;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * @author xuyang
 * @date 2022/2/9
 */
public class Pipe<T> {

    private LinkedBlockingQueue<T> queue;
    private ProducerAction producerAction;

    public Pipe(ProducerAction action) {
        queue = new LinkedBlockingQueue<>();
        producerAction = action;
    }

    public T getTask() {
        while (true) {
            boolean producerStatus = producerAction.finished();
            if (!producerStatus) {
                try {
                    T poll = queue.poll(1, TimeUnit.SECONDS);
                    if (Objects.isNull(poll)) {
                        continue;
                    }
                    return poll;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                return null;
            }
        }
    }


    public void putTask(T task) {
        if (producerAction.finished()) {
            return;
        }
        queue.add(task);
    }

    public boolean finished() {
        return producerAction.finished();
    }
}
