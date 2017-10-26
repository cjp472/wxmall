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
package com.dbumama.market.web.common;

import com.dbumama.market.web.core.config.WxmWebConfig;
import com.dbumama.market.web.core.interceptor.BaseApiInterceptor;
import com.dbumama.market.web.core.route.ApiRoutes;
import com.jfinal.config.Handlers;
import com.jfinal.config.Interceptors;
import com.jfinal.config.Plugins;
import com.jfinal.config.Routes;
import com.jfinal.template.Engine;

/**
 * @author yangzy
 * 2017年7月10日
 */
public class WebConfig extends WxmWebConfig{

	@Override
	protected void configWebRoute(Routes me) {
		me.add(new ApiRoutes());
	}

	@Override
	protected void configWebPlugin(Plugins me) {
	}

	@Override
	protected void configWebHandler(Handlers me) {
	}

	@Override
	protected void configWebInterceptor(Interceptors me) {
		me.add(new BaseApiInterceptor());
	}

	@Override
	protected void configWebEngine(Engine me) {
	}

	@Override
	protected void onWxMallStart() {
		logger.info(" dbu api server is ready!!");
	}

	@Override
	protected void onWxMallStop() {
		
	}

}
