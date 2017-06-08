/*
* Copyright 2015 herd contributors
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package org.finra.herd.dao.impl;

import com.amazonaws.services.sqs.model.SendMessageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import org.finra.herd.dao.AwsClientFactory;
import org.finra.herd.dao.SqsDao;
import org.finra.herd.dao.SqsOperations;
import org.finra.herd.model.dto.AwsParamsDto;

/**
 * The SQS DAO implementation.
 */
@Repository
public class SqsDaoImpl implements SqsDao
{
    @Autowired
    private AwsClientFactory awsClientFactory;

    @Autowired
    private SqsOperations sqsOperations;

    @Override
    public SendMessageResult sendMessage(AwsParamsDto awsParamsDto, String queueName, String messageText)
    {
        return sqsOperations.sendMessage(queueName, messageText, awsClientFactory.getAmazonSQSClient(awsParamsDto));
    }
}
