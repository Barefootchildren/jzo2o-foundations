package com.jzo2o.foundations.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjectUtil;
import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.SortOptions;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.MultiMatchQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import com.jzo2o.common.expcetions.ElasticSearchException;
import com.jzo2o.common.utils.LambdaUtils;
import com.jzo2o.common.utils.ObjectUtils;
import com.jzo2o.es.core.ElasticSearchTemplate;
import com.jzo2o.es.utils.SearchResponseUtils;
import com.jzo2o.foundations.constants.IndexConstants;
import com.jzo2o.foundations.model.domain.ServeAggregation;
import com.jzo2o.foundations.model.dto.response.ServeSimpleResDTO;
import com.jzo2o.foundations.service.ServeAggregationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 服务相关
 *
 * @author itcast
 * @create 2025/10/30 21:58
 **/
@Slf4j
@Service
public class ServeAggregationServiceImpl implements ServeAggregationService {

    @Resource
    private ElasticSearchTemplate elasticSearchTemplate;



    /**
     * 根据城市编码、服务类型ID和关键字查询服务列表
     *
     * @param cityCode 城市编码，用于筛选指定城市的服务数据
     * @param serveTypeId 服务类型ID，用于筛选指定类型的服务数据
     * @param keyword 搜索关键字，用于模糊匹配服务项名称和服务类型名称
     * @return 服务简要信息列表，如果查询失败则返回空列表
     */
    @Override
    public List<ServeSimpleResDTO> findServeList(String cityCode, Long serveTypeId, String keyword) {
        // 构建Elasticsearch搜索请求
        SearchRequest.Builder builder=new SearchRequest.Builder();

        // 设置查询条件：城市编码必须匹配、服务类型ID必须匹配、关键字可选匹配
        builder.query(query->query.bool(bool->{
            bool.must(must->
                    must.term(term->
                            term.field("cityCode").value(cityCode)));
            //匹配服务类型
            bool.must(must->
                    must.term(term->
                            term.field("serve_type_id").value(serveTypeId)));
            if(ObjectUtils.isNotEmpty(keyword)){
                bool.must(must->
                        must.multiMatch(multiMatch->
                                multiMatch.fields("serve_item_name","serve_type_name").query(keyword)));
            }
            return bool;
        }));

        // 设置排序规则：按服务项排序号升序排列
        List<SortOptions> sortOptions=new ArrayList<>();
        sortOptions.add(SortOptions.of(sortOption->sortOption.field(field->field.field("serve_item_sort_num").order(SortOrder.Asc))));
        builder.sort(sortOptions);
        builder.index("serve_aggregation");

        // 执行搜索并处理结果
        SearchRequest sear = builder.build();
        SearchResponse<ServeAggregation> searchRes = elasticSearchTemplate.opsForDoc().search(sear, ServeAggregation.class);
        if(SearchResponseUtils.isSuccess(searchRes)){
            List<ServeAggregation> collect = searchRes.hits().hits()
                    .stream().map(Hit::source)
                    .collect(Collectors.toList());
            return BeanUtil.copyToList(collect, ServeSimpleResDTO.class);
        }
        return Collections.emptyList();
    }


}