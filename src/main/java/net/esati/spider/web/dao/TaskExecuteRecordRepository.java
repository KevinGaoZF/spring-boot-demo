package net.esati.spider.web.dao;

import net.esati.spider.web.domain.TaskExecuteRecord;
import net.esati.spider.web.domain.TaskRecord;
import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author Gao
 * @version 1.0
 * @description: TODO
 * @date 2023/8/10 16:14
 */
@Component
public interface TaskExecuteRecordRepository extends PagingAndSortingRepository<TaskExecuteRecord, Integer> {

    List<TaskExecuteRecord> findAllByTaskNoIn(List<String> taskNoList);

    List<TaskExecuteRecord> findAllByTaskNoInAndOtherResultBetweenOrTokenResultBetweenOrHolderBetween(List<String> taskNoList
            ,Double otherStart,Double otherEnd,Double tokenStart,Double tokenEnd,Double holderStart,Double holderEnd);

    @Modifying
    @Query("DELETE FROM tbl_task_exec_record WHERE task_no =:taskNo ")
    void deleteAllByTaskNo(String taskNo);

    TaskExecuteRecord findTaskExecuteRecordByTaskNoAndAndExecFlag(String taskNo,int execFlag);
}
