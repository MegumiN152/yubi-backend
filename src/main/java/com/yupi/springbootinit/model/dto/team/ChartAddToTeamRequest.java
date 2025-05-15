package com.yupi.springbootinit.model.dto.team;

import lombok.Data;

import java.io.Serializable;

@Data
public class ChartAddToTeamRequest implements Serializable {

    private Long chartId;

    private Long teamId;

}