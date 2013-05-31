/**
 * Copyright 2012 Netflix, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.netflix.edda.aws

import com.amazonaws.auth.AWSCredentials
import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.auth.AWSCredentialsProvider
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain

import com.amazonaws.services.ec2.AmazonEC2Client
import com.amazonaws.services.autoscaling.AmazonAutoScalingClient
import com.amazonaws.services.elasticloadbalancing.AmazonElasticLoadBalancingClient
import com.amazonaws.services.identitymanagement.AmazonIdentityManagementClient
import com.amazonaws.services.s3.AmazonS3Client
import com.amazonaws.services.sqs.AmazonSQSClient
import com.amazonaws.services.cloudwatch.AmazonCloudWatchClient
import com.amazonaws.services.route53.AmazonRoute53Client
import com.amazonaws.services.securitytoken.AWSSecurityTokenServiceClient
import com.amazonaws.services.securitytoken.model.AssumeRoleRequest
import com.amazonaws.services.securitytoken.model.AssumeRoleResult

/** provides access to AWS service client objects
  *
  * @param credentials provider used to connect to AWS services
  * @param region used to select endpoint for AWS services
  */
class AwsClient(var provider: AWSCredentialsProvider, val region: String) {

  /** uses [[com.amazonaws.auth.AWSCredentials]] to create AWSCredentialsProvider
    *
    * @param credentials used to connect to AWS services
    * @param region to select endpoint for AWS services
    */
  def this(credentials: AWSCredentials, region: String) =
    this(new AWSCredentialsProvider() {def getCredentials = credentials; def refresh = {}}, region)

  /** uses [[com.amazonaws.auth.DefaultAWSCredentialsProviderChain]] to discover credentials
    *
    * @param region to select endpoint for AWS services
    */
  def this(region: String) =
    this(new DefaultAWSCredentialsProviderChain(), region)

  /** create credential from provided arguments
    *
    * @param accessKey for account access
    * @param secretKey for account access
    * @param region used to select endpoint for AWS service
    */
  def this(accessKey: String, secretKey: String, region: String) =
    this(new BasicAWSCredentials(accessKey, secretKey), region)

  def assumeRole(arn: String): AwsClient = {
    val client = securityToken
    provider = new AWSCredentialsProvider() {
      val req = (new AssumeRoleRequest).withRoleArn(arn)
      def update = {
        var result = client.assumeRole(req) 
        new BasicAWSCredentials(result.getCredentials.getAccessKeyId, result.getCredentials.getSecretAccessKey)
      }
      var cred = update
      def getCredentials = cred
      def refresh = cred = update
    }
    this
  }

  /** get [[com.amazonaws.services.ec2.AmazonEC2Client]] object */
  def ec2 = {
    val client = new AmazonEC2Client(provider)
    client.setEndpoint("ec2." + region + ".amazonaws.com")
    client
  }

  /** get [[com.amazonaws.services.autoscaling.AmazonAutoScalingClient]] object */
  def asg = {
    val client = new AmazonAutoScalingClient(provider)
    client.setEndpoint("autoscaling." + region + ".amazonaws.com")
    client
  }

  /** get [[com.amazonaws.services.elasticloadbalancing.AmazonElasticLoadBalancingClient]] object */
  def elb = {
    val client = new AmazonElasticLoadBalancingClient(provider)
    client.setEndpoint("elasticloadbalancing." + region + ".amazonaws.com")
    client
  }

  /** get [[com.amazonaws.services.s3.AmazonS3Client]] object */
  def s3 = {
    val client = new AmazonS3Client(provider)
    if (region == "us-east-1")
      client.setEndpoint("s3.amazonaws.com")
    else
      client.setEndpoint("s3-" + region + ".amazonaws.com")
    client
  }

  /** get [[com.amazonaws.services.identitymanagement.AmazonIdentityManagementClient]] object */
  def identitymanagement = {
    val client = new AmazonIdentityManagementClient(provider)
    if (region == "us-gov")
      client.setEndpoint("iam.us-gov.amazonaws.com")
    else
      client.setEndpoint("iam.amazonaws.com")
    client
  }

  /** get [[com.amazonaws.services.sqs.AmazonSQSClient]] object */
  def sqs = {
    val client = new AmazonSQSClient(provider)
    client.setEndpoint("sqs." + region + ".amazonaws.com")
    client
  }

  /** get [[com.amazonaws.services.sqs.AmazonCloudWatchClient]] object */
  def cw = {
    val client = new AmazonCloudWatchClient(provider)
    client.setEndpoint("monitoring." + region + ".amazonaws.com")
    client
  }
 
   /** get [[com.amazonaws.services.route53.AmazonRoute53Client]] object */
   def route53 = {
      val client = new AmazonRoute53Client(provider)
      client.setEndpoint("route53.amazonaws.com")
      client
   }

  def securityToken = {
    val client = new AWSSecurityTokenServiceClient(provider);
    client.setEndpoint("sts.amazonaws.com");
    client
  }

}
