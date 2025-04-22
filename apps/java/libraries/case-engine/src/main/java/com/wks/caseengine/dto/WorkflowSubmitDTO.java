package com.wks.caseengine.dto;

import java.util.List;

import com.wks.bpm.engine.model.spi.ProcessVariable;
import com.wks.caseengine.cases.instance.CaseInstance;


import org.springframework.context.annotation.Configuration;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Configuration
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class WorkflowSubmitDTO {
    CaseInstance caseInstance;
    WorkflowDTO workflowDTO;
    List<ProcessVariable> variables;
   com.wks.caseengine.cases.instance.CaseComment CaseComment;
    String taskId;

}
