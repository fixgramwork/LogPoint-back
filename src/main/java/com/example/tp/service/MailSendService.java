package com.example.tp.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Random;

@Service
public class MailSendService {

    @Value("${spring.mail.username}")
    private String username;

    @Value("${spring.mail.password}")
    private String password;

    @Autowired
    private JavaMailSender mailSender;
    @Autowired
    private  RedisUtil redisUtil;
    private String authNumber;

    public boolean CheckAuthNum(String email,String authNum){
        if(redisUtil.getData(authNum)==null){
            return false;
        }
        else if(redisUtil.getData(authNum).equals(email)){
            return true;
        }
        else{
            return false;
        }
    }

    public void makeRandomNumber() {
        Random rand = new Random();
        StringBuffer randomNumber = new StringBuffer("");
        for(int i = 0; i < 6; i++) {
            randomNumber.append(rand.nextInt(10));
        }
        authNumber = String.format("%06d", Integer.parseInt(randomNumber.toString()));
    }

    //mail을 어디서 보내는지, 어디로 보내는지 , 인증 번호를 html 형식으로 어떻게 보내는지 작성합니다.
    @Async
    public void joinEmail(String email) {
        makeRandomNumber();
        String setFrom = username; // email-config에 설정한 자신의 이메일 주소를 입력
        String toMail = email;
        String title = "저는 공덕현 입니다."; // 이메일 제목
        String content =
               "<h1>당신은 덕바오의 매력에 빠졌습니다.</h1>\n" +
                       "    <br><br>\n" +
                       "    <p>인증번호는 " + authNumber + "</p>" +
                       "    <br>\n" +
                       "    <h2>인증번호를 제대로 입력해라</h2>\n" +
                       "    <img src='https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcRSbstcFDWfUlsExu2k61K4-SLFp0vgqy9RVQ&s'/>";
        mailSend(setFrom, toMail, title, content);
    }

    //이메일을 전송합니다.
    public void mailSend(String setFrom, String toMail, String title, String content) {
        MimeMessage message = mailSender.createMimeMessage();//JavaMailSender 객체를 사용하여 MimeMessage 객체를 생성
        try {
            MimeMessageHelper helper = new MimeMessageHelper(message,true,"utf-8");//이메일 메시지와 관련된 설정을 수행합니다.
            // true를 전달하여 multipart 형식의 메시지를 지원하고, "utf-8"을 전달하여 문자 인코딩을 설정
            helper.setFrom(setFrom);//이메일의 발신자 주소 설정
            helper.setTo(toMail);//이메일의 수신자 주소 설정
            helper.setSubject(title);//이메일의 제목을 설정
            helper.setText(content,true);//이메일의 내용 설정 두 번째 매개 변수에 true를 설정하여 html 설정으로한다.
            mailSender.send(message);
        } catch (MessagingException e) {//이메일 서버에 연결할 수 없거나, 잘못된 이메일 주소를 사용하거나, 인증 오류가 발생하는 등 오류
            // 이러한 경우 MessagingException이 발생
            e.printStackTrace();//e.printStackTrace()는 예외를 기본 오류 스트림에 출력하는 메서드
        }
        redisUtil.setDataExpire(authNumber,toMail,60*5L);

    }

}