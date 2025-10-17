package com.jzo2o.foundations.mapper;

import com.jzo2o.foundations.model.dto.response.ServeResDTO;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.util.List;

@Slf4j
@SpringBootTest
public class ServeMapperTest {
    @Resource
    private ServeMapper serveMapper;
    @Test
    void test_queryServeListByRegionId(){
        List<ServeResDTO> serveResDTOS = serveMapper.queryServeListByRegionId(1677152267410149378L);
        log.info("serveResDTOS:{}",serveResDTOS);
    }
}
