package com.holeinone.ssafit.controller;

import com.holeinone.ssafit.model.service.PostService;
import com.holeinone.ssafit.model.service.VideoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@Slf4j
@RestController
@RequestMapping("/post")
public class PostController {

    private final PostService postService;


}
