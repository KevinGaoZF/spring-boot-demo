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
 * @date 2023/8/9 23:31
 */
@Component
public interface TaskRecordRepository extends PagingAndSortingRepository<TaskRecord, Integer> {

    List<TaskRecord> findAllByUserIdInOrderByCreateTimeDesc(List<String> userIds);

    List<TaskRecord> findAllByToken(String token);

    TaskRecord findByTaskNo(String taskNo);

    @Modifying
    @Query("DELETE FROM tbl_task_record WHERE task_no =:taskNo ")
    void deleteAllByTaskNo(String taskNo);
}
