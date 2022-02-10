package cc.w0rm.douban;

import cc.w0rm.douban.service.DoubanEngine;
import com.google.common.collect.Lists;

import java.io.IOException;

/**
 * Hello world!
 */
public class App {
    public static void main(String[] args) throws IOException {
        DoubanEngine.run("望京", Lists.newArrayList("电梯", "阳台", "无中介"), 10,
                "bid=ZSuQFFBm9SY; gr_user_id=71140210-ec3e-4718-93b2-39dffb5296a2; UM_distinctid=17d930f8586156-0370e42e92e805-1f396452-1ea000-17d930f85872ca; douban-fav-remind=1; ll=\"108288\"; viewed=\"26853217_21331443\"; dbcl2=\"223241655:drJoWTERrTg\"; ck=c9A8; ct=y; push_doumail_num=0; push_noty_num=0");

        DoubanEngine.run("望京西", Lists.newArrayList("电梯", "阳台", "无中介"), 10,
                "bid=ZSuQFFBm9SY; gr_user_id=71140210-ec3e-4718-93b2-39dffb5296a2; UM_distinctid=17d930f8586156-0370e42e92e805-1f396452-1ea000-17d930f85872ca; douban-fav-remind=1; ll=\"108288\"; viewed=\"26853217_21331443\"; dbcl2=\"223241655:drJoWTERrTg\"; ck=c9A8; ct=y; push_doumail_num=0; push_noty_num=0");

        DoubanEngine.run("望京南", Lists.newArrayList("电梯", "阳台", "无中介"), 10,
                "bid=ZSuQFFBm9SY; gr_user_id=71140210-ec3e-4718-93b2-39dffb5296a2; UM_distinctid=17d930f8586156-0370e42e92e805-1f396452-1ea000-17d930f85872ca; douban-fav-remind=1; ll=\"108288\"; viewed=\"26853217_21331443\"; dbcl2=\"223241655:drJoWTERrTg\"; ck=c9A8; ct=y; push_doumail_num=0; push_noty_num=0");

        DoubanEngine.run("将台", Lists.newArrayList("电梯", "阳台", "无中介"), 10,
                "bid=ZSuQFFBm9SY; gr_user_id=71140210-ec3e-4718-93b2-39dffb5296a2; UM_distinctid=17d930f8586156-0370e42e92e805-1f396452-1ea000-17d930f85872ca; douban-fav-remind=1; ll=\"108288\"; viewed=\"26853217_21331443\"; dbcl2=\"223241655:drJoWTERrTg\"; ck=c9A8; ct=y; push_doumail_num=0; push_noty_num=0");

        DoubanEngine.run("东湖渠", Lists.newArrayList("电梯", "阳台", "无中介"), 10,
                "bid=ZSuQFFBm9SY; gr_user_id=71140210-ec3e-4718-93b2-39dffb5296a2; UM_distinctid=17d930f8586156-0370e42e92e805-1f396452-1ea000-17d930f85872ca; douban-fav-remind=1; ll=\"108288\"; viewed=\"26853217_21331443\"; dbcl2=\"223241655:drJoWTERrTg\"; ck=c9A8; ct=y; push_doumail_num=0; push_noty_num=0");

        System.exit(0);
    }
}
