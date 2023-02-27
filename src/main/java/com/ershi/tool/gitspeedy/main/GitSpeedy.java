package com.ershi.tool.gitspeedy.main;

import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

/**
 * Description: TODO <br/>
 * Copyright: (c) 2023 SunTime Co'Ltd Inc. All rights reserved.<br/>
 *
 * @author 沈科
 * @version 1.0
 * @date 2023/1/10 15:48
 * @since JDK11
 */
@Slf4j
public class GitSpeedy {

    static String WORKSPACE = "D:\\workproject\\fund";

    static String GIT_STATUS = "git status";
    static String GIT_CHECKOUT = "git checkout ";
    static String GIT_PULL = "git pull";
    static String GIT_MERGE = "git merge --no-ff ";
    static String GIT_PUSH = "git push origin ";
    static String GIT_DELETE_BRANCH = "git push origin --delete ";

    public static void main(String[] args) throws Exception {
        List<Map<String, Object>> result = new ArrayList<>();
        String userDir = System.getProperty("user.dir");
        log.info("根目录：" + userDir);
        ArrayList<ArrayList<String>> excel = ExcelUtil.getExcel(new File(userDir + "\\" + "模板.xlsx"), null).get(0);
        for (int i = 0; i < excel.size(); i ++) {
            if (i > 0) {
                ArrayList<String> arrayLists = excel.get(i);
                Map<String, Object> map = new HashMap<>();
                for (int j = 0; j < arrayLists.size(); j ++) {
                    if (arrayLists.get(j) == null || "".equals(arrayLists.get(j).toString().trim())) {
                        break;
                    }
                    switch (j){
                        case 0:
                            map.put("project_name", arrayLists.get(j).trim());
                            break;
                        case 1:
                            map.put("main_branch_name", arrayLists.get(j).trim());
                            break;
                        case 2:
                            map.put("merge_branch_name", arrayLists.get(j).trim());
                            break;
                        case 3:
                            map.put("is_delete_merge_branch", arrayLists.get(j).trim().equals("是") ? 1 : 0);
                            break;
                    }
                }
                result.add(map);
            }
        }

        log.info(result.toString());
        for (Map<String, Object> data : result) {
            String projectName = data.get("project_name").toString();
            log.info("项目：" + projectName + "， 开始执行");
            //进入工作目录Git操作单个流程
            exec(projectName, appendCMD(WORKSPACE, projectName, data.get("main_branch_name").toString(),
                    data.get("merge_branch_name").toString(), Integer.parseInt(data.get("is_delete_merge_branch").toString()) == 1 ? true : false));
            log.info("项目：" + projectName + "， 执行结束");
        }
    }

    public static String[] appendCMD(String workspace, String projectName, String mainBranchName, String mergeBranchName, boolean isDeleteMergeBranch) {
        List<String> list = new ArrayList<>();
        list.add("cd " + workspace);
        list.add("cd " + projectName);
        list.add(GIT_STATUS);
        list.add(GIT_CHECKOUT + mainBranchName);
        list.add(GIT_PULL);
        if (mergeBranchName != null) {
            list.add(GIT_CHECKOUT + mergeBranchName);
            list.add(GIT_PULL);
            list.add(GIT_CHECKOUT + mainBranchName);
            list.add(GIT_MERGE + mergeBranchName);
            list.add(GIT_PUSH + mainBranchName);
        }
        if (isDeleteMergeBranch) {
            list.add(GIT_DELETE_BRANCH + mergeBranchName);
        }
        return new String[]{"cmd", "/c", getCMD(list)};
    }

    public static void exec(String projectName, String[] cmd) {
        try {

            Process process = Runtime.getRuntime().exec(cmd);
            //获取进程的标准输入流
            final InputStream is1 = process.getInputStream();
            //获取进城的错误流
            final InputStream is2 = process.getErrorStream();
            //启动两个线程，一个线程负责读标准输出流，另一个负责读标准错误流
            RunThread runThread = new RunThread(is1);
            runThread.start();

            RunThread runThread2 = new RunThread(is2);
            runThread2.start();

            if (!runThread.isSucceed() || !runThread2.isSucceed()) {
                log.error("项目：" + projectName + "，执行异常，请手动重试；");
            } else {
                log.info("项目：" + projectName + "，执行正常。");
            }

            int status = process.waitFor();
            log.info("项目：" + projectName + "， 执行" + (status == 0 ? "结束" : "未结束"));

            boolean alive = process.isAlive();
            log.info("命令是否还在运行：" + alive);
            process.destroy();
        } catch (IOException | InterruptedException e) {
            log.error("执行异常", e);
            e.printStackTrace();
        }
    }

    public static String getCMD(String... cmd) {
        StringBuffer command = new StringBuffer();
        Arrays.stream(cmd).forEach(s -> {
            command.append(s + " & ");
        });
        return command.toString().substring(0, command.length() - 3);
    }

    public static String getCMD(List<String> cmds) {
        StringBuffer command = new StringBuffer();
        cmds.forEach(s -> {
            command.append(s + " & ");
        });
        return command.toString().substring(0, command.length() - 3);
    }

}
