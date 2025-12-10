package main;

import org.example.EmailSender;
import org.example.QueryExecutor;

// 자바 코드 모음
public class main {
    public static void main(String[] args) {
        // 데이터베이스 쿼리 실행
        String htmlcontent = QueryExecutor.executeQuery();

        // 결과를 이메일로 전송
        if (!htmlcontent.isEmpty()) {
            EmailSender.sendEmail(htmlcontent);
        } else {
            System.out.println("No data found to send.");
        }
    }
}