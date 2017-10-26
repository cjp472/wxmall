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
package com.dbumama.market.plugin;

import java.util.ArrayList;
import java.util.List;

import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import com.dbumama.market.sched.Job;
import com.dbumama.market.sched.Scheduler;
import com.dbumama.market.sched.SchedulerMBean;
import com.jfinal.plugin.IPlugin;
/**
 * 基于jfinal调度插件
 * @author yangzy
 *
 */
public class SchedPlugin implements IPlugin{

	@Override
	public boolean start() {
		SchedulerMBean scheduler = new Scheduler();
		scheduler.start();
		try {
			List<Job> jobs = readTask();
			for(Job job : jobs){
				scheduler.addJob(job);
			}
		} catch (DocumentException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	@Override
	public boolean stop() {
		return true;
	}
	
	private List<Job> readTask() throws DocumentException{
		List<Job> jobs = new ArrayList<Job>();
		SAXReader sr = new SAXReader();//获取读取xml的对象。
		Document doc = sr.read(getClass().getClassLoader().getResourceAsStream("task.config.xml"));
		Element root = doc.getRootElement();
		
		@SuppressWarnings("unchecked")
		List<Element> elements = root.elements();
		for(Element el : elements){
			Attribute av = el.attribute("value");
			if("true".equals(av.getText())){
				Attribute ac = el.attribute("class");
				try {
					Job job = (Job) Class.forName(ac.getText()).newInstance();
					jobs.add(job);
				} catch (Exception e) {
					throw new DocumentException(e.getMessage());
				}
			}
		}
		return jobs;
	}

}
