#!/usr/bin/env groovy

import io.alauda.devops.Build

def setup(String address = "index.alauda.cn", String tag = "latest", String dockerfilePath = "Dockerfile", String context = ".", String credentialsId = "", int retryTimes = 2) {
  return new Build().setup(dockerfilePath, context, address, tag, credentialsId, retryTimes)
}

