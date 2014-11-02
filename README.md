# b2deploy Ant task for Blackboard Building Blocks

This code was previously maintained at [http://projects.oscelot.org/gf/project/b2_deploy_tools/](http://projects.oscelot.org/gf/project/b2_deploy_tools/)


## Structure
This project has two parts, 
1. an Ant task for sending your Building Block to a Blackboard Development server, and;
2. a servlet to extend Blackboard's "Starting Block" B2, so that it can receive the data from the Ant task, and deploy your Building Block.  This has been incorporated by Blackboard into the Starting Block, so as long as you're using a relatively recent version, you don't need to worry about this.


## Using the Ant task

The easiest way to get the Ant task, is from Blackboard's Maven repository. 
The repository URL is: ````https://bbprepo.blackboard.com/content/repositories/releases/````

and the artifact for the current version is: 
* group: org.oscelot
* name: b2deploy-task
* version: 0.1.0

If you're using gradle, you can use the following code snippets in your build.gradle file.

If your development server, isn't ````localhost:8081````, you'll need
to change the ````task```` declaration to whatever's appropriate.

````
repositories {
    mavenCentral()
    maven {
        url "https://bbprepo.blackboard.com/content/repositories/releases/"
    }
}

configurations {
    b2deploy
}

...

dependencies {
...
    b2deploy 'org.oscelot:b2deploy-task:0.1.0'
...
}

ant.taskdef(name: 'b2deploy', classname: 'org.oscelot.ant.B2DeployTask', classpath: configurations.b2deploy.asPath)

task deployb2(dependsOn: 'war') << {
    println "Deploying \"" + war.archivePath + "\""
    ant.b2deploy(localfilepath: war.archivePath,
        host: 'localhost:8081',
        clean: 'true',
        courseorgavailable:'true')
}

````

## Building.
If you want to build the code from source, then:
* checkout this project with git.
* ````cd```` to the b2deploy-task sub-directory.
* execute ````gradlew jar````
* the jar containing the ant task will be in ````build/libs````