/**
 * Copyright (c) 成都次时代信息科技有限公司 2016-2017, 33732992@qq.com.
 *
 * Licensed under the GNU Lesser General Public License (LGPL) ,Version 3.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.gnu.org/licenses/lgpl-3.0.txt
 *	    http://www.ybwqy.com
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.dbumama.market.sched;

import java.util.List;

public interface SchedulerMBean {
	/** 启动任务管理 */
    public void start();
    
    /** 关闭任务管理 */
    public void stop();
    
    /** 是否已启动 */
    public boolean isStarting();
    
    /** 增加一个任务 @param 当前仅支持 Minute, Hour, Day, Month, Year的子类 */
    public String addJob(Job job);
    
    /** 删除已增加的任务 */
    public void removeJob(String scheduleId);
    
    /** 任务数 */
    public int getJobNum();
    
    /** 任务数据 */
    public String getJobString();
    
    /** 获取任务列表 */
    public List<JobInfo> jobList();
    
    /** 任务是否已启动 */
    public boolean jobStarting(String scheduleId);
    
    /** 启动任务 */
    public void startJob(String scheduleId);
    
    /** 关闭任务 */
    public void stopJob(String scheduleId);
    
    /** 手工执行任务 */
    public void executeJob(String scheduleId);
    
    /** 加载参加 */
    public void loadJobParameter(String scheduleId);
}
