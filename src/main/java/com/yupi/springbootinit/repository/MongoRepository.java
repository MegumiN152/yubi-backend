package com.yupi.springbootinit.repository;

import java.util.Map;

/**
 * @author 黄昊
 * @version 1.0
 **/
public interface MongoRepository extends org.springframework.data.mongodb.repository.MongoRepository<Map<String,Object>,String> {
}
