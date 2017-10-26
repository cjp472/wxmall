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
package com.dbumama.market.web.core.ueditor.manager;

import java.io.IOException;
import java.io.InputStream;
import java.util.Date;

import org.apache.commons.io.IOUtils;

import com.dbumama.market.model.SellerImages;
import com.dbumama.market.model.SellerUser;
import com.dbumama.market.service.api.fdfs.FileToolsService;
import com.dbumama.market.web.core.plugin.spring.IocInterceptor;
import com.dbumama.market.web.core.ueditor.define.AppInfo;
import com.dbumama.market.web.core.ueditor.define.BaseState;
import com.dbumama.market.web.core.ueditor.define.State;

/**
 * @author yangzy
 * 2017年7月10日
 */
public class FdfsFileManager extends AbstractFileManager{

	private FileToolsService fileToolsService = (FileToolsService) IocInterceptor.ctx.getBean("fileToolsService");
	
	/* (non-Javadoc)
	 * @see com.dbumama.market.web.core.ueditor.manager.AbstractFileManager#saveFile(java.io.InputStream, java.lang.String, java.lang.String, long)
	 */
	@Override
	public State saveFile(InputStream is, String rootPath, String savePath, String fileName, long maxSize) {
		SellerUser user = (SellerUser) getSubject().getPrincipal();
		if(user == null){
			return new BaseState(false, AppInfo.NOT_EXIST);			
		}
		if (savePath.startsWith("/")) {
			savePath = savePath.substring(1);
		}
		byte[] data = null;
		String url = "";
		try {
			data = IOUtils.toByteArray(is);
			if (data.length > maxSize) {
				return new BaseState(false, AppInfo.MAX_SIZE);
			}
			url = fileToolsService.uploadFile(data, fileName, data.length, null);
			SellerImages sellerImages = new SellerImages();
			sellerImages.setSellerId(user.getId());
			sellerImages.setImgPath(url);
			sellerImages.setActive(1);
			sellerImages.setCreated(new Date());
			sellerImages.setUpdated(new Date());
			sellerImages.save();
		} catch (IOException e) {
			return new BaseState(false, AppInfo.IO_ERROR);
		} catch (Exception e) {
			return new BaseState(false, AppInfo.IO_ERROR);
		} finally {
			IOUtils.closeQuietly(is);
		}
		State state = new BaseState(true);
		state.putInfo("size", data.length);
		state.putInfo("title", getFileName(fileName));
		state.putInfo("fdfs_url", url);
		return state;
	}

}

