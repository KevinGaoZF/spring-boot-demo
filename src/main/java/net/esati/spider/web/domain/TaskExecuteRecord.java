package net.esati.spider.web.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @author Gao
 * @version 1.0
 * @description: TODO
 * @date 2023/8/10 15:55
 */
@Data
@Table("tbl_task_exec_record")
public class TaskExecuteRecord {

    @Id
    private Integer recordId;
    private String taskNo;
    private Integer execFlag;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date successTime;
    private String otherResult;
    private String tokenResult;
    private String holder;
    private String aveHolders;
    private String avePriceChange;
    private String aveVolume;
    private String tokenPercent;
    private String holderPercent;
    private String totalTotalPercent;
    private String totalHolderPercent;
    private Integer colorFlag;
    private String token;
    private String volume;
    private String tokenName;
    private String extValue;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date showTime;


    public TaskExecuteRecord(String taskNo, Integer execFlag, Date createTime
            , Date successTime, String tokenResult, String otherResult, String holder, String aveHolders, String avePriceChange
            , String token, String tokenName, String tokenPercent, String holderPercent, String totalHolderPercent, String totalTotalPercent, int colorFlag, Date showTime,  String volume) {
        this.taskNo = taskNo;
        this.execFlag = execFlag;
        this.createTime = createTime;
        this.successTime = successTime;
        this.otherResult = otherResult;
        this.tokenResult = tokenResult;
        this.holder = holder;
        this.aveHolders = aveHolders;
        this.avePriceChange = avePriceChange;
        this.token = token;
        this.tokenName = tokenName;
        this.totalTotalPercent = totalTotalPercent;
        this.totalHolderPercent = totalHolderPercent;
        this.tokenPercent = tokenPercent;
        this.holderPercent = holderPercent;
        this.colorFlag = colorFlag;
        this.showTime = showTime;
        this.volume = volume;
    }
}
