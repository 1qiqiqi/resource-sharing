package com.dahaiwuliang.code.run;

import com.dahaiwuliang.code.service.ArcTypeService;
import com.dahaiwuliang.code.service.ArticleService;
import com.dahaiwuliang.code.service.LinkService;
import com.dahaiwuliang.code.util.Consts;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import javax.servlet.ServletContext;
import java.util.List;

/**
 * 启动服务加载数据
 */
@Component("StartupRunner")
public class StartupRunner implements CommandLineRunner {
    @Autowired
    private ServletContext application;

    @Autowired
    private ArcTypeService arcTypeService;

    @Autowired
    private LinkService linkService;

    @Autowired
    @Lazy
    private ArticleService articleService;

    @Override
    public void run(String... args) throws Exception {
        loadData();
    }

    /**
     * 加载数据到application缓存中
     */
    public void loadData(){
        application.setAttribute(Consts.ARC_TYPE_LIST,arcTypeService.listAll(Sort.Direction.ASC,"sort"));   //所有资源分类
        application.setAttribute(Consts.NEW_ARTICLE,articleService.getNewArticle(10));                  //10条最新资源
        application.setAttribute(Consts.CLICK_ARTICLE,articleService.getClickArticle(10));              //10条热门资源
        application.setAttribute(Consts.RANDOM_ARTICLE,articleService.getRandomArticle(10));            //10条随机资源（热搜推荐）
        application.setAttribute(Consts.LINK_LIST,linkService.listAll(Sort.Direction.ASC,"sort"));          //所有友情链接
    }
}
