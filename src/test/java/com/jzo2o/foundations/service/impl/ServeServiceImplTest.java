package com.jzo2o.foundations.service.impl;

import com.jzo2o.common.model.PageResult;
import com.jzo2o.foundations.model.dto.request.ServePageQueryReqDTO;
import com.jzo2o.foundations.model.dto.response.ServeResDTO;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

import static org.junit.jupiter.api.Assertions.*;
@Slf4j
@SpringBootTest
class ServeServiceImplTest {
    @Resource
    private ServeServiceImpl serveService;
    @Test
    public void test(){
        ServePageQueryReqDTO servePageQueryReqDTO = new ServePageQueryReqDTO();
        servePageQueryReqDTO.setRegionId(1692472339767234562L);
        servePageQueryReqDTO.setPageNo(1L);
        servePageQueryReqDTO.setPageSize(2L);
        PageResult<ServeResDTO> page = serveService.page(servePageQueryReqDTO);
        log.info("page:{}",page);
    }
}