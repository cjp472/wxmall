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
package com.dbumama.market.service.base;

import com.dbumama.market.service.api.base.BaseService;
import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.TableMapping;

/**
 * @author yangzy
 * 2017年7月9日
 */
public abstract class BaseServiceImpl <M extends Model<M>> extends AbstractServiceImpl implements BaseService {

	protected abstract Model<M> getModel();

	public M findById(Object id) {
		return getModel().findById(id);
	}
	
	public String getTable() {
		return TableMapping.me().getTable(getModel().getClass()).getName();
	}
	
}

