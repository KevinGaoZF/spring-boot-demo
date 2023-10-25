package net.esati.spider.web.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.RandomUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import net.esati.spider.web.dao.TaskExecuteRecordRepository;
import net.esati.spider.web.dao.TaskRecordRepository;
import net.esati.spider.web.domain.TaskExecuteRecord;
import net.esati.spider.web.domain.TaskRecord;
import net.esati.spider.web.service.SendRequestService;
import net.esati.spider.web.util.HttpClientUtil;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.concurrent.CompletableFuture;

import static net.esati.spider.web.controller.AppForDexspyController.play;

/**
 * @author Gao
 * @version 1.0
 * @description: TODO
 * @date 2023/8/16 13:59
 */
@Slf4j
@Service
public class SendRequestServiceImpl implements SendRequestService {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private TaskRecordRepository taskRecordRepository;

    @Autowired
    private TaskExecuteRecordRepository taskExecuteRecordRepository;

    static String URL = "https://api.etherscan.io/api";
    static String aveChain = "eth";
    static String aveAuth = "bsc";
    static String aveXAuth = "bsc";
    static String aveCookie = "bsc";
    static String aveUrl = "eth";
    static String aveAuthUrl = "eth";
    static String configJsonFilePath = "D:\\test-eth\\config.json";
    public static BigDecimal tokenResultCompare = new BigDecimal("0.0");
    public static BigDecimal tokenPercentCompareStart = new BigDecimal("0.0");
    public static BigDecimal tokenPercentCompareEnd = new BigDecimal("10.0");
    public static BigDecimal tokenPercentCompareMiddle = new BigDecimal("100.0");

    public String getEtherScanApiResult(Map params){

        Boolean replayRequest = true;
        int replayCount = 0;
        while (replayRequest && replayCount <2) {
            replayCount++;
            String requestUrl = URL+mapToUrlParameters(params);
            OkHttpClient client = HttpClientUtil.getOkHttpClient(AppForCoinToolController.isUseProxy,AppForCoinToolController.proxyHost,AppForCoinToolController.proxyPort);
            Request request = new Request.Builder()
                    .url(requestUrl)
                    .get()
                    .build();
            try {
                Response response = client.newCall(request).execute();
                String rspStr = response.body().string();
    //            log.info(" 请求地址：{} \n ethers 返回状态为: {} \n 返回结果为: {} ",requestUrl,response.code(),rspStr);
                JSONObject rsp = JSONObject.parseObject(rspStr);
                replayRequest = false;
                return rsp.getString("result");
            } catch (IOException e) {
                log.error("请求 ether scan 出错，地址： {} 准备重跑：第 {} 次重跑,错误原因：{} ",requestUrl,replayCount ,e);
            }
        }
        return "0.00";
    }

    @Override
    public String getTokenCreator(String token) {
        Map params = new HashMap();
        params.put("module","contract");
        params.put("action","getcontractcreation");
        params.put("contractaddresses",token);
        return getEtherScanApiResult(params);
    }

    @Override
    public String getTokenSupply(String token) {
        Map params = new HashMap();
        params.put("module","stats");
        params.put("action","tokensupply");
        params.put("contractaddress",token);
        return this.getEtherScanApiResult(params);
    }

    @Override
    public String getTokenBalance(String token, String contractAddress) {
        Map params = new HashMap();
        params.put("module","account");
        params.put("action","tokenbalance");
        params.put("contractaddress",token);
        params.put("address",contractAddress); // 持币地址
        return this.getEtherScanApiResult(params);
    }

    @Override
    public JSONObject requestHoneyPot(String token) throws IOException, NoSuchAlgorithmException, KeyManagementException {
        JSONObject honeyPotData = new JSONObject();
        JSONObject response;
        if(AppForDexspyController.httpClient == 2){
            response = AppForDexspyController.requestGetByOk3Honey("https://api.honeypot.is/v2/IsHoneypot?address=" + token);
        } else {
            response = requestGetByRestTemplate("https://api.honeypot.is/v2/IsHoneypot?address=" + token);
        }
        if(response.keySet().contains("honeypotResult") && response.keySet().contains("simulationResult")  && response.keySet().contains("flags")   && response.keySet().contains("token") ){
            honeyPotData.put("isHoneypot",response.getJSONObject("honeypotResult").getString("isHoneypot"));
            honeyPotData.put("buyTax",response.getJSONObject("simulationResult").getString("buyTax"));
            honeyPotData.put("bsellTax",response.getJSONObject("simulationResult").getString("sellTax"));
            honeyPotData.put("totalHolders",response.getJSONObject("token").getString("totalHolders"));
            if (response.keySet().contains("holderAnalysis")){
                honeyPotData.put("holders",response.getJSONObject("holderAnalysis").getString("holders"));
            } else {
                honeyPotData.put("holders","0");
            }

            honeyPotData.put("flags",response.getJSONArray("flags").size()>0?"medium_tax":"");
        }
        return honeyPotData;
    }

    public static JSONObject requestGetByOk3Dex(String token) throws IOException, NoSuchAlgorithmException, KeyManagementException {

        JSONObject response =  AppForDexspyController.requestGetByOk3Honey("https://api.dexscreener.com/latest/dex/tokens/" + token);
        JSONObject dexScreenerData = new JSONObject();
        if(response.keySet().contains("pairs")){
            dexScreenerData.put("volume-h24",response.getJSONArray("pairs").getJSONObject(0).getJSONObject("volume").getString("h24"));
            dexScreenerData.put("priceChange-h24",response.getJSONArray("pairs").getJSONObject(0).getJSONObject("priceChange").getString("h24"));
        }
        return dexScreenerData;
    }

    public  JSONObject requestGetByRestTemplate(String url){
        // Make a GET request and receive the response
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Arrays.asList(org.springframework.http.MediaType.APPLICATION_JSON));
        headers.add("user-agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/54.0.2840.99 Safari/537.36");
        HttpEntity<String> entity = new HttpEntity<>("parameters", headers);
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
        // Process the response
        if (response.getStatusCode().is2xxSuccessful()) {
            String responseBody = response.getBody();
            // Do something with the response body
            return JSONObject.parseObject(responseBody);
        } else {
            // Handle error response
            // ...
        }
        return new JSONObject();
    }

    public static String mapToUrlParameters(Map<String, String> params) {
        StringBuilder result = new StringBuilder();

        for (Map.Entry<String, String> entry : params.entrySet()) {
            try {
                if (result.length() > 0) {
                    result.append("&");
                } else {
                    result.append("?");
                }

                String key = URLEncoder.encode(entry.getKey(), "UTF-8");
                String value = URLEncoder.encode(entry.getValue(), "UTF-8");

                result.append(key).append("=").append(value);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace(); // Handle encoding exception
            }
        }

        return result.append("&apikey=M456M7AMQQTF81TZV9HYQAHYZ57RSQY7V7").toString();
    }

    @Override
    public String createTask(String token,String pairsAddress, Integer execNumber, Integer timeInterval,String userId){
        String taskNo = generateTaskNo();
        redisTemplate.opsForValue().set(taskNo, "-1#"+execNumber);
        CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
            // 异步任务的逻辑
            log.info("异步任务开始执行...");
            mainTask(new JSONObject(),token,pairsAddress,taskNo,execNumber,timeInterval,1);
        });
        TaskRecord row = taskRecordRepository.save(new TaskRecord(taskNo,token,pairsAddress,execNumber,timeInterval,userId));
        log.info("自动创建任务 createTask 成功 ：token {} ，,taskNo {} ，,execNumber {} ，,timeInterval {} ,pairsAddress, {} ,userId, {} ",token,taskNo,execNumber,timeInterval,pairsAddress,userId);        return taskNo;
    }

    @Override
    public void mainTask(JSONObject dexInfo,String token,String pairsAddress,String taskNo,Integer execNumber,Integer timeInterval,Integer taskType){
        try {
            List<TaskExecuteRecord> taskExecuteRecordList = new ArrayList<>();
            String tokenName = "";
            for (int i = 0; i < execNumber ; i++) {
                log.info("任务号：{}，第{}次运行",taskNo,i);
                BigDecimal tokenResult = BigDecimal.ZERO;
                BigDecimal otherResult = BigDecimal.ZERO;
                if(taskType != 3 && taskType != 6 && taskType != 8){
                    JSONObject data = getCoinToolData(token);
                    JSONArray coinArray = data.getJSONObject("data").getJSONArray("items");
                    BigDecimal supply = new BigDecimal(0);
                    BigDecimal tokenBalance = new BigDecimal(0);
                    BigDecimal otherBalance = new BigDecimal(0);
                    for (int j = 0; j < coinArray.size() ; j++) {
                        JSONObject coinData = coinArray.getJSONObject(j);
                        if (j ==0) {
                            if (i == 0) {
                                tokenName = coinData.getString("contract_ticker_symbol");
                                TaskRecord record = taskRecordRepository.findByTaskNo(taskNo);
                                record.setTokenName(tokenName);
                                taskRecordRepository.save(record);
                            }
                            supply = coinData.getBigDecimal("total_supply");
    //                        continue;
                        }
                        String address = coinData.getString("address");
                        if("0x000000000000000000000000000000000000dEaD".equalsIgnoreCase(address) || address.equalsIgnoreCase(pairsAddress)){
                            continue;
                        } else if(token.equalsIgnoreCase(address)){
                                tokenBalance =  tokenBalance.add(new BigDecimal(getTokenBalance(token,address) ) );
                        } else {
                                otherBalance =  otherBalance.add(new BigDecimal(getTokenBalance(token,address) ) );
                        }
                    }
                    tokenResult = tokenBalance.divide(supply,2, RoundingMode.HALF_UP);
                    otherResult = otherBalance.divide(supply,2, RoundingMode.HALF_UP);
                } else {
                    if (dexInfo.keySet().contains("dexTokenSymbol")) {
                        tokenName = dexInfo.getString("dexTokenSymbol");
                    } else {
                        tokenName = "dex为空";
                    }

                }
                JSONObject honeyResult = requestHoneyPot(token);
//                JSONObject aveResult = sendToAve(token);
                JSONObject dexResult = requestGetByOk3Dex(token);
                BigDecimal holder = BigDecimal.ZERO;
                BigDecimal aveHolders = BigDecimal.ZERO;
                BigDecimal totalHoldersPercent = BigDecimal.ZERO;
                BigDecimal totalTotalPercent = BigDecimal.ZERO;
                String avePriceChange = "";
                BigDecimal volume = BigDecimal.ZERO;
                if(!Objects.isNull(honeyResult) ){
                    if ( honeyResult.keySet().contains("holders")) {
                        holder = honeyResult.getBigDecimal("holders");
                    }
                    if ( honeyResult.keySet().contains("totalHolders")) {
                        aveHolders = honeyResult.getBigDecimal("totalHolders");
                    }
                    if (holder.compareTo(BigDecimal.ZERO)!=0 ){
                        totalTotalPercent = aveHolders.subtract(holder).divide(holder, 4, BigDecimal.ROUND_HALF_UP).abs() ;
                    }
                    if (aveHolders.compareTo(BigDecimal.ZERO)!=0 ){
                        totalHoldersPercent = aveHolders.subtract(holder).divide(aveHolders, 4, BigDecimal.ROUND_HALF_UP).abs() ;
                    }

                }
                if(!Objects.isNull(dexResult)){
                    /*if(aveResult.keySet().contains("holders")){
                        aveHolders = aveResult.getString("holders");
                    }*/
                    if(dexResult.keySet().contains("priceChange-h24")){
                        avePriceChange = dexResult.getString("priceChange-h24");
                        volume = dexResult.getBigDecimal("volume-h24");
                    }
                }
                // 增加计算逻辑
                /*
                * totalHolders - holders / totalHolders
                * totalHolders - holders / holders
                *  */

                /*
                *  add 2023年9月13日
                * 从dex 取 priceChange  volumn
                *
                * */


                // 增加指标计算逻辑
                BigDecimal tokenPercent = new BigDecimal("0.0");
                BigDecimal holderPercent = new BigDecimal("0.0");
                BigDecimal preTokenResult = new BigDecimal("0.0");
                int colorFlag = 0;
                if(i > 0){

                    TaskExecuteRecord preTaskExecuteRecord = taskExecuteRecordRepository.findTaskExecuteRecordByTaskNoAndAndExecFlag(taskNo,i-1);
                    if(taskType == 3) {
                        // chrome driver
                        BigDecimal volumeSub = volume.subtract(new BigDecimal(preTaskExecuteRecord.getVolume()));
                        BigDecimal aveHolderSub = aveHolders.subtract(new BigDecimal(preTaskExecuteRecord.getAveHolders()));
                        // chrome driver 大于10判断
                        if (volumeSub.compareTo(new BigDecimal("3000")) >= 0 && aveHolderSub.compareTo(new BigDecimal("6")) >= 0) {
                            colorFlag = 4; // 橙色
                            if (honeyResult.getBoolean("isHoneypot") && honeyResult.getBigDecimal("buyTax").compareTo(new BigDecimal("30")) >= 0 && honeyResult.getBigDecimal("bsellTax").compareTo(new BigDecimal("30")) >= 0) {

                            } else {
                                sendMsgToZhang();
                            }
                        }

                    } else if(taskType == 6) {

                        // chrome driver 5分钟任务
                        BigDecimal volumeSub = volume.subtract(new BigDecimal(preTaskExecuteRecord.getVolume()));
                        BigDecimal aveHolderSub = aveHolders.subtract(new BigDecimal(preTaskExecuteRecord.getAveHolders()));
                        // chrome driver 大于10判断
                        if (volumeSub.compareTo(new BigDecimal("3500")) >= 0 && aveHolderSub.compareTo(new BigDecimal("8")) >= 0) {
                            colorFlag = 4; // 橙色
                            if (honeyResult.getBoolean("isHoneypot") && honeyResult.getBigDecimal("buyTax").compareTo(new BigDecimal("30")) >= 0 && honeyResult.getBigDecimal("bsellTax").compareTo(new BigDecimal("30")) >= 0) {

                            } else {
                                sendMsgToZhang();
                            }
                        }

                    } else if(taskType == 8) {

                        // telegram 创建任务
                        BigDecimal volumeSub = volume.subtract(new BigDecimal(preTaskExecuteRecord.getVolume()));
                        BigDecimal aveHolderSub = aveHolders.subtract(new BigDecimal(preTaskExecuteRecord.getAveHolders()));
                        // chrome driver 大于10判断
                        if (volumeSub.compareTo(new BigDecimal("3500")) >= 0 && aveHolderSub.compareTo(new BigDecimal("8")) >= 0) {
                            colorFlag = 4; // 橙色
                            if (honeyResult.getBoolean("isHoneypot") && honeyResult.getBigDecimal("buyTax").compareTo(new BigDecimal("30")) >= 0 && honeyResult.getBigDecimal("bsellTax").compareTo(new BigDecimal("30")) >= 0) {

                            } else {
                                sendMsgToZhang();
                            }
                        }

                    }
                    else if(taskType == 1) {
                        if (tokenResult.compareTo(tokenResultCompare)>0){
                            preTokenResult =  new BigDecimal(preTaskExecuteRecord.getTokenResult());
                            tokenPercent = tokenResult.subtract(preTokenResult).divide(preTokenResult, 4, BigDecimal.ROUND_HALF_UP).abs() ;
                        }
                        BigDecimal preHolderResult = new BigDecimal(preTaskExecuteRecord.getHolder());
                        holderPercent = holder.subtract(preHolderResult).divide(preHolderResult, 4, BigDecimal.ROUND_HALF_UP).abs() ;

                        BigDecimal volumeSub = volume.subtract(new BigDecimal(preTaskExecuteRecord.getVolume()));
                        // 大于10判断
                        if (volumeSub.compareTo(new BigDecimal("7000")) >= 0) {

                            if(preTokenResult.compareTo(new BigDecimal("0.05")) < 0 && holder.subtract(preHolderResult).compareTo(new BigDecimal("10"))>=0 ){
                                colorFlag = 4; //橙色

                            }
                            else if( (tokenPercentCompareStart.compareTo(tokenPercent) <= 0 && tokenPercentCompareEnd.compareTo(tokenPercent) >= 0)
                                    || (tokenPercentCompareStart.compareTo(holderPercent) <= 0 && tokenPercentCompareEnd.compareTo(holderPercent) >= 0 ) ) {
                                // 绿色
                                colorFlag = 1 ;
                                if (tokenPercentCompareMiddle.compareTo(tokenPercent) <= 0 || tokenPercentCompareMiddle.compareTo(holderPercent) <= 0) {
                                    // 蓝色
                                    colorFlag = 2 ;
                                    if (tokenPercentCompareMiddle.compareTo(tokenPercent) <= 0 && tokenPercentCompareMiddle.compareTo(holderPercent) <= 0) {
                                        // 红色
                                        colorFlag = 3 ;
                                    }
                                }
                            }
                        }
                    }
                    else if (taskType == 2) {
                        // 20230927 新需求 volume 判断
                        BigDecimal preVolume = new BigDecimal(preTaskExecuteRecord.getVolume());
                        if (preVolume.subtract(volume).compareTo(new BigDecimal("5000")) >= 0) {
                            colorFlag =5;
                        }

                    } else {
                        log.error("未找到 任务类型");
                    }

                }
                if (colorFlag == 3 || colorFlag == 2 || colorFlag == 1) {
                    play(3);
                } else if ( colorFlag == 4 || colorFlag == 5){
                    play(2);
                }


                TaskExecuteRecord taskExecuteRecord =taskExecuteRecordRepository.save(
                        new TaskExecuteRecord(taskNo,i,new Date()
                        ,new Date(),tokenResult.toString()
                                ,otherResult.toString()
                                ,holder.setScale(0,RoundingMode.HALF_UP).toString()
                        ,aveHolders.setScale(0,RoundingMode.HALF_UP).toString(),avePriceChange
                        ,token,tokenName,tokenPercent.setScale(4,RoundingMode.HALF_UP).abs().toString()
                        ,holderPercent.setScale(4,RoundingMode.HALF_UP).abs().toString()
                        ,totalHoldersPercent.setScale(4,RoundingMode.HALF_UP).abs().toString()
                        ,totalTotalPercent.setScale(4,RoundingMode.HALF_UP).abs().toString(),colorFlag,new Date()
                        ,volume.setScale(4,RoundingMode.HALF_UP).abs().toString()));
                redisTemplate.opsForValue().set(taskNo, i+"#"+execNumber);
                taskExecuteRecordList.add(taskExecuteRecord);

                Thread.sleep(timeInterval*1000);

            }
            TaskRecord record = taskRecordRepository.findByTaskNo(taskNo);
            record.setSuccessTime(new Date());
            taskRecordRepository.save(record);
            log.info("-----------任务号：{} ， 执行完成, 插入明细条数：{}",taskNo,taskExecuteRecordList.size());
        } catch (Exception e) {
            log.error("任务 {} 出错：{}",taskNo,e);
            e.printStackTrace();
        }
    }

    /**
     * 生成订单流水号
     * @return orderNo
     */
    static String generateTaskNo(){
        String format = DateUtil.format(new Date(),"yyyyMMddHHmmsss");
        String numbers = RandomUtil.randomNumbers(5);
        return format+numbers;
    }

    @Override
    public JSONObject getCoinToolData(String token)  {
        OkHttpClient client = new OkHttpClient().newBuilder()
                .build();
        MediaType mediaType = MediaType.parse("text/plain");
        RequestBody body = RequestBody.create(mediaType, "");
        Request request = new Request.Builder()
                .url("https://cointool.glitch.me/proxy/tokenApi/?type=3&chainid=1&address="+token+"&block_height=&scan_number=12")
                .get()
                .addHeader("authority", "cointool.glitch.me")
                .addHeader("accept", "application/json, text/plain, */*")
                .addHeader("accept-language", "zh-CN,zh;q=0.9,en;q=0.8,en-GB;q=0.7,en-US;q=0.6")
                .addHeader("if-none-match", "W/\"1471-vp3nww87gMBUfaWaxED499mtFWI\"")
                .addHeader("origin", "https://cointool.app")
                .addHeader("referer", "https://cointool.app/")
                .addHeader("sec-ch-ua", "\"Not/A)Brand\";v=\"99\", \"Microsoft Edge\";v=\"115\", \"Chromium\";v=\"115\"")
                .addHeader("sec-ch-ua-mobile", "?0")
                .addHeader("sec-ch-ua-platform", "\"Windows\"")
                .addHeader("sec-fetch-dest", "empty")
                .addHeader("sec-fetch-mode", "cors")
                .addHeader("sec-fetch-site", "cross-site")
                .addHeader("user-agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/115.0.0.0 Safari/537.36 Edg/115.0.1901.188")
                .build();
        Response response = null;
        String rspStr = null;
        try {
            response = client.newCall(request).execute();
            rspStr = response.body().string();
//            log.info("请求 coin tool 返回状态为: {}  ",response.code());
        } catch (IOException e) {
            log.error("代币 {} 请求 coin tool 出错：{}",token,e);
        }

        JSONObject jsonObject = JSONObject.parseObject(rspStr);
        return jsonObject;
    }

    @Override
    public List<TaskExecuteRecord> findAllTask(List<String> taskNoList, Double otherStart, Double otherEnd, Double tokenStart, Double tokenEnd, Double holderStart, Double holderEnd) {
        if(Objects.isNull(otherStart)){
            otherStart = AppForCoinToolController.otherStart;
        }
        if(Objects.isNull(otherEnd)){
            otherEnd = AppForCoinToolController.otherEnd;
        }
        if(Objects.isNull(tokenStart)){
            tokenStart = AppForCoinToolController.tokenStart;
        }
        if(Objects.isNull(tokenEnd)){
            tokenEnd = AppForCoinToolController.tokenEnd;
        }
        if(Objects.isNull(holderStart)){
            holderStart = AppForCoinToolController.holderStart;
        }
        if(Objects.isNull(holderEnd)){
            holderEnd = AppForCoinToolController.holderEnd;
        }
        return taskExecuteRecordRepository.findAllByTaskNoInAndOtherResultBetweenOrTokenResultBetweenOrHolderBetween(taskNoList,otherStart,otherEnd,tokenStart,tokenEnd,holderStart,holderEnd);
    }

    void sendMsgToZhang(){
        try {

            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                // 异步任务的逻辑
                log.info("发送短信：异步任务开始执行...");
                // 模拟耗时操作
                try {
                    for (int i = 0; i < 2; i++) {
                        String code = RandomUtil.randomNumbers(6);
                        boolean status = sendMsg("15511609461",code );
                        log.info(" 发送验证码成功 ：{}, {} ," ,status,code);
                        Thread.sleep(1000*60);

                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                log.info("发送短信：异步任务执行完成");
            });
        } catch (Exception e) {
            log.error("发送您短信失败： ex : {} ",e);
        }

    }

    boolean sendMsg(String phone,String code){
        try {
            OkHttpClient client = HttpClientUtil.getOkHttpClient(false,"127.0.0.1",4780);
            MediaType mediaType = MediaType.parse("application/x-www-form-urlencoded");
            RequestBody body = RequestBody.create(mediaType, "mobile="+phone+"&tpl_id=145500&tpl_value=%23code%23%3D"+code+"&key=6ccca840e7eee4c132dc0dd52ae9a977");
            Request request = new Request.Builder()
                    .url("http://v.juhe.cn/sms/send")
                    .method("POST", body)
                    .addHeader("Content-Type", "application/x-www-form-urlencoded")
                    .addHeader("Cookie", "aliyungf_tc=0bb95f1a5045ad1912a4eda8f5b2f1e2f2b89699d2faa55087b3a1a7cba631be")
                    .build();
            Response response = client.newCall(request).execute();
            String rspStr = response.body().string();
            JSONObject responseJson = JSONObject.parseObject(rspStr);
            if(responseJson.keySet().contains("error_code") && responseJson.getInteger("error_code") == 0) {
                return true;
            } else {
                return false;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 请求BSC链
     * @param address
     */
    @Override
    public JSONObject sendToAve(String address) {
        String url = null;
        Request request ;
        Response response ;
        JSONObject jsonObject = new JSONObject();
        OkHttpClient client = new OkHttpClient.Builder().build();
        int i = 1;
        boolean replayRequest = false;

        replayRequest = true;
        while (replayRequest && i < 5 ) {
            try {
                i++;
                url = aveUrl+address+"-eth";
                request = new Request.Builder()
                        .url(url)
                        .addHeader("authority", aveAuthUrl)
                        .addHeader("accept", "application/json, text/plain, */*")
                        .addHeader("accept-language", "zh-CN,zh;q=0.9,en;q=0.8,en-GB;q=0.7,en-US;q=0.6")
                        .addHeader("authorization", aveAuth)
                        .addHeader("lang", "en")
                        .addHeader("origin", "https://m.ave.ai")
                        .addHeader("referer", "https://m.ave.ai/")
                        .addHeader("sec-ch-ua", "\"Google Chrome\";v=\"111\", \"Not(A:Brand\";v=\"8\", \"Chromium\";v=\"111\"")
                        .addHeader("sec-ch-ua-mobile", "?0")
                        .addHeader("sec-ch-ua-platform", "\"Windows\"")
                        .addHeader("sec-fetch-dest", "empty")
                        .addHeader("sec-fetch-mode", "cors")
                        .addHeader("sec-fetch-site", "cross-site")
                        .addHeader("user-agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/111.0.0.0 Safari/537.36")
                        .addHeader("x-auth", aveXAuth)
                        .addHeader("Cookie", aveCookie)
                        .build();
                // 使用HTTP库发送请求并处理响应
                response = client.newCall(request).execute();
                String responseBody = response.body().string();
                jsonObject = JSONObject.parseObject(responseBody);
                jsonObject = jsonObject.getJSONObject("data").getJSONObject("token");
                replayRequest = false;

            } catch (IOException e) {
                log.error(" 请求 AVE {} 出现 IO 异常，准备重跑： {}  异常信息：{} ",url,address,e);
                log.error("异常{} ",e);
                replayRequest = true;
            } catch (Exception e) {
                log.error(" 请求 AVE {} 出现 异常，准备重跑： {}  异常信息：{} ",url,address,e);
                log.error("异常{} ",e);
                replayRequest = true;
            }
        }

        return jsonObject;

    }

    @Override
    public void initConfig() {
        loadRuntimeConfig();
    }

    @Override
    public JSONObject sendToDex() {
        log.info("開始執行");
        String url = "https://api.dexscreener.com/latest/dex/tokens/";
        List<String> address = FileUtil.readLines("D:\\test-eth\\dex_address.txt", Charset.defaultCharset());
        JSONObject dexResponse = HttpClientUtil.requestGetUrl(url+String.join(",", address) ,true,AppForCoinToolController.proxyHost,AppForCoinToolController.proxyPort);
        JSONArray pairs = dexResponse.getJSONArray("pairs");

        JSONObject priceChange = new JSONObject();
        for (int i = 0; i < pairs.size(); i++) {
            priceChange = pairs.getJSONObject(i).getJSONObject("priceChange");
            String h1 = priceChange.getString("h1");
            String m5  = priceChange.getString("m5");
            String h6 = priceChange.getString("h6");
            String h24 = priceChange.getString("h24");
            String pairsAddress = pairs.getJSONObject(i).getString("pairAddress");
            if (h1.equals(h6) && h24.equals(h1)) {
                log.info("三個值相等，不處理");
            } else {
                if( new BigDecimal(h1).compareTo(BigDecimal.ZERO) > 5 &&  new BigDecimal(m5).compareTo(BigDecimal.ZERO) > 5  ){

                    String taskNo = generateTaskNo();
                    redisTemplate.opsForValue().set(taskNo, "-1#"+10);
                    int finalI = i;
                    CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                        // 异步任务的逻辑
                        log.info("异步任务开始执行...");
                        mainTask(dexResponse,address.get(finalI),pairsAddress,taskNo,10,180,2);
                    });
                    TaskRecord row = taskRecordRepository.save(new TaskRecord(taskNo,address.get(i),pairsAddress,10,180,"sys_spider_01"));
                    log.info(" 自动创建任务 createTask 成功 ：token {} ，,taskNo {} ，,execNumber {} ，,timeInterval {} ,pairsAddress, {} ,userId, {} ",address.get(i),taskNo,10,180,pairsAddress,"sys");
                }
            }
        }
        JSONObject res = new JSONObject();
        res.put("status","ok");
        return res;
    }

    static void loadRuntimeConfig(){
        System.out.println(DateUtil.date()+" ###################### "+aveChain.toUpperCase(Locale.ROOT)+" WEB APP 开始加载配置文件，配置文件地址："+configJsonFilePath);
        JSONObject configInfo = JSONObject.parseObject(FileUtil.readUtf8String(new File(configJsonFilePath)));

        aveChain = configInfo.getString("aveChain");
        aveAuth = configInfo.getString("aveAuth");
        aveUrl = configInfo.getString("aveUrl");
        aveAuthUrl = configInfo.getString("aveAuthUrl");
        aveXAuth = configInfo.getString("aveXAuth");
        aveCookie = configInfo.getString("aveCookie");

        System.out.println("------------------------------------------- AVE Config Init OK --------------------------------------------------");
    }

}
