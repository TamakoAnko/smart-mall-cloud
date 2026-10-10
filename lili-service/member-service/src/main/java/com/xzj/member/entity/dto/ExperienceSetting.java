package com.xzj.member.entity.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/** Member-owned projection of experience rules returned by message-service. */
@Data
public class ExperienceSetting implements Serializable {
    private List<ExperienceRuleItem> items = new ArrayList<>();
    private String description;

    @Data
    public static class ExperienceRuleItem implements Serializable {
        private String ruleKey;
        private String ruleName;
        private Boolean enabled;
        private Integer value;
        private Integer maxValue;
    }
}
