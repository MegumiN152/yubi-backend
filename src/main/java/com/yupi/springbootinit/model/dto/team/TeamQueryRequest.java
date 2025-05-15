package com.yupi.springbootinit.model.dto.team;

import com.yupi.springbootinit.common.PageRequest;
import lombok.Data;

import java.io.Serializable;

@Data
public class TeamQueryRequest extends PageRequest implements Serializable {

    private String searchParam;

}