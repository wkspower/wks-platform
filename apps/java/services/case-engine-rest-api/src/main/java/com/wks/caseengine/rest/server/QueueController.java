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
package com.wks.caseengine.rest.server;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.wks.caseengine.queue.Queue;
import com.wks.caseengine.queue.QueueService;

import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("queue")
@Tag(name = "Queue", description = "Queues are used to distribute incoming cases or tasks among available resources, such as case workers or teams")
public class QueueController {

	@Autowired
	private QueueService queueService;

	@GetMapping
	public List<Queue> find() throws Exception {
		return queueService.find();
	}

	@GetMapping(value = "/{queueId}")
	public Queue get(@PathVariable final String queueId) throws Exception {
		return queueService.get(queueId);
	}

	@PostMapping
	public void save(@RequestBody final Queue queue) throws Exception {
		queueService.save(queue);
	}

	@PutMapping(value = "/{queueId}")
	public void update(@PathVariable final String queueId, @RequestBody final Queue queue) throws Exception {
		queueService.update(queueId, queue);
	}

	@DeleteMapping(value = "/{queueId}")
	public void delete(@PathVariable final String queueId) throws Exception {
		queueService.delete(queueId);
	}

}
