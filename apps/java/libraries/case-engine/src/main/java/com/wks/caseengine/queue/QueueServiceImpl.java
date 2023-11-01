/*
 * WKS Platform - Open-Source Project
 * 
 * This file is part of the WKS Platform, an open-source project developed by WKS Power.
 * 
 * WKS Platform is licensed under the MIT License.
 * 
 * © 2021 WKS Power. All rights reserved.
 * 
 * For licensing information, see the LICENSE file in the root directory of the project.
 */
package com.wks.caseengine.queue;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.wks.caseengine.command.CommandExecutor;
import com.wks.caseengine.queue.command.DeleteQueueCmd;
import com.wks.caseengine.queue.command.FindQueueCmd;
import com.wks.caseengine.queue.command.GetQueueCmd;
import com.wks.caseengine.queue.command.CreateQueueCmd;
import com.wks.caseengine.queue.command.UpdateQueueCmd;

@Component
public class QueueServiceImpl implements QueueService {

	@Autowired
	private CommandExecutor commandExecutor;

	@Override
	public void save(Queue queue) throws Exception {
		commandExecutor.execute(new CreateQueueCmd(queue));
	}

	@Override
	public Queue get(String id) throws Exception {
		return commandExecutor.execute(new GetQueueCmd(id));
	}

	@Override
	public List<Queue> find() throws Exception {
		return commandExecutor.execute(new FindQueueCmd());
	}

	@Override
	public void delete(String id) throws Exception {
		commandExecutor.execute(new DeleteQueueCmd(id));
	}

	@Override
	public void update(String id, Queue queue) throws Exception {
		commandExecutor.execute(new UpdateQueueCmd(id, queue));
	}

}
