package net.esati.spider.web.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.util.Date;

/**
 * @author Gao
 * @version 1.0
 * @description: TODO
 * @date 2023/8/9 23:32
 */
@Data
@Table("tbl_task_record")
@NoArgsConstructor
@AllArgsConstructor
public class TaskRecord {

    @Id
    private Integer taskId;
    private String userId;
    private String taskNo;
    private String token;
    private String pair;
    private String tokenName;
    private Integer execNum;
    private Integer execTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date successTime;

    public TaskRecord(String taskNo, String token, String pair, Integer execNum, Integer execTime,String userId) {
        this.taskNo = taskNo;
        this.token = token;
        this.pair = pair;
        this.execNum = execNum;
        this.execTime = execTime;
        this.createTime = new Date();
        this.userId = userId;
    }
}
