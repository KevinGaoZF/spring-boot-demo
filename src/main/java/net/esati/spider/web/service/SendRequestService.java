package net.esati.spider.web.service;

import com.alibaba.fastjson.JSONObject;
import net.esati.spider.web.domain.TaskExecuteRecord;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Map;

/**
 * @author Gao
 * @version 1.0
 * @description: TODO
 * @date 2023/8/16 13:59
 */
public interface SendRequestService {

    /**
     * 获取token crater
     * @param contractAddresses
     * @return
     */
    String getTokenCreator(String contractAddresses);

    String getTokenSupply(String token);

    String getTokenBalance(String token, String contractAddress);

    JSONObject requestHoneyPot(String token) throws IOException, NoSuchAlgorithmException, KeyManagementException;

    String createTask(String token, String pairsAddress, Integer execNumber, Integer timeInterval, String userId);

    void mainTask(JSONObject dexInfo,String token,String pairsAddress, String taskNo, Integer execNumber, Integer timeInterval, Integer taskType);

    JSONObject getCoinToolData(String token);

    List<TaskExecuteRecord> findAllTask(List<String> taskNoList, Double otherStart, Double otherEnd, Double tokenStart, Double tokenEnd, Double holderStart, Double holderEnd);

    JSONObject sendToAve(String address);

    void initConfig();

    JSONObject sendToDex();
}
