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

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 控制器实现，有独立的线程控制。<br>
 *  通过addHandler和removeHandler方法组装消息处理器。使用postRequest方法投入Request后，<br>
 *  由Controller通过独立线程，按顺序调用处理器处理。<br>
 *  使用此方法时，一般不用继承。此类如果destroy后就不可用了。<br>
 *  注意: <br>
 *      该类在构造时没有create(),所有new 之后请create() <br>
 */
public class ControllerImp implements Controller, Runnable {
	private static final Log log = LogFactory.getLog(ControllerImp.class);
    private static int index = 0;
    
    private Map<String, Handler> requestMap = new HashMap<String, Handler>();
    private Handler defaultHandler = null;
    
    private Thread[] controlThread = null;
    private int threadnum = 1;
    
    protected Queue<Object> messageQueue;
    protected boolean isExit = false;
    protected String controllerName;
    
    /**
     * 无参数构造方法,创建新的队列,创建一个线程,无日志打印
     */
    public ControllerImp()
    {
        controllerName = "Controller";
        messageQueue = new Queue<Object>();
    }
    
    /**
     * 部分参数构造方法,创建新的队列,创建threadnum个线程,传递日志logger
     * @param threadnum 线程数
     */
    public ControllerImp(int threadnum)
    {
        this.threadnum = threadnum;
        controllerName = "Controller";
        messageQueue = new Queue<Object>();
    }
    
    /**
     * 部分参数构造方法,创建新的队列,根据name创建一个线程,传递日志logger
     * @param name 控制器名，如果为空或者长度为0，会使用缺省的名称
     */
    public ControllerImp(String name)
    {
        if ((null == name) || (name.length() == 0))
            controllerName = "Controller";
        else
            controllerName = name;
        messageQueue = new Queue<Object>();
    }
    
    /**
     * 完整参数构造方法,创建新的队列,根据name创建threadnum个线程,传递日志logger
     * @param name 控制器名
     * @param threadnum 线程数
     */
    public ControllerImp(String name, int threadnum)
    {
        this.threadnum = threadnum;
        if ((null == name) || (name.length() == 0))
            controllerName = "Controller";
        else
            controllerName = name;
        messageQueue = new Queue<Object>();
    }
    
    /**
     * 创建线程,用于构造方法或子类构造方法调用。
     * <p/>注意如果子类在构造函数中调用了此函数，则子类必须定义为final，保证不能在被继承，否则run函数可能会访问还没有构造的属性。
     * <p/>需要主动调用此函数，启动线程
     */
    public void create()
    {
        if (controlThread != null)
            destroy();

        isExit = false;
        controlThread = new Thread[threadnum];
        for (int i = 0; i < threadnum; i++)
        {
            controlThread[i] = new Thread(this, controllerName + (index++));
            controlThread[i].start();
        }
    }
    
    /**
     * 注销控制器。如果等待内部线程出错，会抛出异常RuntimeException
     */
    public void destroy() 
    {
        synchronized(this)
        {
	        if (controlThread == null)
	            return;
	            
	        isExit = true;
	        
	        try
	        {
	            for (int i = 0; i < threadnum; i++)
	            {
		            controlThread[i].join(2000);
		            controlThread[i] = null;
	            }
	        }
	        catch (InterruptedException e)
	        {
	            controlThread = null;
	            throw new RuntimeException(e);
	        }
	        
	        controlThread = null;
        }
    }
    
    /**
     * 获取控制器名
     * 
     * @return String
     */
    public String getName()
    {
        return this.controllerName;
    }
    
    /**
     * 添加请求的处理器
     * 
     * @param request 请求的名字
     * @param handler 请求的处理器
     */
    public void addHandler(Class<?> requestClass, Handler handler)
    {
        synchronized(requestMap)
        {
            if (requestMap.containsKey(requestClass.getName()))
                log.warn(controllerName + "已经有处理[" + requestClass.getName() + "]消息的处理器");
                
            requestMap.put(requestClass.getName(), handler);
        }
    }

    /**
     * 注销请求的处理器
     * 
     * @param requestClass
     * @return 返回request对应的处理器 
     */
    public Handler removeHandler(Class<?> requestClass)
    {
        synchronized(requestMap)
        {
            return (Handler)requestMap.remove(requestClass.getName());
        }
    }
    
    /**
     * 添加缺省处理器，当请求没有处理器时，使用此处理器
     * 
     * @param handler 缺省处理器
     */
    public void addDefaultHandler(Handler handler)
    {
        if (null == handler)
            return ;
        
        synchronized(requestMap)
        {
            defaultHandler = handler; 
        }
    }
    
    /**
     * 删除缺省处理器
     */
    public void removeDefaultHandler()
    {
        synchronized(requestMap)
        {
            defaultHandler = null;
        }
    }
    
    /**
     * 判断对象是否存在队列
     * @param obj 对象，需自行实现hasCode和equal方法
     * @return =true表示存在，=false表示不存在
     */
    public boolean isExist(Object obj)
    {
        return messageQueue.isExist(obj);
    }
    
    /**
     * 投递消息到处理器。如果对应的消息没有处理器，则会记录错误，如果controller的状态不对，则会抛出运行时异常
     * @param request 具体的请求
     */
    public void postRequest(Object obj)
    {
        if (isExit)
        {
            log.warn(controllerName + "已经destroy，对象不可用");
            return;
        }
            
        if (obj == null)
            return;
        
        if (defaultHandler != null || requestMap.containsKey(obj.getClass().getName()))
            messageQueue.postMessage(obj);
        else
            log.error(controllerName + "没有消息[" + obj.getClass().getName() + "]的处理器");
    }

    /** 投递到队列首端 */
    public void postRequestToHead(Object obj)
    {
        if (isExit)
        {
            log.warn(controllerName + "已经destroy，对象不可用");
            return;
        }
            
        if (obj == null)
            return;
        
        if (defaultHandler != null || requestMap.containsKey(obj.getClass().getName()))
            messageQueue.postMessageAtHead(obj);
        else
            log.error(controllerName + "没有消息[" + obj.getClass().getName() + "]的处理器");
    }
    
    /** 获取消息队列大小 */
    public int getMessageQueueSize()
    {
        if (messageQueue != null)
            return messageQueue.getMessageNum();
        else
            return 0;
    }
    
    /** 获取线程数 */
    public int getThreadNum()
    {
        return threadnum;
    }
    
    /**
     * 用于判断投递Request时作安全检查,受保护方法
     * 
     * @return 是否控制器已退出
     */
    protected boolean isExit()
    {
        return isExit;
    }
    
    /**
     * 获取Request的名称对应的Handler,如果Handler==null,则使用缺省Handler,受保护方法
     * 
     * @param name Request的名称
     * @return RequestHandler Request的名称对应的Handler
     */
    protected Handler getHandler(Class<?> objClass)
    {
        Handler handler = (Handler)requestMap.get(objClass.getName());
        if (handler == null)
            return defaultHandler;
        
        return handler;
    }
    
    /** RUN方法.*/
    public void run()
    {
        while (!isExit)
        {           
            Object request = messageQueue.waitForNormalMessage(2000);
            if (request == null)
                continue;
            
            Handler handler = getHandler(request.getClass());
            if (handler == null)
            {
                log.error("控制器[" + getName() + "]的请求[" + request.getClass().getName() + "]没有相应的处理器");
                continue;
            }
            
            try
            {
                handler.process(request);
            }
            catch (Exception e)
            {
                log.error("控制器[" + getName() + "]的调用请求[" + request.getClass().getName() + 
                        "]的处理器[" + handler.toString() + "]时发生异常："+ e.getMessage(), e);
            }

        }
    }
}
