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

public abstract class TenMinute implements Job {
	private static final Log log = LogFactory.getLog(TenMinute.class);
    
    private String scheduleId = null;
    private String scheduleName = null;
    private String tenMinute = "19700101000";//已处理的分钟数,默认1970年1月1日0时0十分
    private String minuteSecond = "000";//处理的秒位数,默认0秒

    private boolean isFirstExecute = true;//是否第一次默认处理
    
    private String curTenMinute;
    private String curMinuteSecond;
    private Thread thread = null;
    private boolean isStarting = false;
    private Event event = new Event();
    
    /** 提供一个适配方法,子类可选继承 */
    public void loadParameter()
    {
    }
    
    public void setTime(int minute, int second)
    {
        if (minute < 0 || minute > 9 || second < 0 || second > 59)
            return;
        

        this.minuteSecond = String.valueOf(minute);
        
        if (second < 10)
            this.minuteSecond += "0" + second;
        else
            this.minuteSecond += String.valueOf(second);
    }
    
    public String getJobType()
    {
        return TENMINUTE;
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
        
        curTenMinute = datetime.substring(0, 11);
        curMinuteSecond = datetime.substring(11);
       
        if ("19700101000".equals(tenMinute) && !isFirstExecute)
        {
            tenMinute = curTenMinute;//直接赋当前小时值，等待下次处理
            return;
        }
        
        if (curTenMinute.compareTo(tenMinute) > 0 && curMinuteSecond.compareTo(minuteSecond) >= 0)
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
                    tenMinute = curTenMinute;
                else
                    Thread.sleep(1000 * ret);
            }
            catch(Exception e)
            {
                log.error("小时定时任务异常:"+e.getMessage(), e);
                
                //异常时置该次任务执行为已处理
                tenMinute = curTenMinute;
            }
        }
    }
}
