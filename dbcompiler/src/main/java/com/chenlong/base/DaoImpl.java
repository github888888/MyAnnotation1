package com.chenlong.base;

import java.io.Serializable;
import java.util.List;

/**
 * 实体操作的通用接口
 * @author chenlong
 */
public interface DaoImpl<M extends BaseBean> {

    long insert(M t);

    long insertAll(List<M> list);

    int delete(Serializable id);

    int deleteByCondition(String selection, String[] selectionArgs);

    int update(M t);

    List<M> findAll();

    List<M> findByCondition(String selection, String[] selectionArgs, String orderBy);

    List<M> findByCondition(String selection, String[] selectionArgs, String groupBy, String having, String orderBy);
}