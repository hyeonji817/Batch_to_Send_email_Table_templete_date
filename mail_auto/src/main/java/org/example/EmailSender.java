package org.example;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

public class EmailSender {

    private static final String CONFIG_FILE_PATH = "/email-config.xml";

    public static void sendEmail(String htmlContent) {
        final String fromEmail = "no-reply@lotteon.com"; // 보내는 사람 이메일 주소
        String toEmails = getRecipientEmails(); // XML 파일에서 여러 수신자 이메일 주소를 가져옴

        // SMTP 서버 설정
        Properties prop = new Properties();
        prop.put("mail.smtp.host", "127.0.0.1");
        prop.put("mail.smtp.port", "25"); // 로컬 SMTP 서버 사용 시 기본 포트는 25입니다.
        // SMTP 서버에서 배치 돌릴 목적으로 사용

        // 세션 생성
        Session session = Session.getInstance(prop, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return null; // 로컬 SMTP 서버는 인증이 필요 없을 수 있습니다.
            }
        });

        try {
            // 이메일 메시지 작성
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(fromEmail));
            message.setRecipients(
                    Message.RecipientType.TO,
                    InternetAddress.parse(toEmails) // 여러 수신자를 쉼표로 구분된 문자열로 파싱
            );
//            message.setSubject("TMS 앱푸시 발송량 현황 (일단위)");
            message.setSubject("TMS 앱푸시 발송량 현황");

            // 이메일 내용을 HTML로 설정
            MimeBodyPart mimeBodyPart = new MimeBodyPart();
            mimeBodyPart.setContent(htmlContent, "text/html; charset=utf-8");

            Multipart multipart = new MimeMultipart();
            multipart.addBodyPart(mimeBodyPart);

            message.setContent(multipart);

            // 이메일 발송
            Transport.send(message);
            System.out.println("Email sent successfully for TMS");

        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }

    private static String getRecipientEmails() {
        try (InputStream inputStream = EmailSender.class.getResourceAsStream(CONFIG_FILE_PATH)) {
            if (inputStream == null) {
                throw new FileNotFoundException("Config file not found in JAR: " + CONFIG_FILE_PATH);
            }

            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(inputStream);
            document.getDocumentElement().normalize();

            // recipient 태그를 모두 가져와 쉼표로 구분된 문자열로 반환
            Element root = document.getDocumentElement();
            NodeList recipientNodes = root.getElementsByTagName("recipient");

            StringBuilder recipients = new StringBuilder();
            for (int i = 0; i < recipientNodes.getLength(); i++) {
                recipients.append(recipientNodes.item(i).getTextContent());
                if (i < recipientNodes.getLength() - 1) {
                    recipients.append(",");
                }
            }
            return recipients.toString();

        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    public static String getTableDataAsHtml(Connection connection, String query1, String query2) {
        StringBuilder htmlTable = new StringBuilder();
        htmlTable.append("<h2>앱 푸시 발송량 (요약)</h2>");
        htmlTable.append("<table border='1' style='border-collapse: collapse; table-layout: auto; width: auto; text-align: center; font-family: Arial, sans-serif; font-size: 14px;'>");
        try (Statement stmt = connection.createStatement(); ResultSet resultSet = stmt.executeQuery(query1)) {
            int columnCount = resultSet.getMetaData().getColumnCount();

            // Table header
            htmlTable.append("<thead><tr style='background-color: #f2f2f2;'>");
            for (int i = 1; i <= columnCount; i++) {
                htmlTable.append("<th style='padding: 2px 8px; border: 1px solid #ddd; text-align: left;'>")
                        .append(resultSet.getMetaData().getColumnName(i))
                        .append("</th>");
            }
            htmlTable.append("</tr></thead>");

            // Table body
            htmlTable.append("<tbody>");
            while (resultSet.next()) {
                htmlTable.append("<tr>");
                for (int i = 1; i <= columnCount; i++) {
                    htmlTable.append("<td style='padding: 2px 8px; border: 1px solid #ddd; text-align: left;'>")
                            .append(resultSet.getString(i))
                            .append("</td>");
                }
                htmlTable.append("</tr>");
            }
            htmlTable.append("</tbody>");
            htmlTable.append("</table>");

        } catch (SQLException e) {
            e.printStackTrace();
        }

        htmlTable.append("<h2>앱 푸시 발송량 (상세)</h2>");
        htmlTable.append("<table border='1' style='border-collapse: collapse; table-layout: auto; width: auto; text-align: center; font-family: Arial, sans-serif; font-size: 14px;'>");
        try (Statement stmt = connection.createStatement(); ResultSet resultSet = stmt.executeQuery(query2)) {
            int columnCount = resultSet.getMetaData().getColumnCount();

            // Table header
            htmlTable.append("<thead><tr style='background-color: #f2f2f2;'>");
            for (int i = 1; i <= columnCount; i++) {
                htmlTable.append("<th style='padding: 2px 8px; border: 1px solid #ddd; text-align: left;'>")
                        .append(resultSet.getMetaData().getColumnName(i))
                        .append("</th>");
            }
            htmlTable.append("</tr></thead>");

            // Table body
            htmlTable.append("<tbody>");
            while (resultSet.next()) {
                htmlTable.append("<tr>");
                for (int i = 1; i <= columnCount; i++) {
                    htmlTable.append("<td style='padding: 2px 8px; border: 1px solid #ddd; text-align: left;'>")
                            .append(resultSet.getString(i))
                            .append("</td>");
                }
                htmlTable.append("</tr>");
            }
            htmlTable.append("</tbody>");
            htmlTable.append("</table>");

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return htmlTable.toString();
    }
}