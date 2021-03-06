package com.scheduler.szz.helpers;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RestController;

import com.scheduler.szz.controller.AppController;
import com.scheduler.szz.model.DBEntry;
import com.scheduler.szz.model.Email;


@Service
public class MessageReceivedComponent implements MessageListener {
	
	RabbitTemplate rabbitTemplate;;
	DBEntryDao dbEntryDao;
	
	
	public MessageReceivedComponent(RabbitTemplate rabbitTemplate, DBEntryDao dbEntryDao) {
		this.rabbitTemplate = rabbitTemplate;
		this.dbEntryDao = dbEntryDao;
	}

	@Override
	public void onMessage(Message message) {
			Logger l = Logger.getLogger(MessageReceivedComponent.class);
			String routingKey = message.getMessageProperties().getReceivedRoutingKey();
            System.out.println(routingKey);
            routingKey = routingKey + "";
            System.out.println(routingKey);
			String[] array = routingKey.split("\\.");
			String projectName = array[2];
			String token = array[3];
			String email = "";
			for (int i = 4; i < array.length; i++)
				email += array[i] + ".";
			email = email.substring(0,email.length()-1);
			int index = routingKey.lastIndexOf(".") + 1;
			ObjectInputStream ois = null;
			try {
				byte[] resource = message.getBody();
				FileUtils.writeByteArrayToFile(new File("mydata/"+ token + ".csv"), resource);
				DBEntry dbEntry = dbEntryDao.findByToken(token);
				dbEntry.setStatus(DBEntry.Status.ANALYSED);
				dbEntry.setEndEpoch(new Date().getTime());
				dbEntryDao.save(dbEntry);
			}
			catch (Exception e){
				e.printStackTrace();
			}
			
		}
	

	
}
