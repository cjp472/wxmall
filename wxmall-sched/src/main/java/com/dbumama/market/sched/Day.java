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

public abstract class Day implements Job {
	private static final Log log = LogFactory.getLog(Day.class);
    
    private String scheduleId = null;
    private String scheduleName = null;
    private String day = "19700101";//已处理的分钟数,默认1970年1月1日0时0分
    private String time = "000000";//处理的时分秒位数,默认0
    
    private boolean isFirstExecute = true;//是否第一次默认处理
    
    private String curDay;
    private String curTime;
    private Thread thread = null;
    private boolean isStarting = false;
    private Event event = new Event();
    
    /** 提供一个适配方法,子类可选继承 */
    public void loadParameter()
    {
    }
    
    public void setTime(int hour, int minute, int second)
    {
        if (hour < 0 || hour > 23 || minute < 0 || minute > 59 || second < 0 || second > 59)
            return;
        
        if (hour < 10)
            this.time = "0" + hour;
        else
            this.time = String.valueOf(hour);
        
        if (minute < 10)
            this.time += "0" + minute;
        else
            this.time += String.valueOf(minute);
        
        if (second < 10)
            this.time += "0" + second;
        else
            this.time += String.valueOf(second);
    }
    
    public String getJobType()
    {
        return DAY;
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
    
    public void setFirstExecute(boolean isFirstExecute)
    {
        this.isFirstExecute = isFirstExecute;
    }

    public boolean isFirstExecute()
    {
        return isFirstExecute;
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
        
        curDay = datetime.substring(0, 8);
        curTime = datetime.substring(8);
        
        if ("19700101".equals(day) && !isFirstExecute)
        {
            day = curDay;//直接赋当前分钟值，等待下次处理
            return;
        }

        if (curDay.compareTo(day) > 0 && curTime.compareTo(time) >= 0)
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
                    day = curDay;
                else
                    Thread.sleep(1000 * ret);
            }
            catch(Exception e)
            {
                log.error("日定时任务异常:"+e.getMessage(), e);
                
                //异常时置该次任务执行为已处理
                day = curDay;
            }
        }
    }
}
