package cc.w0rm.douban.service;

import cc.w0rm.douban.model.DoubanAnalyseModel;
import cc.w0rm.douban.model.Pipe;
import cc.w0rm.douban.model.Result;

import java.util.List;

/**
 * @author xuyang
 * @date 2022/2/9
 */
public class Manager {

    public static Result run(String place, List<String> tagList, String cookie) {
        DBService service = new DBService();
        Pipe<DoubanAnalyseModel> pipeWebResource = Downloader.search(place, cookie, service.getFilter());
        Pipe<DoubanAnalyseModel> pipeText = TextProcessor.process(pipeWebResource);
        service.save(pipeText);
        return search(service, place, tagList);
    }

    private static Result search(DBService service, String place, List<String> tagList) {


        return null;
    }
}
