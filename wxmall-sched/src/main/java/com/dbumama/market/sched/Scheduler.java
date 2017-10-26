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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.dbumama.market.service.utils.DateTimeUtil;

public class Scheduler implements Runnable, SchedulerMBean{
	private static final Log log = LogFactory.getLog(Scheduler.class);

    private LinkedHashMap<String, Job> jobMap = new LinkedHashMap<String, Job>(11);   //任务Map
    
    private Thread thread = null;                           //任务管理线程
    private boolean isStarting = false;                     //任务管理线程是否启动
    
    private int sequenceSeed = 0;                           //任务编号种子    
    private Object sequenceLock = new Object();             //任务编号生成加锁对象
    
    /** 启动任务管理 */
    public void start()
    {
        thread = new Thread(this, "SchedulerMBean");
        thread.start();
    }
    
    /** 关闭任务管理 */
    public void stop()
    {
        isStarting = false;

        if (thread != null && !thread.isInterrupted())
        {
            thread.interrupt();
            thread = null;
        }
    }
    
    /** 销毁任务管理器 */
    public void destroy()
    {
        stop();
        
        if (!jobMap.isEmpty())
        {
            synchronized (jobMap)
            {
                for (Iterator<Job> it=jobMap.values().iterator();it.hasNext();)
                {
                    Job job = it.next();
                    it.remove();
                    
                    job.stop();
                    job = null;
                }
            }
        }
    }
    
    /** 
     * 增加一个任务
     * 
     * @param 当前仅支持 Minute, Hour, Day, Month, Year的子类
     */
    public String addJob(Job job)
    {
        if (!(job instanceof Minute) &&
            !(job instanceof TenMinute) && 
            !(job instanceof Hour) &&
            !(job instanceof Day) &&
            !(job instanceof Month) &&
            !(job instanceof Year))
        {
            log.error("拒绝增加不是Minute, TenMinute, Hour, Day, Month, Year的子类的定时任务");
            return null;
        }
        
        String scheduleId = job.getScheduleId();
        if (scheduleId == null)
            scheduleId = "Scheduler"+getSequence();
        else if (jobMap.containsKey(scheduleId))
        {
            log.error("拒绝增加相同scheduleId的定时任务");
            return null;
        }
        
        job.setScheduleId(scheduleId);
        if (job.getScheduleName() == null)
            job.setScheduleName(scheduleId);
        
        job.start();
        synchronized (jobMap)
        {
            jobMap.put(scheduleId, job);
        }
        return scheduleId;
    }
    
    /** 删除已增加的任务 */
    public void removeJob(String scheduleId)
    {
        Job job = null;
        synchronized (jobMap)
        {
            job = (Job)jobMap.remove(scheduleId);
        }
        
        if (job != null)
        {
            job.stop();
        }
    }
    
    /** 单线程运行 */
    public void run()
    {
        //置启动为true
        isStarting = true;
        
        while(isStarting)
        {
            String datetime = DateTimeUtil.getDateTime14String();
            if (!jobMap.isEmpty())
            {
                synchronized (jobMap)
                {
                    for (Iterator<Job> it=jobMap.values().iterator();it.hasNext();)
                    {
                        Job job = it.next();
                        job.process(datetime);
                    }
                }
            }
            
            try
            {
                Thread.sleep(1000);
            }
            catch (InterruptedException e)
            {
                log.error("定时器任务InterruptedException异常"+e.getMessage(),e);
                break;
            }
        }
    }
    
    /** 读取编号自增 */
    public int getSequence()
    {
        synchronized (sequenceLock)
        {
            if (sequenceSeed < 0)
                sequenceSeed = 0;
            return (++sequenceSeed);
        }
    }
    
    /** 是否已启动 */
    public boolean isStarting()
    {
        return isStarting;
    }
    
    /** 任务数 */
    public int getJobNum()
    {
        return jobMap.size();
    }
    
    /** 任务数据 */
    public String getJobString()
    {
        StringBuffer strb = new StringBuffer();
        for (Iterator<Job> it=jobMap.values().iterator();it.hasNext();)
        {
            Job job = it.next();
            strb.append("[").append(job.getScheduleId()).append("]")
                .append("[").append(job.getJobType()).append("]")
                .append("<br>");
        }
        
        return strb.toString();
    }
    
    /** 获取任务列表 */
    public List<JobInfo> jobList()
    {
        List<JobInfo> jobList = new ArrayList<JobInfo>(jobMap.size());
        for (Iterator<Job> it=jobMap.values().iterator();it.hasNext();)
        {
            Job job = it.next();
            JobInfo jobInfo = new JobInfo();
            jobInfo.setScheduleId(job.getScheduleId());
            jobInfo.setScheduleName(job.getScheduleName());
            jobInfo.setJobType(job.getJobType());
            jobInfo.setFirstExecute(job.isFirstExecute());
            jobInfo.setStarting(job.isStarting());
            
            jobList.add(jobInfo);
        }
        
        return jobList;
    }
    
    /** 任务是否已启动 */
    public boolean jobStarting(String scheduleId)
    {
        Job job = (Job)jobMap.get(scheduleId);
        if (job == null)
            return false;
        
        return job.isStarting();
    }
    
    /** 启动任务 */
    public void startJob(String scheduleId)
    {
        Job job = (Job)jobMap.get(scheduleId);
        if (job == null)
            return;
        
        job.start();
    }
    
    /** 关闭任务 */
    public void stopJob(String scheduleId)
    {
        Job job = (Job)jobMap.get(scheduleId);
        if (job == null)
            return;
        
        job.stop();
    }
    
    /** 手工执行任务 */
    public void executeJob(String scheduleId)
    {
        Job job = (Job)jobMap.get(scheduleId);
        if (job == null)
            return;
        
        job.execute();
    }
    
    /** 加载参加 */
    public void loadJobParameter(String scheduleId)
    {
        Job job = (Job)jobMap.get(scheduleId);
        if (job == null)
            return;
        
        job.loadParameter();
    }
}
