package net.esati.spider.web.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author Gao
 * @version 1.0
 * @description: TODO
 * @date 2023/8/10 18:14
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TaskDetailVO {

    private TaskRecord taskRecord;
    private List<TaskExecuteRecord> executeRecordList;


}
