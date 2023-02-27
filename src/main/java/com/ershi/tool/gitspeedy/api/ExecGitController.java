package com.ershi.tool.gitspeedy.api;

import com.ershi.tool.gitspeedy.main.GitSpeedy;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * Description: TODO <br/>
 * Copyright: (c) 2023 SunTime Co'Ltd Inc. All rights reserved.<br/>
 *
 * @author 沈科
 * @version 1.0
 * @date 2023/1/12 14:14
 * @since JDK11
 */
@RestController
@RequestMapping("/git_speedy")
@Slf4j
public class ExecGitController {

    @RequestMapping("/exec")
    public String execGitCMD(@RequestBody(required = true) @Validated ExecParam execParam) {
        List<ExecParam> execParamList = execParam.getExecParamList();
        if (StringUtils.isBlank(execParam.getWorkspace()) || execParamList == null || execParamList.size() == 0) {
            return "必填参数缺失";
        }
        for (ExecParam exec : execParamList) {
            if (StringUtils.isBlank(exec.getProjectName()) || StringUtils.isBlank(exec.getMainBranchName()) ||
                    StringUtils.isBlank(exec.getMergeBranchName())) {
                return "必填参数缺失";
            }
        }

        for (ExecParam param : execParamList) {
            String projectName = param.getProjectName();
            log.info("==================================================================");
            log.info("项目：" + projectName + "， 开始执行");
            //进入工作目录Git操作单个流程
            GitSpeedy.exec(projectName, GitSpeedy.appendCMD(execParam.getWorkspace(), projectName, param.getMainBranchName(),
                    param.getMergeBranchName(), param.validDeleteMergeBranch()));
            log.info("项目：" + projectName + "， 执行结束");
            log.info("******************************************************************");
        }
        return "执行完毕";
    }
}
