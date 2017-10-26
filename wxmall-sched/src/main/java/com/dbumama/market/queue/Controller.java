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
package com.dbumama.market.queue;

public interface Controller {
	/**
     * 创建线程,用于构造方法或子类构造方法调用。
     * <p/>注意如果子类在构造函数中调用了此函数，则子类必须定义为final，保证不能在被继承，否则run函数可能会访问还没有构造的属性。
     * <p/>需要主动调用此函数，启动线程
     */
    public void create();
    
    /**
     * 释放controller的相关资源，controller的容器类，在释放controller时必须调用。
     */
    public void destroy();
    
    /**
     * 获得controller的名字
     * 
     * @return controller的名字
     */
    public String getName();
    
    /**
     * 向Controller投递request对象，此方法是异步处理方式，即消息投递后，由其他线程处理。
     * 如果Controller的状态错误，会抛出RuntimeException。如果request在Controller中
     * 无法处理，则一般会记录日志，不抛出运行时异常。注意此方法无返回值，也就是说处理结果必
     * 须通过其他方式返回给调用者。
     * 
     * 投递到队列尾端
     * 
     * @param obj 请求对象
     */
    public void postRequest(Object obj);

    /**
     * 向Controller投递request对象，此方法是异步处理方式，即消息投递后，由其他线程处理。
     * 如果Controller的状态错误，会抛出RuntimeException。如果request在Controller中
     * 无法处理，则一般会记录日志，不抛出运行时异常。注意此方法无返回值，也就是说处理结果必
     * 须通过其他方式返回给调用者。
     * 
     * 投递到队列首端
     * 
     * @param obj 请求对象
     */
    public void postRequestToHead(Object obj);
    
    /**
     * 判断对象是否存在队列
     * @param obj 对象，需自行实现hasCode和equal方法
     * @return =true表示存在，=false表示不存在
     */
    public boolean isExist(Object obj);
    
    /**
     * 返回Controller中等待的请求消息数。
     * @return 返回等待的请求消息数
     */
    public int getMessageQueueSize();
    
    /**
     * 对Controller进行装配，添加指定request的处理器为handler。当controller收到
     * request对象时，会调用handler处理此对象。注意如果controller是多线程的，则可
     * 能会多线程调用handler，则handler必须能够正确处理重入问题。
     * @param request 请求名
     * @param handler 处理request的处理器handler
     */
    public void addHandler(Class<?> requestClass, Handler handler);
    
    /**
     * 添加缺省的request处理器handler。如果request在controller中无法找到指定的
     * request，则会调用此处理器handler。注意如果controller是多线程的，则可能会
     * 多线程调用handler，则handler必须能够正确处理重入问题。
     * @param handler 缺省的处理器handler
     */
    public void addDefaultHandler(Handler handler);
    
    /**
     * 删除指定request的处理器handler。
     * @param request
     * @return
     */
    public Handler removeHandler(Class<?> requestClass);
    
    /**
     * 删除缺省的request的处理器handler。
     */
    public void removeDefaultHandler();
}
