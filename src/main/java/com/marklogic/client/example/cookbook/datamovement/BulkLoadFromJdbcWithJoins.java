/*
 * Copyright 2012-2017 MarkLogic Corporation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.marklogic.client.example.cookbook.datamovement;

import com.marklogic.client.datamovement.DataMovementManager;
import com.marklogic.client.datamovement.JobTicket;
import com.marklogic.client.datamovement.JobReport;
import com.marklogic.client.datamovement.WriteBatcher;
import com.marklogic.client.example.cookbook.Util.ExampleProperties;
import com.marklogic.client.example.cookbook.Util;

import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowCallbackHandler;

import com.fasterxml.jackson.core.JsonParser.Feature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Calendar;
import javax.sql.DataSource;
import java.text.SimpleDateFormat;

public class BulkLoadFromJdbcWithJoins {
  private static Logger logger = LoggerFactory.getLogger(BulkLoadFromJdbcWithJoins.class);
  public static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

  private static int threadCount = 10;
  private static int batchSize   = 1000;

  public static final DataMovementManager moveMgr =
    DatabaseClientSingleton.get().newDataMovementManager();

  public static void main(String[] args) throws IOException, SQLException {
    System.setProperty("org.apache.commons.logging.simplelog.log.org.apache.http.wire", "debug");
    new BulkLoadFromJdbcWithJoins().run();
  }

  public Employee parseEmployee(ResultSet row) throws SQLException {
    try {
      ObjectMapper mapper = new ObjectMapper()
        .configure(Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
      return mapper.readValue(row.getString(1), Employee.class);
    } catch (Exception exception) {
      exception.printStackTrace();
      throw new RuntimeException(exception);
    }
  }

  public void run() throws IOException, SQLException {
    JdbcTemplate jdbcTemplate = new JdbcTemplate(getDataSource());
    WriteBatcher wb = moveMgr.newWriteBatcher()
      .withBatchSize(batchSize)
      .withThreadCount(threadCount)
      .onBatchSuccess(batch -> System.out.println("Written: " + batch.getJobWritesSoFar()))
      .onBatchFailure((batch,exception) -> exception.printStackTrace());
    JobTicket ticket = moveMgr.startJob(wb);
    // the following is required because GROUP_CONCAT calls in following SQL can generate long strings
    jdbcTemplate.execute("SET GLOBAL group_concat_max_len = 1000000");
    jdbcTemplate.query(
      /******** Generate JSON Employee record with nested salaries and titles arrays ********/
      "SELECT CONCAT('{employeeId:', e.emp_no, ',', " +
      "              ' firstName:\"', e.first_name, '\",', " +
      "              ' lastName:\"', e.last_name, '\",', " +
      "              ' gender:\"', IF(e.gender='M','MALE',IF(e.gender='F','FEMALE','UNKNOWN')), '\",', " +
      "              ' birthDate:\"', DATE_FORMAT(e.birth_date,'%Y-%m-%d'), '\",'," +
      "              ' hireDate:\"', DATE_FORMAT(e.hire_date,'%Y-%m-%d'), '\",', " +
      "              ' salaries:[', " +
      "                GROUP_CONCAT(CONCAT('{salary:',salary,',', " +
      "                  ' fromDate:\"', DATE_FORMAT(s.from_date,'%Y-%m-%d'), '\"', " +
      "                  IF(s.to_date='9999-01-01','',CONCAT(', toDate:\"', DATE_FORMAT(s.to_date,'%Y-%m-%d'), '\"')), " +
      "                '}') SEPARATOR ','), " +
      "              '], titles:[', titles, ']}') " +
      "FROM salaries s, " +
      "  (SELECT employees.*, GROUP_CONCAT(CONCAT('{title:\"',title,'\",', " +
      "    ' fromDate:\"', DATE_FORMAT(t.from_date,'%Y-%m-%d'), '\"', " +
      "    IF(t.to_date='9999-01-01','',CONCAT(', toDate:\"', DATE_FORMAT(t.to_date,'%Y-%m-%d'), '\"')), " +
      "  '}') SEPARATOR ',') titles " +
      "   FROM employees, titles t " +
      "   WHERE employees.emp_no=t.emp_no " +
      "   GROUP BY employees.emp_no) e " +
      "WHERE e.emp_no=s.emp_no " +
      "GROUP BY e.emp_no " +
      "LIMIT 100",
      (RowCallbackHandler) row -> {
        Employee employee = parseEmployee(row);
        wb.addAs(employee.getEmployeeId() + ".json", employee);
      }
    );
    wb.flushAndWait();
    moveMgr.stopJob(wb);
    JobReport report = moveMgr.getJobReport(ticket);
    if ( report.getFailureBatchesCount() > 0 ) {
      throw new IllegalStateException("Encountered " +
        report.getFailureBatchesCount() + " failed batches");
    }
  }

  private DataSource getDataSource() throws IOException {
    ExampleProperties properties = Util.loadProperties();
    return new DriverManagerDataSource(properties.jdbcUrl);
  }
}
