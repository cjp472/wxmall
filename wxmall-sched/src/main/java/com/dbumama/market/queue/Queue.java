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

import java.io.Serializable;
import java.util.LinkedList;

public class Queue<E> implements Serializable{
	private static final long serialVersionUID = 1L;
    private Event event = new Event();
    
    private LinkedList<E> msgList = new LinkedList<E>();

    public Queue()
    {
    }

    /**
     * 等待消息队列中的消息，如果超时没有消息，则返回null
     * 
     * @param dwMilliseconds 等待的时间长度，以毫秒为单位
     * @return 如果有消息，则返回消息，如果没有则返回null
     */
    public Object waitForNormalMessage(int dwMilliseconds)
    {
        synchronized (msgList)
        {
            if (!msgList.isEmpty())
                return msgList.removeFirst();
        }

        event.WaitNotifyEvent(dwMilliseconds);

        synchronized (msgList)
        {
            if (msgList.isEmpty())
                return null;
            return msgList.removeFirst();
        }
    }

    /**
     * 消息是否在队列中
     * 
     * @param message 消息对象
     * @return =true表示存在，=false表示不存在
     */
    public boolean isExist(E message)
    {
        return msgList.contains(message);
    }
    
    /**
     * 投递消息到消息队列的尾部
     * 
     * @param Message 待投递的消息
     */
    public void postMessage(E message)
    {
        synchronized (msgList)
        {
            msgList.add(message);
        }
        
        event.notifyEvent();
    }
    
    /**
     * 投递消息到消息队列的头部
     * 
     * @param Message 待投递的消息
     */
    public void postMessageAtHead(E Message)
    {
        synchronized (msgList)
        {
            msgList.addFirst(Message);
        }
        
        event.notifyEvent();
    }
    
    /**
     * 获取当前队列长度,不锁定
     * 
     * @return MessageNum
     */
    public int getMessageNum()
    {
        return msgList.size();
    }
}
