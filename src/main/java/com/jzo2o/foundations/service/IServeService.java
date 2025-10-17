package com.jzo2o.foundations.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jzo2o.common.model.PageResult;
import com.jzo2o.foundations.model.domain.Serve;
import com.jzo2o.foundations.model.dto.request.ServePageQueryReqDTO;
import com.jzo2o.foundations.model.dto.request.ServeUpsertReqDTO;
import com.jzo2o.foundations.model.dto.response.ServeResDTO;

import java.math.BigDecimal;
import java.util.List;

public interface IServeService extends IService<Serve> {

    /**
     * 分页查询服务列表
     * @param servePageQueryReqDTO 查询条件
     * @return 分页结果
     */
    PageResult<ServeResDTO> page(ServePageQueryReqDTO servePageQueryReqDTO);
    /**
     * 批量新增
     *
     * @param serveUpsertReqDTOList 批量新增数据
     */
    void batchAdd(List<ServeUpsertReqDTO> serveUpsertReqDTOList);
    /**
     * 服务价格修改
     *
     * @param id    服务id
     * @param price 价格
     * @return 服务
     */
    Serve update(Long id, BigDecimal price);
    /**
     * 上架
     *
     * @param id         服务id
     */
    Serve onSale(Long id);

    /**
     * 根据ID删除记录
     *
     * @param id 要删除记录的唯一标识符
     */
    void deleteById(Long id);

    /**
     * 下架商品
     *
     * @param id 商品ID
     */
    void offSale(Long id);

    /**
     * 处理热点事件的方法
     *
     * @param id 热点事件的唯一标识符
     */
    void onHot(Long id);

    /**
     * 关闭热点功能
     *
     * @param id 热点ID，用于标识需要关闭的热点对象
     */
    void offHot(Long id);
}