package cc.w0rm.douban.service;

import cc.w0rm.douban.model.*;
import cc.w0rm.douban.util.CollUtil;
import cc.w0rm.douban.util.DateUtil;
import cc.w0rm.douban.util.JsonUtil;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.Getter;
import lombok.Setter;
import okhttp3.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * @author xuyang
 * @date 2022/2/9
 */
public class Downloader {

    private static final int MAX_THREAD_NUM = 1;
    private static final int INTERVAL = 5000;
    private static final int MAX_TIME_OUT = 5500;


    static class DoubanSearchClient {

        @Getter
        @Setter
        private String cooike = "";

        private static final OkHttpClient OK_HTTP_CLIENT = buildClient();

        private static OkHttpClient buildClient() {
            ConnectionPool connectionPool = new ConnectionPool(256, 5, TimeUnit.MINUTES);

            return new OkHttpClient.Builder()
                    .callTimeout(3, TimeUnit.SECONDS)
                    .connectionPool(connectionPool)
                    .retryOnConnectionFailure(false)
                    .build();
        }

        DoubanListResponseDTO getDoubanList(DoubanListRequestBO requestBO) {
            Response response = null;
            try {
                Request req = new Request.Builder()
                        .url(requestBO.getUrl())
                        .addHeader("Connection", "close")
                        .addHeader("Host", "uz.yurixu.com")
                        .addHeader("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/98.0.4758.80 Safari/537.36")
                        .build();
                Call call = OK_HTTP_CLIENT.newCall(req);
                response = call.execute();
                String responseStr = response.body().string();
                BaseResponseDTO<DoubanListResponseDTO> responseDTO = JsonUtil.toObject(responseStr, new TypeReference<BaseResponseDTO<DoubanListResponseDTO>>() {
                });
                if (Objects.isNull(responseDTO.getCode()) && responseDTO.getCode() != 0) {
                    throw new RuntimeException("请求返回值不为0!");
                }
                return responseDTO.getData();
            } catch (Exception e) {
                throw new RuntimeException(e);
            } finally {
                if (Objects.nonNull(response)) {
                    response.close();
                }
            }
        }

        String getDoubanHtml(String url) {
            Response response = null;
            try {
                Request req = new Request.Builder()
                        .url(url)
                        .addHeader("Cookie", cooike)
                        .addHeader("Host", "www.douban.com")
                        .addHeader("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/98.0.4758.80 Safari/537.36")
                        .build();
                Call call = OK_HTTP_CLIENT.newCall(req);
                response = call.execute();
                return response.body().string();
            } catch (Exception e) {
                throw new RuntimeException(e);
            } finally {
                if (Objects.nonNull(response)) {
                    response.close();
                }
            }
        }
    }

    static class StatisticsWindow {
        private static final int WINDOW_SIZE = 50;
        private static final int MIN_REPEAT_NUM = 100;
        private static final int REPEAT_RATIO = 95;


        private Queue<Boolean> queue = new ArrayDeque<>();
        private int repeat = 0;
        private long total = 0;

        public void repeatAndIncrement() {
            if (queue.size() == WINDOW_SIZE) {
                boolean poll = queue.poll();
                if (poll) {
                    repeat--;
                }
            }
            queue.offer(true);
            repeat++;
            total++;
        }

        public void increment() {
            if (queue.size() == WINDOW_SIZE) {
                boolean poll = queue.poll();
                if (poll) {
                    repeat--;
                }
            }
            queue.offer(false);
            total++;
        }

        public boolean overflow() {
            if (total < MIN_REPEAT_NUM) {
                return false;
            }
            if (repeat * 100 / queue.size() > REPEAT_RATIO) {
                return true;
            }
            return false;
        }
    }

    static class DownloaderAction implements ProducerAction {
        private volatile boolean status = false;


        @Override
        public boolean finished() {
            return status;
        }

        @Override
        public void setFinish() {
            status = true;
        }
    }

    public static Pipe<DoubanAnalyseModel> search(String place, String cookie, int day, FilterMask<DoubanListResponseDTO.ItemDTO> filterMask) {
        ProducerAction action = new DownloaderAction();
        Pipe<DoubanAnalyseModel> pipe = new Pipe<>(action);
        StatisticsWindow window = new StatisticsWindow();

        new Thread(() -> {
            DoubanSearchClient doubanSearchClient = new DoubanSearchClient();
            doubanSearchClient.setCooike(cookie);

            long total = -1L;
            long maxTotal = 0L;

            DoubanListRequestBO requestBO = new DoubanListRequestBO();
            requestBO.setPage(0);
            requestBO.setKey(place);

            while (total < maxTotal) {
                try {
                    requestBO.setPage(requestBO.getPage() + 1);
                    DoubanListResponseDTO responseDTO = doubanSearchClient.getDoubanList(requestBO);
                    List<DoubanListResponseDTO.ItemDTO> dataList = responseDTO.getDataList();
                    if (CollUtil.isEmpty(dataList)) {
                        break;
                    }

                    PageInfoDTO pageInfo = responseDTO.getPageInfo();
                    maxTotal = pageInfo.getTotal();
                    if (total == -1L){
                        total = dataList.size();
                    }else {
                        total += dataList.size();
                    }

                    List<DoubanListResponseDTO.ItemDTO> newDataList = null;
                    try {
                        newDataList = filter(dataList, day, window, filterMask);
                    } catch (Exception e) {
                        e.printStackTrace();
                        break;
                    }
                    if (CollUtil.isEmpty(newDataList)) {
                        continue;
                    }

                    getDetail(doubanSearchClient, newDataList, pipe);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            action.setFinish();
            System.out.println("=== Downloader 下载完成 ===");
        }).start();

        return pipe;
    }

    private static List<DoubanListResponseDTO.ItemDTO> filter(List<DoubanListResponseDTO.ItemDTO> dataList,
                                                              int day,
                                                              StatisticsWindow window,
                                                              FilterMask<DoubanListResponseDTO.ItemDTO> filterMask) {
        if (CollUtil.isEmpty(dataList)) {
            return dataList;
        }

        long now = Instant.now().toEpochMilli();

        List<DoubanListResponseDTO.ItemDTO> newDataList = dataList.stream()
                .filter(data -> "豆瓣".equals(data.getSourceName()))
                .collect(Collectors.toList());

        List<DoubanListResponseDTO.ItemDTO> result = new ArrayList<>(dataList.size());

        Map<DoubanListResponseDTO.ItemDTO, Boolean> filterResult = filterMask.filter(newDataList);
        for (DoubanListResponseDTO.ItemDTO itemDTO : filterResult.keySet()) {
            boolean old = DateUtil.msToDays(now - itemDTO.getPubTime().getTime()) > day;
            boolean repeat = filterResult.getOrDefault(itemDTO, false);
            if (old || repeat) {
                window.repeatAndIncrement();
            } else {
                result.add(itemDTO);
                window.increment();
            }
            if (window.overflow()) {
                throw new RuntimeException("重复数据过多！");
            }
        }

        return result;
    }

    private static final ExecutorService EXECUTOR_SERVICE = new ThreadPoolExecutor(MAX_THREAD_NUM,
            MAX_THREAD_NUM, 0, TimeUnit.MILLISECONDS, new ArrayBlockingQueue<>(500));

    private static void getDetail(DoubanSearchClient doubanSearchClient, List<DoubanListResponseDTO.ItemDTO> dataList, Pipe<DoubanAnalyseModel> pipe) {
        if (CollUtil.isEmpty(dataList) || Objects.isNull(pipe)) {
            return;
        }

        final AtomicInteger interval = new AtomicInteger(INTERVAL);
        CompletableFuture[] completableFutures = dataList.stream()
                .map(item -> CompletableFuture.supplyAsync(() -> {
                    String doubanHtml = StringUtils.EMPTY;
                    try {

                        doubanHtml = doubanSearchClient.getDoubanHtml(item.getUrl());

                        int intevalInt = interval.get();
                        intevalInt /= 2;
                        intevalInt = Math.max(intevalInt, INTERVAL);
                        interval.set(intevalInt);
                    } catch (Exception e) {
                        interval.set(interval.get() * 2);
                        e.printStackTrace();
                    } finally {
                        try {
                            Thread.sleep(interval.get());
                        } catch (Exception e) {
                            //do nothing
                        }
                    }
                    return Pair.of(item, doubanHtml);
                }, EXECUTOR_SERVICE).whenComplete((r, e) -> {
                    if (Objects.nonNull(e)) {
                        return;
                    }
                    DoubanAnalyseModel model = new DoubanAnalyseModel();
                    model.setItem(r.getKey());
                    model.setHtml(r.getRight());
                    pipe.putTask(model);
                })).collect(Collectors.toList()).toArray(new CompletableFuture[dataList.size()]);

        CompletableFuture<Void> oneFuture = CompletableFuture.allOf(completableFutures);

        try {
            oneFuture.get((long) MAX_TIME_OUT * dataList.size(), TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
