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

import java.io.Serializable;

public class JobInfo implements Serializable{
	private static final long serialVersionUID = 1L;
    
    private String scheduleId;
    private String scheduleName;
    private String jobType;
    private boolean isStarting;
    private boolean isFirstExecute;
    
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
    public String getJobType()
    {
        return jobType;
    }
    public void setJobType(String jobType)
    {
        this.jobType = jobType;
    }
    public boolean isStarting()
    {
        return isStarting;
    }
    public void setStarting(boolean isStarting)
    {
        this.isStarting = isStarting;
    }
    public boolean isFirstExecute()
    {
        return isFirstExecute;
    }
    public void setFirstExecute(boolean isFirstExecute)
    {
        this.isFirstExecute = isFirstExecute;
    }
}
