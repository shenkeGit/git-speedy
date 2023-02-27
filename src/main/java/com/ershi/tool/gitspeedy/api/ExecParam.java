package com.ershi.tool.gitspeedy.api;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * Description: TODO <br/>
 * Copyright: (c) 2023 SunTime Co'Ltd Inc. All rights reserved.<br/>
 *
 * @author 沈科
 * @version 1.0
 * @date 2023/1/12 14:20
 * @since JDK11
 */
@Data
@Slf4j
@NoArgsConstructor
public class ExecParam {

    private List<ExecParam> execParamList;
    @NonNull
    private String workspace;

    @NonNull
    private String projectName;
    @NonNull
    private String mainBranchName;
    @NonNull
    private String mergeBranchName;
    @NonNull
    private int isDeleteMergeBranch;

    public String toString(){
        String data = "workspace:" + workspace + "; projectName:" + this.projectName + "; mainBranchName:" + this.mainBranchName + "; mergeBranchName:" + this.mergeBranchName + "; isDeleteMergeBranch:" + this.isDeleteMergeBranch;
        log.info(data);
        return data;
    }

    public boolean validDeleteMergeBranch() {
        if (this.isDeleteMergeBranch != 0 && (this.mergeBranchName.equals("master") || this.mergeBranchName.equals("main") ||
            this.mergeBranchName.equals("pre") || this.mergeBranchName.equals("dev"))) {
            this.isDeleteMergeBranch = 0;
            return false;
        }
        return this.isDeleteMergeBranch == 1 ? true : false;
    }
}
