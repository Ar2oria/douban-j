package cc.w0rm.douban.service;

import cc.w0rm.douban.model.Pipe;
import cc.w0rm.douban.model.Result;

import java.util.ArrayList;
import java.util.List;

/**
 * @author xuyang
 * @date 2022/2/9
 */
public class Manager {

    public static Result run(String place) {
        return run(place, new ArrayList<>());
    }

    public static Result run(String place, List<String> tagList) {
        DBService service = new DBService();
        Pipe pipeWebResource = Downloader.search(place, service.getFilter());
        Pipe pipeText = TextProcessor.process(pipeWebResource, tagList);
        service.save(pipeText);
        return search(service, place, tagList);
    }

    private static Result search(DBService service, String place, List<String> tagList) {
        return null;
    }
}
