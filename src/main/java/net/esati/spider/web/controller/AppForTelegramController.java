package net.esati.spider.web.controller;

import cn.hutool.core.date.DateUtil;
import com.alibaba.fastjson.JSONObject;
import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.models.auth.In;
import lombok.extern.slf4j.Slf4j;
import net.esati.spider.web.dao.TaskRecordRepository;
import net.esati.spider.web.domain.RestResponse;
import net.esati.spider.web.domain.TaskRecord;
import net.esati.spider.web.service.SendRequestService;
import net.esati.spider.web.util.HttpClientUtil;
import net.esati.spider.web.util.RestResponseUtil;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.CompletableFuture;

/**
 * @author Gao
 * @version 1.0
 * @description: TODO
 * @date 2023/10/22 21:32
 */
@Slf4j
@Api(tags = "Telegram爬虫")
@RestController
@RequestMapping(value = "/spider/",produces = org.springframework.http.MediaType.APPLICATION_JSON_VALUE)
public class AppForTelegramController {

    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private SendRequestService sendRequestService;
    @Autowired
    private TaskRecordRepository taskRecordRepository;

    @ApiOperation("任务创建操作")
    @ApiOperationSupport(order = 4)
    @GetMapping("/exec-task/{msgId}")
    public RestResponse<String> execTgTask(@PathVariable Integer msgId,@RequestParam Boolean isStop){

        if(redisTemplate.hasKey("task_run_tg")){
            log.info("TG执行记录已存在，不执行");
            if (isStop) {
                redisTemplate.delete("task_run_tg");
                log.info("TG执行记录已存在，中断任务进行");
                return RestResponseUtil.success("已停止___"+msgId+"___"+isStop);
            } else {
                return RestResponseUtil.success("运行中___"+msgId+"___当前执行消息号："+redisTemplate.opsForValue().get("task_run_tg"));
            }
        } else {
            log.info("TG执行记录不存在，开始添加任务！");
            redisTemplate.opsForValue().set("task_run_tg", msgId);
        }
        CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
           try {
               Integer finalMsgId = msgId;
               Integer execCount = 0;
               while (redisTemplate.hasKey("task_run_tg")) {
                   execCount++;
                   String address = requestForTg(finalMsgId);
                   redisTemplate.opsForValue().set("task_run_tg", finalMsgId+"###"+execCount);
                   if(address.startsWith("0x") || address.startsWith("0X")){

                       log.info(" msg id ： {} 已正常处理，准备请求下一个ID : {}",finalMsgId,finalMsgId+1);
                       finalMsgId = finalMsgId+1;
                   }

                   Thread.sleep(15*1000);
               }
               log.info("任务已停止----------------");
           } catch (Exception e) {
               log.error("执行出错：：：：：： {}",e);
               e.printStackTrace();
           }
        });

        return RestResponseUtil.success("监听任务执行中___"+msgId+"___"+isStop);
    }

    String requestForTg(Integer msgId){
        String url = "https://t.me/mengmeibot1/"+msgId;
        String res = HttpClientUtil.requestGetUrlStr(url, AppForCoinToolController.isUseProxy, AppForCoinToolController.proxyHost, AppForCoinToolController.proxyPort,6);
//        log.info(res);
        // 使用 Jsoup 解析 HTML
        Document document = Jsoup.parse(res);
        // 获取所有的 <meta> 元素
        Elements metaElements = document.select("meta");
        // 遍历 <meta> 元素并输出属性信息
        String address = "";
        for (Element metaElement : metaElements) {
            String tagName = metaElement.tagName();
            String name = metaElement.attr("name");
            String content = metaElement.attr("content");
            if("twitter:description".equals(name)){
                address = content.substring(0, content.indexOf('\n'));
                if(address.startsWith("0x") || address.startsWith("0X")){
                    String taskNo = dualData(address);
                    log.info("msgId : {} , 已获取到合约地址： {} ， 开始执行任务 ：{}",msgId,address,taskNo);
                    break;
                } else {
                    continue;
                }
            } else {
                continue;
            }
        }
        return address;
    }

    String dualData(String token){
        String finalToken = token;
        String taskNo = AppForDexspyController.generateTaskNo();


        CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
            int execNumber = 6;
            // 异步任务的逻辑
            log.info("TG异步任务开始执行...");
            if(redisTemplate.hasKey(token+"_tg")){
                log.info("TG代币执行记录已存在，不执行");
            } else {
                redisTemplate.opsForValue().set(token+"_tg", DateUtil.current());
                redisTemplate.opsForValue().set(taskNo, "-1#"+ execNumber);
                JSONObject dexScreenerInfo = new JSONObject();
                try {
                    dexScreenerInfo = AppForDexspyController.requestDexScreener(finalToken);
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (NoSuchAlgorithmException e) {
                    e.printStackTrace();
                } catch (KeyManagementException e) {
                    e.printStackTrace();
                }
                String pairsAddress = dexScreenerInfo.getString("pairsAddress");
                TaskRecord row = taskRecordRepository.save(new TaskRecord(taskNo,finalToken,pairsAddress,execNumber,120,"sys_spider_01"));
                sendRequestService.mainTask(dexScreenerInfo,finalToken,pairsAddress,taskNo,execNumber,120,8);

            }

        });
        return taskNo;
    }
}
