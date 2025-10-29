package com.jzo2o.foundations.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.jzo2o.common.expcetions.BadRequestException;
import com.jzo2o.foundations.constants.RedisConstants;
import com.jzo2o.foundations.enums.FoundationStatusEnum;
import com.jzo2o.foundations.mapper.ServeItemMapper;
import com.jzo2o.foundations.mapper.ServeMapper;
import com.jzo2o.foundations.model.domain.Region;
import com.jzo2o.foundations.model.domain.Serve;
import com.jzo2o.foundations.model.domain.ServeItem;
import com.jzo2o.foundations.model.dto.response.ServeAggregationSimpleResDTO;
import com.jzo2o.foundations.model.dto.response.ServeAggregationTypeSimpleResDTO;
import com.jzo2o.foundations.model.dto.response.ServeCategoryResDTO;
import com.jzo2o.foundations.model.dto.response.ServeSimpleResDTO;
import com.jzo2o.foundations.service.HomeService;
import com.jzo2o.foundations.service.IRegionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.redis.cache.RedisCache;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class HomeServiceImpl implements HomeService {
    @Resource
    private IRegionService regionService;
    @Resource
    private ServeMapper serveMapper;

    /**
     * 根据区域ID查询服务图标分类信息（带缓存）
     *
     * @param regionId 区域ID
     * @return 服务分类响应DTO列表，最多返回2个分类，每个分类最多包含4个服务
     */
    @Override
    @Caching(cacheable={
            //缓存击穿时缓存空值
            @Cacheable(value = RedisConstants.CacheName.SERVE_ICON,key = "#regionId",unless = "#result.size()!=0"
            ,cacheManager = RedisConstants.CacheManager.THIRTY_MINUTES),
            //正常查询时缓存数据，并永久缓存
            @Cacheable(value = RedisConstants.CacheName.SERVE_ICON,key ="#regionId",unless = "#result.size()==0"
            ,cacheManager = RedisConstants.CacheManager.FOREVER)
    })
    public List<ServeCategoryResDTO> queryServeIconCategoryByRegionIdCache(Long regionId) {
        // 验证区域是否存在且处于启用状态
        Region region = regionService.getById(regionId);
        if(ObjectUtil.isEmpty( region)||ObjectUtil.equal(FoundationStatusEnum.DISABLE.getStatus(),region.getActiveStatus())){
            return Collections.emptyList();
        }

        // 查询该区域下的所有服务图标分类
        List<ServeCategoryResDTO> list = serveMapper.findServeIconCategoryByRegionId(regionId);
        if (ObjectUtil.isEmpty(list)){
            return Collections.emptyList();
        }

        // 截取前2个分类
        int endIndex= Math.min(list.size(), 2);
        List<ServeCategoryResDTO> serveCategoryResDTOS=new ArrayList<>(list.subList(0, endIndex));

        // 对每个分类中的服务列表进行截取，每个分类最多保留4个服务
        serveCategoryResDTOS.forEach(v->{
            List<ServeSimpleResDTO> serveResDTOList = v.getServeResDTOList();
            int endIndex1= Math.min(serveResDTOList.size(), 4);
            List<ServeSimpleResDTO> serveSimpleResDTOS=new ArrayList<>(serveResDTOList.subList(0, endIndex1));
            v.setServeResDTOList(serveSimpleResDTOS);
        });

        return serveCategoryResDTOS;
    }

        /**
     * 根据区域ID查询服务类型列表（带缓存）
     *
     * @param regionId 区域ID
     * @return 服务聚合类型简单信息列表
     */
    @Override
    @Caching(cacheable={
            //缓存击穿时缓存空值
            @Cacheable(value = RedisConstants.CacheName.SERVE_TYPE,key = "#regionId",unless = "#result.size()!=0"
                    ,cacheManager = RedisConstants.CacheManager.THIRTY_MINUTES),
            //正常查询时缓存数据，并永久缓存
            @Cacheable(value = RedisConstants.CacheName.SERVE_TYPE,key ="#regionId",unless = "#result.size()==0"
                    ,cacheManager = RedisConstants.CacheManager.FOREVER)
    })
    public List<ServeAggregationTypeSimpleResDTO> queryServeTypeListByRegionIdCache(Long regionId) {
        // 校验区域信息是否有效
        Region region = regionService.getById(regionId);
        if(ObjectUtil.isEmpty( region)||ObjectUtil.equal(FoundationStatusEnum.DISABLE.getStatus(),region.getActiveStatus())){
            return Collections.emptyList();
        }
        // 查询区域下的服务聚合类型列表
        List<ServeAggregationTypeSimpleResDTO> dtoList = serveMapper.findServeAggregationTypeSimpleByRegionId(regionId)
                .stream().distinct().collect(Collectors.toList());
        if (ObjectUtil.isNotEmpty(dtoList)){
            return dtoList;
        }
        return List.of();
    }

    @Override
    @Caching(cacheable={
            //缓存击穿时缓存空值
            @Cacheable(value = RedisConstants.CacheName.HOT_SERVE,key = "#regionId",unless = "#result.size()!=0"
                    ,cacheManager = RedisConstants.CacheManager.THIRTY_MINUTES),
            //正常查询时缓存数据，并永久缓存
            @Cacheable(value = RedisConstants.CacheName.HOT_SERVE,key ="#regionId",unless = "#result.size()==0"
                    ,cacheManager = RedisConstants.CacheManager.FOREVER)
    })
    public List<ServeAggregationSimpleResDTO> queryHotServeListByRegionIdCache(Long regionId) {
        // 校验区域信息是否有效
        Region region = regionService.getById(regionId);
        if(ObjectUtil.isEmpty( region)||ObjectUtil.equal(FoundationStatusEnum.DISABLE.getStatus(),region.getActiveStatus())){
            return Collections.emptyList();
        }
        List<ServeAggregationSimpleResDTO> dtoList = serveMapper.findServeAggregationSimpleByRegionId(regionId);
        if (ObjectUtil.isNotEmpty(dtoList)){
            return dtoList;
        }
        return List.of();
    }
/*
    @Resource
    private ServeItemMapper serveItemMapper;
    */
/**
     * 根据服务ID查询服务详情并缓存结果
     * <p>
     * 该方法使用两级缓存策略：<br>
     * 1. 当查询结果为空时，缓存空值30分钟，防止缓存击穿<br>
     * 2. 当查询结果存在时，永久缓存数据，提高访问效率<br>
     * </p>
     *
     * @param id 服务ID，不能为空
     * @return 服务聚合信息简单响应DTO，包含服务基本信息和服务项详细信息
     * @throws BadRequestException 当ID为空、服务不存在、服务已下架、服务项不存在或服务项未启用时抛出异常
     *//*

    @Override
    public ServeAggregationSimpleResDTO queryServeDetailByIdCache(Long id) {
        // 参数校验：检查服务ID是否为空
        if(ObjectUtil.isEmpty(id)){
            throw new BadRequestException("服务ID不能为空");
        }

        // 查询服务基础信息
        Serve serve = serveMapper.selectById(id);
        if(ObjectUtil.isNull(serve)){
            throw  new BadRequestException("服务不存在");
        }

        // 检查服务销售状态是否为启用状态
        Integer saleStatus = serve.getSaleStatus();
        if(!ObjectUtil.equal(FoundationStatusEnum.ENABLE.getStatus(),saleStatus)){
            throw new BadRequestException("服务已下架");
        }

        // 查询服务项信息
        ServeItem serveItem = serveItemMapper.selectById(id);
        if(ObjectUtil.isNull(serveItem)){
            throw new BadRequestException("服务项不存在");
        }

        // 检查服务项激活状态是否为启用状态
        Integer activeStatus = serveItem.getActiveStatus();
        if(!ObjectUtil.equal(FoundationStatusEnum.ENABLE.getStatus(),activeStatus)){
            throw new BadRequestException("服务项未启用");
        }

        // 构建并返回服务聚合信息响应对象
           return ServeAggregationSimpleResDTO.builder()
                .id(serve.getId())
                .serveItemId(serveItem.getId())
                .serveItemName(serveItem.getName())
                .serveItemImg(serveItem.getImg())
                .unit(serveItem.getUnit())
                .price(serve.getPrice())
                .detailImg(serveItem.getDetailImg())
                .cityCode(serve.getCityCode())
                .build();
    }
*/



}