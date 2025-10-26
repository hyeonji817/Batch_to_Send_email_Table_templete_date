package org.example;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class QueryExecutor {
    public static String executeQuery() {
        String htmlContent = "";
        try (Connection conn = DBConnection.getConnection()) {

            String query1 =
                    "select \n" +
                            "sum(PUSHED_CNT) as '발송건수',\n" +
                            "(select\n" +
                            "count(*) as 'tencount'\n" +
                            "from pushlogque where 1=1\n" +
                            "and ID > (select MAX(ID) from tms_push_que_log) - 20000000\n" +
                            "and REQ_UID like 'CC%'\n" +
                            "and REG_DATE between date_sub(curdate(), interval 1 day) + interval 9 hour and date_sub(curdate(), interval 1 day) + interval 21 hour\n" +
                            "group by left(RES_DATE,11)\n" +
                            "order by 'tencount' desc limit 1) as '10분당 최대발송건수',\n" +
                            "sum(FAIL_CNT) as '실패건수',\n" +
                            "round(((sum(PUSHED_CNT)-sum(FAIL_CNT))/sum(TARGET_CNT)*100),1) as '도달율',\n" +
                            "round(((sum(FAIL_CNT))/sum(TARGET_CNT)*100),1) as '실패율',\n" +
                            "round(((sum(OPEN_CNT))/sum(PUSHED_CNT)*100),1) as '오픈율'\n" +
                            "from tms_camp_schd_info where 1=1\n" +
                            "and CHANNEL_TYPE ='PU'\n" +
                            "and REQ_DATE between date_sub(CURDATE(), interval 1 day) + interval 9 hour and DATE_SUB(curdate(), interval 1 day) + interval 21 hour\n" +
                            "order by REQ_DATE";
            String query2 =
                    "select \n" +
                            "date_format(A.REQ_DATE, '%Y/%m/%d') as '날짜', \n" +
                            "A.IF_ID as '캠페인 번호', \n" +
                            "A.REQ_DATE as '발송예약시간', \n" +
                            "sum(A.PUSHED_CNT+B.PUSHED_CNT) as '타겟건수', \n" +
                            "sum(A.FAIL_CNT+B.FAIL_CNT) as '실패건수', \n" +
                            "sum((round(((A.PUSHED_CNT-A.FAIL_CNT)/A.TARGET_CNT*100),1)+round(((B.PUSHED_CNT-B.FAIL_CNT)/B.TARGET_CNT*100),1))/2) as '발송성공율', \n" +
                            "sum((round((A.FAIL_CNT/A.TARGET_CNT*100),1)+round((B.FAIL_CNT/B.TARGET_CNT*100),1))/2) as '발송실패율', \n" +
                            "sum((round((A.OPEN_CNT/A.PUSHED_CNT*100),1) + round((B.OPEN_CNT/B.PUSHED_CNT*100),1))/2) as '오픈율', \n" +
                            "greatest(timestampdiff(minute, A.start_date, A.end_date), timestampdiff(minute, B.start_date,B.end_date))  as '발송소요시간' \n" +
                            "from TableMassSchedule A join TableMassSchedule2 B on A.IF_ID = B.IF_ID \n" +
                            "and A.SERVER_ID < B.SERVER_ID \n" +
                            "where 1=1 \n" +
                            "and A.CHANNEL_TYPE ='PU' \n" +
                            "and A.REQ_DATE between now() - interval 25 hour and now() - interval 11 hour \n" +
                            "group by A.IF_ID, A.SERVER_ID \n" +
                            "having mod(A.SERVER_ID, 2) = 1 \n" +
                            "order by A.REQ_DATE";
            htmlContent = EmailSender.getTableDataAsHtml(conn, query1, query2);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return htmlContent;
    }
}
