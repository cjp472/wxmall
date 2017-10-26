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
package com.dbumama.market.config;

import org.apache.log4j.Logger;

import com.alibaba.druid.filter.stat.StatFilter;
import com.dbumama.market.model._MappingKit;
import com.dbumama.market.plugin.SchedPlugin;
import com.jfinal.config.Constants;
import com.jfinal.config.Handlers;
import com.jfinal.config.Interceptors;
import com.jfinal.config.JFinalConfig;
import com.jfinal.config.Plugins;
import com.jfinal.config.Routes;
import com.jfinal.core.JFinal;
import com.jfinal.kit.PropKit;
import com.jfinal.plugin.activerecord.ActiveRecordPlugin;
import com.jfinal.plugin.activerecord.CaseInsensitiveContainerFactory;
import com.jfinal.plugin.druid.DruidPlugin;
import com.jfinal.plugin.ehcache.EhCachePlugin;
import com.jfinal.template.Engine;
import com.weixin.sdk.kit.WxSdkPropKit;

public final class WxSchedConfig extends JFinalConfig{

	private Logger logger = Logger.getLogger(WxSchedConfig.class);
	
	@Override
	public void configConstant(Constants me) {
		PropKit.use("sched.properties");
		me.setEncoding("UTF-8");
		me.setI18nDefaultBaseName("i18n");
		me.setI18nDefaultLocale(PropKit.get("locale"));
	}

	@Override
	public void configRoute(Routes me) {
	}

	@Override
	public void configPlugin(Plugins me) {
		DruidPlugin druidPlugin = new DruidPlugin(PropKit.get("jdbc.url"), PropKit.get("jdbc.username"), PropKit.get("jdbc.password"));
		druidPlugin.addFilter(new StatFilter());
		me.add(druidPlugin);
		//添加自动绑定model与表插件
		ActiveRecordPlugin activeRecordPlugin = new ActiveRecordPlugin(druidPlugin);
		activeRecordPlugin.setShowSql(JFinal.me().getConstants().getDevMode());
		activeRecordPlugin.setContainerFactory(new CaseInsensitiveContainerFactory(true));
		_MappingKit.mapping(activeRecordPlugin);
		me.add(activeRecordPlugin);
		
		//缓存插件
		me.add(new EhCachePlugin());
		
		//调度插件
		me.add(new SchedPlugin());
	}
	
	@Override
	public void configInterceptor(Interceptors me) {
	}

	@Override
	public void configHandler(Handlers me) {
	}

	@Override
	public void afterJFinalStart() {
		WxSdkPropKit.use("weixin.sdk.properties");
		logger.info(" dbu sched server is Ready !!");
		super.afterJFinalStart();
	}
	
	@Override
	public void beforeJFinalStop() {
		super.beforeJFinalStop();
	}

	@Override
	public void configEngine(Engine me) {
	}

}
