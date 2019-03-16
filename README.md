# docker-build-library
Jenkins shared library docker build implementation

## Setup and requirements

### 1. Setup as shared library

Setup the jenkins shared library using Jenkins configuration interfaces. [Reference](https://jenkins.io/doc/book/pipeline/shared-libraries/)

### 2. Agent with docker client CLI

The specified agent should have a `docker client` as a `docker` command. If running inside a container, it should attach the `docker.sock ` so that the host's `dockerd` is reachable.

### 3. sh DSL

Under the hood, the `Build` uses Jenkins `sh` DSL to execute all the build commands. For this reason, this library is only compatible with Linux/Unix systems, but it should be easily fixed to support other operating systems.

## TL; DR

Using the [Kubernetes plugin](<https://github.com/jenkinsci/kubernetes-plugin>) a `docker` label agent can be created with the docker client library

```
@Library('dockerbuild') _

node('docker') {
    git url: 'https://github.com/alauda/tasks-gateway'
    container('docker') {
        def image = dockerBuild.setup(
            "index.alauda.cn/alauda/tasks-gateway",
            "latest",
            "Dockerfile",
            ".",
            "alauda-credentials",
            1,
        )
        image.build().push()
        def imageUrl = image.getImage()
        echo "${imageUrl}"
    }
}

```




## DSL

### dockerBuild.setup(String *address* = "index.alauda.cn", String *tag* = "latest", String *dockerfilePath* = "Dockerfile", String *context* = ".", String *credentialsId* = "", int *retryTimes* = 2)

`dockerBuild.setup` method is the entrypoint for the initiate a `Build` instance.

The `setup` method has the following parameters. As:

| parameter      | description                                                  | Type   | default value     |
| -------------- | ------------------------------------------------------------ | ------ | ----------------- |
| address        | Image repository address used to push the image repository. Should be the final repository address: i.e `index.alauda.cn/alauda/hello-world` | String | `index.alauda.cn` |
| tag            | Tag given to the docker image during build. Other tags can be used in specific `push` commands in from `Build` instance. | String | `latest`          |
| dockerFilePath | Dockerfile path and filename                                 | String | `Dockerfile`      |
| context        | Docker build context (directory) used for dockerbuild. The Dockerfile must be inside the `context` folder | String | `.`               |
| credentialsId  | If docker login is required for pushing/pull the images, a credential should be added to Jenkins with the registry's username and password, and its ID should be given as a String | String | ``                |
| retryTimes     | Number of times the commands will retry during the process if fails | Int    | `2`               |

Example with only basic parameters:

```groovy
node('docker') {
    def imageBuild = dockerBuild.setup(
      "index.alauda.cn/alauda/hello-world", // docker repository address
      "latest", // tag 
    )
    //  will build the docker image, push as "latest" tag, give a "v1.0" and push again
    imageBuild.build().push().push("v1.0")
  
    def imageAddress = imageBuild.getImage()
	  echo "${imageAddress}" // will print "index.alauda.cn/alauda/hello-world:latest" 
}
```



Example with all parameters:

```groovy
node('docker') {
    def imageBuild = dockerBuild.setup(
      "index.alauda.cn/alauda/hello-world", // docker repository address
      "latest", // tag
      "Dockerfile", // Dockerfile path to file
      ".", // Docker build context
      "docker-credentials", // user/password credentials stored in Jenkins
      2, // number of retries if a specific command fails
    )
    //  will build the docker image, push as "latest" tag, give a "v1.0" and push again
    imageBuild.setArg("commit_id", env.COMMIT_ID).build().push().push("v1.0")
  
    def imageAddress = imageBuild.getImage("v1.0")
	  echo "${imageAddress}" // will print "index.alauda.cn/alauda/hello-world:v1.0" 
}
```



## Build object

`dockerBuild.setup` only initiates the `Build` object. Most methods will `return this`  which makes it convenient to chain calls of different methods. The full list of methods is described bellow:

### setArg(String *name*, String *value*)

This command prepares a key/value argument in memory to be used once the `build` method executes. The list of parameters follows:

| parameter | description                                | Type   |
| --------- | ------------------------------------------ | ------ |
| name      | argument name as defined in the Dockerfile | String |
| value     | value given to the argument                | String |

Returns: `Build` object

### setFullAddress(String *address*)

Overwrites the image repository address given in the `setup` method.

Returns: `Build` object



### setRetries(int *retryTimes* = 2)

Overwrites the retry number of commands. All docker commands are wrapped in a `retry(n)` , and this method can be used to set `n` values.

Returns: `Build` object



### getRegistryAddress()

Returns the registry address of the repository. i.e if the docker repository address is `index.alauda.cn/alauda/hello-world` it returns `index.alauda.cn`

Returns: `String`



### build()

Starts the build process. Will execute `login` method if a credential was given and if yet not logged in.

Returns: `Build` object



### push(String *tag* = "")

Pushes the image. If a tag value is given will also automatically tag the built image and push to the docker registry.

Returns: `Build` object



### pull(String *tag*="")

Pulls the docker repository used. Will also login if a credential ID is provided and not-yet logged int. If a tag value is given will automatically pull the specified tag using the docker repository address.

Returns: `Build` object



### login()

Executes docker login if a credential ID is given and not-yet logged in. Although this method is accessible, it is not necessary to explicitly call it as all commands that require login will execute this method implicitly.

Returns: `Build` object



### getImage(String *tag* = "")

Returns the full address of the docker repository. By default returns the tag given during `setup`, but if a tag value is given will use it instead.

