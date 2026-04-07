package kr.co.hectofinancial.mps.api.v1.customer.repository;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@Slf4j
public class CustomerHistoryRepository {


    @Autowired
    private JdbcTemplate jdbcTemplate;


    public void insertMpsCustHistory(String custNo) {
        // A 테이블의 컬럼 이름을 가져오는 쿼리
        String getColumnsQuery = "SELECT COLUMN_NAME FROM SYS.ALL_TAB_COLUMNS WHERE TABLE_NAME = 'TB_MPS_CUST' AND OWNER = 'MPS'";

        // A 테이블의 컬럼 리스트를 가져옴
        List<String> columns = jdbcTemplate.queryForList(getColumnsQuery, String.class);

        // 컬럼 리스트를 콤마로 구분된 문자열로 변환
        String columnList = String.join(", ", columns);

        // 동적 SQL 생성
        String insertQuery = "INSERT INTO MPS.TH_MPS_CUST (" + columnList + ", HIST_DATE) " +
                "SELECT " + columnList + " , SYSDATE " +
                "FROM MPS.TB_MPS_CUST A WHERE A.MPS_CUST_NO = ? ";

        // SQL 실행
        jdbcTemplate.update(insertQuery, custNo);

        log.info("회원 이력 저장 완료 => 회원번호:[{}]", custNo);
    }
}


