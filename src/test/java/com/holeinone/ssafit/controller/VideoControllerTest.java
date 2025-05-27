package com.holeinone.ssafit.controller;

import com.google.api.services.youtube.model.Video;
import com.holeinone.ssafit.model.dao.VideoDao;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

@Slf4j
@SpringBootTest
class VideoControllerTest {

    @Autowired
    VideoDao videoDao;

    @Test
    public void getVideo() {
        HttpSession session = mock(HttpSession.class);

        Object videoRoutineData = session.getAttribute("videoRoutineData");
        log.info("videoRoutineData: {}", videoRoutineData);
    }
}