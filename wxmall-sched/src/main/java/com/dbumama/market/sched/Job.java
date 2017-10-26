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

public interface Job extends Runnable{

	public static final int MAX_DAY = 0;
    public static final int SUCC = 0;
    
    public static final String MINUTE = "MINUTE";
    public static final String TENMINUTE = "TENMINUTE";
    public static final String HOUR = "HOUR";
    public static final String DAY = "DAY";
    public static final String MONTH = "MONTH";
    public static final String YEAR = "YEAR";
    
    /** 获取任务类型 */
    public String getJobType();
    
    /** 获取任务编号 */
    public String getScheduleId();
    
    /** 设置任务编号 */
    public void setScheduleId(String scheduleId);
    
    /** 获取任务名称 */
    public String getScheduleName();
    
    /** 设置任务名称 */
    public void setScheduleName(String scheduleName);

    /** 设置是否启动即运行 */
    public void setFirstExecute(boolean isFirstExecute);
    
    /** 设置是否启动即运行 */
    public boolean isFirstExecute();
    
    /** 启动 */
    public void start();
    
    /** 停止 */
    public void stop();
    
    /** 是否正在运行 */
    public boolean isStarting();
    
    /** 处理 */
    public void process(String datetime);
    
    /** 执行 */
    public int execute();
    
    /** 加载参数 */
    public void loadParameter();
    
}
