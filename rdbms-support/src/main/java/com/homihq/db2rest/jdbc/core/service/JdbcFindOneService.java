package com.homihq.db2rest.jdbc.core.service;

import com.homihq.db2rest.core.exception.GenericDataAccessException;
import com.homihq.db2rest.jdbc.JdbcManager;
import com.homihq.db2rest.jdbc.core.DbOperationService;
import com.homihq.db2rest.jdbc.dto.ReadContext;
import com.homihq.db2rest.jdbc.processor.ReadProcessor;
import com.homihq.db2rest.jdbc.sql.SqlCreatorTemplate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;

import java.util.List;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
public class JdbcFindOneService implements FindOneService {

    private final JdbcManager jdbcManager;
    private final SqlCreatorTemplate sqlCreatorTemplate;
    private final List<ReadProcessor> processorList;
    private final DbOperationService dbOperationService;

    @Override
    public Map<String, Object> findOne(ReadContext readContext) {

        for (ReadProcessor processor : processorList) {
            processor.process(readContext);
        }

        String sql = sqlCreatorTemplate.findOne(readContext);
        Map<String, Object> bindValues = readContext.getParamMap();

        log.debug("SQL - {}", sql);
        log.debug("Params - {}", bindValues);

        try {
            return dbOperationService.findOne(
                    jdbcManager.getNamedParameterJdbcTemplate(readContext.getDbId()),
                    sql, bindValues);
        } catch (DataAccessException e) {
            log.error("Error in read op : ", e);
            throw new GenericDataAccessException(e.getMostSpecificCause().getMessage());
        }
    }



}
