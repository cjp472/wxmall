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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.dbumama.market.queue.Event;

public abstract class Minute implements Job{
	private static final Log log = LogFactory.getLog(Minute.class);
    
    private String scheduleId = null;
    private String scheduleName = null;
    private String minute = "197001010000";//已处理的分钟数,默认1970年1月1日0时0分
    private int second = 0;//处理的秒位数,默认0秒

    private boolean isFirstExecute = true;//是否第一次默认处理
    
    private String curMinute;
    private int curSecond;
    private Thread thread = null;
    private boolean isStarting = false;
    private Event event = new Event();
    
    /** 提供一个适配方法,子类可选继承 */
    public void loadParameter()
    {
    }
    
    /** 设置定时秒数 */
    public void setTime(int second)
    {
        if (second > 59 || second < 0)
            return;
        
        this.second = second;
    }
    
    public String getJobType()
    {
        return MINUTE;
    }
    
    public void setFirstExecute(boolean isFirstExecute)
    {
        this.isFirstExecute = isFirstExecute;
    }
    
    public boolean isFirstExecute()
    {
        return isFirstExecute;
    }
    
    public String getScheduleId()
    {
        return scheduleId;
    }
    
    public void setScheduleId(String scheduleId)
    {
        this.scheduleId = scheduleId;
    }
    
    public String getScheduleName()
    {
        return scheduleName;
    }
    
    public void setScheduleName(String scheduleName)
    {
        this.scheduleName = scheduleName;
    }
    
    /** 是否正在运行 */
    public boolean isStarting()
    {
        return isStarting;
    }
    
    public void start()
    {
        if (isStarting)
            return;
        
        thread = new Thread(this, scheduleId);
        thread.start();
    }
    
    public void stop()
    {
        isStarting = false;
        if (thread != null && !thread.isInterrupted())
        {
            thread.interrupt();
        }
    }
    
    public void process(String datetime)
    {
        if (!event.isWait())
            return;//如果正在处理，则取消本次处理
        
        curMinute = datetime.substring(0, 12);
        curSecond = Integer.parseInt(datetime.substring(12));
        if ("197001010000".equals(minute) && !isFirstExecute)
        {
            minute = curMinute;//直接赋当前分钟值，等待下次处理
            return;
        }
        
        if (curMinute.compareTo(minute) > 0 && curSecond >= second)
        {
            if (event.isWait())
            {
                event.notifyEvent();
            }
        }
    }
    
    public void run()
    {
        //置启动为true
        isStarting = true;
        
        while(isStarting)
        {
            event.WaitNotifyEvent(0);
            
            try
            {
                int ret = execute();
                if (ret == SUCC)
                    minute = curMinute;
                else
                    Thread.sleep(1000 * ret);
            }
            catch(Exception e)
            {
                log.error("分钟定时任务异常:"+e.getMessage(), e);
                
                //异常时置该次任务执行为已处理
                minute = curMinute;
            }
        }
    }
}
